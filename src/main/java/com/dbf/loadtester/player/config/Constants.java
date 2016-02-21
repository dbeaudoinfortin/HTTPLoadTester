package com.dbf.loadtester.player.config;

public class Constants
{
	//HOST SETTINGS
	public static final String  DEFAULT_HOST = "localhost";
	public static final String  DEFAULT_HTTP_PORT = "10080";
	public static final String  DEFAULT_HTTPS_PORT = "10443";
	
	//THREADS
	public static final long    DEFAULT_STAGGER_TIME = 5000;
	public static final Double  MAX_STAGGER_OFFSET = 0.75; 
	public static final int     DEFAULT_THREAD_COUNT = 5;
	
	//TIMINGS
	public static final boolean USE_TEST_PLAN_TIMINGS = true;
	public static final int DEFAULT_TIME_BETWEEN_ACTIONS = 100;
	
}
