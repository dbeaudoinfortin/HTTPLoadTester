package com.dbf.loadtester.player.jmx;

import java.io.File;

import org.apache.log4j.Logger;

import com.dbf.loadtester.player.LoadTestPlayer;
import com.dbf.loadtester.player.config.PlayerConfiguration;

public class PlayerManager implements PlayerManagerMBean
{
	private static final Logger log = Logger.getLogger(LoadTestPlayer.class);
	
	private final PlayerConfiguration config;
	
	public PlayerManager(PlayerConfiguration config)
	{
		this.config = config;
	}
	
	@Override
	public boolean isRunning()
	{
		return LoadTestPlayer.isRunning();
	}

	@Override
	public int getRunningThreadCount()
	{
		// TODO Auto-generated method stub
		return LoadTestPlayer.getRunningThreadCount();
	}

	@Override
	public void start()
	{
		try
		{
			LoadTestPlayer.start();	
		}
		catch(IllegalArgumentException e)
		{
			log.error("Invalid player configuration: " + e.getMessage());
			throw new RuntimeException("Invalid player configuration: " + e.getMessage());
		}
		catch(Exception e)
		{
			log.error("Failed to start load test player.", e);
			throw new RuntimeException("Failed to start load test player");
		}
	}

	@Override
	public void stop()
	{
		LoadTestPlayer.stop();
	}

	@Override
	public int getThreadCount()
	{
		return config.getThreadCount();
	}

	@Override
	public void setThreadCount(int threadCount)
	{
		config.setThreadCount(threadCount);
	}

	@Override
	public long getStaggerTime()
	{
		return config.getStaggerTime();
	}

	@Override
	public void setStaggerTime(long staggerTime)
	{
		config.setStaggerTime(staggerTime);
	}

	@Override
	public long getMinRunTime()
	{
		return config.getMinRunTime();
	}

	@Override
	public void setMinRunTime(long minRunTime)
	{
		config.setMinRunTime(minRunTime);
	}

	@Override
	public String getTestPlanFile()
	{
		File f = config.getTestPlanFile();
		return f == null ? "" : config.getTestPlanFile().toString();
	}

	@Override
	public void setTestPlanFile(String testPlanFile)
	{
		config.setTestPlanFile(new File(testPlanFile));	
	}

	@Override
	public String getHost()
	{
		return config.getHost();
	}

	@Override
	public void setHost(String host)
	{
		config.setHost(host);
	}

	@Override
	public int getHttpPort()
	{
		return config.getHttpPort();
	}

	@Override
	public void setHttpPort(int httpPort)
	{
		config.setHttpPort(httpPort);
	}

	@Override
	public int getHttpsPort()
	{
		return config.getHttpPort();
	}

	@Override
	public void setHttpsPort(int httpsPort)
	{
		config.setHttpsPort(httpsPort);
		
	}

	@Override
	public int getActionDelay()
	{
		return config.getActionDelay();
	}

	@Override
	public void setActionDelay(int actionDelay)
	{
		if(isRunning()) throw new RuntimeException("Can't modify configuration while running.");
		config.setActionDelay(actionDelay);	
	}

	@Override
	public String toString()
	{
		return "PlayerMBean";
	}
}
