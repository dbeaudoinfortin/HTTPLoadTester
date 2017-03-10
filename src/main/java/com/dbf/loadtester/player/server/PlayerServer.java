package com.dbf.loadtester.player.server;

import javax.servlet.ServletException;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

public class PlayerServer
{
	private static final int ADMIN_SERVER_PORT = 5009;
	
	public static void initializeServer() throws Exception
	{		
		Undertow server = Undertow.builder()
                .addHttpListener(ADMIN_SERVER_PORT, "localhost")
                .setHandler(buildHttpHandler()).build();
		
		server.start();

	}
	
	private static HttpHandler buildHttpHandler() throws ServletException
	{
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(PlayerServer.class.getClassLoader())
                .setDeploymentName("PlayerServer")
                .setContextPath("/")
                .addServlets(
                		 Servlets.servlet("Admin Rest Service", HttpServletDispatcher.class)
                        .addMapping("/*")
                        .addInitParam("javax.ws.rs.Application", "com.dbf.loadtester.player.jaxrs.AdminApplication")
                ).addListeners(
                        Servlets.listener(ResteasyBootstrap.class)
                );

        DeploymentManager deploymentManager =  Servlets.defaultContainer().addDeployment(servletBuilder);
        deploymentManager.deploy();
        return deploymentManager.start();
    }
	
	
}
