package com.dbf.loadtester.player.cookie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SM;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.DefaultCookieSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CookieHandler
{
	private static final Logger log = LoggerFactory.getLogger(CookieHandler.class);
	
	private static final CookieSpec cookieSpec = new DefaultCookieSpec();
	
	public static CookieOrigin determineCookieOrigin(String hostName, int port, String path, boolean secure)
	{
		if(path == null || path.length() == 0)
		{
			path =  "/";
		}
		else if (!path.equals("/") && path.endsWith("/"))
		{
			path = path.substring(0, path.length() -1);
		}

		return new CookieOrigin(hostName, port, path, secure);
	}
	
	/**
	 * Adapted from org.apache.http.client.protocol.ResponseProcessCookies
	 */
	public static void storeCookie(CookieStore cookieStore, CookieOrigin cookieOrigin,  HttpResponse response)
	{
		//Cookies are manually managed instead of relying on the Apache HTTP Client
		//because, for performance reasons, the HTTP Client is shared across test plan
		//runs and threads
		for(Header header : response.getHeaders(SM.SET_COOKIE))
		{
			try
			{
				for (Cookie cookie : cookieSpec.parse(header, cookieOrigin))
				{
					try
					{
						cookieSpec.validate(cookie, cookieOrigin);
						cookieStore.addCookie(cookie);
					}
					catch (MalformedCookieException e)
					{
						log.warn("Recieved a malformed cookie '" + formatCooke(cookie) + "': " + e.getMessage());
					}
				}
			}
			catch (MalformedCookieException e)
			{
				log.warn("Recieved a malformed cookie in header '" + header + "': " + e.getMessage());
			}
		}
		
	}
	
	public static List<Cookie> applyWhiteListCookies(Map<String,String> headers, Collection<String> cookieWhiteList)
	{
		List<Cookie> retainedCookies = new ArrayList<Cookie>();
		
		if(headers == null || headers.size() == 0) return retainedCookies;
		if (cookieWhiteList == null || cookieWhiteList.size() == 0) return retainedCookies;
		
		for (Map.Entry<String, String> entry : headers.entrySet())
		{
			String headerValue = entry.getValue();
			String headerNameLowerCase = entry.getKey().toLowerCase();
			if(!headerNameLowerCase.equals("cookie")) continue;
						
			//Parse the cookie header to determine the cookie name
			//Cookie parsing is hard! It requires knowing CookieOrigin and having a defined CookieSpec.
			//I'm just going to split the string in the case of multiple cookie headers
			for(String cookie : headerValue.split("; "))
			{
				//Now, I'm going to cheat and just take everything before the equals ('=') sign.
				int splitIndex = cookie.indexOf('=');
				if (splitIndex < 0 ) continue;

				String cookieName = cookie.substring(0, splitIndex);
				if(!cookieWhiteList.contains(cookieName)) continue;
				
				String cookieValue = cookie.substring(splitIndex + 1, cookie.length());
				retainedCookies.add(new BasicClientCookie(cookieName, cookieValue));
			}
		}
		
		return retainedCookies;
	}
	
	/**
	 * Adapted from org.apache.http.client.protocol.RequestAddCookies
	 * 
	 * Returns a list of headers that were added to the request
	 */
	public static void applyCookies(CookieStore cookieStore, List<Cookie> additionalCookie, CookieOrigin cookieOrigin, HttpRequestBase request)
	{
		//Since we recycle the request objects, we need to clear the cookies that were added from the previous execution.
		request.removeHeaders(SM.COOKIE);
		
		final Date now = new Date();
		
		// Find cookies matching the given origin
		final List<Cookie> matchedCookies = new ArrayList<Cookie>();

		boolean expired = false;
		for (final Cookie cookie : cookieStore.getCookies())
		{
			if (!cookie.isExpired(now))
			{
				if (cookieSpec.match(cookie, cookieOrigin)) matchedCookies.add(cookie);
			}
			else
			{
				expired = true;
			}
		}
		
		// Per RFC 6265, 5.3
		// The user agent must evict all expired cookies if, at any time, an expired cookie exists in the cookie store
		if (expired) cookieStore.clearExpired(now);
		
		//Add any white list cookies taken from the test plan
		matchedCookies.addAll(additionalCookie);
		
		// Generate Cookie request header(s)
		if (!matchedCookies.isEmpty())
		{
			List<Header> headers = cookieSpec.formatCookies(matchedCookies);
			for (final Header header : headers) request.addHeader(header);
		}
	}

	private static String formatCooke(Cookie cookie)
	{
		final StringBuilder buf = new StringBuilder();
		buf.append(cookie.getName());
		buf.append("=\"");
		String v = cookie.getValue();
		if (v != null)
		{
			if (v.length() > 100)
			{
				v = v.substring(0, 100) + "...";
			}
			buf.append(v);
		}
		buf.append("\"");
		buf.append(", version:");
		buf.append(Integer.toString(cookie.getVersion()));
		buf.append(", domain:");
		buf.append(cookie.getDomain());
		buf.append(", path:");
		buf.append(cookie.getPath());
		buf.append(", expiry:");
		buf.append(cookie.getExpiryDate());
		return buf.toString();
	}

	public static CookieSpec getCookieSpec()
	{
		return cookieSpec;
	}
}
