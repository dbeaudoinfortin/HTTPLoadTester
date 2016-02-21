package com.dbf.loadtester;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class HTTPAction implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private Date absoluteTime;
	private long timePassed;
	private String servletPath;
	private String method;
	private String characterEncoding;
	private String content; 
	private int contentLength;
	private String contentType;
	private String scheme;
	private String queryString;
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
	
	public String getContent()
	{
		return content;
	}
	
	public void setContent(String content)
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

	/**
	 * @return the absoluteTime
	 */
	public Date getAbsoluteTime() {
		return absoluteTime;
	}

	/**
	 * @param absoluteTime the absoluteTime to set
	 */
	public void setAbsoluteTime(Date absoluteTime) {
		this.absoluteTime = absoluteTime;
	}

	/**
	 * @return the queryString
	 */
	public String getQueryString() {
		return queryString;
	}

	/**
	 * @param queryString the queryString to set
	 */
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
	
}
