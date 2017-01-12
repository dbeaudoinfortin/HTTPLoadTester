package com.dbf.loadtester.recorder.filter;

import java.util.Map;

public class RecorderServletFilterOptions
{
	private String testPlanDirectory;
	private boolean immediateStart;
	
	private Map<String, String> pathSubs;
	private Map<String, String> querySubs;
	private Map<String, String> bodySubs;
	
	public String getTestPlanDirectory()
	{
		return testPlanDirectory;
	}
	
	public void setTestPlanDirectory(String testPlanDirectory)
	{
		this.testPlanDirectory = testPlanDirectory;
	}
	
	public boolean isImmediateStart()
	{
		return immediateStart;
	}
	
	public void setImmediateStart(boolean immediateStart)
	{
		this.immediateStart = immediateStart;
	}
	
	public Map<String, String> getPathSubs()
	{
		return pathSubs;
	}
	
	public void setPathSubs(Map<String, String> pathSubs)
	{
		this.pathSubs = pathSubs;
	}
	
	public Map<String, String> getQuerySubs()
	{
		return querySubs;
	}
	public void setQuerySubs(Map<String, String> querySubs)
	{
		this.querySubs = querySubs;
	}
	
	public Map<String, String> getBodySubs()
	{
		return bodySubs;
	}
	
	public void setBodySubs(Map<String, String> bodySubs)
	{
		this.bodySubs = bodySubs;
	}
	
	
	public RecorderServletFilterOptions withTestPlanDirectory(String testPlanDirectory)
	{
		this.testPlanDirectory = testPlanDirectory;
		return this;
	}

	public RecorderServletFilterOptions withImmediateStart(boolean immediateStart)
	{
		this.immediateStart = immediateStart;
		return this;
	}
	
	public RecorderServletFilterOptions withPathSubs(Map<String, String> pathSubs)
	{
		this.pathSubs = pathSubs;
		return this;
	}
	
	public RecorderServletFilterOptions withQuerySubs(Map<String, String> querySubs)
	{
		this.querySubs = querySubs;
		return this;
	}
	
	public RecorderServletFilterOptions withBodySubs(Map<String, String> bodySubs)
	{
		this.bodySubs = bodySubs;
		return this;
	}
}
