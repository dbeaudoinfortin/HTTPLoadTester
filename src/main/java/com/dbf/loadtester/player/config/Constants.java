package com.dbf.loadtester.player.config;

public class Constants
{
	//HOST SETTINGS
	public static final String  DEFAULT_HOST = "localhost";
	public static final int  	DEFAULT_HTTP_PORT = 10080;
	public static final int  	DEFAULT_HTTPS_PORT = 10443;
	
	//THREADS
	//Stagger time is an average amount of time between each thread launching
	public static final long    DEFAULT_STAGGER_TIME = 5000;	
	//Max stagger offset is the maximum that the stagger time can vary 
	public static final Double  MAX_STAGGER_OFFSET = 0.75; 
	public static final int     DEFAULT_THREAD_COUNT = 1;
	
	//TIMINGS
	public static final int DEFAULT_TIME_BETWEEN_ACTIONS = -1;
	public static final int DEFAULT_MINIMUM_RUN_TIME = -1;
	
	//MANAGEMENT
	public static final int DEFAULT_PLAYER_REST_PORT = 5009;
	public static final int DEFAULT_RECORDER_REST_PORT = 6009;
	
	//RECORDER
	public static final int DEFAULT_FORWARD_HTTP_PORT = 80;
	public static final int DEFAULT_FORWARD_HTTPS_PORT = 443;
}
