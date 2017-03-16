package com.dbf.loadtester.recorder.proxy.server;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;

import com.dbf.loadtester.common.ssl.IncomingSSLUtil;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecorderProxyServer
{
	private static final Logger log = LoggerFactory.getLogger(RecorderProxyServer.class);
	
	public static void initializeServer(RecorderProxyOptions options)
	{
		log.info("Attempting to start Recorder Proxy server...");
		
		try
		{
			initializeProxyServer(options);
		} 
		catch(Throwable t)
		{
			log.error("Failed to initialize Recorder Proxy server.", t);
			return;
		}
		
		log.info("Recorder Proxy server started.");
	}
	
	private static void initializeProxyServer(RecorderProxyOptions options) throws Exception
	{
		Undertow server = Undertow.builder()
	                .addHttpListener(options.getHttpPort(), "localhost")
	                .addHttpsListener(options.getHttpsPort(), "localhost", IncomingSSLUtil.buildSSLContext())
	                .setHandler(buildProxyHttpHandler(options)).build();
		
		
		server.start();
	}
	
	private static HttpHandler buildProxyHttpHandler(RecorderProxyOptions options) throws ServletException
	{
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(RecorderProxyServer.class.getClassLoader())
                .setDeploymentName("RecorderProxy")
                .setContextPath("/")
                .addServlets(
                        Servlets.servlet("Recorder Proxy Servlet", RecorderProxyServlet.class)
                        .addInitParam(RecorderProxyServlet.PARAM_PROXY_HTTP_PORT, "" + options.getForwardHTTPPort())
                        .addInitParam(RecorderProxyServlet.PARAM_PROXY_HTTPS_PORT, "" + options.getForwardHTTPSPort())
                        .addInitParam(RecorderProxyServlet.PARAM_LISTENER_HTTP_PORT, "" + options.getHttpPort())
                        .addInitParam(RecorderProxyServlet.PARAM_LISTENER_HTTPS_PORT, "" + options.getHttpsPort())
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
				.withBodySubs(options.getBodySubs())
				.withEnableJMX(!options.isDisableJMX())
				.withEnableREST(!options.isDisableREST())
				.withRestPort(options.getRestPort())));
		return filter;
	}
}
