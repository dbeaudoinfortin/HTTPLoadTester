package com.dbf.loadtester.common.action;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import com.dbf.loadtester.recorder.RecorderHttpServletRequestWrapper;

public class HTTPConverter
{
	/**
	 * Converts a Servlet request to an HTTP Action.
	 * 
	 * Used by the Recorder to save actions
	 */
	public static HTTPAction convertServletRequestToHTTPAction(RecorderHttpServletRequestWrapper httpRequest, Date currentDate, long timePassed) throws IOException
	{
		HTTPAction httpAction = new HTTPAction();
		httpAction.setAbsoluteTime(currentDate);
		httpAction.setTimePassed(timePassed);
		httpAction.setMethod(httpRequest.getMethod());
		httpAction.setCharacterEncoding(httpRequest.getCharacterEncoding());
		
		httpAction.setContentType(httpRequest.getContentType());
		httpAction.setQueryString(httpRequest.getQueryString());
		httpAction.setHeaders(extractHeaders(httpRequest));
		httpAction.setScheme(httpRequest.getScheme());
		httpAction.setPath(httpRequest.getPathInfo());
		httpAction.setContent(new String(httpRequest.getRequestBody()));
		httpAction.setContentLength(httpRequest.getContentLength());
		
		return httpAction;
	}
	
	/**
	 * Converts an HTTP Action to an Apache HTTP Client Request.
	 * 
	 * Used by the Player for load testing.
	 */
	public static HttpRequestBase convertHTTPActionToApacheRequest(HTTPAction action, String host, int httpPort, int httpsPort) throws URISyntaxException
	{
		return buildApacheRequest(action.getScheme(), host, httpPort, httpsPort, action.getQueryString(), action.getPath(), action.getMethod(), action.getHeaders(), action.getContent().getBytes(), action.getContentType(), false, true);
	}
	
	/**
	 * Converts a Servlet request to an Apache HTTP Client Request.
	 * 
	 * Used by the Recorder Proxy to forward requests
	 */
	public static HttpRequestBase convertServletRequestToApacheRequest(RecorderHttpServletRequestWrapper httpRequest, String host, int httpPort, int httpsPort, boolean overrideHostHeader) throws URISyntaxException
	{
		return buildApacheRequest(httpRequest.getScheme(), host, httpPort, httpsPort, httpRequest.getQueryString(), httpRequest.getPathInfo(), httpRequest.getMethod(), extractHeaders(httpRequest),
				httpRequest.getRequestBody(), httpRequest.getContentType(), true, overrideHostHeader);
	}
	
	/**
	 * 
	 * Extracts header from a Servlet Request
	 * 
	 * Used by the Recorder for both saving actions and forwarding requests
	 * 
	 */
	private static Map<String, String> extractHeaders(RecorderHttpServletRequestWrapper httpRequest)
	{
		Map<String, String> headers = new HashMap<String, String>(); 
		Enumeration<String> headerNames = httpRequest.getHeaderNames();
		while(headerNames.hasMoreElements())
		{
			String headerName = headerNames.nextElement();
			
			//Ignore content headers
			if(headerName.equals("Content-Length") || headerName.equals("Content-Type"))
				continue;
			
			String value = httpRequest.getHeader(headerName);
			headers.put(headerName, value);
		}
		
		return headers;
	}
	
	/**
	 * Build an Apache HTTP Client Request.
	 * 
	 * Used by the Player for load testing and by the Recorder Proxy for forwarding requests.
	 */
	private static HttpRequestBase buildApacheRequest(String scheme, String host, int httpPort, int httpsPort, String queryString, String path, String method, Map<String,String> headers, byte[] content, String contentType, boolean retainCookies, boolean overrideHostHeader) throws URISyntaxException
	{
		URI uri = buildURI(scheme, host, httpPort, httpsPort, queryString, path);
		String actionMethod = method.toUpperCase();
		
		boolean hasContent = false;
		
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
			default:
				return null;	
		}

		//Apply the content
		if(hasContent)
		{
			//TODO: Multipart support
			//MultipartEntityBuilder builder;
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
				
				//It good practice to treat headers as case-insensitive
				String headerNameLowerCase = headerName.toLowerCase();
				
				//Note about he overrideHostHeader flag: Sometimes we want to override the host and sometimes we don't.
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
				if(headerName.equals("Content-Length") || headerName.equals("Content-Type"))
					continue;
				
				//When using the recorder proxy and forwarding requests, we want to retain the cookies and forward them too. 
				//The browser is managing the cookies and we are just a middle man. However, when using the load tester, 
				//the load tester simulating a browser and must manage the cookies itself. We don't want to use the cookies
				//saved at the time of recording the actions since these are out of date.
				if(headerName.equals("Cookie") && !retainCookies)
					continue;
				
				String headerValue = entry.getValue();
				
				//For performance reasons, don't force close the connection
				if(headerName.equals("Connection") && headerValue.equals("close"))
					continue;
	
				httpMethod.addHeader(headerName, headerValue);
			}
		}
		
		return httpMethod;
	}
	
	private static URI buildURI(String scheme, String host, int httpPort, int httpsPort, String queryString, String path) throws URISyntaxException
	{
		scheme = scheme.toLowerCase();
		
		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setScheme(scheme);
		uriBuilder.setHost(host);
		uriBuilder.setPort(scheme.equals("https") ? httpsPort : httpPort);
		uriBuilder.setCustomQuery((queryString != null && queryString.equals("")) ? null : queryString);
		uriBuilder.setPath((path != null && path.equals("")) ? null : path);
		return uriBuilder.build();
	}
	
	//TODO: Multipart support
	/* 
	private static void convertServletParts(Collection<Part> parts)
	{
		for(Part part : parts)
		{
			System.out.println(part);
		}
	}
	*/
	
}
