package com.dbf.loadtester.player.jaxrs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
public class AdminApplication extends Application
{
	
	//PlayerManagerMBean manager
	
	@Override
	public Set<Object> getSingletons()
	{
		Set<Object> singletons = new HashSet<Object>();
		singletons.add(new AdminEndpoint(null));
		return singletons;
	}
}
