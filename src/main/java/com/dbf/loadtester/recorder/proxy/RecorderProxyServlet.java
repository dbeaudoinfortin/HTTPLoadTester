package com.dbf.loadtester.recorder.proxy;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.log4j.Logger;

import com.dbf.loadtester.action.HTTPConverter;
import com.dbf.loadtester.httpclient.HTTPClientFactory;
import com.dbf.loadtester.recorder.RecorderHttpServletRequestWrapper;

public class RecorderProxyServlet implements Servlet
{
	private static final Logger log = Logger.getLogger(RecorderProxyServlet.class);
	
	public static final int MAX_CONNECTIONS = 20;
	public static final String PARAM_PROXY_HTTP_PORT = "proxyHTTPPort";
	public static final String PARAM_PROXY_HTTPS_PORT = "proxyHTTPSPort";
	public static final String PARAM_PROXY_HOST = "proxyHost";
	
	private Integer httpPort;
	private Integer httpsPort;
	private String host;
	
	private ServletConfig servletConfig;
	
	private HttpClient httpClient = HTTPClientFactory.getHttpClient(MAX_CONNECTIONS);
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException
	{
		this.servletConfig = servletConfig;
		httpPort = Integer.parseInt(servletConfig.getInitParameter(PARAM_PROXY_HTTP_PORT));
		httpsPort = Integer.parseInt(servletConfig.getInitParameter(PARAM_PROXY_HTTPS_PORT));
		host = servletConfig.getInitParameter(PARAM_PROXY_HOST);
	}

	@Override
	public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException
	{
		//Note The RecorderServletFilter must have been invoke or this cast will fail
		
		if(!(servletRequest instanceof RecorderHttpServletRequestWrapper))
				servletRequest = new RecorderHttpServletRequestWrapper((HttpServletRequest) servletRequest);
		
		RecorderHttpServletRequestWrapper wrappedRequest = (RecorderHttpServletRequestWrapper) servletRequest;
		
		//Proxy the request forward
		try
		{
			HttpRequestBase httpMethod = HTTPConverter.convertServletRequestToHTTPClientRequest(wrappedRequest, host, httpPort, httpsPort);
			HttpResponse response = httpClient.execute(httpMethod);
			convertResponse(response, (HttpServletResponse) servletResponse);
		}
		catch (Exception e)
		{
			log.error("Failed to proxy request.", e);
			throw new ServletException("Failed to proxy request.", e);
		}
	}
	
	private void convertResponse(HttpResponse response, HttpServletResponse httpServletResponse) throws UnsupportedOperationException, IOException
	{
		for(Header header : response.getAllHeaders())
		{
			httpServletResponse.setHeader(header.getName(), header.getValue());
		}
		
		httpServletResponse.setStatus(response.getStatusLine().getStatusCode());
		
		HttpEntity entity = response.getEntity();
		httpServletResponse.setContentLengthLong(entity.getContentLength());
		IOUtils.copy(entity.getContent(), httpServletResponse.getOutputStream());
	}
	
	@Override
	public ServletConfig getServletConfig()
	{
		return servletConfig;
	}

	@Override
	public String getServletInfo()
	{
		return "Recorder Proxy Servlet";
	}

	@Override
	public void destroy(){}

	

}
