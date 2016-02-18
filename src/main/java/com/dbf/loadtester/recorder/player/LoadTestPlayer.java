package com.dbf.loadtester.recorder.player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.dbf.loadtester.HTTPAction;
import com.google.gson.Gson;

public class LoadTestPlayer
{
	private static final Logger log = Logger.getLogger(LoadTestPlayer.class);
	
	private static final long   DEFAULT_STAGGER_TIME = 1000;
	private static final int    DEFAULT_THREAD_COUNT = 5;
	private static final String DEFAULT_HOST = "http://localhost:10080/tpn-polycom-config-secure";
	
	private static final Double MAX_STAGGER_OFFSET = 0.75; 

	private static final Random random = new Random();

	private static final Gson gson = new Gson();
	
	public static void main(String[] args) throws IOException
	{
		FileInputStream fis = null;
		try
		{
			if(!(args.length == 1 || args.length == 8))
			{
				log.error("Invalid agruments. Must provide path to a test plan or provide all arguments. Usage: TestPlanFilePath, ThreadCount, StaggerTime, MinRunTime, Host");
				return;
			}
			
			//Read from arguments
			File testPlan = new File(args[0]);
			if(!testPlan.isFile())
			{
				log.error("Unable to locate test plan at path '" + args[0] + "'.");
				return;
			}

			//Load the test plan
			List<HTTPAction> actions = loadTestPlan(testPlan);
			
			if (actions.size() < 1)
			{
				log.error("Invalid test plan. Must contain at least one action.");
				return;
			}
			
			long totalTestPlanTime = 0;
			for(HTTPAction action : actions)
				totalTestPlanTime += action.getTimePassed();
			
			log.info("Test plan loaded. Total duration approx. " + totalTestPlanTime/60000.0 + " minutes.");
			
			int threadCount = DEFAULT_THREAD_COUNT;
			if(args.length > 1)
				threadCount = Integer.parseInt(args[1]);
			else
				log.info("Using default thread count:" + DEFAULT_THREAD_COUNT);
			
			long staggerTime = DEFAULT_STAGGER_TIME;
			if(args.length > 1)
				staggerTime = Long.parseLong(args[2]);
			else
				log.info("Using default stagger time:" + DEFAULT_STAGGER_TIME /1000.0 + " seconds");
			
			//Minimum runtime ensures that no thread will terminate before the last thread finished at least 1 run.
			long minRunTime = ((threadCount - 1) * staggerTime) + totalTestPlanTime;
			if(args.length > 1)
				minRunTime = Long.parseLong(args[3]);
			else
				log.info("Using default minimum run time:" + minRunTime/60000.0 + " minutes");
			
			String host = DEFAULT_HOST;
			if(args.length > 1)
				host = args[4];
			else
				log.info("Using default host:" + DEFAULT_HOST);
			
			//Launch the worker threads
			for (int i = 1; i < threadCount+1; i++)
			{
				Thread thread = new Thread(new LoadTestThread(actions, i, host, minRunTime));
				thread.start();
				log.info("Starting thread " + i);
				
				int staggerOffset = (int)(staggerTime*MAX_STAGGER_OFFSET);
				long stagger = (staggerTime - staggerOffset) + random.nextInt((int)(staggerOffset*2));
				Thread.sleep(stagger < 0 ? 0 : stagger);
			}
			log.info("All threads launched.");
		}
		catch(Exception e)
		{
			log.error("Failed to run test plan.",e);
		}
		finally
		{
			if (null != fis) fis.close();
		}
	}
	
	private static List<HTTPAction> loadTestPlan(File testPlan) throws IOException
	{
		List<HTTPAction> actions = new ArrayList<HTTPAction>();
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader (new FileReader(testPlan));
			String line;
	
			while ((line = reader.readLine()) != null)
				actions.add(gson.fromJson(line, HTTPAction.class));
		}
		catch(Exception e)
		{
			log.error("Failed to load test plan at path '" + testPlan + "'.", e);
		}
		finally
		{
			if(reader != null) reader.close();
		}
		return actions;
	}

}
