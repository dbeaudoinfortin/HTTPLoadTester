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
	private final RollingStats rolling5;
	private final RollingStats rolling10;
	private final RollingStats rolling25;
	private final RollingStats rolling50;
	private final RollingStats rolling100;
	private final RollingStats rolling250;
	
	public TimeStats()
	{
		rolling5 = new RollingStats(5);
		rolling10 = new RollingStats(10);
		rolling25 = new RollingStats(25);
		rolling50 = new RollingStats(50);
		rolling100 = new RollingStats(100);
		rolling250 = new RollingStats(250);
	}
	
	public TimeStats(TimeStats other)
	{
		this.min = other.min;
		this.max = other.max;
		this.total = other.total;
		this.count = other.count;
		this.average = other.average;
		this.lastUpdated = other.lastUpdated;
		rolling5 = new RollingStats(other.rolling5);
		rolling10 = new RollingStats(other.rolling10);
		rolling25 = new RollingStats(other.rolling25);
		rolling50 = new RollingStats(other.rolling50);
		rolling100 = new RollingStats(other.rolling100);
		rolling250 = new RollingStats(other.rolling250);

	}
	
	/**
	 * Increment stats by adding a new duration.
	 * 
	 * THIS METHOD IS NOT THREAD SAFE. 
	 * 
	 */
	public void increment(long duration)
	{
		if(duration < 1) duration = 1;
		
		lastUpdated = new Date();
		
		total += duration;
		count += 1;
		average = total/count;
		min = Math.min(duration, min);
		max = Math.max(duration, max);
		
		rolling5.increment(duration);
		rolling10.increment(duration);
		rolling25.increment(duration);
		rolling50.increment(duration);
		rolling100.increment(duration);
		rolling250.increment(duration);
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("min:");
		if(min == Long.MAX_VALUE)
		{
			sb.append("N/A");
		}
		else
		{
			sb.append(String.format("%.2f",min / 1000.0));
			sb.append("s");
		}

		sb.append("\tmax:");
		if(max == Long.MIN_VALUE)
		{
			sb.append("N/A");
		}
		else
		{
			sb.append(String.format("%.2f",max / 1000.0));
			sb.append("s");
		}

		sb.append("\ttotal:");
		sb.append(String.format("%.2f",total / 1000.0));
		sb.append("s");

		sb.append("\tcount:");
		sb.append(count);

		sb.append("\tavg:");
		sb.append(String.format("%.2f",average / 1000.0));
		sb.append("s");

		return sb.toString();

	}
}
