package com.dbf.loadtester.recorder.proxy.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
import org.apache.http.client.utils.URIBuilder;

import com.dbf.loadtester.common.action.HTTPConverter;
import com.dbf.loadtester.common.httpclient.HTTPClientFactory;
import com.dbf.loadtester.recorder.RecorderHttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecorderProxyServlet implements Servlet
{
	private static final Logger log = LoggerFactory.getLogger(RecorderProxyServlet.class);
	
	public static final int MAX_CONNECTIONS = 20;
	public static final String PARAM_PROXY_HTTP_PORT = "proxyHTTPPort";
	public static final String PARAM_PROXY_HTTPS_PORT = "proxyHTTPSPort";
	public static final String PARAM_LISTENER_HTTP_PORT = "listenerHTTPPort";
	public static final String PARAM_LISTENER_HTTPS_PORT = "listenerHTTPSPort";
	public static final String PARAM_PROXY_HOST = "proxyHost";
	
	private Integer proxyHTTPPort;
	private Integer proxyHTTPSPort;
	private Integer listenerHTTPPort;
	private Integer listenerHTTPSPort;
	private String proxyHost;
	
	private ServletConfig servletConfig;
	
	private HttpClient httpClient = HTTPClientFactory.getHttpClient(MAX_CONNECTIONS);
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException
	{
		this.servletConfig = servletConfig;
		proxyHTTPPort = Integer.parseInt(servletConfig.getInitParameter(PARAM_PROXY_HTTP_PORT));
		proxyHTTPSPort = Integer.parseInt(servletConfig.getInitParameter(PARAM_PROXY_HTTPS_PORT));
		listenerHTTPPort = Integer.parseInt(servletConfig.getInitParameter(PARAM_LISTENER_HTTP_PORT));
		listenerHTTPSPort = Integer.parseInt(servletConfig.getInitParameter(PARAM_LISTENER_HTTPS_PORT));
		proxyHost = servletConfig.getInitParameter(PARAM_PROXY_HOST);
	}

	@Override
	public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException
	{
		//Note: The RecorderServletFilter must have been invoked or this cast will fail
		if(!(servletRequest instanceof RecorderHttpServletRequestWrapper))
				servletRequest = new RecorderHttpServletRequestWrapper((HttpServletRequest) servletRequest);
		
		RecorderHttpServletRequestWrapper wrappedRequest = (RecorderHttpServletRequestWrapper) servletRequest;
		
		//Proxy the request forward
		HttpRequestBase httpMethod = null;
		try
		{
			httpMethod = HTTPConverter.convertServletRequestToApacheRequest(wrappedRequest, proxyHost, proxyHTTPPort, proxyHTTPSPort);
			HttpResponse response = httpClient.execute(httpMethod);
			convertResponse(response, (HttpServletResponse) servletResponse, wrappedRequest);
		}
		catch (Exception e)
		{
			log.error("Failed to proxy request.", e);
			throw new ServletException("Failed to proxy request.", e);
		}
		finally
		{
			if(null != httpMethod) httpMethod.releaseConnection();
		}
	}
	
	private void convertResponse(HttpResponse response, HttpServletResponse httpServletResponse, RecorderHttpServletRequestWrapper wrappedRequest) throws UnsupportedOperationException, IOException
	{
		for(Header header : response.getAllHeaders())
		{
			String headerName = header.getName();
			String headerValue = header.getValue();
			
			if(headerName.equalsIgnoreCase("Location"))
				headerValue = modifyLocationHeader(headerValue, wrappedRequest.getServerName());
			
			httpServletResponse.setHeader(headerName, headerValue);
		}
		
		httpServletResponse.setStatus(response.getStatusLine().getStatusCode());
		
		HttpEntity entity = response.getEntity();
		if(entity != null)
		{
			httpServletResponse.setContentLengthLong(entity.getContentLength());
			IOUtils.copy(entity.getContent(), httpServletResponse.getOutputStream());
		}
	}
	
	private String modifyLocationHeader(String originalHeaderValue, String originalRequestHostName)
	{
		//In order to correctly handle redirects, we need to alter the redirect address to feed it back into our proxy
		//This only applies when the redirect is an address we are proxying to
		try
		{
			//Use the URI object to handle parsing
			URI uri = new URI(originalHeaderValue);
			String uriHost = uri.getHost().toLowerCase();
			if(proxyHost.equals(uriHost))
			{
				//The ports must also match. Since the port may not be explicitly defined,
				//use the defaults depending on HTTP or HTTPS
				boolean isHTTPs = uri.getScheme().equalsIgnoreCase("https");
				int uriPort = (uri.getPort() > 0) ? uri.getPort() : (isHTTPs ? 443 : 80);
				int proxyPort = isHTTPs ? proxyHTTPSPort : proxyHTTPPort;
				if(uriPort == proxyPort)
				{
					URIBuilder newUriBuilder = new URIBuilder(uri);
					newUriBuilder.setHost(originalRequestHostName);
					newUriBuilder.setPort(isHTTPs ? listenerHTTPSPort : listenerHTTPPort);
					return newUriBuilder.toString();
				}
			}
		}
		catch (URISyntaxException e)
		{
			//Oh well
		}
		return originalHeaderValue;
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
