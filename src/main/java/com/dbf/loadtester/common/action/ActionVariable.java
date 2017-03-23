package com.dbf.loadtester.common.action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dbf.loadtester.common.util.Utils;

public class ActionVariable
{
	private final String urlPath;
	private final String textStart;
	private final String textEnd;
	private final String variableName;
	
	private final Pattern pathPattern;
	private final Pattern textPattern;
	private final String variableSubstitution;
	
	public ActionVariable(String path, String textStart, String textEnd, String variableName)
	{
		this.urlPath = path;
		this.textStart = textStart;
		this.textEnd = textEnd;
		this.variableName = variableName;
		
		validate();
		
		//Init Patterns
		pathPattern = Pattern.compile(Pattern.quote(path));
		if (textStart == null || textStart.equals(""))
			textPattern = Pattern.compile(Pattern.quote(textEnd));
		else if (textEnd == null || textEnd.equals(""))
			textPattern = Pattern.compile(Pattern.quote(textStart));
		else
			textPattern = Pattern.compile(Pattern.quote(textStart) + "(.*?)" + Pattern.quote(textEnd));	
		
		variableSubstitution = "<" + variableName + ">"; 
	}
	
	private void validate() throws IllegalArgumentException
	{
		if(urlPath == null || urlPath.equals(""))
			throw new IllegalArgumentException("Path cannot be empty.");
		
		if((textStart == null || textStart.equals("")) && (textEnd == null || textEnd.equals("")))
			throw new IllegalArgumentException("Either the start or the end (or both) of the text to match must be specified.");
		
		if(urlPath == null || urlPath.equals(""))
			throw new IllegalArgumentException("Path cannot be empty.");
		
		if(variableName == null || variableName.equals(""))
			throw new IllegalArgumentException("The variable name cannot be empty.");
	}
	
	public boolean pathMatches(String path)
	{
		Matcher matcher = pathPattern.matcher(path);
		return matcher.matches();
	}
	
	public String substituteVariable(String text)
	{
		return Utils.applyRegexSubstitution(text, textPattern, variableSubstitution);
	}
	
	public String getMatchingPath()
	{
		return urlPath;
	}
	
	public String getTextStart()
	{
		return textStart;
	}
	
	public String getTextEnd()
	{
		return textEnd;
	}
	
	public String getVariableName()
	{
		return variableName;
	}
	
	
}
