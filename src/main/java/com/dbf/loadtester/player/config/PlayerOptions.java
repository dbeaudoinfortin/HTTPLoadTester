package com.dbf.loadtester.player.config;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.dbf.loadtester.common.action.HTTPAction;
import com.dbf.loadtester.common.json.JsonEncoder;
import com.dbf.loadtester.player.stats.PlayerStats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerOptions
{
	private static final Logger log = LoggerFactory.getLogger(PlayerOptions.class);
	
	private static final Options options = new Options();
	
	static
	{
		options.addOption("testPlanFile", true, "The absolute path to the Json test plan file. ");	
		options.addOption("threadCount", true, "The total number of threads to run. Each thread will repeat the test plan at least once and until MinRunTime is reached.");
		options.addOption("staggerTime", true, "The average time offset between the start of each subsequent thread (staggered start). Value in milliseconds.");
		options.addOption("minRunTime", true, "Minimum runtime ensures that no thread will terminate before the last thread finished at least 1 run. Value in seconds.");
		options.addOption("actionDelay", true, "Time between each action. Set to zero for no delay, set to -1 to use the test plan timings. Value in milliseconds.");
		options.addOption("host", true, "The target host.");
		options.addOption("httpPort", true, "Port to use for HTTP requests.");
		options.addOption("httpsPort", true, "Port to use for HTTPs requests.");
		options.addOption("cookieWhiteList", true, "List of comma-seperated Cookie names that will used from the test plan. All other cookies will be discarded.");
		options.addOption("pause", false, "Pause and wait for JMX/REST invocation to start.");
		options.addOption("keepAlive", false, "Keep Load Test Player alive after all threads have halted.");
		options.addOption("overrideHttps", false, "Override all HTTPs actions with HTTP");
		options.addOption("applyFixedSubs", false, "Apply fixed substitutions, such as <THREAD_ID>, in the test plan.");
		options.addOption("applyVariableSubs", false, "Retrieve and subsequently apply variable substitutions in the test plan.");
		options.addOption("restPort", true, "Port to use for REST API managment interface.");
		options.addOption("disableREST", false, "Disable the REST API managment interface.");
		options.addOption("disableJMX", false, "Disable the JMX managment interface.");
		options.addOption("shareConnections", false, "Share connections (sockets) accross threads for improved efficiency.");
	}
	
	private String host = Constants.DEFAULT_HOST;
	private int httpPort = Constants.DEFAULT_HTTP_PORT;
	private int httpsPort = Constants.DEFAULT_HTTPS_PORT;
	private int threadCount = Constants.DEFAULT_THREAD_COUNT;
	private long staggerTime = Constants.DEFAULT_STAGGER_TIME;
	private int actionDelay = Constants.DEFAULT_TIME_BETWEEN_ACTIONS;
	private boolean useFixedSubstitutions = false;
	private boolean useVariableSubstitutions = false;
	private long minRunTime = Constants.DEFAULT_MINIMUM_RUN_TIME;
	private int restPort = Constants.DEFAULT_PLAYER_REST_PORT;
	private File testPlanFile;
	private boolean overrideHttps = false;
	private boolean pauseOnStartup = false;
	private boolean keepAlive = false;
	private boolean disableREST = false;
	private boolean disableJMX = false;
	private boolean shareConnections = false;
	private List<HTTPAction> actions;
	private PlayerStats globalPlayerStats = new PlayerStats();
	private Collection<String> cookieWhiteList = new HashSet<String>();

	public PlayerOptions(){}

	public PlayerOptions(String[] args) throws IllegalArgumentException, IOException
	{
		loadFromArgs(args);
	}
	
	public void loadFromArgs(String[] args) throws IllegalArgumentException, IOException
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
		
		if(cmd.hasOption("pause"))
		{
			pauseOnStartup = true;
			log.info("Paused flag set, will await JMX/REST configuration before proceeding.");
		}
		
		if(cmd.hasOption("keepAlive"))
		{
			keepAlive = true;
			log.info("Keep Alive flag set, will prevent termination after threads have completed.");
		}
		
		if(cmd.hasOption("testPlanFile"))
		{
			testPlanFile = new File(cmd.getOptionValue("testPlanFile"));
			log.info("Using test plan file: " + testPlanFile.getAbsolutePath());
		}
		
		if(cmd.hasOption("overrideHttps"))
		{
			overrideHttps = true;
			log.info("HTTPS override enabled.");
		}
		
		if(cmd.hasOption("applyFixedSubs"))
		{
			useFixedSubstitutions = true;
			log.info("Fixed substitutions will be applied to the Test Plan.");
		}
		
		if(cmd.hasOption("shareConnections"))
		{
			shareConnections = true;
			log.info("Share Connections flag set, a single client and connection pool will be used across all threads of the load tester.");
		}
		
		if(cmd.hasOption("disableREST"))
		{
			disableREST = true;
			log.info("Disable REST flag set, REST management will not be availible.");
		}
		
		if(cmd.hasOption("disableJMX"))
		{
			disableJMX = true;
			log.info("Disable JMX flag set, JMX management will not be availible.");
		}
		
		if(cmd.hasOption("threadCount"))
		{
			threadCount = Integer.parseInt(cmd.getOptionValue("threadCount"));
			log.info("Using thread count: " + threadCount);
		}
		else
		{
			log.info("Using default thread count: " + Constants.DEFAULT_THREAD_COUNT);
		}
		
		if(cmd.hasOption("staggerTime"))
		{
			staggerTime = Long.parseLong(cmd.getOptionValue("staggerTime"));
			log.info("Using stagger time: " + String.format("%.2f", staggerTime /1000.0) + " seconds");
		}
		else
		{
			log.info("Using default stagger time: " + String.format("%.2f",Constants.DEFAULT_STAGGER_TIME /1000.0) + " seconds");
		}
		
		if(cmd.hasOption("actionDelay"))
			actionDelay = Integer.parseInt(cmd.getOptionValue("actionDelay"));	
		
		if (actionDelay < 0)
			log.info("Using test plan timings for action delay.");
		else
			log.info("Using action delay: " + actionDelay + " ms");
		
		
		if(cmd.hasOption("minRunTime"))
			minRunTime = Long.parseLong(cmd.getOptionValue("minRunTime")) * 1000;
		
		if(minRunTime < 0)
			log.info("Using default minimum run time, as calculated from test plan.");
		else
			log.info("Using minimum run time: " + String.format("%.2f", minRunTime/60000.0) + " minutes");
		
		if(cmd.hasOption("host"))
		{
			host = cmd.getOptionValue("host");
			log.info("Using host: " + host);
		}
		else
		{
			log.info("Using default host: " + Constants.DEFAULT_HOST);
		}
		
		if(cmd.hasOption("httpPort"))
		{
			httpPort = Integer.parseInt(cmd.getOptionValue("httpPort"));
			log.info("Using HTTP port: " + httpPort);
		}
		else
		{
			log.info("Using default HTTP port: " + Constants.DEFAULT_HTTP_PORT);
		}
		
		if(cmd.hasOption("httpsPort"))
		{
			httpsPort = Integer.parseInt(cmd.getOptionValue("httpsPort"));
			log.info("Using HTTPS port: " + httpsPort);
		}
		else
		{
			log.info("Using default HTTPS port: " + Constants.DEFAULT_HTTPS_PORT);
		}
		
		if(cmd.hasOption("restPort"))
		{
			restPort = Integer.parseInt(cmd.getOptionValue("restPort"));
			log.info("Using management REST API port: " + restPort);
		}
		else
		{
			log.info("Using default management REST API port: " + Constants.DEFAULT_PLAYER_REST_PORT);
		}
		
		
		if(cmd.hasOption("cookieWhiteList"))
		{
			String rawList = cmd.getOptionValue("cookieWhiteList");
			for (String cookieName : rawList.split(",")) cookieWhiteList.add(cookieName);
			log.info("Using the following Cookie White List: " + cookieWhiteList +". These cookies will be used from the test plan.");
		}
		else
		{
			log.info("Cookie White List disabled. All cookies from the test plan will be ignored.");
		}
	}
	
	private void validate() throws IllegalArgumentException, IOException
	{
		if(null == testPlanFile) throw new IllegalArgumentException("Missing test plan.");
		if(!testPlanFile.isFile()) throw new IllegalArgumentException("Unable to locate test plan at path '" + testPlanFile + "'.");
		if(threadCount < 1) throw new IllegalArgumentException("Invalid thread count.");
		if(staggerTime < 0) throw new IllegalArgumentException("Invalid stagger time.");		
		if(host == null || host.isEmpty()) throw new IllegalArgumentException("Invalid host.");	
		if(httpPort < 1) throw new IllegalArgumentException("Invalid HTTP port.");
		if(httpsPort < 1) throw new IllegalArgumentException("Invalid HTTPS port.");	
		if(restPort < 1) throw new IllegalArgumentException("Invalid management REST API port.");	
	}
	
	public void loadTestPlan() throws IOException, IllegalArgumentException
	{
		//Validate before loading the test plan
		validate();
		
		//Load the test plan. We need to determine the test plan length before continuing
		log.info("Loading test plan " + testPlanFile);
		actions = JsonEncoder.loadTestPlan(testPlanFile);
		if (actions.size() < 1) throw new IllegalArgumentException("Invalid test plan " + testPlanFile + ". Must contain at least one action.");

		long totalTestPlanTime = 0;
		for(HTTPAction action : actions)
			totalTestPlanTime += action.getTimePassed();

		//Minimum runtime ensures that no thread will terminate before the last thread finished at least 1 run.
		if(minRunTime < 0)
		{
			minRunTime = ((threadCount - 1) * staggerTime) + totalTestPlanTime;
			log.info("Using default minimum run time: " + String.format("%.2f",minRunTime/60000.0) + " minutes");
		}
		
		//Clear out all existing stats
		globalPlayerStats = new PlayerStats(actions);
		
		log.info("Test plan loaded. Total duration approx. " + String.format("%.2f",totalTestPlanTime/60000.0) + " minutes.");
	}
	
	public static void printOptions()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(" ", options);
	}


	public int getThreadCount()
	{
		return threadCount;
	}

	public void setThreadCount(int threadCount)
	{
		this.threadCount = threadCount;
	}

	public long getStaggerTime()
	{
		return staggerTime;
	}

	public void setStaggerTime(long staggerTime)
	{
		this.staggerTime = staggerTime;
	}

	public long getMinRunTime()
	{
		return minRunTime;
	}

	public void setMinRunTime(long minRunTime)
	{
		this.minRunTime = minRunTime;
	}

	public File getTestPlanFile()
	{
		return testPlanFile;
	}

	public void setTestPlanFile(File testPlanFile)
	{
		this.testPlanFile = testPlanFile;
	}

	public List<HTTPAction> getActions()
	{
		return actions;
	}

	public void setActions(List<HTTPAction> actions)
	{
		this.actions = actions;
	}
	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public int getHttpPort()
	{
		return httpPort;
	}

	public void setHttpPort(int httpPort)
	{
		this.httpPort = httpPort;
	}

	public int getHttpsPort()
	{
		return httpsPort;
	}

	public void setHttpsPort(int httpsPort)
	{
		this.httpsPort = httpsPort;
	}

	public int getActionDelay()
	{
		return actionDelay;
	}

	public void setActionDelay(int actionDelay)
	{
		this.actionDelay = actionDelay;
	}

	public boolean isUseFixedSubstitutions()
	{
		return useFixedSubstitutions;
}

	public void setUseFixedSubstitutions(boolean useFixedSubstitutions)
	{
		this.useFixedSubstitutions = useFixedSubstitutions;
	}

	public boolean isOverrideHttps()
	{
		return overrideHttps;
	}

	public void setOverrideHttps(boolean overrideHttps)
	{
		this.overrideHttps = overrideHttps;
	}

	public boolean isPauseOnStartup()
	{
		return pauseOnStartup;
	}

	public boolean isKeepAlive()
	{
		return keepAlive;
	}

	public PlayerStats getGlobalPlayerStats()
	{
		return globalPlayerStats;
	}
	
	public int getRestPort()
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

	public boolean isShareConnections()
	{
		return shareConnections;
	}

	public void setShareConnections(boolean shareConnections)
	{
		this.shareConnections = shareConnections;
	}
	
	public Collection<String> getCookieWhiteList()
	{
		return cookieWhiteList;
	}

	public void setCookieWhiteList(Collection<String> cookieWhiteList)
	{
		this.cookieWhiteList = cookieWhiteList;
	}

	public boolean isUseVariableSubstitutions()
	{
		return useVariableSubstitutions;
	}
}
