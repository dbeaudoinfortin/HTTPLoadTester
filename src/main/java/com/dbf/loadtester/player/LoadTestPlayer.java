package com.dbf.loadtester.player;

import java.io.IOException;
import java.util.Random;

import org.apache.log4j.Logger;

import com.dbf.loadtester.player.config.Constants;
import com.dbf.loadtester.player.config.PlayerConfiguration;

public class LoadTestPlayer
{
	private static final Logger log = Logger.getLogger(LoadTestPlayer.class);
	
	private static final Random random = new Random();
	
	/**
	 * Run an existing test plan
	 * 
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		PlayerConfiguration config = null;
		try
		{
			config = new PlayerConfiguration(args);
		}
		catch(IllegalArgumentException e)
		{
			log.error(e.getMessage() + "\r\nMust either provide path to a test plan or provide all arguments.\r\n\r\n" + PlayerConfiguration.getUsage());
			return;
		}
		catch(Exception e)
		{
			log.error("Failed to load test plan.", e);
			return;
		}
		
		launchWorkerThreads(config);
	}
	
	private static void launchWorkerThreads(PlayerConfiguration config)
	{
		try
		{
			for (int i = 1; i < config.getThreadCount() +1; i++)
			{
				Thread thread = new Thread(new LoadTestThread(config, i));
				thread.start();
				log.info("Starting thread " + i);
				
				int staggerOffset = (int)(config.getStaggerTime() * Constants.MAX_STAGGER_OFFSET);
				long stagger = (config.getStaggerTime() - staggerOffset) + random.nextInt((int)(staggerOffset*2));
				Thread.sleep(stagger < 0 ? 0 : stagger);
			}
			log.info("All threads launched.");
		}
		catch(Exception e)
		{
			log.error("Failed to run test plan.",e);
		}
	}
}
