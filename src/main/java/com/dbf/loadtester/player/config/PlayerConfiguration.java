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
		if(!(args.length == 1 || args.length == 7)) throw new IllegalArgumentException("Incorrect number of arguments.");
		
		//Read from arguments
		testPlanFile = new File(args[0]);
		if(!testPlanFile.isFile()) throw new IllegalArgumentException("Unable to locate test plan at path '" + args[0] + "'.");

		if(args.length > 1)
			threadCount = Integer.parseInt(args[1]);
		else
			log.info("Using default thread count:" + Constants.DEFAULT_THREAD_COUNT);
		
		if(args.length > 1)
			staggerTime = Long.parseLong(args[2]);
		else
			log.info("Using default stagger time:" + Constants.DEFAULT_STAGGER_TIME /1000.0 + " seconds");
		
		if(args.length > 1)
			host = args[4];
		else
			log.info("Using default host:" + Constants.DEFAULT_HOST);
		
		if(args.length > 1)
			httpPort = args[5];
		else
			log.info("Using default HTTP port:" + Constants.DEFAULT_HTTP_PORT);
		
		if(args.length > 1)
			httpsPort = args[6];
		else
			log.info("Using default HTTPs port:" + Constants.DEFAULT_HTTPS_PORT);
		
		//Load the test plan. We need to determine the test plan length before continuing
		actions = JsonEncoder.loadTestPlan(testPlanFile);
		if (actions.size() < 1) throw new IllegalArgumentException("Invalid test plan" + args[0] + ". Must contain at least one action.");

		long totalTestPlanTime = 0;
		for(HTTPAction action : actions)
			totalTestPlanTime += action.getTimePassed();

		//Minimum runtime ensures that no thread will terminate before the last thread finished at least 1 run.
		minRunTime = ((threadCount - 1) * staggerTime) + (totalTestPlanTime > 100 ? totalTestPlanTime - 100 : totalTestPlanTime);
		if(args.length > 1)
			minRunTime = Long.parseLong(args[3]);
		else
			log.info("Using default minimum run time:" + minRunTime/60000.0 + " minutes");
		
		log.info("Test plan loaded. Total duration approx. " + totalTestPlanTime/60000.0 + " minutes.");
	}
	
	public static String getUsage()
	{
		return "Usage 1: ThreadCount StaggerTime MinRunTime Host HttpPort HttpsPort\r\n"
				+ "Usage 2: TestPlanFilePath ThreadCount StaggerTime MinRunTime Host HttpPort HttpsPort\r\n\r\n"
				+ "TestPlanFilePath		The absolute path to the Json test plan file.\r\n"
				+ "ThreadCount			The total number of threads to run. Each thread will repeat the test plan at least once and until MinRunTime is reached.\r\n"
				+ "StaggerTime			The average time offset between the start of each subsequent thread (staggered start).\r\n"
				+ "MinRunTime			Minimum runtime ensures that no thread will terminate before the last thread finished at least 1 run.\r\n"
				+ "Host					The target host.\r\n"
				+ "HttpPort				Port to use for HTTP requests.\r\n"
				+ "HttpsPort			Port to use for HTTPs requests.";
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
}
