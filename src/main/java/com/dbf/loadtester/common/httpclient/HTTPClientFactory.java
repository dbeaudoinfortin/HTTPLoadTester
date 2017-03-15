package com.dbf.loadtester.common.httpclient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.contrib.ssl.EasyX509TrustManager;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPClientFactory
{
	private static final Logger log = LoggerFactory.getLogger(HTTPClientFactory.class);
	
	private static SSLConnectionSocketFactory sslFactory;
	
	static
	{
		try
		{
    		SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] {new EasyX509TrustManager(null)}, null);
            sslFactory = new SSLConnectionSocketFactory(sslcontext, new DefaultHostnameVerifier());
		}
        catch (Throwable t)
		{
        	log.error("Failed to initialize SSL Trust Manager.",t);
		}
	}
	
	public static HttpClient getHttpClient(int maxConnections)
	{
		return HttpClientBuilder
				.create()
				.disableRedirectHandling()
				.disableCookieManagement()
				.setMaxConnPerRoute(maxConnections)
				.setMaxConnTotal(maxConnections)
				.setSSLSocketFactory(sslFactory)
				.build();
	}
}
