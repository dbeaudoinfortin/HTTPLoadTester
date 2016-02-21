package com.dbf.loadtester;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

import com.dbf.loadtester.recorder.RecorderHttpServletRequestWrapper;

public class HTTPActionConverter
{
	public static HTTPAction convertHTTPRequest(RecorderHttpServletRequestWrapper httpRequest, Date currentDate, long timePassed) throws IOException
	{
		HTTPAction httpAction = new HTTPAction();
		httpAction.setAbsoluteTime(currentDate);
		httpAction.setTimePassed(timePassed);
		httpAction.setServletPath(httpRequest.getServletPath());
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
	
	public static HttpMethod convertHTTPAction(HTTPAction action, String host, String httpPort, String httpsPort)
	{
		String url = action.getScheme() + "://" + host + ":" + (action.getScheme().equals("https") ? httpsPort : httpPort) + action.getServletPath();
		String actionMethod = action.getMethod().toUpperCase();
		
		boolean hasContent = false;
		
		HttpMethodBase httpMethod = null;
		switch(actionMethod)
		{
			case "POST":
				httpMethod = new PostMethod(url);
				hasContent = true;
				break;
			case "PUT":
				httpMethod = new PutMethod(url);
				hasContent = true;
				break;
			case "HEAD":
				httpMethod = new HeadMethod(url);
				break;
			case "GET":
				httpMethod = new GetMethod(url);
				break;
			default:
				return null;	
		}
		
		if(hasContent)
		{
			RequestEntity requestEntity = new InputStreamRequestEntity(new ByteArrayInputStream(Base64.decodeBase64(action.getContent())), action.getContentType());
			((EntityEnclosingMethod) httpMethod).setRequestEntity(requestEntity);
		}
		
		for (Map.Entry<String,String> entry : action.getHeaders().entrySet())
		{
			String headerName = entry.getKey();
			if(headerName.equals("host"))
			{
				httpMethod.addRequestHeader(headerName, host);
			}
			else
			{
				httpMethod.addRequestHeader(headerName, entry.getValue());
			}
		}
		
		httpMethod.setQueryString(action.getQueryString());
		//httpMethod.setDoAuthentication(false);
		httpMethod.setFollowRedirects(false);
		return httpMethod;
	}
}
