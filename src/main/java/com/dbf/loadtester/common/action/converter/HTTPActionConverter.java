package com.dbf.loadtester.common.action.converter;

import java.io.IOException;
import java.util.Date;

import com.dbf.loadtester.common.action.HTTPAction;
import com.dbf.loadtester.recorder.RecorderHttpServletRequestWrapper;

/**
 * Provides methods for converting a Servlet Requests to an HTTP Action
 */
public class HTTPActionConverter extends HTTPConverter
{	
	public HTTPActionConverter(){}
	
	/**
	 * Converts a Servlet request to an HTTP Action.
	 * 
	 * Used by the Recorder to save actions
	 */
	public HTTPAction convertServletRequestToHTTPAction(RecorderHttpServletRequestWrapper httpRequest, Date currentDate, long timePassed) throws IOException
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
		return httpAction;
	}
}
