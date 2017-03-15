package com.dbf.loadtester.recorder.proxy;

import com.dbf.loadtester.recorder.proxy.server.RecorderProxyServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecorderProxy
{
	private static final Logger log = LoggerFactory.getLogger(RecorderProxy.class);
	
	public static void main(String[] args)
	{
		RecorderProxyOptions options = null;
    	try
    	{
    		options = new RecorderProxyOptions(args);
    	}
    	catch(IllegalArgumentException e)
    	{
    		log.error("Invalid CMD line Arguments: " + e.getMessage());
    		RecorderProxyOptions.printOptions();
    		System.exit(1);
    	}
		
    	RecorderProxyServer.initializeServer(options);
	}
	
	
}
