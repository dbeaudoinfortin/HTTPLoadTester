package com.dbf.loadtester.common.action.substitutions;

import java.util.regex.Pattern;

public class TextMatch
{
	private String textMatchStart;
	private String textMatchEnd;
	private String textPattern;
	private transient Pattern compiledPattern;
	
	public void validate() throws IllegalArgumentException
	{
		boolean patternIsEmpty = (textPattern == null || textPattern.equals(""));
		boolean startIsEmpty = (textMatchStart == null || textMatchStart.equals(""));
		boolean endIsEmpty = (textMatchEnd == null || textMatchEnd.equals(""));
		
		//Must specify a pattern or the start and end, both not both
    	if(!(patternIsEmpty ^ (startIsEmpty && endIsEmpty)))
    		throw new IllegalArgumentException("Must specify a Regex pattern or the start & end of the text to match, but not both.");
	}
	
	public Pattern getCompiledPattern()
	{
		if(null == compiledPattern) compiledPattern = compilePattern();
		return compiledPattern;
	}
	
	private Pattern compilePattern()
	{
		validate();
		
		if(textPattern == null || textPattern.equals(""))
		{
			if (textMatchStart == null || textMatchStart.equals(""))
				return Pattern.compile(Pattern.quote(textMatchEnd));
			else if (textMatchEnd == null || textMatchEnd.equals(""))
				return Pattern.compile(Pattern.quote(textMatchStart));
			else
				return Pattern.compile("(?<=" + Pattern.quote(textMatchStart)  + ")(.*?)(?=" + Pattern.quote(textMatchEnd) + ")");
		}
		else
		{
			return Pattern.compile(textPattern);
		}
	}
	
	public String getTextMatchStart()
	{
		return textMatchStart;
	}
	
	public void setTextMatchStart(String textMatchStart)
	{
		this.textMatchStart = textMatchStart;
	}
	
	public String getTextMatchEnd()
	{
		return textMatchEnd;
	}
	
	public void setTextMatchEnd(String textMatchEnd)
	{
		this.textMatchEnd = textMatchEnd;
	}

	public String getTextPattern()
	{
		return textPattern;
	}

	public void setTextPattern(String textPattern)
	{
		this.textPattern = textPattern;
	}
	
	@Override
	public String toString()
	{
		if(textPattern != null && !textPattern.equals(""))
    		return textPattern;
		
		return textMatchStart + " to " + textMatchEnd;
	}
}
