package com.dbf.loadtester.player.management.jaxrs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.dbf.loadtester.player.management.PlayerManagerMBean;

@ApplicationPath("/")
public class ManagementApplication extends Application
{
	private final PlayerManagerMBean manager;
	
	public ManagementApplication (PlayerManagerMBean manager)
	{
		this.manager = manager;
	}
	
	@Override
	public Set<Object> getSingletons()
	{
		Set<Object> singletons = new HashSet<Object>();
		singletons.add(new ManagementEndpoint(manager));
		return singletons;
	}
}
