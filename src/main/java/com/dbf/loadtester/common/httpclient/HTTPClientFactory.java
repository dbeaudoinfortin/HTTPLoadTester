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
				.disableRedirectHandling()
				.disableCookieManagement()
				.setMaxConnPerRoute(maxConnections)
				.setMaxConnTotal(maxConnections)
				.setSSLSocketFactory(OutgoingSSLUtil.getSSLFactory())
				.build();
	}
}
