package com.dbf.loadtester.common.action.substitutions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dbf.loadtester.common.util.Utils;

public class VariableSubstitution implements Substitution
{
	private static final Pattern ANYTHING_PATTERN = Pattern.compile("(.*)");

	private String variableName;
	private String retrievalPath;
	private String replacementPath;
	private TextMatch retrievalTextToMatch;
	private TextMatch replacementTextToMatch;
	
	//Pre-compiled patterns are done for performance reasons.
	private transient Pattern retrievalPathPattern; //The URL path used when retrieving the value 
	private transient Pattern replacementPathPattern; //The URL path used when applying the value
	private transient Pattern retrievalTextPattern; //The text used to locate the value when retrieving. Matched against the HTTP Response Body
	private transient Pattern replacementTextPattern; //The text used to locate where to apply the variable value. Matched against the HTTP Request Body
	
	private void validate() throws IllegalArgumentException
	{
		if(variableName == null || variableName.equals(""))
			throw new IllegalArgumentException("The variable name cannot be empty.");
	}
	
	@Override
	public void init() throws IllegalArgumentException
	{
		validate();
		
		//Init Patterns
		this.retrievalPathPattern = compilePathPattern(retrievalPath);
		this.replacementPathPattern = compilePathPattern(replacementPath);
		this.retrievalTextPattern = retrievalTextToMatch.getCompiledPattern();
		this.replacementTextPattern = replacementTextToMatch.getCompiledPattern();
	}
	
	private Pattern compilePathPattern(String path)
	{
		if(path == null || path.equals(""))
			return ANYTHING_PATTERN;
		else
			return Pattern.compile(ANYTHING_PATTERN + Pattern.quote(path) + ANYTHING_PATTERN);
	}
	
	public boolean retrievalPathMatches(String path)
	{
		return retrievalPathPattern.matcher(path).matches();
	}
	
	public boolean replacementPathMatches(String path)
	{
		return replacementPathPattern.matcher(path).matches();
	}
	
	public String replaceVariable(String text, String newValue)
	{
		return Utils.applyRegexSubstitution(text, replacementTextPattern, newValue);
	}
	
	public String retrieveVariable(String text)
	{
		Matcher matcher = retrievalTextPattern.matcher(text);
		if(!matcher.find()) return null;
		return matcher.group();
	}

	public String getVariableName()
	{
		return variableName;
	}

	public String getRetrievalPath()
	{
		return retrievalPath;
	}

	public void setRetrievalPath(String retrievalPath)
	{
		this.retrievalPath = retrievalPath;
	}

	public String getReplacementPath()
	{
		return replacementPath;
	}

	public void setReplacementPath(String replacementPath)
	{
		this.replacementPath = replacementPath;
	}

	public TextMatch getRetrievalTextToMatch()
	{
		return retrievalTextToMatch;
	}

	public void setRetrievalTextToMatch(TextMatch retrievalTextToMatch)
	{
		this.retrievalTextToMatch = retrievalTextToMatch;
	}

	public TextMatch getReplacementTextToMatch()
	{
		return replacementTextToMatch;
	}

	public void setReplacementTextToMatch(TextMatch replacementTextToMatch)
	{
		this.replacementTextToMatch = replacementTextToMatch;
	}

	public void setVariableName(String variableName)
	{
		this.variableName = variableName;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(variableName);
		sb.append(": retrievePath=");
		sb.append(retrievalPath);
		sb.append(", replacementPath=");
		sb.append(replacementPath);
		sb.append(", retrieveMatch=");
		sb.append(retrievalTextToMatch);
		sb.append(", replacementMatch=");
		sb.append(replacementTextToMatch);
		sb.append("}");
		return sb.toString();
	}
}
