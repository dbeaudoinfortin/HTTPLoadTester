package com.dbf.loadtester.common.action.substitutions;

import java.util.regex.Pattern;

public class FixedSubstitution implements Substitution
{
	private TextMatch textToMatch;
	private SubstitutionType type;
	private String replacement;
	
	private transient Pattern textPattern;
	
	public void init() throws IllegalArgumentException
	{
		textPattern = textToMatch.getCompiledPattern();
	}
	
	public String applySubstitution(String source)
	{
		return textPattern.matcher(source).replaceAll(replacement);
	}
	
	public TextMatch getTextToMatch()
	{
		return textToMatch;
	}
	public void setTextToMatch(TextMatch textToMatch)
	{
		this.textToMatch = textToMatch;
	}
	public SubstitutionType getType()
	{
		return type;
	}
	public void setType(SubstitutionType type)
	{
		this.type = type;
	}
	public String getReplacement()
	{
		return replacement;
	}
	public void setReplacement(String replacement)
	{
		this.replacement = replacement;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{type=");
		sb.append(type);
		sb.append(", replacement=");
		sb.append(replacement);
		sb.append(", textToMatch=");
		sb.append(textToMatch);
		sb.append("}");
		return sb.toString();
	}
}
