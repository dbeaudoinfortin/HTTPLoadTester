package com.dbf.loadtester.common.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dbf.loadtester.common.action.HTTPAction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class JsonEncoder
{
	private static final Logger log = LoggerFactory.getLogger(JsonEncoder.class);
	
	private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();

	public static List<HTTPAction> loadTestPlan(File testPlan) throws IOException
	{
		List<HTTPAction> actions = new LinkedList<HTTPAction>();
		BufferedReader reader = null;
		try
		{
			//Wrap in a BOMInputStream to discard the UTF BOM marker, if present.
			reader = new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(testPlan))));
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
	
	public static <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException
	{
		return gson.fromJson(json, classOfT);
	}
	
	public static void toJson(Object entity, Class<?> type, OutputStream entityStream) throws IOException 
	{
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(entityStream, "UTF-8"));
		gson.toJson(entity, type, writer);
		writer.flush();
	}
	
	public static Object fromJson(Class<Object> type, InputStream entityStream) throws IOException
	{
		JsonReader reader = new JsonReader(new InputStreamReader(entityStream));
		return gson.fromJson(reader, type);
	}
}
