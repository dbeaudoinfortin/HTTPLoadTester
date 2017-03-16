package com.dbf.loadtester.player.management.server;

import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;

import com.dbf.loadtester.common.json.JsonMessageBodyReader;
import com.dbf.loadtester.common.json.JsonMessageBodyWriter;
import com.dbf.loadtester.player.config.PlayerOptions;
import com.dbf.loadtester.player.management.PlayerManagerMBean;
import com.dbf.loadtester.player.management.jaxrs.PlayerManagementApplication;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

public class PlayerManagementServer
{
	public static Undertow initializeServer(PlayerManagerMBean manager, PlayerOptions config) throws Exception
	{		
		Undertow server = Undertow.builder()
                .addHttpListener(config.getRestPort(), "0.0.0.0")
                .setHandler(buildDeploymentManager(manager)).build();
		
		server.start();
		return server;
	}
	
	private static HttpHandler buildDeploymentManager(PlayerManagerMBean manager) throws ServletException
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
                .addServletContextAttribute(ResteasyDeployment.class.getName(), getDeployment(manager));

        DeploymentManager deploymentManager =  Servlets.defaultContainer().addDeployment(servletBuilder);
        deploymentManager.deploy();
        return deploymentManager.start();
    }

	public static ResteasyDeployment getDeployment(PlayerManagerMBean manager)
	{
		Application application = new PlayerManagementApplication(manager);
		ResteasyDeployment deployment = new ResteasyDeployment();
		deployment.setApplication(application);
		deployment.getProviders().add(new JsonMessageBodyReader());
		deployment.getProviders().add(new JsonMessageBodyWriter());
		return deployment;
	}

}
