package com.dbf.loadtester.common.httpclient;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.dbf.loadtester.common.ssl.OutgoingSSLUtil;

public class HTTPClientFactory
{	
	public static HttpClient getHttpClient(int maxConnections)
	{
		return HttpClientBuilder
				.create()
				.disableRedirectHandling() //Redirects will be manually managed
				.disableCookieManagement() //Cookies will be manually managed
				.setMaxConnPerRoute(maxConnections)
				.setMaxConnTotal(maxConnections)
				.setSSLSocketFactory(OutgoingSSLUtil.getSSLFactory())
				.build();
	}
}
