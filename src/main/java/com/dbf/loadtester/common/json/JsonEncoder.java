package com.dbf.loadtester.common.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.dbf.loadtester.common.action.HTTPAction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class JsonEncoder
{
	private static final Logger log = Logger.getLogger(JsonEncoder.class);
	
	private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();

	public static List<HTTPAction> loadTestPlan(File testPlan) throws IOException
	{
		List<HTTPAction> actions = new LinkedList<HTTPAction>();
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader (new FileReader(testPlan));
			String line;
	
			int i = 1;
			while ((line = reader.readLine()) != null)
			{
				HTTPAction action = gson.fromJson(line, HTTPAction.class);
				action.setId(i++);
				actions.add(action);
			}
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
	
	public static String toJson(Object o)
	{
		return gson.toJson(o);
	}
	
	public static <T> T fromJson(String json, Class<T> classOfT)
	{
		try
		{
			return gson.fromJson(json, classOfT);
		}
		catch(JsonSyntaxException e)
		{
			log.warn("JSON conversion failed.");
			return null;
		}
	}
}
