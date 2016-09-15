package com.dbf.loadtester.recorder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.dbf.loadtester.action.HTTPAction;
import com.dbf.loadtester.action.HTTPConverter;
import com.dbf.loadtester.json.JsonEncoder;
import com.dbf.loadtester.util.Utils;

/**
 * Records incoming HTTP requests and saves them to disk
 * 
 * Note that this only support a single test case at a time.
 *
 */
public class RecorderCommon
{
	private static final Logger log = Logger.getLogger(RecorderCommon.class);
		
	private static final String DEFAULT_DIRECTORY_PATH = Utils.isWindows() ? "C:\\temp\\httploadtester\\" : "/var/httploadtester/";
	
	private static final String PARAM_MAGIC_START = "MAGIC_START_PARAM";
	private static final String PARAM_MAGIC_STOP  = "MAGIC_STOP_PARAM";
	
	private long previousTime = -1L;
	
	private boolean running = false;
	
	private final String testPlanPrefix = getTestPlanFileNamePrefix();
	private Path testPlanDirectory = Paths.get(DEFAULT_DIRECTORY_PATH);
	private int  testPlanCount = 0;
	private Path testPlanPath = null;
	private BufferedWriter testPlanWriter = null;
	
	public ServletRequest handleHTTPRequest(HttpServletRequest httpRequest)
	{
		try
		{
			//Don't capture control requests
			if(handleParams(httpRequest) || !running) return httpRequest;
			
			Date currentDate = new Date();
			RecorderHttpServletRequestWrapper httpRequestWrapper = new RecorderHttpServletRequestWrapper(httpRequest);
		
			long timePassed = (previousTime < 0 ? 0 : currentDate.getTime() - previousTime);
			previousTime = currentDate.getTime() ;
	      	saveHTTPAction(HTTPConverter.convertServletRequestToHTTPAction(httpRequestWrapper, currentDate, timePassed));
	      	return httpRequestWrapper;
		}
		catch(Exception e)
		{
			log.error("Unhandled Exception",e);
		}
		
		return httpRequest;
	}
	
	/**
	 * Handles various control params. Returns true if any param was found.
	 * @throws IOException 
	 */
	private boolean handleParams(HttpServletRequest httpRequest) throws IOException
	{
		if (httpRequest.getParameter(PARAM_MAGIC_START) != null)
		{
			running = true;
			createNewTestPlan();
			return true;
		}
		else if(httpRequest.getParameter(PARAM_MAGIC_STOP) != null)
		{
			running = false;
			closeTestPlan();
			return true;
		}
		return false;
	}
	
	private void closeTestPlan() throws IOException
	{
		if (testPlanWriter != null)
		{
			log.info("Closing off test plan " + testPlanPath);
			testPlanWriter.close();
			testPlanWriter = null;
		}
	}
	
	private void createNewTestPlan() throws IOException
	{
		if (testPlanWriter == null)
		{
			testPlanCount +=1;
			previousTime = -1L;
			testPlanPath = testPlanDirectory.resolve(testPlanPrefix + testPlanCount + ".json");
			log.info("Creating new test plan" + testPlanPath);
			testPlanWriter = new BufferedWriter(new FileWriter(testPlanPath.toFile(), true));
		}
	}
	
	private void saveHTTPAction(HTTPAction action) throws IOException
	{
		if(null != testPlanWriter)
		{
			testPlanWriter.write(JsonEncoder.toJson(action));
			testPlanWriter.newLine();
			testPlanWriter.flush();
		}
	}
	
	private String getTestPlanFileNamePrefix()
	{
		return "TestPlan-"
				+ (new SimpleDateFormat("yyyy-MM-dd")).format(new Date())
				+ "-" + (new Random()).nextInt(Integer.MAX_VALUE) + "-";
	}

	public Path getTestPlanDirectory() {
		return testPlanDirectory;
	}

	public void setTestPlanDirectory(Path testPlanDirectory) {
		this.testPlanDirectory = testPlanDirectory;
	}

	public boolean isRunning() {
		return running;
	}
}
