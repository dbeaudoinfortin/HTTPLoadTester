package com.dbf.loadtester.recorder.management;

public interface RecorderManagerMBean
{
	public void start();
	
	public void stop();
	
	public String getTestPlanDirectory();

	public void setTestPlanDirectory(String testPlanDirectory);

	public boolean isRunning();
	
	public String getTestPlanPath();
	
	public void setTestPlanPath(String testPlanPath);
	
	public void setFixedSubstitutions(String map);
	
	public String getFixedSubstitutions();
}
