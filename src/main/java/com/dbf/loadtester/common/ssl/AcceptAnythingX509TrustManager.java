package com.dbf.loadtester.common.ssl;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.SSLEngine;

/**
 * Accepts any cert. Unsigned (???), self-signed, invalid, expired, whatever!
 */
public class AcceptAnythingX509TrustManager extends X509ExtendedTrustManager
{
	@Override
	public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {}

	@Override
	public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {}

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1, Socket arg2) throws CertificateException {}

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2) throws CertificateException {}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1, Socket arg2) throws CertificateException {}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2) throws CertificateException {}
	
	@Override
	public X509Certificate[] getAcceptedIssuers()
	{
		return null;
	}
}