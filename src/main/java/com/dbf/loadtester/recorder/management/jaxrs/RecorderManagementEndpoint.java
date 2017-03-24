package com.dbf.loadtester.recorder.management.jaxrs;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.dbf.loadtester.recorder.management.RecorderManagerMBean;

@Path("/recorder")
public class RecorderManagementEndpoint
{
	private final RecorderManagerMBean manager;
	
	public RecorderManagementEndpoint(RecorderManagerMBean manager)
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
	@Path("/testPlanPath")
	@Produces({ MediaType.APPLICATION_JSON })
	public String testPlanPath()
	{
		return manager.getTestPlanPath();
	}
	
	@POST
	@Path("/testPlanPath")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response testPlanPath(String testPlanPath)
	{
		manager.setTestPlanPath(testPlanPath);
		return Response.ok().build();
	}
	
	@GET
	@Path("/fixedSubstitutions")
	@Produces({ MediaType.APPLICATION_JSON })
	public String fixedSubstitutions()
	{
		return manager.getFixedSubstitutions();
	}
	
	@POST
	@Path("/fixedSubstitutions")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response fixedSubstitutions(String fixedSubstitutions)
	{
		manager.setFixedSubstitutions(fixedSubstitutions);
		return Response.ok().build();
	}
}
