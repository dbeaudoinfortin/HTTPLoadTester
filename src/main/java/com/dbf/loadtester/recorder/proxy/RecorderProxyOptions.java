package com.dbf.loadtester.recorder.proxy;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.binary.Base64;

import com.dbf.loadtester.common.action.substitutions.FixedSubstitution;
import com.dbf.loadtester.common.json.JsonEncoder;
import com.dbf.loadtester.player.config.Constants;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class RecorderProxyOptions
{
	private static final Options options = new Options();
	
	static
	{
		options.addOption("dir", true, "The test plan directory. Recorded test plans will be saved in this directory.");	
		options.addOption("httpPort", true, "Listener HTTP port.");
		options.addOption("httpsPort", true, "Listener HTTPS port.");
		options.addOption("fHost", true, "Proxy forwarding host.");
		options.addOption("fHttpPort", true, "Proxy forwarding HTTP port.");
		options.addOption("fHttpsPort", true, "Proxy forwarding HTTPS port.");
		options.addOption("start", false, "Start recording immediately.");
		options.addOption("fixedSubs", true, "Fixed substitutions in Base64 encoded Json format.");
		options.addOption("restPort", true, "Port to use for REST API managment interface.");
		options.addOption("disableREST", false, "Disable the REST API managment interface.");
		options.addOption("disableJMX", false, "Disable the JMX managment interface.");
		options.addOption("overrideHostHeader", false, "Overrides the 'Host' header on every request to match the forwarding host.");
		options.addOption("rewriteUrls", false, "Inspects the response of every HTTP request and attempts to rewrite URLs to point back to the proxy.");
	}

	private String directory;
	private Integer httpPort;
	private Integer httpsPort;
	private String forwardHost;
	private Integer forwardHTTPPort = Constants.DEFAULT_FORWARD_HTTP_PORT;
	private Integer forwardHTTPSPort = Constants.DEFAULT_FORWARD_HTTPS_PORT;
	private boolean immediateStart;
	private Integer restPort = Constants.DEFAULT_RECORDER_REST_PORT;
	private boolean disableREST = false;
	private boolean disableJMX = false;
	private boolean overrideHostHeader = true;
	private boolean rewriteUrls = false;
	
	private List<FixedSubstitution> fixedSubs;
	
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
		
		disableREST = cmd.hasOption("disableREST");
		disableJMX = cmd.hasOption("disableJMX");
		overrideHostHeader = cmd.hasOption("overrideHostHeader");
		rewriteUrls = cmd.hasOption("rewriteUrls");
				
		directory = cmd.getOptionValue("dir");
		
		if(null != directory && !Files.isDirectory(Paths.get(directory)))
			throw new IllegalArgumentException("Invalid test plan directory.");
	
		try
		{
			String portString = cmd.getOptionValue("restPort");
			if(portString != null) restPort = Integer.parseInt(portString);
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("Invalid REST API managment port number.");
		}
		
		if(null != restPort && restPort < 0) throw new IllegalArgumentException("Invalid REST API managment port number.");
		
		try
		{
			String portString = cmd.getOptionValue("httpPort");
			if(portString != null) httpPort = Integer.parseInt(portString);
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("Invalid listener HTTP port number.");
		}
		
		if(null != httpPort  && httpPort < 0) throw new IllegalArgumentException("Invalid listener HTTP port number.");
		
		try
		{
			String portString = cmd.getOptionValue("httpsPort");
			if(portString != null) httpsPort = Integer.parseInt(portString);
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("Invalid listener HTTPS port number.");
		}
		
		if(null != httpsPort  && httpsPort < 0) throw new IllegalArgumentException("Invalid listener HTTPS port number.");
		
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
		
		
		if(cmd.hasOption("fixedSubs"))
		{
			try
			{
				setFixedSubs(convertArgToSubList(cmd.getOptionValue("fixedSubs")));
			}
			catch (Exception e)
	    	{
				throw new IllegalArgumentException("Failed to convert Base64-encoded 'fixedSubs': " + e.getMessage(), e); 
	    	}
		}	
	}
	
	private static List<FixedSubstitution> convertArgToSubList(String arg)
	{
		String base64Decoded = new String(Base64.decodeBase64(arg));
		try
		{
			
			return JsonEncoder.fromJson(base64Decoded, (new TypeToken<List<FixedSubstitution>>(){}).getType());
		}
		catch (JsonSyntaxException e)
		{
			throw new IllegalArgumentException("Failed to parse Json value '" + base64Decoded + "'.",e);
		}
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

	public Integer getHttpPort()
	{
		return httpPort;
	}
	
	public Integer getHttpsPort()
	{
		return httpsPort;
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

	public Integer getRestPort()
	{
		return restPort;
	}

	public boolean isDisableREST()
	{
		return disableREST;
	}

	public boolean isDisableJMX()
	{
		return disableJMX;
	}

	public boolean isOverrideHostHeader()
	{
		return overrideHostHeader;
	}

	public void setOverrideHostHeader(boolean overrideHostHeader)
	{
		this.overrideHostHeader = overrideHostHeader;
	}

	public boolean isRewriteUrls()
	{
		return rewriteUrls;
	}

	public void setRewriteUrls(boolean rewriteUrls)
	{
		this.rewriteUrls = rewriteUrls;
	}

	public List<FixedSubstitution> getFixedSubs()
	{
		return fixedSubs;
	}

	public void setFixedSubs(List<FixedSubstitution> fixedSubs)
	{
		this.fixedSubs = fixedSubs;
	}
}
