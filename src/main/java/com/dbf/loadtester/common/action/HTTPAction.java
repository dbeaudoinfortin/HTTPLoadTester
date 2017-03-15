package com.dbf.loadtester.common.action;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.CookieOrigin;

public class HTTPAction implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private transient int id = -1;
	private Date absoluteTime;
	private long timePassed;
	private String path;
	private String method;
	private String characterEncoding;
	private String content; 
	private int contentLength;
	private String contentType;
	private String scheme;
	private String queryString;
	private Map<String, String> headers;
	private transient String identifier;
	private transient HttpRequestBase httpRequest;
	private transient CookieOrigin cookieOrigin;
	
	public HTTPAction(){}
	
	public HTTPAction(HTTPAction other)
	{
		this.id = other.id;
		this.absoluteTime = other.absoluteTime;
		this.timePassed = other.timePassed;
		this.path = other.path;
		this.method = other.method;
		this.characterEncoding = other.characterEncoding;
		this.content = other.content; 
		this.contentLength = other.contentLength;
		this.contentType = other.contentType;
		this.scheme = other.scheme;
		this.queryString = other.queryString;
		this.headers = other.headers;
		this.identifier = other.getIdentifier(); //Must call getter
	}
	
	public long getTimePassed()
	{
		return timePassed;
	}
	
	public void setTimePassed(long timePassed)
	{
		this.timePassed = timePassed;
	}
	
	public String getPath()
	{
		return path;
	}
	
	public void setPath(String path)
	{
		this.path = path;
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

	public Date getAbsoluteTime() {
		return absoluteTime;
	}

	public void setAbsoluteTime(Date absoluteTime) {
		this.absoluteTime = absoluteTime;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public HttpRequestBase getHttpRequest()
	{
		return httpRequest;
	}

	public void setHttpRequest(HttpRequestBase httpRequest)
	{
		this.httpRequest = httpRequest;
	}
	
	public String getIdentifier()
	{
		//The action identifier should simply be the description prior to applying any thread substitutions
		//For this to work, it needs to be manually set prior to applying substitutions
		if (null == identifier) identifier = getDescription();
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	private String getDescription()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getMethod());
		sb.append(" ");
		sb.append(scheme);
		sb.append(" ");
		sb.append(path);
		
		if(queryString != null && !queryString.equals(""))
		{
			sb.append("?");
			sb.append(queryString);
		}
		return sb.toString();
	}

	public CookieOrigin getCookieOrigin()
	{
		return cookieOrigin;
	}

	public void setCookieOrigin(CookieOrigin cookieOrigin)
	{
		this.cookieOrigin = cookieOrigin;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		if(id > -1)
		{
			sb.append(id);
			sb.append(":");
		}
		sb.append("[");
		sb.append(getDescription());
		sb.append("]");
		return sb.toString();
	}
}
