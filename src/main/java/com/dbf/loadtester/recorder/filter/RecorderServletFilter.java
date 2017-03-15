package com.dbf.loadtester.recorder.filter;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.dbf.loadtester.recorder.RecorderBase;
import com.dbf.loadtester.recorder.management.RecorderManager;
import com.dbf.loadtester.recorder.management.RecorderManagerMBean;
import com.dbf.loadtester.recorder.management.server.RecorderManagementServer;

/**
 * Servlet Filter version of the recorder.
 *
 */
public class RecorderServletFilter extends RecorderBase implements Filter
{
	private static final Logger log = Logger.getLogger(RecorderServletFilter.class);

	public static final String PARAM_DIRECTORY_PATH = "TestPlanDirectory";
	public static final String PARAM_IMMEDIATE_START = "Start";
	
	private boolean useParams = true;
	private RecorderServletFilterOptions options;
	
	public RecorderServletFilter() {}
	
	public RecorderServletFilter(RecorderServletFilterOptions options)
	{
		this.options = options;
		useParams = false;
	}

	public void init(FilterConfig filterConfig) throws ServletException 
	{
		String testPlanDirectory = useParams ? filterConfig.getInitParameter(PARAM_DIRECTORY_PATH) : options.getTestPlanDirectory();
		if(null != testPlanDirectory && !testPlanDirectory.isEmpty()) setTestPlanDirectory(Paths.get(testPlanDirectory));
		log.info("Initializing HTTP Load Test Recorder Filter using test plan directory " + this.getTestPlanDirectory());
		
		//Set the substitutions, not possible using init params
		if(!useParams)
		{
			if(null != options.getPathSubs()) 
			{
				this.setPathSubs(options.getPathSubs());
				log.info("Using path-based substitutions: " + options.getPathSubs().toString());
			}
				
			if(null != options.getQuerySubs())
			{
				this.setQuerySubs(options.getQuerySubs());
				log.info("Using query-based substitutions: " + options.getQuerySubs().toString());
			}
				
			if(null != options.getBodySubs())
			{
				this.setBodySubs(options.getBodySubs());
				log.info("Using body-based substitutions: " + options.getBodySubs().toString());
			}
		}
		
		//Register management once the options are set but before starting the recording
		initManagement();
		
		log.info("HTTP Load Test Recorder Filter initialization complete.");
		
		//Start only if configured for immediate start
		if((useParams && "true".equals(filterConfig.getInitParameter(PARAM_IMMEDIATE_START))) || (!useParams && options.isImmediateStart()))
		{
    		try
    		{
    			log.info("Immediate recording requested.");
    			this.startRecording();
    		}
    		catch (IOException e)
    		{
    			throw new ServletException("Failed to start filter post-initialization.",e);
    		}
		}
	}
	
	private void initManagement()
	{
		RecorderManagerMBean manager = new RecorderManager(this);
		if(options.isEnableJMX()) registerMBean(manager);
		if(options.isEnableREST()) RecorderManagementServer.initializeServer(manager, options.getRestPort());
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException 
    {	
		if(request instanceof HttpServletRequest)
			request = this.handleHTTPRequest((HttpServletRequest) request);
		
      	chain.doFilter(request, response);
    }
	
	public void destroy(){
	}
	
	private void registerMBean(RecorderManagerMBean manager)
	{
		log.info("Attempting to register Recorder Filter MBean...");
		
		try
		{
			ObjectName  mbeanName = new ObjectName("com.dbf.loadtester:name=" + manager.toString());
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			mbs.registerMBean(manager, mbeanName);
		}
		catch(Exception e)
		{
			log.warn("Failed to register MBean. JMX management will not be possible.", e);
		}
	}
}
