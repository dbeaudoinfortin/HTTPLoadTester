package com.dbf.loadtester.player.management.server;

import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;

import com.dbf.loadtester.player.management.PlayerManagerMBean;
import com.dbf.loadtester.player.management.jaxrs.ManagementApplication;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

public class PlayerServer
{
	private static final int ADMIN_SERVER_PORT = 5009;
	
	public static void initializeServer(PlayerManagerMBean manager) throws Exception
	{		
		Undertow server = Undertow.builder()
                .addHttpListener(ADMIN_SERVER_PORT, "localhost")
                .setHandler(buildDeploymentManager(manager)).build();
		
		server.start();
	}
	
	private static HttpHandler buildDeploymentManager(PlayerManagerMBean manager) throws ServletException
	{
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(PlayerServer.class.getClassLoader())
                .setDeploymentName("PlayerManagementServer")
                .setContextPath("/")
                .addServlets(
                		 Servlets.servlet("Management Rest Service", HttpServlet30Dispatcher.class)
                		 .setAsyncSupported(true)
                         .setLoadOnStartup(1)
                         .addMapping("/*")
                )
                .addServletContextAttribute(ResteasyDeployment.class.getName(), getDeployment(manager));

        DeploymentManager deploymentManager =  Servlets.defaultContainer().addDeployment(servletBuilder);
        deploymentManager.deploy();
        return deploymentManager.start();
    }

	public static ResteasyDeployment getDeployment(PlayerManagerMBean manager)
	{
		Application application = new ManagementApplication(manager);
		ResteasyDeployment deployment = new ResteasyDeployment();
		deployment.setApplication(application);
		deployment.getProviders().add(new JsonMessageBodyReader());
		deployment.getProviders().add(new JsonMessageBodyWriter());
		return deployment;
	}

}
