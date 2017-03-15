package com.dbf.loadtester.player;

import java.net.URISyntaxException;
import java.util.ArrayList;
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
import com.dbf.loadtester.common.action.HTTPAction;
import com.dbf.loadtester.common.action.HTTPConverter;
import com.dbf.loadtester.common.util.Utils;
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

	private Map<Pattern, String> substitutions;
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
	private final List<HTTPAction> actions;
	private final PlayerStats globalPlayerStats;
	private final LoadTestCoordinator master;
	
	
	
	public LoadTestThread(LoadTestCoordinator master, PlayerOptions config, int threadNumber, HttpClient httpClient)
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
		this.actions = initializeHTTPActions(config.getActions(), threadNumber);
		this.globalPlayerStats = config.getGlobalPlayerStats();
		this.master = master;
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
		
		for(HTTPAction action : actions)
		{
			//Handle termination of the test plan via JMX/REST
			if(!master.isRunning()) return;
			
			//By-pass Test Plan timings for debug purposes
			long waitTime = (actionDelay < 0 ? action.getTimePassed() : actionDelay);
			
			//Ensure that the start time of every action matches the timings in the test plan
			long currentTime = System.currentTimeMillis();
			while(currentTime - lastActionTime < waitTime)
			{
				if(!master.isRunning()) return;
				Thread.sleep(10);
				currentTime = System.currentTimeMillis();
			}
			
			lastActionTime = currentTime;
			
			//Run the action and store the duration
			//Not that the duration is the server response time including network delay
			//It does not include the overhead of this thread
			Long duration =	runAction(action);
			
			if (null != duration)
				globalPlayerStats.recordActionTime(action.getIdentifier(), duration);
		}
	}
	
	private Long runAction(HTTPAction action) throws Exception
	{
		if(null == action.getHttpRequest())
		{
			log.error("Cannot execute action " + action + ". Method " + action.getMethod() + " is not supported.");
			return null;
		}

		long startTime;
		long endTime;
		HttpResponse response = null;
		HttpRequestBase request = action.getHttpRequest();
		try
		{
			//Apply any relevant non-expired cookies
			CookieHandler.applyCookies(cookieStore, action.getCookieOrigin(), request);
			
    		startTime = System.currentTimeMillis();
    		response = httpClient.execute(request);
    		
    		//Consume the response body fully so that  the connection can be reused
    		HttpEntity entity = response.getEntity();
    		if(null != entity) Utils.discardStream(entity.getContent());

    		endTime = System.currentTimeMillis();
    		
    		//Store any cookies for subsequent calls
    		CookieHandler.storeCookie(cookieStore, action.getCookieOrigin(), response);
		}
		catch(Exception e)
		{
			log.error("Failed to execute HTTP Action " + action, e);
    		throw e;
		}
    	finally
		{
    		//Release connection and make it reusable
    		request.releaseConnection();
		}
		
		log.info("Thread " + threadNumber + " recieved HTTP code " + response.getStatusLine().getStatusCode() + " for action " + action + ".");
		return endTime - startTime;
	}
	
	private void initSubstitutions()
	{
		//These are built-in replacement string
		//These are calculated ahead of time for better performance
		//Currently there is only Thread Number, more will be added later
		substitutions = new HashMap<Pattern, String>();
		substitutions.put(THREAD_ID_PARAM_PATTERN, "" + threadNumber);
	}
	
	private List<HTTPAction> initializeHTTPActions(List<HTTPAction> actions, int threadNumber)
	{
		//Apply substitutions during initialization for better performance
		if(useSubstitutions) initSubstitutions();
					
		//Must do a deep copy because every thread will have different values for request path, query and body
		List<HTTPAction> returnList = new ArrayList<HTTPAction>(actions.size());
		for(HTTPAction source : actions)
		{
			HTTPAction actionCopy = new HTTPAction(source);
			returnList.add(actionCopy);
		
			//Apply substitutions before converting to HTTPMethod
			if(useSubstitutions) applySubstitutions(actionCopy);
			
			//Handle HTTPs override
			if(overrideHttps) actionCopy.setScheme("http");
			
			//Re-use HTTP Requests for better performance
			try
			{
				actionCopy.setHttpRequest(HTTPConverter.convertHTTPActionToHTTPClientRequest(actionCopy, host, httpPort, httpsPort));
			}
			catch (URISyntaxException e)
			{
				throw new RuntimeException("Failed to convert HTTP Action " + source, e);
			}
			
			//Pre-compute the cookie origin for each request
			boolean isSecure = actionCopy.getScheme().equalsIgnoreCase("https");
			actionCopy.setCookieOrigin(CookieHandler.determineCookieOrigin(host, httpPort, actionCopy.getPath(), isSecure));
		}
		return returnList;
	}
	
	private void applySubstitutions(HTTPAction action)
	{
		if(action.getPath() != null)
			action.setPath(Utils.applyRegexSubstitutions(action.getPath(), substitutions));
		
		if(action.getQueryString() != null)
			action.setQueryString(Utils.applyRegexSubstitutions(action.getQueryString(), substitutions));
		
		//Body only applies to post and put
		if(action.getContent() != null && ("PUT".equals(action.getMethod()) || "POST".equals(action.getMethod())))
		{
			String content = Utils.applyRegexSubstitutions(action.getContent(), substitutions);
			action.setContent(content);
			action.setContentLength(content.length());
		}
	}
}
