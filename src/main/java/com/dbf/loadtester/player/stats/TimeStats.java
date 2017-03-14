package com.dbf.loadtester.player.stats;

import java.util.Date;

/**
 * Holds basic time based statistics.
 * 
 * Not thread-safe, must be externally synchronized.
 *
 */
public class TimeStats
{
	private long min = Long.MAX_VALUE;
	private long max = Long.MIN_VALUE;
	private long total;
	private int count;
	private double average;
	private Date lastUpdated;
	
	public void increment(long time)
	{
		total += time;
		count += 1;
		average = total/count;
		min = Math.min(time, min);
		max = Math.max(time, max);
		lastUpdated = new Date();
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

		return sb.toString();

	}
}
