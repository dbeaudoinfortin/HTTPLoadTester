package com.dbf.loadtester.common.util;

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

public class SSLUtil
{
	private static final String DUMMY_KEYSTORE =  "/u3+7QAAAAIAAAABAAAAAQAKc2VsZnNpZ25lZAAAAVSf6kLSAAAFAjCCBP4wDgYKKwYBBAEqAhEBAQUABIIE6prlRncwWQ2CEzfW"
												+ "PXjxU90Ne7NVvpOkLSXdnzfT3zILxL3Z38ytcHcFxstJiAfp/O00WrE0Ib5PRXNaHarcWI1p7oCJrZ8SLIgeXwh9NHfbjizSDiBw"
												+ "mDvol6kcajXpMPxbyBoMf5t0ahnQSVd23h4ahddvO338DA2gnmDQnVekSRGQpLmpwv+oM1UNLZpcA+XWt0vFtlmFT9YfU67vev+5"
												+ "38e3Xc3M/UyL97NZ9P2YFFVLWAaEt2t0WUqHcKFAJBkqcmCNWnhcMJoJvsvPlRV1F9/XyiLn0UibHlQX4pihAIEY1qXzmZkeos3K"
												+ "AOb4b+858rioTb7FgkmEPQRJIyDnLEtNDBvAc6rCvcVLU1dXOIBpsq2ZUwZiuQHxthfZPKmBNdFJIAm7Aon//9rcXEh4DP2g9JVt"
												+ "VkVDGZqlxcMixl84GRUCwG1jiERn333FdxQ9qwlg7M42jUXlba0EU8nTJukS7sHdpxfeyTk+WvgB05MoUDdk3ysYohGM485XXcoR"
												+ "5JDRhUWx6t0Q21ld13h2RP4PmuCaXgQWk1E8lUYLIj/Xs5YLdJmqLo3t/+TWkWPegkWRYP9YYRQHBsaBDk9V+ItRUoJ+hYHsKr1/"
												+ "D/NcoYtlQEQvCbvI99VapdDo26vcTNpUNjoUYEgpQmj3vkKBHeenyRhZTF5gaGhQp+hk1+BRtbR5gUIC8h40B+nJ0fO8EtQaWQJN"
												+ "xCJ2qdlHXQRCqJ/ZF3iAK9fi36Li1HCUwoHGF9AnFTPZLwy0o4XE5vRG+I1obrPtn7eSRT3R5zgpmxJrn6WuNPmZrJTt6/X04BjZ"
												+ "kXY2cWp/JZr81N43llNSvq/b+cNX7NI/MaAwNVBUGg4k2G7ekKftt6jooisvPhGC4HSSAdgCv467YMgaRl9zXPo3HYnKtxQQJkf4"
												+ "imo5VQKgnUObdcKnDrUmp6+8xySPBVUyLH956BQeHlwYHqyvdihJAJ8Nav4rcxjBHdhnBzeolUc7Gx3z4R106RQje44bPpO2Vkxq"
												+ "hE9vep4NQxJzkrAhywppqSpIhl2lG/G20vFuV9b9j4gaDoac1TdjzkfVmjXwqio5xdWFvcejWNkSM3F8T/MSv8FYEj75MPN1v9K3"
												+ "OO/kZ7xfbpbbesGjRkz/WTQihpKpjcwZBa48UZuF3y2+gCallsxvKlNZ8l1OLzr6oX69gQWnYxChodMTAF9lvjyKcAtlJLYZpbpd"
												+ "+TADNgR1gIimurChjGPlo3DyUjFqamC61lYfoGd8tXR5e9yTVvt3iH6syVT6tOKm2aTPtp53iIH7lNKZYbJIxa69lqM3q6bBSa7K"
												+ "tJ3gHcc5fINwy36sOTSjRJ3Nrdbfe513LZKJoVutz+yyLSLFLKDw851Nyjt4egfzNFk6KXEg3IprFXSt6O+BriIJEW1u+IqxZiCH"
												+ "ZVG0/E9wgjmarIpWIv+dK0mzd/9c3hD3sC8UPj+egn4a81ZCh3p3xSwW3VOZ1JwvmGip3b8czTQiGv0XcAJQPGRfXYJEkRTPkuCL"
												+ "G4gBEyQ+/1J9slBaKsmezVeSdJxrKOrFpgjv9FLx1yAn9bS6uuh26/JrI7Xmc/xwN+1xKgWA+ffSertXVZKi2xwyLzCmiYL9CPk9"
												+ "FyG2Q70mgAxZ0J/KIEAs+h8EehWzzSCpH6zWysB2yOwPUGBTdG1QtdL5JHAuWKcAAAABAAVYLjUwOQAAA1cwggNTMIICO6ADAgEC"
												+ "AgRKUlgGMA0GCSqGSIb3DQEBCwUAMFoxCzAJBgNVBAYTAkNBMQswCQYDVQQIEwJPTjEPMA0GA1UEBxMGT3R0YXdhMQ0wCwYDVQQK"
												+ "EwRGdXplMQwwCgYDVQQLDANSJkQxEDAOBgNVBAMTB1Vua25vd24wHhcNMTYwNTExMTMwMzI1WhcNMjExMjE5MTMwMzI1WjBaMQsw"
												+ "CQYDVQQGEwJDQTELMAkGA1UECBMCT04xDzANBgNVBAcTBk90dGF3YTENMAsGA1UEChMERnV6ZTEMMAoGA1UECwwDUiZEMRAwDgYD"
												+ "VQQDEwdVbmtub3duMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjnv8F6Z6tSHE7YupCMtd5PIl2jQV/Xgi5fkPw7QY"
												+ "+Wz3IXO/ll9/BcIfUSmU16DDzjT5QAlHIMccFouCXyTEUl633SluB+eMGLC5n6xKd5aiyGWAIyZNpz77Dnux8IfA8+oEDmloBZD2"
												+ "RxfcUiOkfCwq+YbjZx5IIvR5jgRCg+jLed7AlameWp5xkEOeRgjb2KH5uk7YgS6oLUC64REXL5dTz0UkG8nUei6S2j7IWcMzzbEM"
												+ "6OmZCz557zw7FvzRBhqWSaw4RXIyt3LsjpRMmz3TK4zBMIUmga3tA/MLcK4jzm5Udl8LTYtIknbof7jymhNW7v75MESgc/x1bEGQ"
												+ "5wIDAQABoyEwHzAdBgNVHQ4EFgQUSez8amwbIp6UkbFj1iGxTa+Leg8wDQYJKoZIhvcNAQELBQADggEBAERV9UKxJYEp1/aHEmIh"
												+ "VjQeiXWAI6skY+PQJmB24K2tB5rsJnzxYZ023NV7ginI08NNIRdxFIas0SOnykSZ5yAHqHBI90enxq94ndaSMtG64LCUwVvYchr4"
												+ "Aco4kN5wmhqhN4IDmVs8V5qCJ6oaoSD/iXYS10u6zBmz5JKQjwsFkZioAtU4h1CHvbQAOm4mzmmNKcrA4pu4+RR3t28Xzx/AfI5w"
												+ "MnP7sW0ixm/y1eKbzJWskT+4tDLxfjNRQ4pXxUU85T037uQqP1dQfDHyZfkpeMY/CRJsUtxv9zzWi7LqVL1dT49yhI7SKuBK0d7M"
												+ "MpBf3GgY6gDOvEaBIqXO4bUUe+J6mnPYcBZv2i2NXCg5/p6XNA==";
	
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
			KeyStore keyStore = buildKeyStore(DUMMY_KEYSTORE, "JKS", "tpn123");
			return buildKeyManagers(keyStore, "tpn123");
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
