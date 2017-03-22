package com.dbf.loadtester.player.management.jaxrs;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.dbf.loadtester.player.management.PlayerManagerMBean;
import com.dbf.loadtester.player.stats.TimeStats;

@Path("/player")
public class PlayerManagementEndpoint
{
	private final PlayerManagerMBean manager;
	
	public PlayerManagementEndpoint(PlayerManagerMBean manager)
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
	
	@GET
	@POST
	@Path("/start")
	public Response start()
	{
		manager.start();
		return Response.ok().build();
	}
	
	@GET
	@POST
	@Path("/stop")
	public Response stop()
	{
		manager.stop();
		return Response.ok().build();
	}
	
	@GET
	@Path("/threadCount")
	@Produces({ MediaType.APPLICATION_JSON })
	public Integer threadCount()
	{
		return manager.getThreadCount();
	}
	
	@POST
	@Path("/threadCount")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response threadCount(Integer threadCount)
	{
		manager.setThreadCount(threadCount);
		return Response.ok().build();
	}
	
	@GET
	@Path("/testPlanFile")
	@Produces({ MediaType.APPLICATION_JSON })
	public String testPlanFile()
	{
		return manager.getTestPlanFile();
	}
	
	@POST
	@Path("/testPlanFile")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response testPlanFile(String testPlanFile)
	{
		manager.setTestPlanFile(testPlanFile);
		return Response.ok().build();
	}
	
	@GET
	@Path("/host")
	@Produces({ MediaType.APPLICATION_JSON })
	public String host()
	{
		return manager.getHost();
	}
	
	@POST
	@Path("/host")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response host(String host)
	{
		manager.setHost(host);
		return Response.ok().build();
	}
	
	@GET
	@Path("/httpPort")
	@Produces({ MediaType.APPLICATION_JSON })
	public Integer httpPort()
	{
		return manager.getHttpPort();
	}
	
	@POST
	@Path("/httpPort")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response httpPort(Integer httpPort)
	{
		manager.setHttpPort(httpPort);
		return Response.ok().build();
	}
	
	@GET
	@Path("/httpsPort")
	@Produces({ MediaType.APPLICATION_JSON })
	public Integer httpsPort()
	{
		return manager.getHttpsPort();
	}
	
	@POST
	@Path("/httpsPort")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response httpsPort(Integer httpsPort)
	{
		manager.setHttpsPort(httpsPort);
		return Response.ok().build();
	}
	
	@GET
	@Path("/actionDelay")
	@Produces({ MediaType.APPLICATION_JSON })
	public Integer actionDelay()
	{
		return manager.getActionDelay();
	}
	
	@POST
	@Path("/actionDelay")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response actionDelay(Integer actionDelay)
	{
		manager.setActionDelay(actionDelay);
		return Response.ok().build();
	}
	
	@GET
	@Path("/useSubstitutions")
	@Produces({ MediaType.APPLICATION_JSON })
	public Boolean useSubstitutions()
	{
		return manager.isUseSubstitutions();
	}
	
	@POST
	@Path("/useSubstitutions")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response useSubstitutions(Boolean useSubstitutions)
	{
		manager.setUseSubstitutions(useSubstitutions);
		return Response.ok().build();
	}
	
	@GET
	@Path("/overrideHttps")
	@Produces({ MediaType.APPLICATION_JSON })
	public Boolean overrideHttps()
	{
		return manager.isOverrideHttps();
	}
	
	@POST
	@Path("/overrideHttps")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response overrideHttps(Boolean overrideHttps)
	{
		manager.setOverrideHttps(overrideHttps);
		return Response.ok().build();
	}
	
	@GET
	@Path("/actionStats")
	@Produces({ MediaType.APPLICATION_JSON })
	public Map<String, TimeStats> actionStats()
	{
		return manager.getActionStats();
	}
	
	@GET
	@Path("/testPlanStats")
	@Produces({ MediaType.APPLICATION_JSON })
	public TimeStats testPlanStats()
	{
		return manager.getTestPlanStats();
	}
	
	@GET
	@Path("/aggregateActionStats")
	@Produces({ MediaType.APPLICATION_JSON })
	public TimeStats aggregateActionStats()
	{
		return manager.getAggregateActionStats();
	}
	
	@GET
	@Path("/shareConnections")
	@Produces({ MediaType.APPLICATION_JSON })
	public Boolean shareConnections()
	{
		return manager.isShareConnections();
	}
	
	@POST
	@Path("/shareConnections")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response shareConnections(Boolean shareConnections)
	{
		manager.setShareConnections(shareConnections);
		return Response.ok().build();
	}
	
	@POST
	@Path("/cookieWhiteList")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response cookieWhiteList(List<String> cookieWhiteList)
	{
		manager.setCookieWhiteList(cookieWhiteList);
		return Response.ok().build();
	}
	
	@GET
	@Path("/cookieWhiteList")
	@Produces({ MediaType.APPLICATION_JSON })
	public List<String> cookieWhiteList()
	{
		return manager.getCookieWhiteList();
	}
}
