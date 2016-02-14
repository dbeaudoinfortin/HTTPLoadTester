package com.dbf.loadtester.recorder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import com.dbf.loadtester.HTTPAction;
import com.dbf.loadtester.HTTPActionConverter;
import com.dbf.loadtester.util.Utils;
import com.google.gson.Gson;

/**
 * Records incoming HTTP requests and saves them to disk
 * 
 * Note that this only support a single test case at a time.
 *
 */
public class RecorderFilter implements Filter
{
	private static final Logger log = Logger.getLogger(RecorderFilter.class);
	private static final Gson gson = new Gson();
	
	private static final String DEFAULT_DIRECTORY_PATH = Utils.isWindows() ? "C:\\temp\\httploadtester\\" : "/var/httploadtester/";
	private static final String PARAM_DIRECTORY_PATH = "TestPlanDirectory";
	
	private static final String PARAM_MAGIC_START = "MAGIC_START_PARAM";
	private static final String PARAM_MAGIC_STOP  = "MAGIC_STOP_PARAM";
	
	private long previousTime = -1L;
	
	private boolean running = false;
	
	private Path testPlanDirectory;
	private int testPlanCount = 0;
	private BufferedWriter testPlanWriter = null;

	public void init(FilterConfig filterConfig) throws ServletException 
	{
		String testPlanDirectory = filterConfig.getInitParameter(PARAM_DIRECTORY_PATH);
		if(null == testPlanDirectory || testPlanDirectory.isEmpty()) testPlanDirectory = DEFAULT_DIRECTORY_PATH;
		this.testPlanDirectory = Paths.get(testPlanDirectory);
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException 
    {	
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		
		//Don't capture control requests
		if(handleParams(httpRequest) || !running)
		{
			chain.doFilter(httpRequest, response);
			return;
		}
		
		Date currentDate = new Date();
		RecorderRequestWrapper httpRequestWrapper = new RecorderRequestWrapper(httpRequest);
		
		try
		{
			long timePassed = (previousTime < 0 ? 0 : currentDate.getTime() - previousTime);
			previousTime = currentDate.getTime() ;
	      	saveHTTPAction(HTTPActionConverter.convertHTTPRequest(httpRequestWrapper, timePassed));
		}
		catch(Exception e)
		{
			log.error("Unhandled Exception",e);
		}
      	
      	chain.doFilter(httpRequest, response);
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
			testPlanWriter.close();
			testPlanWriter = null;
		}
	}
	
	private void createNewTestPlan() throws IOException
	{
		if (testPlanWriter == null)
		{
			testPlanCount +=1;
			
			Path testPlanPath = testPlanDirectory.resolve("TestPlan-" + testPlanCount + ".json");
			testPlanWriter = new BufferedWriter(new FileWriter(testPlanPath.toFile(), true));
		}
	}
	
	private void saveHTTPAction(HTTPAction action) throws IOException
	{
		testPlanWriter.write(gson.toJson(action));
		testPlanWriter.newLine();
		testPlanWriter.flush();
	}

	public void destroy(){
	}
}
