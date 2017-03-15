package com.dbf.loadtester.recorder.management.server;

import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;

import com.dbf.loadtester.common.json.JsonMessageBodyReader;
import com.dbf.loadtester.common.json.JsonMessageBodyWriter;
import com.dbf.loadtester.player.management.server.PlayerManagementServer;
import com.dbf.loadtester.recorder.management.RecorderManagerMBean;
import com.dbf.loadtester.recorder.management.jaxrs.RecorderManagementApplication;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

public class RecorderManagementServer
{
	private static final Logger log = Logger.getLogger(RecorderManagementServer.class);
	
	public static void initializeServer(RecorderManagerMBean manager, int restPort)
	{
		log.info("Attempting to start REST API managment server...");
		
		try
		{
			//Run the management rest services in a separate Undertow instance.
			//While not the most efficient this simplifies things and avoid conflicts.
			initializeManagementServer(manager, restPort);
		} 
		catch(Throwable t)
		{
			log.error("Failed to initialize REST API managment server. REST management will not be available", t);
			return;
		}
		log.info("REST API managment server started.");
	}
	
	private static void initializeManagementServer(RecorderManagerMBean manager, int restPort) throws ServletException
	{
		Undertow server = Undertow.builder()
                .addHttpListener(restPort, "localhost")
                .setHandler(buildManagementDeploymentManager(manager)).build();
		
		server.start();
	}
	
	private static HttpHandler buildManagementDeploymentManager(RecorderManagerMBean manager) throws ServletException
	{
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(PlayerManagementServer.class.getClassLoader())
                .setDeploymentName("PlayerManagementServer")
                .setContextPath("/")
                .addServlets(
                		 Servlets.servlet("Management Rest Service", HttpServlet30Dispatcher.class)
                		 .setAsyncSupported(true)
                         .setLoadOnStartup(1)
                         .addMapping("/*")
                )
                .addServletContextAttribute(ResteasyDeployment.class.getName(), getManagementDeployment(manager));

        DeploymentManager deploymentManager =  Servlets.defaultContainer().addDeployment(servletBuilder);
        deploymentManager.deploy();
        return deploymentManager.start();
    }

	public static ResteasyDeployment getManagementDeployment(RecorderManagerMBean manager)
	{
		Application application = new RecorderManagementApplication(manager);
		ResteasyDeployment deployment = new ResteasyDeployment();
		deployment.setApplication(application);
		deployment.getProviders().add(new JsonMessageBodyReader());
		deployment.getProviders().add(new JsonMessageBodyWriter());
		return deployment;
	}
}