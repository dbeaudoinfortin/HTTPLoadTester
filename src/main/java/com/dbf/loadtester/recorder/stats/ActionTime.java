package com.dbf.loadtester.recorder.stats;

public class ActionTime
{
	public long min;
	public long max;
	public long total;
	public int count;
	public double average;

	@Override
	public String toString()
	{

		StringBuilder sb = new StringBuilder("min:");

		sb.append(min / 1000.0);
		sb.append("(s) ");

		sb.append("max:");
		sb.append(max / 1000.0);
		sb.append("(s) ");

		sb.append("total:");
		sb.append(total / 1000.0);
		sb.append("(s) ");

		sb.append("count:");
		sb.append(count);
		sb.append(" ");

		sb.append("avg:");
		sb.append(average / 1000.0);
		sb.append("(s)");

		return sb.toString();

	}
}
