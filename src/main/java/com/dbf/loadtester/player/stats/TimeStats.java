package com.dbf.loadtester.player.stats;

import java.util.Date;
import java.util.LinkedList;

/**
 * Holds basic time based statistics.
 * 
 * Not thread-safe, must be externally synchronized.
 *
 */
public class TimeStats
{
	private static final int ROLLING_DURATIONS_MAX = 10;
	
	private long min = Long.MAX_VALUE;
	private long max = Long.MIN_VALUE;
	private long total;
	private int count;
	private double average;
	private double rollingAverage;
	private transient long rollingTotal;
	private transient LinkedList<Long> rollingDurations = new LinkedList<Long>();
	private Date lastUpdated;
	
	/**
	 * Increment stats by adding a new duration.
	 * 
	 * THIS METHOD IS NOT THREAD SAFE. 
	 * 
	 */
	public void increment(long duration)
	{
		lastUpdated = new Date();
		
		total += duration;
		count += 1;
		average = total/count;
		min = Math.min(duration, min);
		max = Math.max(duration, max);
		
		//Remove the first duration
		if(rollingDurations.size() == ROLLING_DURATIONS_MAX)
			rollingTotal -= rollingDurations.removeFirst();
		
		rollingDurations.addLast(duration);
		rollingTotal += duration;
		rollingAverage = rollingTotal/rollingDurations.size();
	}
	
	public TimeStats clone()
	{
		TimeStats clone = new TimeStats();
		clone.total = total;
		clone.count = count;
		clone.average = average;
		clone.min = min;
		clone.max = max;
		clone.lastUpdated = lastUpdated;
		clone.rollingAverage = rollingAverage;
		return clone;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("min:");
		if(min == Long.MAX_VALUE)
		{
			sb.append("N/A ");
		}
		else
		{
			sb.append(String.format("%.2f",min / 1000.0));
			sb.append("(s) ");
		}

		sb.append("max:");
		if(max == Long.MIN_VALUE)
		{
			sb.append("N/A ");
		}
		else
		{
			sb.append(String.format("%.2f",max / 1000.0));
			sb.append("(s) ");
		}

		sb.append("total:");
		sb.append(String.format("%.2f",total / 1000.0));
		sb.append("(s) ");

		sb.append("count:");
		sb.append(count);
		sb.append(" ");

		sb.append("avg:");
		sb.append(String.format("%.2f",average / 1000.0));
		sb.append("(s)");
		
		sb.append("rolling avg:");
		sb.append(String.format("%.2f",rollingAverage / 1000.0));
		sb.append("(s)");

		return sb.toString();

	}
}
