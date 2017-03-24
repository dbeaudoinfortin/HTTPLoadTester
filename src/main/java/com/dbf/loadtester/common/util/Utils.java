package com.dbf.loadtester.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import com.dbf.loadtester.common.json.JsonEncoder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import ch.qos.logback.classic.LoggerContext;

public class Utils
{	
	public static final String NEW_LINE = System.getProperty("line.separator");
	private static Boolean isWindows;
	
	public static boolean isWindows()
	{
		if(null == isWindows)
		{
			isWindows = System.getProperty("os.name", "windows").toLowerCase().contains("win");
		}
		return isWindows;
		
	}
	
	public static String applyRegexSubstitutions(String source, Map<Pattern, String> replacements)
	{
		for(Entry<Pattern, String> entry : replacements.entrySet())
			source = applyRegexSubstitution(source, entry.getKey(), entry.getValue());
		
		return source;
	}
	
	public static String applyRegexSubstitution(String source, Pattern pattern, String replacement)
	{
		return pattern.matcher(source).replaceAll(replacement);
	}
	
	public static void discardStream(InputStream in) throws IOException
	{
		try
		{
			byte[] byteArray = new byte[2048];
			while(in.read(byteArray) > -1);
		}
		finally
		{
			in.close();
		}
		
	}
	
	public static void flushAllLogs()
	{
		ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
		if (loggerFactory instanceof LoggerContext)
		{
		    LoggerContext context = (LoggerContext) loggerFactory;
		    context.stop();
		}
	}
	
	public static String determineContentType(Header header)
	{
		if (null == header) return "";
		if(null == header.getValue()) return "";
		
		int splitIndex = header.getValue().indexOf(';');
		String contentType = (splitIndex < 0) ? header.getValue() :  header.getValue().substring(0, splitIndex);
		
		return contentType.trim().toLowerCase();
	}
	
	public static <T> List<T> convertArgToObjectList(String arg)
	{
		String base64Decoded = new String(Base64.decodeBase64(arg));
		try
		{
			return JsonEncoder.fromJson(base64Decoded, (new TypeToken<List<T>>(){}).getType());
		}
		catch (JsonSyntaxException e)
		{
			throw new IllegalArgumentException("Failed to parse Json value '" + base64Decoded + "'.",e);
		}
	}
}
