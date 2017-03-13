package com.dbf.loadtester.recorder.proxy.server;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import com.dbf.loadtester.common.util.SSLUtil;
import com.dbf.loadtester.recorder.filter.RecorderServletFilter;
import com.dbf.loadtester.recorder.filter.RecorderServletFilterFactory;
import com.dbf.loadtester.recorder.filter.RecorderServletFilterOptions;
import com.dbf.loadtester.recorder.proxy.RecorderProxyOptions;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.FilterInfo;

public class RecorderProxyServer
{
	private static final Logger log = Logger.getLogger(RecorderProxyServer.class);
	
	public static void initializeServer(RecorderProxyOptions options)
	{
		log.info("Attempting to start Recorder Proxy Server...");
		
		Undertow server = null;
		try
		{
			server = Undertow.builder()
	                .addHttpListener(options.getHttpPort(), "localhost")
	                .addHttpsListener(options.getHttpsPort(), "localhost", SSLUtil.buildSSLContext())
	                .setHandler(buildHttpHandler(options)).build();
		} 
		catch(Throwable t)
		{
			log.error("Failed to initialize server.", t);
			return;
		}
		
		server.start();
		
		log.info("Recorder Proxy Server started.");
	}
	
	private static HttpHandler buildHttpHandler(RecorderProxyOptions options) throws ServletException
	{
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(RecorderProxyServer.class.getClassLoader())
                .setDeploymentName("RecorderProxy")
                .setContextPath("/")
                .addServlets(
                        Servlets.servlet("Recorder Proxy Servlet", RecorderProxyServlet.class)
                        .addInitParam(RecorderProxyServlet.PARAM_PROXY_HTTP_PORT, "" + options.getForwardHTTPPort())
                        .addInitParam(RecorderProxyServlet.PARAM_PROXY_HTTPS_PORT, "" + options.getForwardHTTPSPort())
                        .addInitParam(RecorderProxyServlet.PARAM_PROXY_HOST, options.getForwardHost())
                        .addMapping("/*")
                )
                .addFilter(buildRecorderServletFilter(options))
                .addFilterUrlMapping("recorderServletFilter", "/*", DispatcherType.REQUEST)
                .setEagerFilterInit(true);

        DeploymentManager deploymentManager =  Servlets.defaultContainer().addDeployment(servletBuilder);
        deploymentManager.deploy();
        return deploymentManager.start();
    }
	
	private static FilterInfo buildRecorderServletFilter(RecorderProxyOptions options) throws ServletException
	{
		FilterInfo filter = Servlets.filter("recorderServletFilter", RecorderServletFilter.class);
		filter.setInstanceFactory(new RecorderServletFilterFactory((new RecorderServletFilterOptions())
				.withImmediateStart(options.isImmediateStart())
				.withTestPlanDirectory(options.getDirectory())
				.withPathSubs(options.getPathSubs())
				.withQuerySubs(options.getQuerySubs())
				.withBodySubs(options.getBodySubs())));
		return filter;
	}
}