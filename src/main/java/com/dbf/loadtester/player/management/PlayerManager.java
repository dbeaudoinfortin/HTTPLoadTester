package com.dbf.loadtester.player.management;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
		if(isRunning()) throw new RuntimeException("Can't modify configuration while running.");
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
		if(isRunning()) throw new RuntimeException("Can't modify configuration while running.");
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
		if(isRunning()) throw new RuntimeException("Can't modify configuration while running.");
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
		if(isRunning()) throw new RuntimeException("Can't modify configuration while running.");
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
		if(isRunning()) throw new RuntimeException("Can't modify configuration while running.");
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
		if(isRunning()) throw new RuntimeException("Can't modify configuration while running.");
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
	public boolean isUseFixedSubstitutions()
	{
		return config.isUseFixedSubstitutions();
	}
	
	@Override
	public void setUseFixedSubstitutions(boolean useFixedSubstitutions)
	{
		if(isRunning()) throw new RuntimeException("Can't modify configuration while running.");
		config.setUseFixedSubstitutions(useFixedSubstitutions);;	
	}
	
	@Override
	public boolean isUseVariableSubstitutions()
	{
		return config.isUseVariableSubstitutions();
	}

	@Override
	public boolean isOverrideHttps()
	{
		return config.isOverrideHttps();
	}

	@Override
	public void setOverrideHttps(boolean overrideHttps)
	{
		if(isRunning()) throw new RuntimeException("Can't modify configuration while running.");
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
	public boolean isShareConnections()
	{
		return config.isShareConnections();
	}
	
	@Override
	public void setShareConnections(boolean shareConnections)
	{
		if(isRunning()) throw new RuntimeException("Can't modify configuration while running.");
		config.setShareConnections(shareConnections);
	}
	
	@Override
	public List<String> getCookieWhiteList()
	{
		return new ArrayList<String>(config.getCookieWhiteList());
	}

	@Override
	public void setCookieWhiteList(List<String> cookieWhiteList)
	{
		if(isRunning()) throw new RuntimeException("Can't modify configuration while running.");
		config.setCookieWhiteList(new HashSet<String>(cookieWhiteList));
	}
	
	@Override
	public String toString()
	{
		return "PlayerMBean";
	}
}
