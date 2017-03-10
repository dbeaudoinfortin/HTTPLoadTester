package com.dbf.loadtester.player;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import com.dbf.loadtester.common.action.HTTPAction;
import com.dbf.loadtester.common.action.HTTPConverter;
import com.dbf.loadtester.common.util.Utils;
import com.dbf.loadtester.player.config.PlayerOptions;
import com.dbf.loadtester.player.stats.ActionTime;

public class LoadTestThread implements Runnable
{
	private static final Logger log = Logger.getLogger(LoadTestThread.class);
	
	private static final String THREAD_ID_PARAM = "<THREAD_ID>";
	private static final Pattern THREAD_ID_PARAM_PATTERN = Pattern.compile(THREAD_ID_PARAM);
	
	private Map<Pattern, String> substitutions;
	
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
	
	private final Map<String, ActionTime> actionTimes = new HashMap<String, ActionTime>(100);
	
	public LoadTestThread(PlayerOptions config, int threadNumber, HttpClient httpClient)
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
	}
	
	@Override
	public void run()
	{
		try
		{
			long startTime = System.currentTimeMillis();
			int runCount = 0; 
			startTime = (new Date()).getTime();
			do
			{
				long lastActionTime = System.currentTimeMillis();
				for(HTTPAction action : actions)
				{
					if(!LoadTestPlayer.isRunning()) return;
					
					//By-pass Test Plan timings for debug purposes
					long waitTime = (actionDelay < 0 ? action.getTimePassed() : actionDelay);
					
					//Ensure that the start time of every action matches the timings in the test plan
					long currentTime = System.currentTimeMillis();
					while(currentTime - lastActionTime < waitTime)
					{
						if(!LoadTestPlayer.isRunning()) return;
						Thread.sleep(10);
						currentTime = System.currentTimeMillis();
					}
					
					lastActionTime = currentTime;
					
					//Run the action and store the duration
					//Not that the duration is the server response time including network delay
					//It does not include the overhead of this thread
					Long duration =	runAction(action);
					
					if (null != duration)
						recordActionTime(action.getPath(), duration);
				}
				runCount++;
			}
			while((new Date()).getTime() - startTime < minRunTime);
			
			long endTime = System.currentTimeMillis();
			double timeInMinutes = (endTime - startTime)/60000.0;
			
 			log.info("Thread " + threadNumber + " completed " + runCount + " run" + (runCount > 1 ? "s" : "") + " of the test plan in " + timeInMinutes + " minutes, including pauses." + (runCount > 1 ? " Average time " + timeInMinutes/runCount + " minutes, including pauses." : ""));
 			printActionTimes();
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
			LoadTestPlayer.threadComplete(Thread.currentThread());
		}
	}
	
	private void printActionTimes()
	{
		StringBuilder sb = new StringBuilder("Times for Thread ");
		sb.append(threadNumber) ;
		sb.append(":");

		for (Map.Entry<String, ActionTime> entry : actionTimes.entrySet())
		{
			sb.append("\r");
			sb.append(entry.getKey());
			sb.append(" ");
			sb.append(entry.getValue());
		}
		log.info(sb.toString());
	}
	
	private void recordActionTime(String actionName, long time)
	{
		ActionTime actionTime = actionTimes.get(actionName);
		
		if (null == actionTime)
		{
			actionTime = new ActionTime();
			actionTime.min = time;
			actionTime.max = time;
			actionTime.total = time;
			actionTime.count = 1;
			actionTime.average = time;
			actionTimes.put(actionName, actionTime);
		}
		else
		{
			actionTime.total += time;
			actionTime.count += 1;
			actionTime.average = actionTime.total/actionTime.count;
			actionTime.min = Math.min(time, actionTime.min);
			actionTime.max = Math.max(time, actionTime.max);
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
		try
		{
    		startTime = System.currentTimeMillis();
    		response = httpClient.execute(action.getHttpRequest());
    		
    		//Consume the response body fully so that  the connection can be reused
    		HttpEntity entity = response.getEntity();
    		if(null != entity) Utils.discardStream(entity.getContent());

    		endTime = System.currentTimeMillis();
		}
		catch(Exception e)
		{
			log.error("Failed to execute HTTP Action " + action, e);
    		throw e;
		}
    	finally
		{
    		//Release connection and make it reusable
    		action.getHttpRequest().releaseConnection();
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
