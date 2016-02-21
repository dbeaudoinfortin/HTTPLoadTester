package com.dbf.loadtester.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.dbf.loadtester.HTTPAction;
import com.dbf.loadtester.json.JsonEncoder;

/**
 * 
 * Utility to merge multiple JSON files together using the absolute time of each action.
 * Especially useful if the two files come from different servers.
 * 
 *
 */
public class MergeJson
{
	private static final Logger log = Logger.getLogger(MergeJson.class);
	
	private static final String JSON_OUTPUT_FILE = "C:\\Users\\dbeaudoinfortin\\Desktop\\Polycom\\Combined.json";
	private static final List<String> jsonFiles;
	
	static
	{
		jsonFiles = new ArrayList<String>();
		jsonFiles.add("C:\\Users\\dbeaudoinfortin\\Desktop\\Polycom\\TestPlan-2016-02-20-275263706-2.json");
		jsonFiles.add("C:\\Users\\dbeaudoinfortin\\Desktop\\Polycom\\TestPlan-2016-02-20-1443823428-3.json");
	}
	
	public static void main(String[] args) throws IOException
	{
		if(jsonFiles.size() < 1)
		{
			log.error("No input files specified.");
			return;
		}
			
		List<List<HTTPAction>> testPlans = new ArrayList<List<HTTPAction>>(jsonFiles.size());
		
		Integer totalActionCount = 0;
		for(String filePath : jsonFiles)
		{
			log.info("Reading test plan file " + filePath);
			List<HTTPAction> actions = JsonEncoder.loadTestPlan(new File(filePath));
			testPlans.add(actions);
			totalActionCount += actions.size();
			log.info("Test plan file " + filePath + " contains " + actions.size() + " action(s).");
		}
		
		log.info("Found " + totalActionCount +" action(s) in " + jsonFiles.size() + " file(s).");
		
		log.info("Creating new output file " + JSON_OUTPUT_FILE);
		BufferedWriter testPlanWriter = new BufferedWriter(new FileWriter(new File(JSON_OUTPUT_FILE), true));
		
		Date maxDate = new Date(Long.MAX_VALUE);
		totalActionCount = 0;
		while(true)
		{
			Date earliestDate = maxDate;
			HTTPAction earliestAction = null;
			List<HTTPAction> earliestTestPlan = null; 
			
			//Find the test plan with the earliest action
			for(List<HTTPAction> testPlan : testPlans)
			{
				//Ignore any empty test plans
				if(testPlan.size() < 1) continue;
				
				HTTPAction action = testPlan.get(0);
				if(action.getAbsoluteTime().before(earliestDate))
				{
					earliestDate = action.getAbsoluteTime();
					earliestAction = action;
					earliestTestPlan = testPlan;
				}
			}
			
			//No actions left
			if(earliestAction == null) break;
			
			totalActionCount +=1;
			testPlanWriter.write(JsonEncoder.toJson(earliestAction));
			testPlanWriter.newLine();
			
			earliestTestPlan.remove(earliestAction);
		}
		
		testPlanWriter.close();
		log.info("Merged " + totalActionCount + " action(s) ");
	}
}
