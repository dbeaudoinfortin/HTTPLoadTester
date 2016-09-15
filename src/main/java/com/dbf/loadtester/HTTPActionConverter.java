package com.dbf.loadtester;

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

public class HTTPActionConverter
{
	public static HTTPAction convertHTTPRequest(RecorderHttpServletRequestWrapper httpRequest, Date currentDate, long timePassed) throws IOException
	{
		HTTPAction httpAction = new HTTPAction();
		httpAction.setAbsoluteTime(currentDate);
		httpAction.setTimePassed(timePassed);
		httpAction.setPath(httpRequest.getServletPath());
		httpAction.setMethod(httpRequest.getMethod());
		httpAction.setCharacterEncoding(httpRequest.getCharacterEncoding());
		httpAction.setContentLength(httpRequest.getContentLength());
		httpAction.setContentType(httpRequest.getContentType());
		httpAction.setQueryString(httpRequest.getQueryString());
		
		Map<String, String> headers = new HashMap<String, String>(); 
		Enumeration<String> headerNames = httpRequest.getHeaderNames();
		while(headerNames.hasMoreElements())
		{
			String name = headerNames.nextElement();
			String value = httpRequest.getHeader(name);
			headers.put(name, value);
		}
		httpAction.setHeaders(headers);
		
		httpAction.setScheme(httpRequest.getScheme());
		httpAction.setContent(Base64.encodeBase64String(httpRequest.getRequestBody()));
		return httpAction;
	}
	
	public static HttpRequestBase convertHTTPAction(HTTPAction action, String host, int httpPort, int httpsPort) throws URISyntaxException
	{
		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setScheme(action.getScheme());
		uriBuilder.setHost(host);
		uriBuilder.setPort(action.getScheme().equals("https") ? httpsPort : httpPort);
		uriBuilder.setCustomQuery(action.getQueryString());
		uriBuilder.setPath(action.getPath());
		
		URI uri = uriBuilder.build();
		String actionMethod = action.getMethod().toUpperCase();
		
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
			HttpEntity requestEntity = new ByteArrayEntity((Base64.decodeBase64(action.getContent())), ContentType.parse(action.getContentType()));
			((HttpEntityEnclosingRequestBase) httpMethod).setEntity(requestEntity);
			
		}
		
		for (Map.Entry<String,String> entry : action.getHeaders().entrySet())
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
}
