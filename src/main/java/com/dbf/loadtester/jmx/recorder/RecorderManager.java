package com.dbf.loadtester.jmx.recorder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

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
	public String getTestPlanDirectory() {
		Path dir = recorderServletFilter.getTestPlanDirectory();
		return dir == null ? "" : dir.toString();
	}

	@Override
	public void setTestPlanDirectory(String testPlanDirectory) {
		recorderServletFilter.setTestPlanDirectory(Paths.get(testPlanDirectory));
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
}
