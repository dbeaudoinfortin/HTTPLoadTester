package com.dbf.loadtester.player.jmx;

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
	
	public boolean isUseSubstitutions();

	public void setUseSubstitutions(boolean useSubstitutions);
	
}
