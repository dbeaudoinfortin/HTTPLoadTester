package com.dbf.loadtester.player;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCookieStore;

import com.dbf.loadtester.common.action.HTTPAction;
import com.dbf.loadtester.common.action.converter.ApacheRequestConverter;
import com.dbf.loadtester.common.action.substitutions.ActionSubstituter;
import com.dbf.loadtester.common.action.substitutions.VariableSubstitution;
import com.dbf.loadtester.common.util.Utils;
import com.dbf.loadtester.player.action.HttpEntityWrapper;
import com.dbf.loadtester.player.action.PlayerHTTPAction;
import com.dbf.loadtester.player.config.PlayerOptions;
import com.dbf.loadtester.player.cookie.CookieHandler;
import com.dbf.loadtester.player.stats.PlayerStats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadTestThread implements Runnable
{
	private static final Logger log = LoggerFactory.getLogger(LoadTestThread.class);
	
	private final CookieStore cookieStore = new BasicCookieStore();
	private final ActionSubstituter substituter;
	
	private final HttpClient httpClient;	
	private final int threadNumber;
	private final int actionDelay;
	private final boolean useFixedSubstitutions;
	private final boolean useVariableSubstitutions;
	private final long minRunTime;
	private final String host;
	private final int httpPort;
	private final int httpsPort;
	private final boolean overrideHttps;
	private final boolean runActionsConcurrently;
	private final List<PlayerHTTPAction> actions;
	private final PlayerStats globalPlayerStats;
	private final LoadTestCoordinator master;
	private final ApacheRequestConverter requestConverter;
	private final Collection<String> cookieWhiteList;
	
	public LoadTestThread(LoadTestCoordinator master, PlayerOptions config, int threadNumber, HttpClient httpClient, ApacheRequestConverter requestConverter, long minRunTime)
	{
		this.threadNumber = threadNumber;
		this.httpClient = httpClient;
		this.actionDelay = config.getActionDelay();
		this.useFixedSubstitutions = config.isUseFixedSubstitutions();
		this.useVariableSubstitutions = config.hasVariableSubstitutions();
		this.minRunTime = minRunTime;
		this.host = config.getHost();
		this.httpPort = config.getHttpPort();
		this.httpsPort = config.getHttpsPort();
		this.overrideHttps = config.isOverrideHttps();
		this.globalPlayerStats = config.getGlobalPlayerStats();
		this.master = master;
		this.requestConverter = requestConverter;
		this.cookieWhiteList = config.getCookieWhiteList();
		this.substituter = new ActionSubstituter(threadNumber, config.getVariableSubstitutions());
		this.runActionsConcurrently = config.isConcurrentActions();
		
		//Initialize Actions last!
		this.actions = initializeHTTPActions(config.getActions(), threadNumber);
	}
	
	/**
	 * Run each thread independently of all others. There is no coordination among threads.
	 * Returns the number of times the test plan was executed
	 * 
	 * @throws Exception 
	 */
	private int runIndependantly(long threadStartTime) throws Exception
	{
		int runCount = 0; 
		do
		{
			long testPlanStartTime = System.currentTimeMillis();
			runTestPlan();
			
			//Record the time it took to execute the entire test plan
			globalPlayerStats.recordTestPlanTime(System.currentTimeMillis() - testPlanStartTime);
			
			//After every test plan, we need to clear the cookies
			cookieStore.clear();
			
			runCount++;
		}
		//Ensure that the threads runs for at least its minimum run time
		//Note that the thread will always run the test plan at least once.
		while(System.currentTimeMillis() - threadStartTime < minRunTime);
		
		return runCount;
	}
	
	/**
	 * Run each action of the test plan concurrently on different threads.
	 * Each thread will grab the next action
	 * Return the total number of actions run
	 * @return 
	 * 
	 * @throws Exception 
	 */
	private int runConcurrently(long threadStartTime) throws Exception
	{
		int runCount = 0; 
		
		long lastActionTime = System.currentTimeMillis();
		while(true)
		{
			//Get the index of the next action to execute.
			//This is synchronized so the threads won't run the same action at the same time.
			int textPlanAction = master.getNextTestPlanAction();
			
			//If configured, stop running when the end of the test plan is reached
			if(textPlanAction < 0) break;
			
			//Run the action, while keeping track of timings
			lastActionTime = runTestPlanAction(actions.get(textPlanAction), lastActionTime);
			
			//Break if shutdown is requested
			if(lastActionTime < 0) break;
			
			runCount++;
			
			//Ensure that the threads runs for at least its minimum run time, if a minimum time is configured 
			if(minRunTime > 0 && (System.currentTimeMillis() - threadStartTime >= minRunTime)) break;
		}
		
		return runCount;
	}
	
	@Override
	public void run()
	{
		try
		{
			long threadStartTime = System.currentTimeMillis();
			int runCount = runActionsConcurrently ? runConcurrently(threadStartTime) : runIndependantly(threadStartTime);
			long endTime = System.currentTimeMillis();
			double timeInMinutes = (endTime - threadStartTime)/60000.0;	
			
 			log.info("Thread " + threadNumber + " completed " + runCount + (runActionsConcurrently ? " action(s)": " run(s) of the test plan")  + " in " + String.format("%.2f",timeInMinutes) 
     			+ " minutes, including pauses." + (runCount > 1 ? " Average " + (runActionsConcurrently ? "action": "test plan") + " time " 
     			+ String.format("%.2f",timeInMinutes/runCount) + " minutes, including pauses." : ""));
		}
		catch(InterruptedException e)
		{
			//Thread externally halted
		}
		catch(Exception e)
		{
			log.error("Thread " + threadNumber + " failed.", e);
		}
		finally
		{
			master.threadComplete(Thread.currentThread());
		}
	}
	
	private void runTestPlan() throws Exception
	{
		long lastActionTime = System.currentTimeMillis();
		for(PlayerHTTPAction action : actions)
		{
			lastActionTime = runTestPlanAction(action, lastActionTime);
			
			//Break if shutdown is requested
			if(lastActionTime < 0) return;
		}
	}
	
	/**
	 * Run a single test plan action.
	 * Return the system time at the start of execution of the action.
	 * 
	 * @param action
	 * @param lastActionTime
	 * @return
	 * @throws Exception
	 */
	private long runTestPlanAction(PlayerHTTPAction action, long lastActionTime) throws Exception
	{
		//Handle termination of the test plan via JMX/REST
		if(!master.isRunning()) return -1l;
		
		//Do all pre-action prep
		//Do this before waiting so that the time it takes to prep is not added
		//to the execution time of the action (or a adds as little as possible)
		preActionRun(action);
		
		//By-pass Test Plan timings if desired
		long waitTime = (actionDelay < 0 ? action.getTimePassed() : actionDelay);
		
		//Ensure that the start time of every action matches the timings in the test plan
		long currentTime = System.currentTimeMillis();
		while(currentTime - lastActionTime < waitTime)
		{
			if(!master.isRunning()) return -1l;
			Thread.sleep(10);
			currentTime = System.currentTimeMillis();
		}
		
		//Run the action and store the duration
		//Note that the duration is the server response time including network delay
		//It does not include the overhead of this thread
		HttpResponse response =	runAction(action);
		globalPlayerStats.recordActionTime(action.getIdentifier(), action.getLastRunDuration());
		
		//Handle all post-run processing
		postActionRun(action, response);
		
		//Note that test plan timings are calculated from the start of execution of the last action
		//NOT from the end of execution of the last action.
		return currentTime;
	}
	
	private void preActionRun(PlayerHTTPAction action) throws Exception
	{	
		//Apply any variables
		if(useVariableSubstitutions) substituter.applyVariableValues(action);
			
		//If the action has not been converted to an HTTP Request, due to variable substitutions, do it now.
		if(null == action.getHttpRequest()) action.setHttpRequest(requestConverter.convertHTTPActionToApacheRequest(action));
		
		//Apply any relevant non-expired cookies
		CookieHandler.applyCookies(cookieStore, action.getWhiteListCookies(), action.getCookieOrigin(), action.getHttpRequest());
	}
	
	private void postActionRun(PlayerHTTPAction action, HttpResponse response)
	{
		//Extract any variables
		if(useVariableSubstitutions && (action.getRetrievalVariables().size() > 0) && (response.getEntity() != null)) 
		{
			String responseBody = ((HttpEntityWrapper) response.getEntity()).getResponseBody();
			substituter.retrieveVariableValues(action, responseBody);
		}
		
		//Store any cookies for subsequent calls
		CookieHandler.storeCookie(cookieStore, action.getCookieOrigin(), response);
	}
	
	private HttpResponse runAction(PlayerHTTPAction action) throws Exception
	{
		if(null == action.getHttpRequest())
			throw new Exception("Cannot execute action " + action + ". Method " + action.getMethod() + " is not supported.");
		
		long startTime;
		long endTime;
		HttpResponse response = null;
		HttpRequestBase request = action.getHttpRequest();
		try
		{
    		startTime = System.nanoTime();
    		response = httpClient.execute(request);
    		
    		//Consume the response body fully so that the connection can be reused.
    		//Reading the body is part of the overall action execution time.
    		HttpEntity entity = response.getEntity();
    		
    		if(null != entity)
    		{
	    		//For variable substitutions, we will need to read the response body
	    		//Otherwise, discard it
	    		if(useVariableSubstitutions && action.getRetrievalVariables().size() > 0)
	    		{
	    			HttpEntityWrapper wrapper = new HttpEntityWrapper(entity);
	    			response.setEntity(wrapper);
	    			wrapper.setResponseBody(IOUtils.toString(entity.getContent()));
	    		}
	    		else
	    		{
	    			Utils.discardStream(entity.getContent());
	    		}
    		}

    		endTime = System.nanoTime();
		}
		catch(Exception e)
		{
    		throw new Exception("Failed to execute HTTP Action " + action, e);
		}
    	finally
		{
    		//Release connection and make it reusable
    		request.releaseConnection();
		}
		
		log.info("Thread " + threadNumber + " recieved HTTP code " + response.getStatusLine().getStatusCode() + " for action " + action + ".");
		action.setLastRunDuration((endTime - startTime)/1000000);
		
		return response;
	}
	
	/**
	 * Every thread will have different values for request path, query and body.
	 * So, we have to initialize the Actions in a thread-specific way.
	 */
	private List<PlayerHTTPAction> initializeHTTPActions(List<HTTPAction> actions, int threadNumber)
	{
		//Must do a deep copy because every thread will have different values for request path, query and body
		List<PlayerHTTPAction> playerHTTPActions = new ArrayList<PlayerHTTPAction>(actions.size());
		int id = 1;
		for(HTTPAction source : actions)
		{
			PlayerHTTPAction action = new PlayerHTTPAction(source);
			playerHTTPActions.add(action);
			action.setId(id);
			
			//Apply fixed substitutions during initialization for better performance
			//Apply before converting to HTTPMethod
			//Also do this before matching the path for variable substitutions
			if(useFixedSubstitutions && action.isHasSubstitutions())
				substituter.applyFixedSubstitutions(action);
			
			//Handle HTTPs override
			if(overrideHttps) action.setScheme("http");
			
			//Pre-compute the cookie origin for each request. This will be different for every action depending on the Path.
			boolean isSecure = action.getScheme().equalsIgnoreCase("https");
			action.setCookieOrigin(CookieHandler.determineCookieOrigin(host, (isSecure ? httpsPort : httpPort), action.getPath(), isSecure));
			
			boolean actionHasVariables = false;
			if(useVariableSubstitutions)
			{
				//Applying multiple regex expressions to every request would be expensive
				//So, we pre-compute which actions use what variables
				List<VariableSubstitution> retrievalVariables = substituter.retrievalVariablesForAction(action);
				List<VariableSubstitution> replacementVariables = substituter.replacementVariablesForAction(action);
				action.setRetrievalVariables(retrievalVariables);
				action.setReplacementVariables(replacementVariables);
				actionHasVariables = !(retrievalVariables.isEmpty() && replacementVariables.isEmpty());
			}
			
			//We re-use Apache HTTP Client Requests for better performance, so pre-generate it.
			//But, we can only pre-generate it if there are no variables in the action.
			if(!actionHasVariables)
			{
				try
				{
					action.setHttpRequest(requestConverter.convertHTTPActionToApacheRequest(action));
				}
				catch (URISyntaxException e)
				{
					throw new RuntimeException("Failed to convert HTTP Action " + source, e);
				}
			}
			
			//Generally, we don't want to use the cookies saved in the test plan, at the time of recording
			//since these are out of date. However, there are exceptions. 
			//For example, the cookie may contain authentication or authorization.
			//So, we provide a cookie white list that we want to retain.
			action.setWhiteListCookies(CookieHandler.applyWhiteListCookies(action.getHeaders(), cookieWhiteList));
			
			id++;
		}
		return playerHTTPActions;
	}
}
