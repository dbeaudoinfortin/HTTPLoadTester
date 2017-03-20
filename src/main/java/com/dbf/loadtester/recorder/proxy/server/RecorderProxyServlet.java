package com.dbf.loadtester.recorder.proxy.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

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

import com.dbf.loadtester.common.action.converter.ApacheRequestConverter;
import com.dbf.loadtester.common.httpclient.HTTPClientFactory;
import com.dbf.loadtester.common.util.Utils;
import com.dbf.loadtester.recorder.RecorderHttpServletRequestWrapper;
import com.dbf.loadtester.recorder.proxy.RecorderProxyOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecorderProxyServlet implements Servlet
{
	private static final Logger log = LoggerFactory.getLogger(RecorderProxyServlet.class);
	private static final Set<String> rewriteSupportedTypes = new HashSet<String>();
	
	public static final int MAX_CONNECTIONS = 20;
	public static final String CONTEXT_OPTIONS_ATTRIBUTE = "options";
	
	private RecorderProxyOptions options;
	private ServletConfig servletConfig;
	private ApacheRequestConverter requestConverter;
	private Pattern forwardHostPattern;
	private final HttpClient httpClient = HTTPClientFactory.getHttpClient(MAX_CONNECTIONS);
	
	static
	{
		//All of the content types 
		rewriteSupportedTypes.add("application/json");
		rewriteSupportedTypes.add("text/html");
		rewriteSupportedTypes.add("text/plain");
	}
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException
	{
		options = (RecorderProxyOptions) servletConfig.getServletContext().getAttribute(CONTEXT_OPTIONS_ATTRIBUTE);
		this.servletConfig = servletConfig;
		this.requestConverter = new ApacheRequestConverter(options.getForwardHost(), options.getForwardHTTPPort(), options.getForwardHTTPSPort(), options.isOverrideHostHeader());
		this.forwardHostPattern = Pattern.compile(Pattern.quote(options.getForwardHost()));
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
			httpMethod = requestConverter.convertServletRequestToApacheRequest(wrappedRequest);
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
	
	private void convertResponse(HttpResponse incomingApacheResponse, HttpServletResponse outgoingServletResponse, RecorderHttpServletRequestWrapper incomingServletRequest) throws UnsupportedOperationException, IOException
	{
		//Process status code
		int responseStatusCode = incomingApacheResponse.getStatusLine().getStatusCode();
		outgoingServletResponse.setStatus(responseStatusCode);
		
		if(responseStatusCode >= 300) 
			log.warn("Received code " + responseStatusCode + " for action " + incomingServletRequest.getMethod() + " " + incomingServletRequest.getPathInfo());
		
		//Process headers
		for(Header header : incomingApacheResponse.getAllHeaders())
		{
			String headerName = header.getName();
			String headerValue = header.getValue();
			
			//Special case for Handling redirection header
			if(headerName.equalsIgnoreCase("Location"))
				headerValue = modifyLocationHeader(headerValue, incomingServletRequest.getServerName());
			
			outgoingServletResponse.setHeader(headerName, headerValue);
		}
		
		HttpEntity entity = incomingApacheResponse.getEntity();
		if(entity != null)
		{
			String contentType = Utils.determineContentType(entity.getContentType());
			if(options.isRewriteUrls() && rewriteSupportedTypes.contains(contentType))
			{
				//In order to correctly support links, we must do some special voodoo.
				//We have to ensure that all the links intended for the original host are actually linked back to us. 
				//1) Some link are relative, and the browser appends them to the host. These are fine.
				//2) Some are absolute and must be changed.
				//3) Others are dynamic, depending on what the 'Host' header is in the request. These are tricky and 
				//may or may not be correct depending on the overrideHostHeader flag.
				//From the proxy's point of view, it's not possible to distinguish cases 2) and 3).
				String content = rewriteURLs(IOUtils.toString(entity.getContent()), incomingServletRequest.getServerName());
				IOUtils.write(content, outgoingServletResponse.getOutputStream());
			}
			else
			{
				outgoingServletResponse.setContentLengthLong(entity.getContentLength());
				IOUtils.copy(entity.getContent(), outgoingServletResponse.getOutputStream());
			}
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
			String originalHost = uri.getHost().toLowerCase();
			if(options.getForwardHost().equals(originalHost))
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
					newUriBuilder.setPort(isHTTPS ? options.getHttpsPort() : options.getHttpPort());
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
	
	private String rewriteURLs(String content, String originalRequestHostName)
	{
		//Lets keep this simple and ignore ports for now
		return forwardHostPattern.matcher(content).replaceAll(originalRequestHostName);
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
