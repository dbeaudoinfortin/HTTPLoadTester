package com.dbf.loadtester.player.management.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.dbf.loadtester.player.management.PlayerManagerMBean;

@Path("/admin")
public class ManagementEndpoint
{
	private final PlayerManagerMBean manager;
	
	public ManagementEndpoint(PlayerManagerMBean manager)
	{
		this.manager = manager;
	}
	
	@GET
	@Path("/running")
	@Produces({ MediaType.APPLICATION_JSON })
	public Boolean running()
	{
		return manager.isRunning();
	}
	
	@GET
	@Path("/runningThreadCount")
	@Produces({ MediaType.APPLICATION_JSON })
	public Integer runningThreadCount()
	{
		return manager.getRunningThreadCount();
	}
}
