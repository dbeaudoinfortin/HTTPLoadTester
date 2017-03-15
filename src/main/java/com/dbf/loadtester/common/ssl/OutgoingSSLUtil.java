package com.dbf.loadtester.common.ssl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutgoingSSLUtil
{
	private static final Logger log = LoggerFactory.getLogger(OutgoingSSLUtil.class);
	
	private static SSLConnectionSocketFactory sslFactory = buildSSLFactory();
	
	public static SSLConnectionSocketFactory getSSLFactory()
	{
		return sslFactory;
	}
	
	private static SSLConnectionSocketFactory buildSSLFactory()
	{
		try
		{
    		SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] {new AcceptAnythingX509TrustManager()}, null);
            sslFactory = new SSLConnectionSocketFactory(sslcontext, new AcceptAnythingHostnameVerifier());
            return sslFactory;
		}
        catch (Throwable t)
		{
        	log.error("Failed to initialize SSL Trust Manager.",t);
		}
		
		return null;
	}

}
