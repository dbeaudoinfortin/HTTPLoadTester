package com.dbf.loadtester.recorder.jmx;

public interface RecorderManagerMBean
{
	public void start();
	
	public void stop();
	
	public String getTestPlanDirectory();

	public void setTestPlanDirectory(String testPlanDirectory);

	public boolean isRunning();
	
	public String getTestPlanPath();
	
	public void setTestPlanPath(String testPlanPath);
	
	public void setPathSubstitutions(String map);
	
	public String getPathSubstitutions();

	public void setQuerySubstitutions(String map);

	public String getQuerySubstitutions();
	
	public void setBodySubstitutions(String map);

	public String getBodySubstitutions();
}
