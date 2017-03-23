package com.dbf.loadtester.common.action.substitutions;

import java.util.regex.Pattern;

import com.dbf.loadtester.common.util.Utils;

public class VariableSubstitution
{
	private static final Pattern ANYTHING_PATTERN = Pattern.compile(".*");

	private final String variableName;
	
	//Pre-compiled patterns are done for performance reasons.
	private final Pattern retrievalPathPattern; //The URL path used when retrieving the value 
	private final Pattern applyPathPattern; //The URL path used when applying the value
	private final Pattern retrievalTextPattern; //The text used to locate the value when retrieving. Matched against the HTTP Response Body
	private final Pattern applyTextPattern; //The text used to locate where to apply the variable value. Matched against the HTTP Request Body
	
	public VariableSubstitution(String retrievalpath, String applyPath, String retrievalTextStart, String retrievalTextEnd, String retrievalPattern, String applyTextStart, String applyTextEnd, String applyPattern, String variableName)
	{
		//Do some validation
		if((retrievalTextStart == null || retrievalTextStart.equals("")) && (retrievalTextEnd == null || retrievalTextEnd.equals("")))
			throw new IllegalArgumentException("Either the start or the end (or both) of the text to match for retrieval must be specified.");
		
		//Do some validation
		if((applyTextStart == null || applyTextStart.equals("")) && (applyTextEnd == null ||applyTextEnd.equals("")))
			throw new IllegalArgumentException("Either the start or the end (or both) of the apply text to match for replacement must be specified.");
		
		if(variableName == null || variableName.equals(""))
			throw new IllegalArgumentException("The variable name cannot be empty.");
		
		this.variableName = variableName;
		
		//Init Patterns
		this.retrievalPathPattern = compilePathPattern(retrievalpath);
		this.applyPathPattern = compilePathPattern(applyPath);
		this.retrievalTextPattern = compileRetrievalTextPattern(retrievalTextStart, retrievalTextEnd);
		this.applyTextPattern = compileRetrievalTextPattern(retrievalTextStart, retrievalTextEnd);
	}
	
	private Pattern compilePathPattern(String path)
	{
		if(path == null || path.equals(""))
			return ANYTHING_PATTERN;
		else
			return Pattern.compile(Pattern.quote(path));
	}
	
	private Pattern compileRetrievalTextPattern(String textStart, String textEnd)
	{
		if (textStart == null || textStart.equals(""))
			return Pattern.compile(Pattern.quote(textEnd));
		else if (textEnd == null || textEnd.equals(""))
			return Pattern.compile(Pattern.quote(textStart));
		else
			return Pattern.compile(Pattern.quote(textStart) + "(.*?)" + Pattern.quote(textEnd));	
	}
	
	public boolean retrievalPathMatches(String path)
	{
		return retrievalPathPattern.matcher(path).matches();
	}
	
	public boolean applyPathMatches(String path)
	{
		return applyPathPattern.matcher(path).matches();
	}
	
	public String substituteVariableName(String text)
	{
		return Utils.applyRegexSubstitution(text, retrievalTextPattern, variableSubstitution);
	}
	
	public String substituteVariableValue(String text, String newvalue)
	{
		return Utils.applyRegexSubstitution(text, variablePattern, newvalue);
	}
	
	public String retrieveVariableValue(String text)
	{
		retrievalTextPattern.matcher(source)
		return Utils.applyRegexSubstitution(text, variablePattern, newvalue);
	}

	public String getVariableName()
	{
		return variableName;
	}
}
