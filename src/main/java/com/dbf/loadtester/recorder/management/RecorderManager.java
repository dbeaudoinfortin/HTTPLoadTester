package com.dbf.loadtester.recorder.management;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.dbf.loadtester.common.action.substitutions.FixedSubstitution;
import com.dbf.loadtester.common.json.JsonEncoder;
import com.dbf.loadtester.player.LoadTestCoordinator;
import com.dbf.loadtester.recorder.filter.RecorderServletFilter;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecorderManager implements RecorderManagerMBean
{
	//Allows multiple instances to each have a unique id
	private static AtomicLong instanceCounter = new AtomicLong(1L);
	
	private static final Logger log = LoggerFactory.getLogger(LoadTestCoordinator.class);
	
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
	public void setFixedSubstitutions(String json)
	{
		try
		{
			List<FixedSubstitution> newSubs = JsonEncoder.fromJson(json, (new TypeToken<List<FixedSubstitution>>(){}).getType());
    		if(null != newSubs) recorderServletFilter.setFixedSubs(newSubs);
    	}
    	catch(JsonSyntaxException e)
    	{
    		log.warn("Cannot set fixed substitutions. JSON conversion failed for '" + json + "'.", e);
    	}
		catch(Exception e)
    	{
    		log.warn("Cannot set fixed substitutions for '" + json + "'.", e);
    	}
	}

	@Override
	public String getFixedSubstitutions()
	{
		return JsonEncoder.toJson(recorderServletFilter.getFixedSubs());
	}
}
