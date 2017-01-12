package com.dbf.loadtester.recorder.proxy;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class RecorderProxyOptions
{
	private static final Options options = new Options();
	
	static
	{
		options.addOption("dir", true, "Test Plan directory");	
		options.addOption("port", true, "Listener HTTP port");
		options.addOption("fhost", true, "Proxy forwarding host");
		options.addOption("fhttpport", true, "Proxy forwarding HTTP port");
		options.addOption("fhttpsport", true, "Proxy forwarding HTTPS port");
		options.addOption("start", false, "Start recording immediately");
	}

	private String directory;
	private Integer port;
	private String forwardHost;
	private Integer forwardHTTPPort = 80;
	private Integer forwardHTTPSPort = 443;
	private boolean immediateStart;
	
	public RecorderProxyOptions(String[] args) throws IllegalArgumentException
	{
		CommandLine cmd = null;
		try
		{
			cmd = (new DefaultParser()).parse(options, args);
		}
		catch(ParseException e)
		{
			throw new IllegalArgumentException(e);
		}
		
		immediateStart = cmd.hasOption("start");
				
		directory = cmd.getOptionValue("dir");
		
		if(null != directory && !Files.isDirectory(Paths.get(directory)))
			throw new IllegalArgumentException("Invalid test plan directory.");
	
		try
		{
			String portString = cmd.getOptionValue("port");
			if(portString != null) port = Integer.parseInt(portString);
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("Invalid listener port number.");
		}
		
		if(null != port  && port < 0) throw new IllegalArgumentException("Invalid listener port number.");
		
		forwardHost = cmd.getOptionValue("fhost");
		
		if(null == forwardHost || forwardHost.isEmpty()) throw new IllegalArgumentException("Invalid proxy forwarding host.");
		
		try
		{
			String portString = cmd.getOptionValue("fhttpport");
			if(portString != null) forwardHTTPPort = Integer.parseInt(portString);		
    	}
    	catch (NumberFormatException e)
    	{
    		throw new IllegalArgumentException("Invalid proxy forwarding http port number.");
    	}
		
		if(forwardHTTPPort < 0) throw new IllegalArgumentException("Invalid proxy forwarding http port number."); 
		
		try
		{
			String portString = cmd.getOptionValue("fhttpsport");
			if(portString != null) forwardHTTPSPort = Integer.parseInt(portString);		
    	}
    	catch (NumberFormatException e)
    	{
    		throw new IllegalArgumentException("Invalid proxy forwarding https port number.");
    	}
		
		if(forwardHTTPSPort < 0) throw new IllegalArgumentException("Invalid proxy forwarding https port number."); 
			
	}
		
	public static void printOptions()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(" ", options);
	}

	public String getDirectory()
	{
		return directory;
	}

	public Integer getPort()
	{
		return port;
	}

	public String getForwardHost()
	{
		return forwardHost;
	}

	public Integer getForwardHTTPPort()
	{
		return forwardHTTPPort;
	}
	
	public Integer getForwardHTTPSPort()
	{
		return forwardHTTPSPort;
	}
	
	public boolean isImmediateStart()
	{
		return immediateStart;
	}
}
