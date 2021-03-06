package com.dbf.loadtester.player.action;

import java.util.List;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;

import com.dbf.loadtester.common.action.HTTPAction;
import com.dbf.loadtester.common.action.substitutions.VariableSubstitution;

/**
 * Adds several runtime calculated fields to HTTPAction.
 * For use by the HTTP Load Test Player.
 *
 */
public class PlayerHTTPAction extends HTTPAction
{
	private static final long serialVersionUID = 1L;
	
	private transient int id = -1;

	private transient HttpRequestBase httpRequest;
	private transient CookieOrigin cookieOrigin;
	private transient List<Cookie> whiteListCookies;
	private transient String identifier;
	private transient long lastRunDuration = -1;
	private transient List<VariableSubstitution> replacementVariables;
	private transient List<VariableSubstitution> retrievalVariables;
	
	public PlayerHTTPAction(HTTPAction other)
	{
		super(other);
		
		//The action identifier should simply be the description prior to applying any thread substitutions
		//For this to work, it needs to be manually set prior to applying substitutions.
		//So, we copy it as part of the constructor
		this.identifier = other.getIdentifier();
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

	public CookieOrigin getCookieOrigin()
	{
		return cookieOrigin;
	}

	public void setCookieOrigin(CookieOrigin cookieOrigin)
	{
		this.cookieOrigin = cookieOrigin;
	}

	public List<Cookie> getWhiteListCookies()
	{
		return whiteListCookies;
	}

	public void setWhiteListCookies(List<Cookie> whiteListCookies)
	{
		this.whiteListCookies = whiteListCookies;
	}

	@Override
	public String getIdentifier()
	{
		return identifier;
	}

	public long getLastRunDuration()
	{
		return lastRunDuration;
	}

	public void setLastRunDuration(long lastRunDuration)
	{
		this.lastRunDuration = lastRunDuration;
	}

	public List<VariableSubstitution> getReplacementVariables()
	{
		return replacementVariables;
	}

	public void setReplacementVariables(List<VariableSubstitution> replacementVariables)
	{
		this.replacementVariables = replacementVariables;
	}

	public List<VariableSubstitution> getRetrievalVariables()
	{
		return retrievalVariables;
	}

	public void setRetrievalVariables(List<VariableSubstitution> retrievalVariables)
	{
		this.retrievalVariables = retrievalVariables;
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
