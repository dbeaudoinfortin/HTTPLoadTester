package com.dbf.loadtester.player;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import com.dbf.loadtester.httpclient.HTTPClientFactory;
import com.dbf.loadtester.player.config.Constants;
import com.dbf.loadtester.player.config.PlayerConfiguration;
import com.dbf.loadtester.player.jmx.PlayerManager;
import com.dbf.loadtester.player.jmx.PlayerManagerMBean;

public class LoadTestPlayer
{
	private static final Logger log = Logger.getLogger(LoadTestPlayer.class);
	private static final Random random = new Random();
	
	private static Boolean running = false;
	
	private static PlayerConfiguration config = null;
	private static final List<Thread> workerThreads = new ArrayList<Thread>();
	private static Thread launcherThread;
			
	/**
	 * Run an existing test plan
	 * 
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		try
		{
			config = new PlayerConfiguration(args);
			registerMBean();
			
			if(args.length > 0)
			{
				start();
			}
			else
			{
				//Wait for JMX (forever)
				while(true) Thread.sleep(1000000);
			}
		}
		catch(IllegalArgumentException e)
		{
			log.error(e.getMessage() + "\r\nMust either provide path to a test plan or provide all arguments.\r\n\r\n" + PlayerConfiguration.getUsage());
			return;
		}
		catch(Exception e)
		{
			log.error("Failed to load player configuration.", e);
			return;
		}
	}
	
	public static void start() throws IOException, IllegalArgumentException
	{
		synchronized(running)
		{
			if(running)
			{
				log.warn("Attempted to start load test while already running.");
				return;
			}
			
			config.loadTestPlan();
			
			log.info("Launching worker threads...");
			running = true;
		}
		
		launchWorkerThreads();
	}
	
	public static void stop()
	{
		synchronized(running)
		{
			if(!running) return;
			
			log.info("Halting worker threads...");
			
			//Interrupt all of the threads
			if(null != launcherThread) launcherThread.interrupt(); 
			for(Thread thread : workerThreads)
			{
				thread.interrupt();
			}
			
			workerThreads.clear();
			running = false;
		}
	}
	
	private static void launchWorkerThreads()
	{
		launcherThread = new Thread()
		{
			 @Override
			 public void run()
			 {
				 try
				{
					HttpClient client = HTTPClientFactory.getHttpClient(config.getThreadCount());
					
					for (int i = 1; i < config.getThreadCount() + 1; i++)
					{
						synchronized(running)
						{	
							if(!running) return;
							Thread thread = new Thread(new LoadTestThread(config, i, client));
							workerThreads.add(thread);
							
							log.info("Starting thread " + i);
							thread.start();
						}
						
						int staggerOffset = (int)(config.getStaggerTime() * Constants.MAX_STAGGER_OFFSET);
						long stagger = (config.getStaggerTime() - staggerOffset) + random.nextInt((int)(staggerOffset*2));
						
						try
						{
							Thread.sleep(stagger < 0 ? 0 : stagger);
						}
						catch(InterruptedException e)
						{
							if(!running) return;
						}
						
					}
					log.info("All threads launched.");
				}
				catch(Exception e)
				{
					log.error("Failed to run test plan.",e);
				}
			 }
		};
		launcherThread.start();	
	}
	
	private static void registerMBean()
	{
		log.info("Attempting to register Player MBean...");
		
		try
		{
			PlayerManagerMBean manager = new PlayerManager(config);
			ObjectName  mbeanName = new ObjectName("com.dbf.loadtester:name=" + manager.toString());
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			mbs.registerMBean(manager, mbeanName);
		}
		catch(Exception e)
		{
			log.warn("Failed to register MBean. JMX monitoring will not be possible.", e);
		}
	}

	public static boolean isRunning()
	{
		synchronized(running)
		{
			return running;
		}
	}
	
	public static int getRunningThreadCount()
	{
		return workerThreads.size();
	}
}
