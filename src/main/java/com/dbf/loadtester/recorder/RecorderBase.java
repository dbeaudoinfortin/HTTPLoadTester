package com.dbf.loadtester.recorder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.dbf.loadtester.common.action.HTTPAction;
import com.dbf.loadtester.common.action.HTTPConverter;
import com.dbf.loadtester.common.json.JsonEncoder;
import com.dbf.loadtester.common.util.Utils;

/**
 * Records incoming HTTP requests and saves them to disk
 * 
 * Note that this only support a single test case at a time.
 *
 */
public class RecorderBase
{
	private static final Logger log = Logger.getLogger(RecorderBase.class);
		
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
	
	private Map<Pattern, String> pathSubs = new HashMap<Pattern, String>();
	private Map<Pattern, String> bodySubs = new HashMap<Pattern, String>();
	private Map<Pattern, String> querySubs = new HashMap<Pattern, String>();
	
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
			
			HTTPAction httpAction = HTTPConverter.convertServletRequestToHTTPAction(httpRequestWrapper, currentDate, timePassed);
			applySubstitutions(httpAction);
	      	saveHTTPAction(httpAction);
	      	
	      	return httpRequestWrapper;
		}
		catch(Exception e)
		{
			log.error("Unhandled Exception",e);
		}
		
		return httpRequest;
	}
	
	private void applySubstitutions(HTTPAction httpAction)
	{
		if(pathSubs.size() > 0 && httpAction.getPath() != null)
			httpAction.setPath(Utils.applyRegexSubstitutions(httpAction.getPath(), pathSubs));
		
		if(querySubs.size() > 0 && httpAction.getQueryString() != null)
    		httpAction.setQueryString(Utils.applyRegexSubstitutions(httpAction.getQueryString(), querySubs));
		
		//Request Body only applies to PUT and POST 
		if(bodySubs.size() > 0 && httpAction.getContent() != null && ("PUT".equals(httpAction.getMethod()) || "POST".equals(httpAction.getMethod())))
		{
			String content = Utils.applyRegexSubstitutions(httpAction.getContent(), bodySubs);
			httpAction.setContent(content);
			httpAction.setContentLength(content.length());
		}
	}	
	
	/**
	 * Handles various control params. Returns true if any param was found.
	 * This is an alternate way to start and stop recording when you don't have JMX access
	 * 
	 * @throws IOException 
	 */
	private boolean handleParams(HttpServletRequest httpRequest) throws IOException
	{
		if (httpRequest.getParameter(PARAM_MAGIC_START) != null)
		{
			startRecording();
			return true;
		}
		else if(httpRequest.getParameter(PARAM_MAGIC_STOP) != null)
		{
			stopRecording();
			return true;
		}
		return false;
	}
	
	public synchronized void startRecording() throws IOException
	{
		running = true;
		createNewTestPlan();
	}
	
	public synchronized void stopRecording() throws IOException
	{
		running = false;
		closeTestPlan();
	}
	
	private void closeTestPlan() throws IOException
	{
		if (testPlanWriter != null)
		{
			log.info("Closing off test plan: " + testPlanPath);
			testPlanWriter.close();
			testPlanWriter = null;
			testPlanPath = null;
		}
	}
	
	private void createNewTestPlan() throws IOException
	{
		if (testPlanWriter == null)
		{
			testPlanCount +=1;
			previousTime = -1L;
			
			if(null == testPlanPath) testPlanPath = testPlanDirectory.resolve(testPlanPrefix + testPlanCount + ".json");
			
			log.info("Creating new test plan: " + testPlanPath);
			testPlanWriter = new BufferedWriter(new FileWriter(testPlanPath.toFile(), true));
		}
	}
	
	private void saveHTTPAction(HTTPAction action) throws IOException
	{
		if(null != testPlanWriter)
		{
			log.info("Recording Action: " + action.getMethod() + " " + action.getPath() + (action.getQueryString() == null ? "" : action.getQueryString()));
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
	
	private Map<Pattern, String> convertToSubstitutionMap(Map<String, String> map) 
	{
		Map<Pattern, String> returnMap = new HashMap<Pattern, String>();
		for(Entry<String, String> entry : map.entrySet())
		{
			//Because of runtime type-erasure during Json conversion, we need the toString().
			returnMap.put(Pattern.compile(entry.getKey()), entry.getValue().toString());
		}
		return returnMap;
	}
	
	private Map<String, String> convertFromSubstitutionMap(Map<Pattern, String> map) 
	{
		Map<String, String> returnMap = new HashMap<String, String>();
		for(Entry<Pattern, String> entry : map.entrySet())
		{
			returnMap.put(entry.getKey().pattern(), entry.getValue());
		}
		return returnMap;
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

	public Path getTestPlanPath()
	{
		return testPlanPath;
	}

	public void setTestPlanPath(Path testPlanPath)
	{
		this.testPlanPath = testPlanPath;
	}
	
	public Map<String, String> getPathSubs()
	{
		return convertFromSubstitutionMap(pathSubs);
	}

	public void setPathSubs(Map<String, String> pathSubs)
	{
		this.pathSubs = convertToSubstitutionMap(pathSubs);
	}

	public Map<String, String> getBodySubs()
	{
		return convertFromSubstitutionMap(bodySubs);
	}

	public void setBodySubs(Map<String, String> bodySubs)
	{
		this.bodySubs = convertToSubstitutionMap(bodySubs);
	}

	public Map<String, String> getQuerySubs()
	{
		return convertFromSubstitutionMap(querySubs);
	}

	public void setQuerySubs(Map<String, String> querySubs)
	{
		this.querySubs = convertToSubstitutionMap(querySubs);
	}
}
