package com.dbf.loadtester.common.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class AcceptAnythingHostnameVerifier implements HostnameVerifier
{
	@Override
	public boolean verify(String arg0, SSLSession arg1)
	{
		//It's all good, baby, baby!
		return true;
	}

}
