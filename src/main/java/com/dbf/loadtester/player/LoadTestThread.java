package com.dbf.loadtester.player;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCookieStore;

import com.dbf.loadtester.common.action.ActionVariable;
import com.dbf.loadtester.common.action.HTTPAction;
import com.dbf.loadtester.common.action.converter.ApacheRequestConverter;
import com.dbf.loadtester.common.util.Utils;
import com.dbf.loadtester.player.action.PlayerHTTPAction;
import com.dbf.loadtester.player.config.PlayerOptions;
import com.dbf.loadtester.player.cookie.CookieHandler;
import com.dbf.loadtester.player.stats.PlayerStats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadTestThread implements Runnable
{
	private static final Logger log = LoggerFactory.getLogger(LoadTestThread.class);
	
	private static final String THREAD_ID_PARAM = "<THREAD_ID>";
	private static final Pattern THREAD_ID_PARAM_PATTERN = Pattern.compile(THREAD_ID_PARAM);

	private Map<Pattern, String> staticSubstitutions;
	private List<ActionVariable> variableSubstitutions;
	
	private final CookieStore cookieStore = new BasicCookieStore();
	
	private final HttpClient httpClient;	
	private final int threadNumber;
	private final int actionDelay;
	private final boolean useSubstitutions;
	private final long minRunTime;
	private final String host;
	private final int httpPort;
	private final int httpsPort;
	private final boolean overrideHttps;
	private final List<PlayerHTTPAction> actions;
	private final PlayerStats globalPlayerStats;
	private final LoadTestCoordinator master;
	private final ApacheRequestConverter requestConverter;
	private final Collection<String> cookieWhiteList;

	public LoadTestThread(LoadTestCoordinator master, PlayerOptions config, int threadNumber, HttpClient httpClient, ApacheRequestConverter requestConverter)
	{
		this.threadNumber = threadNumber;
		this.httpClient = httpClient;
		this.actionDelay = config.getActionDelay();
		this.useSubstitutions = config.isUseSubstitutions();
		this.minRunTime = config.getMinRunTime();
		this.host = config.getHost();
		this.httpPort = config.getHttpPort();
		this.httpsPort = config.getHttpsPort();
		this.overrideHttps = config.isOverrideHttps();
		this.globalPlayerStats = config.getGlobalPlayerStats();
		this.master = master;
		this.requestConverter = requestConverter;
		this.cookieWhiteList = config.getCookieWhiteList();
		
		//Initialize Actions last!
		this.actions = initializeHTTPActions(config.getActions(), threadNumber);
	}
	
	@Override
	public void run()
	{
		try
		{
			int runCount = 0; 
			long threadStartTime = System.currentTimeMillis();
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
			
			long endTime = System.currentTimeMillis();
			double timeInMinutes = (endTime - threadStartTime)/60000.0;
			
 			log.info("Thread " + threadNumber + " completed " + runCount + " run" + (runCount > 1 ? "s" : "") + " of the test plan in " + String.format("%.2f",timeInMinutes) + " minutes, including pauses." + (runCount > 1 ? " Average test plan time " + String.format("%.2f",timeInMinutes/runCount) + " minutes, including pauses." : ""));
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
			//Handle termination of the test plan via JMX/REST
			if(!master.isRunning()) return;
			
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
				if(!master.isRunning()) return;
				Thread.sleep(10);
				currentTime = System.currentTimeMillis();
			}
			
			//Note that test plan timings are calculated from the start of execution of the last action
			//NOT from the end of execution of the last action.
			lastActionTime = currentTime;
			
			//Run the action and store the duration
			//Note that the duration is the server response time including network delay
			//It does not include the overhead of this thread
			HttpResponse response =	runAction(action);
			globalPlayerStats.recordActionTime(action.getIdentifier(), action.getLastRunDuration());
			
			//Handle all post-run processing
			postActionRun(action, response);
		}
	}
	
	private void preActionRun(PlayerHTTPAction action) throws Exception
	{	
		//Apply any variables
		if(action.isHasVariables())
		{
			blah, do it;
		}
			
		//If the action has not been converted to an HTTP Request, due to variable substitutions, do it now.
		if(null == action.getHttpRequest()) action.setHttpRequest(requestConverter.convertHTTPActionToApacheRequest(action));
		
		//Apply any relevant non-expired cookies
		CookieHandler.applyCookies(cookieStore, action.getWhiteListCookies(), action.getCookieOrigin(), action.getHttpRequest());
	}
	
	private void postActionRun(PlayerHTTPAction action, HttpResponse response)
	{
		//Extract any variables
		
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
    		startTime = System.currentTimeMillis();
    		response = httpClient.execute(request);
    		
    		//Consume the response body fully so that the connection can be reused.
    		//Reading the body is part of the overall action execution time.
    		HttpEntity entity = response.getEntity();
    		if(null != entity) Utils.discardStream(entity.getContent());

    		endTime = System.currentTimeMillis();
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
		action.setLastRunDuration(endTime - startTime);
		
		return response;
	}
	
	private void initSubstitutions()
	{
		//These are built-in replacement string
		//These are calculated ahead of time for better performance
		//Currently there is only Thread Number, more will be added later
		staticSubstitutions = new HashMap<Pattern, String>();
		staticSubstitutions.put(THREAD_ID_PARAM_PATTERN, "" + threadNumber);
	}
	
	/**
	 * Every thread will have different values for request path, query and body.
	 * So, we have to initialize the Actions in a thread-specific way.
	 */
	private List<PlayerHTTPAction> initializeHTTPActions(List<HTTPAction> actions, int threadNumber)
	{
		//Apply substitutions during initialization for better performance
		if(useSubstitutions) initSubstitutions();
					
		//Must do a deep copy because every thread will have different values for request path, query and body
		List<PlayerHTTPAction> playerHTTPActions = new ArrayList<PlayerHTTPAction>(actions.size());
		int id = 1;
		for(HTTPAction source : actions)
		{
			PlayerHTTPAction action = new PlayerHTTPAction(source);
			playerHTTPActions.add(action);
			action.setId(id);
			
			//Apply substitutions before converting to HTTPMethod
			if(useSubstitutions && action.isHasSubstitutions()) applySubstitutions(action);
			
			//Handle HTTPs override
			if(overrideHttps) action.setScheme("http");
			
			//Pre-compute the cookie origin for each request. This will be different for every action depending on the Path.
			boolean isSecure = action.getScheme().equalsIgnoreCase("https");
			action.setCookieOrigin(CookieHandler.determineCookieOrigin(host, (isSecure ? httpsPort : httpPort), action.getPath(), isSecure));
			
			//We re-use Apache HTTP Client Requests for better performance, so pre-generate it.
			//But, we can only pre-generate it if there are no variables in the action.
			if(!action.isHasVariables())
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
	
	private void applySubstitutions(HTTPAction action)
	{
		if(action.getPath() != null)
			action.setPath(Utils.applyRegexSubstitutions(action.getPath(), staticSubstitutions));
		
		if(action.getQueryString() != null)
			action.setQueryString(Utils.applyRegexSubstitutions(action.getQueryString(), staticSubstitutions));
		
		//Body only applies to post and put
		if(action.getContent() != null && ("PUT".equals(action.getMethod()) || "POST".equals(action.getMethod())))
		{
			String content = Utils.applyRegexSubstitutions(action.getContent(), staticSubstitutions);
			action.setContent(content);
			action.setContentLength(content.length());
		}
	}
}
