package com.dbf.loadtester.player;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.apache.commons.httpclient.contrib.ssl.EasyX509TrustManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.log4j.Logger;

import com.dbf.loadtester.HTTPAction;
import com.dbf.loadtester.HTTPActionConverter;
import com.dbf.loadtester.player.config.PlayerConfiguration;
import com.dbf.loadtester.player.stats.ActionTime;

public class LoadTestThread implements Runnable
{
	private static final Logger log = Logger.getLogger(LoadTestThread.class);
	
	private static SSLConnectionSocketFactory sslFactory;
	
	private final HttpClient httpClient;	
	
	private final int threadNumber;
	private final PlayerConfiguration config;
	
	private final Map<String, ActionTime> actionTimes = new HashMap<String, ActionTime>(100);
	
	static
	{
		try
		{
		SSLContext sslcontext = SSLContexts.custom().useProtocol("SSL").build();
        sslcontext.init(null, new TrustManager[] {new EasyX509TrustManager(null)}, null);
        sslFactory = new SSLConnectionSocketFactory(sslcontext, new DefaultHostnameVerifier());
		}
        catch (Throwable t)
		{
        	log.fatal("Fail to initialize SSL trust Manager.",t);
		}
	}
	
	public LoadTestThread(PlayerConfiguration config, int threadNumber)
	{
		this.config = config;
		this.threadNumber = threadNumber;
		httpClient = HttpClientBuilder.create().disableRedirectHandling().setSSLSocketFactory(sslFactory).build();
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
				for(HTTPAction action : config.getActions())
				{					
					//By-pass Test Plan timings for debug purposes
					long waitTime = (config.getActionDelay() < 0 ? action.getTimePassed() : config.getActionDelay());
					
					//Ensure that the start time of every action matches the timings in the test plan
					long currentTime = System.currentTimeMillis();
					while(currentTime - lastActionTime < waitTime)
					{
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
			while((new Date()).getTime() - startTime < config.getMinRunTime());
			
			long endTime = System.currentTimeMillis();
			double timeInMinutes = (endTime - startTime)/60000.0;
			
 			log.info("Thread " + threadNumber + " completed " + runCount + " run" + (runCount > 1 ? "s" : "") + " of the test plan in " + timeInMinutes + " minutes, including pauses." + (runCount > 1 ? " Average time " + timeInMinutes/runCount + " minutes, including pauses." : ""));
 			printActionTimes();
		}
		catch(Exception e)
		{
			log.error("Thread " + threadNumber + " failed.", e);
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
		HttpRequestBase method = HTTPActionConverter.convertHTTPAction(action, config.getHost(), config.getHttpPort(), config.getHttpsPort());
		
		if(null == method)
		{
			log.error("Cannot execute action " + action.getPath() + ". Method " + action.getMethod() + " is not supported.");
			return null;
		}

		long startTime = System.currentTimeMillis();
		HttpResponse response = httpClient.execute(method);
		long endTime = System.currentTimeMillis();
		
		log.info("Thread " + threadNumber + " recieved HTTP code " + response.getStatusLine().getStatusCode() + " for action " + action.getPath() + ".");

		return endTime - startTime;
	}
}
