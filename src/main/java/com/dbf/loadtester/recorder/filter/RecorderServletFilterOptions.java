package com.dbf.loadtester.recorder.filter;

import java.util.List;
import com.dbf.loadtester.common.action.substitutions.FixedSubstitution;

/**
 * Options specific to the Recorder Servlet Filter
 *
 */
public class RecorderServletFilterOptions
{
	private String testPlanDirectory;
	private boolean immediateStart;
	private boolean enableREST ;
	private boolean enableJMX;
	private int restPort;
	
	private List<FixedSubstitution> fixedSubs;

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
	
	public List<FixedSubstitution> getFixedSubs()
	{
		return fixedSubs;
	}
	
	public void setFixedSubs(List<FixedSubstitution> fixedSubs)
	{
		this.fixedSubs = fixedSubs;
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
	
	public RecorderServletFilterOptions withFixedSubs(List<FixedSubstitution> fixedSubs)
	{
		this.fixedSubs = fixedSubs;
		return this;
	}
	
	public boolean isEnableREST()
	{
		return enableREST;
	}

	public void setEnableREST(boolean enableREST)
	{
		this.enableREST = enableREST;
	}

	public boolean isEnableJMX()
	{
		return enableJMX;
	}

	public void setEnableJMX(boolean enableJMX)
	{
		this.enableJMX = enableJMX;
	}

	public int getRestPort()
	{
		return restPort;
	}

	public void setRestPort(int restPort)
	{
		this.restPort = restPort;
	}
	
	public RecorderServletFilterOptions withRestPort(int restPort)
	{
		this.restPort = restPort;
		return this;
	}
	
	public RecorderServletFilterOptions withEnableREST(boolean enable)
	{
		this.enableREST = enable;
		return this;
	}
	
	public RecorderServletFilterOptions withEnableJMX(boolean enable)
	{
		this.enableJMX = enable;
		return this;
	}
}
