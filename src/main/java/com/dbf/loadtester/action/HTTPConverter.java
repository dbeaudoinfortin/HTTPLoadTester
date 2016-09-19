package com.dbf.loadtester.action;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
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
	public static HTTPAction convertServletRequestToHTTPAction(RecorderHttpServletRequestWrapper httpRequest, Date currentDate, long timePassed) throws IOException
	{
		HTTPAction httpAction = new HTTPAction();
		httpAction.setAbsoluteTime(currentDate);
		httpAction.setTimePassed(timePassed);
		httpAction.setPath(httpRequest.getPathInfo());
		httpAction.setMethod(httpRequest.getMethod());
		httpAction.setCharacterEncoding(httpRequest.getCharacterEncoding());
		httpAction.setContentLength(httpRequest.getContentLength());
		httpAction.setContentType(httpRequest.getContentType());
		httpAction.setQueryString(httpRequest.getQueryString());
		httpAction.setHeaders(extractHeaders(httpRequest));
		httpAction.setScheme(httpRequest.getScheme());
		httpAction.setContent(Base64.encodeBase64String(httpRequest.getRequestBody()));
		return httpAction;
	}
	
	public static HttpRequestBase convertHTTPActionToHTTPClientRequest(HTTPAction action, String host, int httpPort, int httpsPort) throws URISyntaxException
	{
		return buildHTTPClientRequest(action.getScheme(), host, httpPort, httpsPort, action.getQueryString(), action.getPath(), action.getMethod(), action.getHeaders(), Base64.decodeBase64(action.getContent()), action.getContentType());
	}
	
	public static HttpRequestBase convertServletRequestToHTTPClientRequest(RecorderHttpServletRequestWrapper httpRequest, String host, int httpPort, int httpsPort) throws URISyntaxException
	{
		return buildHTTPClientRequest(httpRequest.getScheme(), host, httpPort, httpsPort, httpRequest.getQueryString(), httpRequest.getPathInfo(), httpRequest.getMethod(), extractHeaders(httpRequest),
				httpRequest.getRequestBody(), httpRequest.getContentType());
	}
	
	private static Map<String, String> extractHeaders(RecorderHttpServletRequestWrapper httpRequest)
	{
		Map<String, String> headers = new HashMap<String, String>(); 
		Enumeration<String> headerNames = httpRequest.getHeaderNames();
		while(headerNames.hasMoreElements())
		{
			String name = headerNames.nextElement();
			String value = httpRequest.getHeader(name);
			headers.put(name, value);
		}
		
		return headers;
	}
	
	private static HttpRequestBase buildHTTPClientRequest(String scheme, String host, int httpPort, int httpsPort, String queryString, String path, String method, Map<String,String> headers, byte[] content, String contentType) throws URISyntaxException
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
		
		if(hasContent)
		{
			HttpEntity requestEntity = new ByteArrayEntity(content, ContentType.parse(contentType));
			((HttpEntityEnclosingRequestBase) httpMethod).setEntity(requestEntity);
			
		}
		
		for (Map.Entry<String,String> entry : headers.entrySet())
		{
			String headerName = entry.getKey();
			if(headerName.equals("host"))
			{
				httpMethod.addHeader(headerName, host);
			}
			else
			{
				httpMethod.addHeader(headerName, entry.getValue());
			}
		}
		
		return httpMethod;
	}
	
	private static URI buildURI(String scheme, String host, int httpPort, int httpsPort, String queryString, String path) throws URISyntaxException
	{
		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setScheme(scheme);
		uriBuilder.setHost(host);
		uriBuilder.setPort(scheme.equals("https") ? httpsPort : httpPort);
		uriBuilder.setCustomQuery(queryString);
		uriBuilder.setPath(path);
		return uriBuilder.build();
	}
	
}
