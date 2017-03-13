package com.dbf.loadtester.player.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dbf.loadtester.common.action.HTTPAction;

public class PlayerStats
{
	private final Map<String, TimeStats> actionTimes;
	private final TimeStats testPlanTime = new TimeStats();
	private final TimeStats aggregateActionTime = new TimeStats();
	
	public PlayerStats (List<HTTPAction> actions)
	{
		actionTimes = new ConcurrentHashMap<String, TimeStats>(actions.size());
		init(actions);
	}
	
	public PlayerStats ()
	{
		//Initialize with empty stats. Implies the test plan has yet to be loaded.
		actionTimes = new ConcurrentHashMap<String, TimeStats>(0);
	}
	
	private void init(List<HTTPAction> actions)
	{
		//Pre-populate the actionTimes
		//This ensures that we don't have to later synchronize on the entire HashMap,
		//only on the individual entries in the HashMap
		for(HTTPAction action : actions) actionTimes.put(action.getIdentifier(), new TimeStats());
	}
	
	public void recordTestPlanTime(long time)
	{
		synchronized (testPlanTime) { testPlanTime.increment(time); }
	}

	public void recordActionTime(String actionName, long time)
	{
		synchronized (aggregateActionTime) { aggregateActionTime.increment(time); }
		
		TimeStats actionTime = actionTimes.get(actionName);
		if(null == actionTime) return;
		synchronized (actionTime) { actionTime.increment(time); }
	}
	
	public String printStatsSummary()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\rOverall Test Plan Stats: ");
		synchronized (testPlanTime) { sb.append(testPlanTime); }
		
		sb.append("\rAggregate stats for all actions across all threads: ");
		synchronized (aggregateActionTime) { sb.append(aggregateActionTime); }
		
		sb.append("\r\rIndividual stats for all actions across all threads: ");
		for (Map.Entry<String, TimeStats> actionTime : actionTimes.entrySet())
		{
			synchronized (actionTime)
			{
				sb.append("\r");
				sb.append(actionTime.getKey());
				sb.append(" ");
				sb.append(actionTime.getValue());
			}
		}
		return sb.toString();
	}

	public Map<String, TimeStats> getActionStats()
	{
		Map<String, TimeStats> cloneActionTimes = new HashMap<String, TimeStats>(actionTimes.size());
		for (Map.Entry<String, TimeStats> actionTime : actionTimes.entrySet())
		{
			synchronized (actionTime)
			{
				cloneActionTimes.put(actionTime.getKey(), actionTime.getValue().clone());
			}
		}
		return cloneActionTimes;
	}

	public TimeStats getTestPlanStats()
	{
		synchronized (testPlanTime) 
		{
			return testPlanTime.clone();
		}
	}
	
	public TimeStats getAggregateActionStats()
	{
		synchronized (aggregateActionTime) 
		{
			return aggregateActionTime.clone();
		}
	}
}
