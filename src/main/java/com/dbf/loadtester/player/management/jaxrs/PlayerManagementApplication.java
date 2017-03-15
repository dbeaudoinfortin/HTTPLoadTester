package com.dbf.loadtester.player.management.jaxrs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.dbf.loadtester.player.management.PlayerManagerMBean;

@ApplicationPath("/")
public class PlayerManagementApplication extends Application
{
	private final PlayerManagerMBean manager;
	
	public PlayerManagementApplication (PlayerManagerMBean manager)
	{
		this.manager = manager;
	}
	
	@Override
	public Set<Object> getSingletons()
	{
		Set<Object> singletons = new HashSet<Object>();
		singletons.add(new PlayerManagementEndpoint(manager));
		return singletons;
	}
}
