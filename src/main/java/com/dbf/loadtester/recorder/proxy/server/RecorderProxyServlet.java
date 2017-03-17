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
import com.dbf.loadtester.recorder.proxy.RecorderProxyOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecorderProxyServlet implements Servlet
{
	private static final Logger log = LoggerFactory.getLogger(RecorderProxyServlet.class);
	
	public static final int MAX_CONNECTIONS = 20;
	public static final String CONTEXT_OPTIONS_ATTRIBUTE = "options";
	
	private RecorderProxyOptions options;
	private ServletConfig servletConfig;
	
	private HttpClient httpClient = HTTPClientFactory.getHttpClient(MAX_CONNECTIONS);
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException
	{
		options = (RecorderProxyOptions) servletConfig.getServletContext().getAttribute(CONTEXT_OPTIONS_ATTRIBUTE);
		this.servletConfig = servletConfig;
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
			httpMethod = HTTPConverter.convertServletRequestToApacheRequest(wrappedRequest, options.getForwardHost(), options.getForwardHTTPPort(), options.getForwardHTTPSPort(), options.isOverrideHostHeader());
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
		
		//In order to correctly support links, we must do some special voodoo.
		//We have to ensure that all the links intended for the original host are actually link back to us. 
		//1) Some link are relative, and the browser appends them to the host. These are fine.
		//2) Some are absolute and must be changed.
		//3) Others are dynamic, depending on what the 'Host' header is in the request. These are tricky and 
		//may or may not be correct depending on the overrideHostHeader flag.
		//From the proxy's point of view, it's not possible to distinguish cases 2) and 3).
		//if(replaceLinks)
		//{
			
		//}
		
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
			if(options.getForwardHost().equals(uriHost))
			{
				//The ports must also match. Since the port may not be explicitly defined,
				//use the defaults depending on HTTP or HTTPS
				boolean isHTTPS = uri.getScheme().equalsIgnoreCase("https");
				int uriPort = (uri.getPort() > 0) ? uri.getPort() : (isHTTPS ? 443 : 80);
				int proxyPort = isHTTPS ? options.getForwardHTTPSPort() : options.getForwardHTTPPort();
				if(uriPort == proxyPort)
				{
					URIBuilder newUriBuilder = new URIBuilder(uri);
					newUriBuilder.setHost(originalRequestHostName);
					newUriBuilder.setPort(isHTTPS ? options.getHttpsPort() : options.getForwardHTTPPort());
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
