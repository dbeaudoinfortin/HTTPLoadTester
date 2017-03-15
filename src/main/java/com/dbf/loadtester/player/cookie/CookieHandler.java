package com.dbf.loadtester.player.cookie;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SM;
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
	
	/*
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
	
	/*
	 * Adapted from org.apache.http.client.protocol.RequestAddCookies
	 */
	public static void applyCookies(CookieStore cookieStore, CookieOrigin cookieOrigin, HttpRequestBase request)
	{
		//Since we recycle the request objects, we need to clear the headers from the previous execution
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
		// The user agent must evict all expired cookies if, at any time, an expired cookie
		// exists in the cookie store
		if (expired) cookieStore.clearExpired(now);
		
		// Generate Cookie request headers
		if (!matchedCookies.isEmpty())
		{
			final List<Header> headers = cookieSpec.formatCookies(matchedCookies);
			for (final Header header : headers)
			{
				request.addHeader(header);
			}
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
}
