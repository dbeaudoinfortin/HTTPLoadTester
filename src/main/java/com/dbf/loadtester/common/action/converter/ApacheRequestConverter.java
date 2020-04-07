package com.dbf.loadtester.common.action.converter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;

import com.dbf.loadtester.common.action.HTTPAction;
import com.dbf.loadtester.recorder.RecorderHttpServletRequestWrapper;

/**
 * Provides methods for converting a request to an Apache HTTP Client Request
 *
 */
public class ApacheRequestConverter extends HTTPConverter
{
	private String host;
	private int httpPort;
	private int httpsPort;
	private boolean overrideHostHeader;
	private boolean retainCookies;

	
	public ApacheRequestConverter(String host, int httpPort, int httpsPort, boolean overrideHostHeader)
	{
		 this(host, httpPort, httpsPort, overrideHostHeader, true);
	}
	
	public ApacheRequestConverter(String host, int httpPort, int httpsPort, boolean overrideHostHeader, boolean retainCookies)
	{
		this.host = host;
		this.httpPort = httpPort;
		this.httpsPort = httpsPort;
		this.overrideHostHeader = overrideHostHeader;
		this.retainCookies = retainCookies;
	}
	
	/**
	 * Converts an HTTP Action to an Apache HTTP Client Request.
	 * 
	 * Used by the Player for load testing.
	 */
	public HttpRequestBase convertHTTPActionToApacheRequest(HTTPAction action) throws URISyntaxException
	{
		return buildApacheRequest(action.getScheme(), action.getQueryString(), action.getPath(), action.getMethod(), action.getHeaders(), action.getContent().getBytes(), action.getContentType());
	}
	
	/**
	 * Converts a Servlet request to an Apache HTTP Client Request.
	 * 
	 * Used by the Recorder Proxy to forward requests
	 */
	public HttpRequestBase convertServletRequestToApacheRequest(RecorderHttpServletRequestWrapper httpRequest) throws URISyntaxException
	{
		return buildApacheRequest(httpRequest.getScheme(), httpRequest.getQueryString(), httpRequest.getPathInfo(), httpRequest.getMethod(), extractHeaders(httpRequest),
				httpRequest.getRequestBody(), httpRequest.getContentType());
	}
	
	/**
	 * Build an Apache HTTP Client Request.
	 * 
	 * Used by the Player for load testing and by the Recorder Proxy for forwarding requests.
	 */
	private HttpRequestBase buildApacheRequest(String scheme, String queryString, String path, String method, Map<String,String> headers, byte[] content, String contentType) throws URISyntaxException
	{
		URI uri = buildURI(scheme, host, httpPort, httpsPort, queryString, path);
		String actionMethod = method.toUpperCase();
		
		boolean hasContent = false;
		
		//Determine the correct Request object, based on the HTTP Method
		HttpRequestBase httpMethod = null;
		switch(actionMethod)
		{
			case "POST":
				httpMethod = new HttpPost(uri);
				hasContent = true;
				break;
			case "PUT":
				httpMethod = new HttpPut(uri);
				hasContent = true;
				break;
			case "HEAD":
				httpMethod = new HttpHead(uri);
				break;
			case "GET":
				httpMethod = new HttpGet(uri);
				break;
			case "DELETE":
				httpMethod = new HttpDelete(uri);
				break;
			case "OPTIONS":
				httpMethod = new HttpOptions(uri);
				break;
			default:
				return null;	
		}

		//Apply the content
		if(hasContent)
		{
			HttpEntity requestEntity = new ByteArrayEntity(content, (contentType == null || contentType.equals("")) ? null : ContentType.parse(contentType));
			((HttpEntityEnclosingRequestBase) httpMethod).setEntity(requestEntity);
		
		}
		
		//Process special headers
		if(headers != null && headers.size() > 0)
		{
			for (Map.Entry<String,String> entry : headers.entrySet())
			{
				String headerName = entry.getKey();
				if(headerName.equals(""))
					continue;
				
				//It's good practice to treat headers as case-insensitive
				String headerNameLowerCase = headerName.toLowerCase();
				
				//Note about the overrideHostHeader flag: Sometimes we want to override the host and sometimes we don't.
				//In the case of the Load Test Player, we always want to override it because the header saved in in the test plan won't match.
				//However, for the Recorder Proxy, it's very tricky.
				//The host header will be set to whatever was typed in the browser's address bar. A website may use the host header for it's
				//own internal routing, in which case we must override it because the address to the recorder proxy will not be correct. 
				//The website may also use the host name for assembling links, in which case we don't want to override the header because 
				//otherwise the link would no longer point back to the proxy. Inspecting and modifying any links in the response may help, 
				//but is not 100% foolproof.  
				if(overrideHostHeader && headerNameLowerCase.equals("host"))
				{
					boolean isHTTPs = scheme.equalsIgnoreCase("https");
					String port;
					if(isHTTPs)
						port = (httpsPort == 443) ? "" : (":" + httpsPort);
					else
						port = (httpPort == 80) ? "" : (":" + httpPort);
						
					httpMethod.addHeader(headerName, host + port);
					continue;
				}
				
				//Already set by the setEntity() method above.
				if(headerNameLowerCase.equals("content-length") || headerNameLowerCase.equals("content-type"))
					continue;
				
				String headerValue = entry.getValue();
				
				//When using the recorder proxy for forwarding requests, we want to retain the cookies and forward them too,
				//since the browser is managing the cookies and we are just a middle man.
				//However, when using the load tester, its simulating a browser and must manage the cookies itself.
				if(!retainCookies && headerNameLowerCase.equals("cookie"))
    				continue;
				
				//For performance reasons, don't force close the connection
				if(headerNameLowerCase.equals("connection") && headerValue.equals("close"))
					continue;
	
				httpMethod.addHeader(headerName, headerValue);
			}
		}
		
		return httpMethod;
	}
}
