package com.dbf.loadtester;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.dbf.loadtester.recorder.RecorderRequestWrapper;

public class HTTPActionConverter
{
	public static HTTPAction convertHTTPRequest(RecorderRequestWrapper httpRequest, long timePassed) throws IOException
	{
		HTTPAction httpAction = new HTTPAction();
		httpAction.setTimePassed(timePassed);
		httpAction.setServletPath(httpRequest.getServletPath());
		httpAction.setMethod(httpRequest.getMethod());
		httpAction.setCharacterEncoding(httpRequest.getCharacterEncoding());
		httpAction.setContentLength(httpRequest.getContentLength());
		httpAction.setContentType(httpRequest.getContentType());
		
		Map<String, String> headers = new HashMap<String, String>(); 
		Enumeration<String> headerNames = httpRequest.getHeaderNames();
		while(headerNames.hasMoreElements())
		{
			String name = headerNames.nextElement();
			String value = httpRequest.getHeader(name);
			headers.put(name, value);
		}
		httpAction.setHeaders(headers);
		httpAction.setContent(httpRequest.getRequestBody());
		return httpAction;
	}
}
