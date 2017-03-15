package com.dbf.loadtester.player.management;

import java.io.File;
import java.util.Map;

import com.dbf.loadtester.player.LoadTestCoordinator;
import com.dbf.loadtester.player.config.PlayerOptions;
import com.dbf.loadtester.player.stats.TimeStats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Class specifically named for JMX
public class PlayerManager implements PlayerManagerMBean
{
	private static final Logger log = LoggerFactory.getLogger(LoadTestCoordinator.class);
	
	private final PlayerOptions config;
	private final LoadTestCoordinator master;
	
	public PlayerManager(LoadTestCoordinator master, PlayerOptions config)
	{
		this.config = config;
		this.master = master;
	}
	
	@Override
	public boolean isRunning()
	{
		return master.isRunning();
	}

	@Override
	public int getRunningThreadCount()
	{
		// TODO Auto-generated method stub
		return master.getRunningThreadCount();
	}

	@Override
	public void start()
	{
		try
		{
			master.start();	
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
		master.stop();
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
		return config.getHttpsPort();
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
	public boolean isUseSubstitutions()
	{
		return config.isUseSubstitutions();
	}
	
	@Override
	public void setUseSubstitutions(boolean useSubstitutions)
	{
		if(isRunning()) throw new RuntimeException("Can't modify configuration while running.");
		config.setUseSubstitutions(useSubstitutions);;	
	}

	@Override
	public boolean isOverrideHttps()
	{
		return config.isOverrideHttps();
	}

	@Override
	public void setOverrideHttps(boolean overrideHttps)
	{
		config.setOverrideHttps(overrideHttps);;
	}

	@Override
	public Map<String, TimeStats> getActionStats()
	{
		return config.getGlobalPlayerStats().getActionStats();
	}

	@Override
	public TimeStats getTestPlanStats()
	{
		return config.getGlobalPlayerStats().getTestPlanStats();
	}
	
	@Override
	public TimeStats getAggregateActionStats()
	{
		return config.getGlobalPlayerStats().getAggregateActionStats();
	}
	
	@Override
	public String toString()
	{
		return "PlayerMBean";
	}
}
