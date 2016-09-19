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

import com.dbf.loadtester.jmx.recorder.RecorderManagerMBean;
import com.dbf.loadtester.jmx.recorder.RecorderManager;
import com.dbf.loadtester.recorder.RecorderCommon;

/**
 * Servlet Filter version of the recorder.
 *
 */
public class RecorderServletFilter extends RecorderCommon implements Filter
{
	private static final Logger log = Logger.getLogger(RecorderServletFilter.class);

	public static final String PARAM_DIRECTORY_PATH = "TestPlanDirectory";
	
	public void init(FilterConfig filterConfig) throws ServletException 
	{
		String testPlanDirectory = filterConfig.getInitParameter(PARAM_DIRECTORY_PATH);
		if(null != testPlanDirectory && !testPlanDirectory.isEmpty()) setTestPlanDirectory(Paths.get(testPlanDirectory));
		log.info("Initializing HTTP Load Test Recorder Filter using test plan directory " + this.getTestPlanDirectory());
		registerMBean();
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException 
    {	
		if(request instanceof HttpServletRequest)
			request = this.handleHTTPRequest((HttpServletRequest) request);
		
      	chain.doFilter(request, response);
    }
	
	public void destroy(){
	}
	
	private void registerMBean()
	{
		log.info("Attempting to register Recorder Filter MBean...");
		
		try
		{
			RecorderManagerMBean manager = new RecorderManager(this);
			ObjectName  mbeanName = new ObjectName("com.dbf.loadtester:name=" + manager.toString());
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			mbs.registerMBean(manager, mbeanName);
		}
		catch(Exception e)
		{
			log.warn("Failed to register MBean. JMX monitoring will not be possible.", e);
		}
	}
}
