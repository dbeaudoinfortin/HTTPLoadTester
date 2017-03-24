package com.dbf.loadtester.player.management;

import java.util.List;
import java.util.Map;

import com.dbf.loadtester.player.stats.TimeStats;

//Interface specifically named for JMX
public interface PlayerManagerMBean
{
	public boolean isRunning();
	
	public int getRunningThreadCount();
	
	public void start();
	
	public void stop();
	
	public int getThreadCount();

	public void setThreadCount(int threadCount);

	public long getStaggerTime();

	public void setStaggerTime(long staggerTime);

	public long getMinRunTime();
	
	public void setMinRunTime(long minRunTime);

	public String getTestPlanFile();
	
	public void setTestPlanFile(String testPlanFile);

	public String getHost();

	public void setHost(String host);

	public int getHttpPort();

	public void setHttpPort(int httpPort);

	public int getHttpsPort();

	public void setHttpsPort(int httpsPort);

	public int getActionDelay();

	public void setActionDelay(int actionDelay);
	
	public boolean isUseFixedSubstitutions();

	public void setUseFixedSubstitutions(boolean useSubstitutions);
	
	public boolean isOverrideHttps();

	public void setOverrideHttps(boolean overrideHttps);
	
	public Map<String, TimeStats> getActionStats();
	
	public TimeStats getTestPlanStats();
	
	public TimeStats getAggregateActionStats();
	
	public boolean isShareConnections();

	public void setShareConnections(boolean shareConnections);
	
	public List<String> getCookieWhiteList();

	public void setCookieWhiteList(List<String> cookieWhiteList);
	
	public boolean hasVariableSubstitutions();

	public String getVariableSubstitutions();

	public void setVariableSubstitutions(String variableSubstitutions);
}
