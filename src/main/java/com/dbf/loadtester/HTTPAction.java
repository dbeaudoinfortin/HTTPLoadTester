package com.dbf.loadtester;

import java.io.Serializable;
import java.util.Map;

public class HTTPAction implements Serializable
{
	private static final long serialVersionUID = 1L;
		
	private long timePassed;
	private String servletPath;
	private String method;
	private String characterEncoding;
	private byte[] content; 
	private int contentLength;
	private String contentType;
	private Map<String, String> headers;
	
	public HTTPAction(){}
	
	public long getTimePassed()
	{
		return timePassed;
	}
	
	public void setTimePassed(long timePassed)
	{
		this.timePassed = timePassed;
	}
	
	public String getServletPath()
	{
		return servletPath;
	}
	
	public void setServletPath(String servletPath)
	{
		this.servletPath = servletPath;
	}
	
	public String getMethod()
	{
		return method;
	}
	
	public void setMethod(String method)
	{
		this.method = method;
	}
	
	public String getCharacterEncoding()
	{
		return characterEncoding;
	}
	
	public void setCharacterEncoding(String characterEncoding)
	{
		this.characterEncoding = characterEncoding;
	}
	
	public byte[] getContent()
	{
		return content;
	}
	
	public void setContent(byte[] content)
	{
		this.content = content;
	}
	
	public int getContentLength()
	{
		return contentLength;
	}
	
	public void setContentLength(int contentLength)
	{
		this.contentLength = contentLength;
	}
	
	public String getContentType()
	{
		return contentType;
	}
	
	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}
	
	public Map<String, String> getHeaders()
	{
		return headers;
	}
	
	public void setHeaders(Map<String, String> headers)
	{
		this.headers = headers;
	}
	
}
