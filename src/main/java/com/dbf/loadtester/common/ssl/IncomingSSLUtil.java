package com.dbf.loadtester.common.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.codec.binary.Base64;

public class IncomingSSLUtil
{
	private static final String DUMMY_KEYSTORE =  "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";
	
	public static SSLContext buildSSLContext() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException
	{
		KeyManager[] keyManagers = getKeyManagers();
		TrustManager[] trustManagers = buildTrustManagers();

		SSLContext sslContext;
		try
		{
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(keyManagers, trustManagers, null);
		}
		catch (Exception e)
		{
			throw new IOException("Unable to create and initialise the SSLContext", e);
		}

		return sslContext;
	}

	private static TrustManager[] buildTrustManagers() throws IOException
	{
		TrustManager[] trustManagers = null;
		try
		{
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init((KeyStore) null);
			trustManagers = trustManagerFactory.getTrustManagers();
		}
		catch (Exception e)
		{
			throw new IOException("Unable to initialise TrustManager[]", e);
		}
		return trustManagers;
	}

	private static KeyManager[] getKeyManagers() throws IOException
	{
		try
		{
			KeyStore keyStore = buildKeyStore(DUMMY_KEYSTORE, "JKS", "dummy");
			return buildKeyManagers(keyStore, "dummy");
		}
		catch (Exception e)
		{
			throw new IOException("Unable to load KeyStore", e);
		}
	}

	private static KeyStore buildKeyStore(String keyStoreBase64, String type, String password) throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException
	{
		ByteArrayInputStream keyStoreStream = new ByteArrayInputStream(Base64.decodeBase64(keyStoreBase64));
		try
		{
			KeyStore loadedKeystore = KeyStore.getInstance(type);
			loadedKeystore.load(keyStoreStream, password.toCharArray());
			return loadedKeystore;
		}
		finally
		{
			keyStoreStream.close();
		}
	}

	private static KeyManager[] buildKeyManagers(final KeyStore keyStore, String keyStorePassword) throws IOException
	{
		char[] storePasswordChars = keyStorePassword.toCharArray();
		KeyManager[] keyManagers;
		try
		{
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, storePasswordChars);
			keyManagers = keyManagerFactory.getKeyManagers();
		}
		catch (Exception e)
		{
			throw new IOException("Unable to initialise KeyManager[]", e);
		}
		return keyManagers;
	}
}
