package com.dbf.loadtester.player.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.dbf.loadtester.player.jmx.PlayerManagerMBean;

@Path("/admin")
public class AdminEndpoint
{
	private final PlayerManagerMBean manager;
	
	public AdminEndpoint(PlayerManagerMBean manager)
	{
		this.manager = manager;
	}
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Boolean isRunning()
	{
		return manager.isRunning();
	}
}
