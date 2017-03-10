package com.dbf.loadtester.recorder.proxy;

import org.apache.log4j.Logger;

import com.dbf.loadtester.recorder.proxy.server.RecorderProxyServer;

public class RecorderProxy
{
	private static final Logger log = Logger.getLogger(RecorderProxy.class);
	
	public static void main(String[] args)
	{
		RecorderProxyOptions options = null;
    	try
    	{
    		options = new RecorderProxyOptions(args);
    	}
    	catch(IllegalArgumentException e)
    	{
    		log.fatal("Invalid CMD line Arguments: " + e.getMessage());
    		RecorderProxyOptions.printOptions();
    		System.exit(1);
    	}
		
    	RecorderProxyServer.initializeServer(options);
	}
	
	
}
