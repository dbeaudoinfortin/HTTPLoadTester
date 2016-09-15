package com.dbf.loadtester.httpclient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.httpclient.contrib.ssl.EasyX509TrustManager;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.log4j.Logger;

public class HTTPClientFactory
{
	private static final Logger log = Logger.getLogger(HTTPClientFactory.class);
	
	private static SSLConnectionSocketFactory sslFactory;
	
	static
	{
		try
		{
    		SSLContext sslcontext = SSLContexts.custom().useProtocol("SSL").build();
            sslcontext.init(null, new TrustManager[] {new EasyX509TrustManager(null)}, null);
            sslFactory = new SSLConnectionSocketFactory(sslcontext, new DefaultHostnameVerifier());
		}
        catch (Throwable t)
		{
        	log.fatal("Fail to initialize SSL trust Manager.",t);
		}
	}
	
	public static HttpClient getHttpClient(int maxConnections)
	{
		return HttpClientBuilder
				.create()
				.disableRedirectHandling()
				.setMaxConnPerRoute(maxConnections)
				.setMaxConnTotal(maxConnections)
				.setSSLSocketFactory(sslFactory)
				.build();
	}
}
