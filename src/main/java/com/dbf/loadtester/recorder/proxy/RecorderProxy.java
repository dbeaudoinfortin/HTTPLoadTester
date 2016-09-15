package com.dbf.loadtester.recorder.proxy;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import com.dbf.loadtester.recorder.filter.RecorderServletFilter;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

public class RecorderProxy
{
	private static final Logger log = Logger.getLogger(RecorderProxy.class);
	
	public static void main(String[] args)
	{
		RecorderProxyOptions options = null;
    	try
    	{
    		options = new RecorderProxyOptions(args);
    	}
    	catch(IllegalArgumentException e)
    	{
    		log.fatal("Invalid CMD line Arguments.");
    		RecorderProxyOptions.printOptions();
    		System.exit(1);
    	}
		
		initializeServer(options);
	}
	
	private static void initializeServer(RecorderProxyOptions options)
	{
		Undertow server = null;
		try
		{
			server = Undertow.builder()
	                .addHttpListener(options.getPort(), "localhost")
	                .setHandler(buildHttpHandler(options)).build();
		} 
		catch(Throwable t)
		{
			log.error("Failed to initialize server.", t);
			return;
		}
		
		server.start();
	}
	
	private static HttpHandler buildHttpHandler(RecorderProxyOptions options) throws ServletException
	{
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(RecorderProxy.class.getClassLoader())
                .setDeploymentName("RecorderProxy")
                .setContextPath("/")
                .addServlets(
                        Servlets.servlet("Recorder Proxy Servlet", RecorderProxyServlet.class)
                        .addInitParam(RecorderProxyServlet.PARAM_PROXY_HTTP_PORT, "" + options.getForwardHTTPPort())
                        .addInitParam(RecorderProxyServlet.PARAM_PROXY_HTTPS_PORT, "" + options.getForwardHTTPSPort())
                        .addInitParam(RecorderProxyServlet.PARAM_PROXY_HOST, options.getForwardHost())
                        .addMapping("/*")
                )
                .addFilter(Servlets.filter("recorderServletFilter", RecorderServletFilter.class))
                .addInitParameter(RecorderServletFilter.PARAM_DIRECTORY_PATH, options.getDirectory())
                .addFilterUrlMapping("recorderServletFilter", "/*", DispatcherType.REQUEST);

        DeploymentManager deploymentManager =  Servlets.defaultContainer().addDeployment(servletBuilder);
        deploymentManager.deploy();
        return deploymentManager.start();
    }
}
