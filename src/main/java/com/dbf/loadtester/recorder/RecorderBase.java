package com.dbf.loadtester.recorder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.dbf.loadtester.common.action.HTTPAction;
import com.dbf.loadtester.common.action.converter.HTTPActionConverter;
import com.dbf.loadtester.common.json.JsonEncoder;
import com.dbf.loadtester.common.util.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Records incoming HTTP requests and saves them to disk
 * 
 * Note that this only support a single test plan at a time.
 *
 */
public class RecorderBase
{
	private static final Logger log = LoggerFactory.getLogger(RecorderBase.class);
		
	private static final String DEFAULT_DIRECTORY_PATH = Utils.isWindows() ? "C:\\temp\\httploadtester\\" : "/var/httploadtester/";
	private static final Set<String> ignoredExtensions = new HashSet<String>();
	
	private static final String PARAM_MAGIC_START = "MAGIC_START_PARAM";
	private static final String PARAM_MAGIC_STOP  = "MAGIC_STOP_PARAM";
	
	private long previousTime = -1L;
	
	private boolean running = false;
	
	private final String testPlanPrefix = getTestPlanFileNamePrefix();
	private final HTTPActionConverter actionConverter = new HTTPActionConverter();
			
	private Path testPlanDirectory = Paths.get(DEFAULT_DIRECTORY_PATH);
	private int  testPlanCount = 0;
	private Path testPlanPath = null;
	private BufferedWriter testPlanWriter = null;
	
	private Map<Pattern, String> pathSubs = new HashMap<Pattern, String>();
	private Map<Pattern, String> bodySubs = new HashMap<Pattern, String>();
	private Map<Pattern, String> querySubs = new HashMap<Pattern, String>();
	
	static
	{
		ignoredExtensions.add("png");
		ignoredExtensions.add("css");
		ignoredExtensions.add("woff");
		ignoredExtensions.add("jpg");
		ignoredExtensions.add("jpeg");
		ignoredExtensions.add("gif");
		ignoredExtensions.add("js");
		ignoredExtensions.add("map");
	}
	
	public ServletRequest handleHTTPRequest(HttpServletRequest httpRequest)
	{
		try
		{
			//It's crucial that the wrapper conversion be the very first step, before anything else consumes the input stream.
			//Otherwise, the act of reading the request parameters could actually consume the input stream in the case
			//of a form data request.
			RecorderHttpServletRequestWrapper httpRequestWrapper = new RecorderHttpServletRequestWrapper(httpRequest);
			
			//Don't capture control requests
			if(handleParams(httpRequest) || !running) return httpRequest;
			
			//Ignore static files, we typically don't need to load test these
			if(ignoreExtension(httpRequest.getPathInfo())) return httpRequestWrapper;
			
			Date currentDate = new Date();
		
			long timePassed = (previousTime < 0 ? 0 : currentDate.getTime() - previousTime);
			previousTime = currentDate.getTime() ;
			
			HTTPAction httpAction = actionConverter.convertServletRequestToHTTPAction(httpRequestWrapper, currentDate, timePassed);
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
			//Content length needs to be updated
			String content = Utils.applyRegexSubstitutions(httpAction.getContent(), bodySubs);
			httpAction.setContent(content);
			httpAction.setContentLength(content.length());
		}
	}	
	
	/**
	 * Handles various control params. Returns true if any param was found.
	 * Only supported as a query param, not a form param.
	 * 
	 * No longer recommended.
	 * 
	 * This is an alternate way to start and stop recording when you don't have JMX or REST access.
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
		log.info("Starting recording.");
		running = true;
		createNewTestPlan();
	}
	
	public synchronized void stopRecording() throws IOException
	{
		log.info("Stopping recording.");
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
			//Note: The individual write() and flush() methods are synchronized by
			//the Writer, so making a single call to write() is safe. Multiple calls
			//would need to be externally synchronized.
			log.info("Recording Action: " + action);
			testPlanWriter.write(JsonEncoder.toJson(action) + Utils.NEW_LINE);
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
	
	private boolean ignoreExtension(String path)
	{
		if(path == null || path.equals("")) return false;
		
		int extensionIndex = path.lastIndexOf('.');
		if (extensionIndex < 1) return false; 
		
		final String extension = path.substring(extensionIndex + 1).toLowerCase();
		return ignoredExtensions.contains(extension);
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
