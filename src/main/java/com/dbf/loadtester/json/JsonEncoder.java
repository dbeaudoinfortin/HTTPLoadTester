package com.dbf.loadtester.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.dbf.loadtester.HTTPAction;
import com.google.gson.Gson;

public class JsonEncoder
{
	private static final Logger log = Logger.getLogger(JsonEncoder.class);
	
	private static final Gson gson = new Gson();
	
	public static List<HTTPAction> loadTestPlan(File testPlan) throws IOException
	{
		List<HTTPAction> actions = new LinkedList<HTTPAction>();
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader (new FileReader(testPlan));
			String line;
	
			while ((line = reader.readLine()) != null)
				actions.add(gson.fromJson(line, HTTPAction.class));
		}
		catch(Exception e)
		{
			log.error("Failed to load test plan at path '" + testPlan + "'.", e);
		}
		finally
		{
			if(reader != null) reader.close();
		}
		return actions;
	}
	
	public static String toJson(HTTPAction action)
	{
		return gson.toJson(action);
	}
}
