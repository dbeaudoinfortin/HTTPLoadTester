package com.dbf.loadtester.recorder.jmx;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.dbf.loadtester.common.json.JsonEncoder;
import com.dbf.loadtester.player.LoadTestPlayer;
import com.dbf.loadtester.recorder.filter.RecorderServletFilter;

public class RecorderManager implements RecorderManagerMBean
{
	//Allows multiple instances to each have a unique id
	private static AtomicLong instanceCounter = new AtomicLong(1L);
	
	private static final Logger log = Logger.getLogger(LoadTestPlayer.class);
	
	private final RecorderServletFilter recorderServletFilter;
	private final Long instanceId;
	
	public RecorderManager(RecorderServletFilter recorderServletFilter)
	{
		this.recorderServletFilter = recorderServletFilter;
		instanceId = instanceCounter.getAndIncrement();
	}

	@Override
	public void start()
	{
		try
		{
			recorderServletFilter.startRecording();
		}
		catch (IOException e)
		{
			log.error("Failed to start recording.", e);
		}
	}

	@Override
	public void stop()
	{
		try
		{
			recorderServletFilter.stopRecording();
		}
		catch (IOException e)
		{
			log.error("Failed to stop recording.", e);
		}
	}
	
	@Override
	public String getTestPlanDirectory()
	{
		Path dir = recorderServletFilter.getTestPlanDirectory();
		return dir == null ? "" : dir.toString();
	}

	@Override
	public void setTestPlanDirectory(String testPlanDirectory)
	{
		Path path = (testPlanDirectory == null || testPlanDirectory.isEmpty()) ? null : Paths.get(testPlanDirectory);
		recorderServletFilter.setTestPlanDirectory(path);
	}

	@Override
	public boolean isRunning() {
		return recorderServletFilter.isRunning();
	}
	
	@Override
	public String toString()
	{
		return "RecorderMBean" + instanceId;
	}
	
	@Override
	public String getTestPlanPath()
	{
		Path p = recorderServletFilter.getTestPlanPath();
		return p==null ? "" : p.toString();
	}
	
	@Override
	public void setTestPlanPath(String testPlanPath)
	{
		Path path = (testPlanPath == null || testPlanPath.isEmpty()) ? null : Paths.get(testPlanPath);
		recorderServletFilter.setTestPlanPath(path);
	}

	@Override
	public void setPathSubstitutions(String map)
	{
		@SuppressWarnings("unchecked")
		Map<String,String> newMap = JsonEncoder.fromJson(map, HashMap.class);
		if(null != newMap) recorderServletFilter.setPathSubs(newMap);
	}

	@Override
	public String getPathSubstitutions()
	{
		return JsonEncoder.toJson(recorderServletFilter.getPathSubs());
	}
	
	@Override
	public void setQuerySubstitutions(String map)
	{
		@SuppressWarnings("unchecked")
		Map<String,String> newMap = JsonEncoder.fromJson(map, HashMap.class);
		if(null != newMap) recorderServletFilter.setQuerySubs(newMap);
	}

	@Override
	public String getQuerySubstitutions()
	{
		return JsonEncoder.toJson(recorderServletFilter.getQuerySubs());
	}
	
	@Override
	public void setBodySubstitutions(String map)
	{
		@SuppressWarnings("unchecked")
		Map<String,String> newMap = JsonEncoder.fromJson(map, HashMap.class);
		if(null != newMap) recorderServletFilter.setBodySubs(newMap);
	}

	@Override
	public String getBodySubstitutions()
	{
		return JsonEncoder.toJson(recorderServletFilter.getBodySubs());
	}
}
