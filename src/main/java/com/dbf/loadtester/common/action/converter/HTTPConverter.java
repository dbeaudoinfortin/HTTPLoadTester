package com.dbf.loadtester.common.action.converter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;
import com.dbf.loadtester.recorder.RecorderHttpServletRequestWrapper;

public abstract class HTTPConverter
{	
	/**
	 * 
	 * Extracts header from a Servlet Request
	 * 
	 * Used by the Recorder for both saving actions and forwarding requests
	 * 
	 */
	protected Map<String, String> extractHeaders(RecorderHttpServletRequestWrapper httpRequest)
	{
		Map<String, String> headers = new HashMap<String, String>();
		Enumeration<String> headerNames = httpRequest.getHeaderNames();
		while(headerNames.hasMoreElements())
		{
			String headerName = headerNames.nextElement();
			String headerNameLower = headerName.toLowerCase();
			
			//Ignore content headers
			if(headerNameLower.equals("content-length") || headerNameLower.equals("content-type"))
				continue;
			
			if(headerNameLower.equals("cookie"))
			{
				//A non compliant client may send multiple cookie headers rather than combining them
				//This is a big pain, so fix it up.
				StringBuilder headerValue = new StringBuilder();
				Enumeration<String> cookieValues = httpRequest.getHeaders(headerName);
				headerValue.append(cookieValues.nextElement());
				while(cookieValues.hasMoreElements())
				{
					headerValue.append("; ");
					headerValue.append(cookieValues.nextElement());
				}
				headers.put(headerName, headerValue.toString());
				continue;
			}

			//Otherwise, this is just a simple header
			String value = httpRequest.getHeader(headerName);
			headers.put(headerName, value);
		}
		
		return headers;
	}
	
	/**
	 * Assemble a URI object from arguments
	 * 
	 */
	protected URI buildURI(String scheme, String host, int httpPort, int httpsPort, String queryString, String path) throws URISyntaxException
	{
		//Just to be safe
		scheme = scheme.toLowerCase();
		
		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setScheme(scheme);
		uriBuilder.setHost(host);
		uriBuilder.setPort(scheme.equals("https") ? httpsPort : httpPort);
		uriBuilder.setCustomQuery((queryString != null && queryString.equals("")) ? null : queryString);
		uriBuilder.setPath((path != null && path.equals("")) ? null : path);
		return uriBuilder.build();
	}	
}
