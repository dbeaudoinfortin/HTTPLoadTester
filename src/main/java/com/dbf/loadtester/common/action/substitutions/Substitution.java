package com.dbf.loadtester.common.action.substitutions;

public abstract class Substitution
{
	private String textMatchStart;
	private String textMatchEnd;
	private String textPattern;
	private String replacement;
	
	public void validate()
	{
		//Must specify a pattern or the start and end, both not both
		if(null != textPattern)
		{
    		if(!(textMatchStart == null || textMatchStart.equals("")) && (textMatchEnd == null || textMatchEnd.equals("")))
    			throw new IllegalArgumentException("Cannot specify start & end of text to match is a Regex pattern is used.");
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
	
	public String getReplacement()
	{
		return replacement;
	}
	
	public void setReplacement(String replacement)
	{
		this.replacement = replacement;
	}

	public String getTextPattern()
	{
		return textPattern;
	}

	public void setTextPattern(String textPattern)
	{
		this.textPattern = textPattern;
	}
}
