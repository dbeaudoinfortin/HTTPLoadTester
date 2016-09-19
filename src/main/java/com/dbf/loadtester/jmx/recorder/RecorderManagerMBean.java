package com.dbf.loadtester.jmx.recorder;

public interface RecorderManagerMBean
{
	public void start();
	
	public void stop();
	
	public String getTestPlanDirectory();

	public void setTestPlanDirectory(String testPlanDirectory);

	public boolean isRunning();
}
