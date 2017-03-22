package com.dbf.loadtester.player;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.http.client.HttpClient;

import com.dbf.loadtester.common.action.converter.ApacheRequestConverter;
import com.dbf.loadtester.common.httpclient.HTTPClientFactory;
import com.dbf.loadtester.common.util.Utils;
import com.dbf.loadtester.player.config.Constants;
import com.dbf.loadtester.player.config.PlayerOptions;
import com.dbf.loadtester.player.management.PlayerManager;
import com.dbf.loadtester.player.management.PlayerManagerMBean;
import com.dbf.loadtester.player.management.server.PlayerManagementServer;

import io.undertow.Undertow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadTestCoordinator implements Runnable
{
	private static final Logger log = LoggerFactory.getLogger(LoadTestCoordinator.class);
	
	private final Random random = new Random();
	private Boolean running = false;
	
	private final PlayerOptions config;
	private final List<Thread> workerThreads = new ArrayList<Thread>();
	private Thread launcherThread;

	public LoadTestCoordinator(PlayerOptions config)
	{
		this.config = config;
	}
	
	/**
	 * Run an existing test plan
	 * 
	 * @param playerArgs
	 * @throws IOException
	 */
	public void run()
	{
		try
		{
			//Build a management object that is shared across both the Web Server and JMX
			PlayerManagerMBean manager = new PlayerManager(this, config);
			
			if (!config.isDisableJMX()) registerMBean(manager);
			
			Undertow server = null;
			if (!config.isDisableREST()) server = startWebServer(manager, config);
			
			if(config.isPauseOnStartup())
			{
				//Wait for JMX/REST to launch worker threads before continuing
				log.info("Initialization complete. Waiting for JMX/REST to start.");
				while(!running) Thread.sleep(100);
			}
			else
			{
				start();
			}
			
			//If keep alive, loop forever and never terminate
			//Otherwise, terminate once the load test has completed
			while(config.isKeepAlive() || running) Thread.sleep(1000);
			
			//Don't forget to shutdown Undertow, otherwise the JVM will never terminate
			if(null !=  server) server.stop();
			
			//Flush the logs before we terminate since they use async appenders
			Utils.flushAllLogs();
		}
		catch(Throwable t)
		{
			log.error("Failed to run Load Test Player.", t);
			return;
		}
	}
	
	public void start() throws IOException, IllegalArgumentException
	{
		synchronized(running)
		{
			if(running)
			{
				log.warn("Attempted to start load test while already running.");
				return;
			}
			
			//Only load the test plan before starting the thread.
			//because the test plan could have been change via JMX/REST
			config.loadTestPlan();

			log.info("Launching worker threads...");
			running = true;
		}
		
		launchWorkerThreads();
	}
	
	public void stop()
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
	
	private void launchWorkerThreads()
	{
		LoadTestCoordinator master = this;
		
		launcherThread = new Thread("Launcher Thread")
		{
			 @Override
			 public void run()
			 {
				 try
				{	
					//Initialize the HTTPClient to share across threads. The most efficient way is to share the client across all threads of the load tester
					//using a connection pool that is as large as the thread pool. This ensures each thread will have a connection but that they will re-use
					//connections whenever possible. However, this poses a problem with some load balancers that use sticky sessions since they will assign
					//the same node of the cluster as long as the connection remains open. This would mean that all load tester threads would be stuck to
					//a single node. Not good :(
					HttpClient client = null;
					if(config.isShareConnections()) client = HTTPClientFactory.getHttpClient(config.getThreadCount() + 1);
			
					//Set retainCookie to false, since we let the CookieHanlder class take care of it.
					ApacheRequestConverter requestConverter = new ApacheRequestConverter(config.getHost(), config.getHttpPort(), config.getHttpsPort(), true, false);

					for (int i = 1; i < config.getThreadCount() + 1; i++)
					{
						//Apply the per-thread HTTP client if needed.
						if(client == null) client = HTTPClientFactory.getHttpClient(1);

						synchronized(running)
						{	
							if(!running) return;
							Thread thread = new Thread(new LoadTestThread(master, config, i, client, requestConverter), "Test Plan - " + i);
							workerThreads.add(thread);
							
							log.info("Starting thread " + i);
							thread.start();
						}
						
						//Apply stagger time, if configured
						//But, don't wait after the last thread is spun up., that would pointless :)
						if(config.getStaggerTime() > 0 && (i != config.getThreadCount()))
						{
							//The strategy is to offset the start of each thread by some amount of time (Thread Stagger Time)
							//We don't want this offset to be exactly the same because that doesn't simulate realistic load
							//So we pick a random number that will on average be equal to the provided stagger time
							//The range is determined by the Max Stagger Offset. ie T = T +/- (T x 0.75)
    						double staggerOffset = (config.getStaggerTime() * Constants.MAX_STAGGER_OFFSET);
    						double stagger = (config.getStaggerTime() - staggerOffset) + (random.nextDouble() * staggerOffset * 2);
    						
    						try
    						{
    							Thread.sleep(Math.round(stagger));
    						}
    						catch(InterruptedException e)
    						{
    							if(!running) return;
    						}
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
	
	private void registerMBean(PlayerManagerMBean manager)
	{
		log.info("Attempting to register Player MBean...");
		
		try
		{
			ObjectName  mbeanName = new ObjectName("com.dbf.loadtester:name=" + manager.toString());
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			mbs.registerMBean(manager, mbeanName);
			
			log.info("Player MBean successfully registered.");
		}
		catch(Throwable e)
		{
			log.warn("Failed to register MBean. JMX monitoring will not be possible.", e);
		}
	}
	
	private Undertow startWebServer(PlayerManagerMBean manager, PlayerOptions config)
	{
		Undertow server = null;
		
		log.info("Attempting to launch administrative web server...");
		try
		{
			server = PlayerManagementServer.initializeServer(manager, config);
			log.info("Administrative web server started.");
		}
		catch(Throwable e)
		{
			log.warn("Failed to launch administrative web server. Web based administration will not be possible.", e);
		}
		return server;
	}

	public boolean isRunning()
	{
		synchronized(running)
		{
			return running;
		}
	}
	
	public int getRunningThreadCount()
	{
		return workerThreads.size();
	}
	
	public void threadComplete(Thread thread)
	{
		boolean allComplete = false;
		synchronized(running)
		{		
			workerThreads.remove(thread);
			if(!running) return;
			if(workerThreads.size() < 1)
			{
				allComplete = true;
				running = false;
			}
		}
		
		//Log some useful stats
		if(allComplete)
		{
			log.info("All worker threads done.");
			log.info(config.getGlobalPlayerStats().printStatsSummary());
		}
	}
}
