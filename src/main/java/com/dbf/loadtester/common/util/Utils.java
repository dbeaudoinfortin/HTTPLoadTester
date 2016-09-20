package com.dbf.loadtester.common.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class Utils
{	
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
		{
			source = entry.getKey().matcher(source).replaceAll(entry.getValue());
		}

		return source;
	}
}
