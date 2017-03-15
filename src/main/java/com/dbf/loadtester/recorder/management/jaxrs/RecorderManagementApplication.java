package com.dbf.loadtester.recorder.management.jaxrs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.dbf.loadtester.recorder.management.RecorderManagerMBean;

@ApplicationPath("/")
public class RecorderManagementApplication extends Application
{
	private final RecorderManagerMBean manager;
	
	public RecorderManagementApplication (RecorderManagerMBean manager)
	{
		this.manager = manager;
	}
	
	@Override
	public Set<Object> getSingletons()
	{
		Set<Object> singletons = new HashSet<Object>();
		singletons.add(new RecorderManagementEndpoint(manager));
		return singletons;
	}
}
