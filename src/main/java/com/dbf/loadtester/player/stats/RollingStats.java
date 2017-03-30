package com.dbf.loadtester.player.stats;

import java.util.LinkedList;

public class RollingStats
{
	private final int rollingCount;
	private double rollingAverage = 0.1;
	private transient long rollingTotal;
	private final transient LinkedList<Long> rollingDurations  = new LinkedList<Long>();
	
	public RollingStats(int rollingCount)
	{
		this.rollingCount = rollingCount;
	}
	
	public RollingStats(RollingStats other)
	{
		this.rollingCount = other.rollingCount;
		this.rollingAverage = other.rollingAverage;
		this.rollingTotal = other.rollingTotal;
		this.rollingDurations.addAll(other.rollingDurations);
	}
	
	public void increment(long duration)
	{
		//Remove the first duration
		if(rollingDurations.size() == rollingCount)
			rollingTotal -= rollingDurations.removeFirst();
		
		rollingDurations.addLast(duration);
		rollingTotal += duration;
		rollingAverage = rollingTotal/rollingDurations.size();
	}
}
