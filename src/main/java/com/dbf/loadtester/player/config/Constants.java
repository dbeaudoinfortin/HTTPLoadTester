package com.dbf.loadtester.player.config;

public class Constants
{
	//HOST SETTINGS
	
	public static final String  DEFAULT_HOST = "localhost";
	public static final int  	DEFAULT_HTTP_PORT = 10080;
	public static final int  	DEFAULT_HTTPS_PORT = 10443;
	
	//THREADS
	public static final long    DEFAULT_STAGGER_TIME = 5000;
	public static final Double  MAX_STAGGER_OFFSET = 0.75; 
	public static final int     DEFAULT_THREAD_COUNT = 1;
	
	//TIMINGS
	public static final int DEFAULT_TIME_BETWEEN_ACTIONS = -1;
	public static final int DEFAULT_MINIMUM_RUN_TIME = -1;
}
