package com.dbf.loadtester.recorder.proxy;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.binary.Base64;

import com.dbf.loadtester.common.json.JsonEncoder;

public class RecorderProxyOptions
{
	private static final Options options = new Options();
	
	static
	{
		options.addOption("dir", true, "Test Plan directory");	
		options.addOption("port", true, "Listener HTTP port");
		options.addOption("fHost", true, "Proxy forwarding host");
		options.addOption("fHttpPort", true, "Proxy forwarding HTTP port");
		options.addOption("fHttpsPort", true, "Proxy forwarding HTTPS port");
		options.addOption("start", false, "Start recording immediately");
		options.addOption("pathSubs", true, "Path Substitutions in Base64 encoded Json format");
		options.addOption("querySubs", true, "Query Substitutions in Base64 encoded Json format");
		options.addOption("bodySubs", true, "Body Substitutions in Base64 encoded Json format");
	}

	private String directory;
	private Integer port;
	private String forwardHost;
	private Integer forwardHTTPPort = 80;
	private Integer forwardHTTPSPort = 443;
	private boolean immediateStart;
	
	private Map<String, String> pathSubs;
	private Map<String, String> querySubs;
	private Map<String, String> bodySubs;
	
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
		
		forwardHost = cmd.getOptionValue("fHost");
		
		if(null == forwardHost || forwardHost.isEmpty()) throw new IllegalArgumentException("Invalid proxy forwarding host.");
		
		try
		{
			String portString = cmd.getOptionValue("fHttpPort");
			if(portString != null) forwardHTTPPort = Integer.parseInt(portString);		
    	}
    	catch (NumberFormatException e)
    	{
    		throw new IllegalArgumentException("Invalid proxy forwarding http port number.");
    	}
		
		if(forwardHTTPPort < 0) throw new IllegalArgumentException("Invalid proxy forwarding http port number."); 
		
		try
		{
			String portString = cmd.getOptionValue("fHttpsPort");
			if(portString != null) forwardHTTPSPort = Integer.parseInt(portString);		
    	}
    	catch (NumberFormatException e)
    	{
    		throw new IllegalArgumentException("Invalid proxy forwarding https port number.");
    	}
		
		if(forwardHTTPSPort < 0) throw new IllegalArgumentException("Invalid proxy forwarding https port number."); 
		
		
		if(cmd.hasOption("pathSubs"))
		{
			try
			{
				pathSubs = convertArgToMap(cmd.getOptionValue("pathSubs"));
			}
			catch (Exception e)
	    	{
				throw new IllegalArgumentException("Failed to convert Base64-encoded 'pathSubs' arg to Map.", e); 
	    	}
		}
		
		if(cmd.hasOption("querySubs"))
		{
			try
			{
				querySubs = convertArgToMap(cmd.getOptionValue("querySubs"));
			}
			catch (Exception e)
	    	{
				throw new IllegalArgumentException("Failed to convert Base64-encoded 'querySubs' arg to Map.", e); 
	    	}
		}
		
		if(cmd.hasOption("bodySubs"))
		{
			try
			{
				bodySubs = convertArgToMap(cmd.getOptionValue("bodySubs"));
			}
			catch (Exception e)
	    	{
				throw new IllegalArgumentException("Failed to convert Base64-encoded 'querySubs' arg to Map.", e); 
	    	}
		}
			
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, String> convertArgToMap(String arg)
	{
		return JsonEncoder.fromJson(new String(Base64.decodeBase64(arg)), HashMap.class);
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

	public Map<String, String> getPathSubs()
	{
		return pathSubs;
	}

	public void setPathSubs(Map<String, String> pathSubs)
	{
		this.pathSubs = pathSubs;
	}

	public Map<String, String> getQuerySubs()
	{
		return querySubs;
	}

	public void setQuerySubs(Map<String, String> querySubs)
	{
		this.querySubs = querySubs;
	}

	public Map<String, String> getBodySubs()
	{
		return bodySubs;
	}

	public void setBodySubs(Map<String, String> bodySubs)
	{
		this.bodySubs = bodySubs;
	}
}
