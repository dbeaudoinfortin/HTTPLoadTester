package com.dbf.loadtester.player.config;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.dbf.loadtester.HTTPAction;
import com.dbf.loadtester.json.JsonEncoder;

public class PlayerConfiguration
{
	private static final Logger log = Logger.getLogger(PlayerConfiguration.class);
	
	private String host = Constants.DEFAULT_HOST;
	private String httpPort = Constants.DEFAULT_HTTP_PORT;
	private String httpsPort = Constants.DEFAULT_HTTPS_PORT;
	private int threadCount = Constants.DEFAULT_THREAD_COUNT;
	private long staggerTime = Constants.DEFAULT_STAGGER_TIME;
	private int actionDelay = Constants.DEFAULT_TIME_BETWEEN_ACTIONS;
	private long minRunTime;
	private File testPlanFile;
	List<HTTPAction> actions;

	public PlayerConfiguration(){}

	public PlayerConfiguration(String[] args) throws IllegalArgumentException, IOException
	{
		loadFromArgs(args);
	}
	
	/**
	 * Expects the following argument in order.
	 * 
	 * TestPlanFilePath		The absolute path to the Json test plan file. 
	 * ThreadCount			The total number of threads to run. Each thread will repeat the test plan at least once and until MinRunTime is reached.
	 * StaggerTime			The average time offset between the start of each subsequent thread (staggered start).
	 * MinRunTime			Minimum runtime ensures that no thread will terminate before the last thread finished at least 1 run.
	 * ActionDelay			Time between each action. Set to -1 to use the test plan timings. 
	 * Host					The target host.
	 * HttpPort				Port to use for HTTP requests.
	 * HttpsPort			Port to use for HTTPs requests.
	 * 
	 * @param args
	 * @throws IllegalArgumentException
	 * @throws IOException 
	 */
	public void loadFromArgs(String[] args) throws IllegalArgumentException, IOException
	{
		if(!(args.length == 1 || args.length == 8)) throw new IllegalArgumentException("Incorrect number of arguments.");
		
		//Read from arguments
		testPlanFile = new File(args[0]);
		if(!testPlanFile.isFile()) throw new IllegalArgumentException("Unable to locate test plan at path '" + args[0] + "'.");

		if(args.length > 1)
		{
			threadCount = Integer.parseInt(args[1]);
			log.info("Using thread count: " + threadCount);
			
			staggerTime = Long.parseLong(args[2]);
			log.info("Using stagger time: " + String.format("%.2f",staggerTime /1000.0) + " seconds");
			
			actionDelay = Integer.parseInt(args[4]);
			if (actionDelay < 0)
				log.info("Using test plan timings for action delay.");
			else
				log.info("Using action delay: " + actionDelay + " ms");
			
			host = args[5];
			log.info("Using host: " + host);
			
			httpPort = args[6];
			log.info("Using HTTP port: " + httpPort);
			
			httpsPort = args[7];
			log.info("Using default HTTPs port: " + httpsPort);
		}
		else
		{
			log.info("Using default thread count: " + Constants.DEFAULT_THREAD_COUNT);
			log.info("Using default stagger time: " + String.format("%.2f",Constants.DEFAULT_STAGGER_TIME /1000.0) + " seconds");
			log.info("Using default host: " + Constants.DEFAULT_HOST);
			log.info("Using default HTTP port: " + Constants.DEFAULT_HTTP_PORT);
			log.info("Using default HTTPs port: " + Constants.DEFAULT_HTTPS_PORT);
		}	
		
		//Load the test plan. We need to determine the test plan length before continuing
		actions = JsonEncoder.loadTestPlan(testPlanFile);
		if (actions.size() < 1) throw new IllegalArgumentException("Invalid test plan" + args[0] + ". Must contain at least one action.");

		long totalTestPlanTime = 0;
		for(HTTPAction action : actions)
			totalTestPlanTime += action.getTimePassed();

		//Minimum runtime ensures that no thread will terminate before the last thread finished at least 1 run.
		minRunTime = ((threadCount - 1) * staggerTime) + totalTestPlanTime;
		if(args.length > 1)
		{
			minRunTime = Long.parseLong(args[3]);
			log.info("Using minimum run time: " + String.format("%.2f",minRunTime/60000.0) + " minutes");
		}
		else
			log.info("Using default minimum run time: " + String.format("%.2f",minRunTime/60000.0) + " minutes");
		
		log.info("Test plan loaded. Total duration approx. " + String.format("%.2f",totalTestPlanTime/60000.0) + " minutes.");
	}
	
	public static String getUsage()
	{
		return "Usage 1: TestPlanFilePath\r\n"
				+ "Usage 2: TestPlanFilePath ThreadCount StaggerTime MinRunTime Host HttpPort HttpsPort\r\n\r\n"
				+ "TestPlanFilePath    The absolute path to the Json test plan file.\r\n"
				+ "ThreadCount         The total number of threads to run. Each thread will repeat the test plan at least once and until MinRunTime is reached.\r\n"
				+ "StaggerTime         The average time offset between the start of each subsequent thread (staggered start). In milliseconds.\r\n"
				+ "MinRunTime          Minimum runtime ensures that no thread will terminate before the last thread finished at least 1 run. In milliseconds.\r\n"
				+ "ActionDelay         Time between each action. Set to -1 to use the test plan timings. In milliseconds.\r\n"
				+ "Host                The target host.\r\n"
				+ "HttpPort            Port to use for HTTP requests.\r\n"
				+ "HttpsPort           Port to use for HTTPs requests.";
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

	public String getHttpPort()
	{
		return httpPort;
	}

	public void setHttpPort(String httpPort)
	{
		this.httpPort = httpPort;
	}

	public String getHttpsPort()
	{
		return httpsPort;
	}

	public void setHttpsPort(String httpsPort)
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
}