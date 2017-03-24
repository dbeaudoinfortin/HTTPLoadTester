package com.dbf.loadtester.common.action.substitutions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.dbf.loadtester.common.action.HTTPAction;
import com.dbf.loadtester.common.util.Utils;
import com.dbf.loadtester.player.action.PlayerHTTPAction;

/**
 * 
 * A class that handles all fixed and variable substitutions during both test plan execution and test plan recording.
 * 
 * This class holds state and is not thread safe. Create one instance per load tester thread or per recorder servlet filter.
 *
 */
public class ActionSubstituter
{
	private static final String THREAD_ID_PARAM = "<THREAD_ID>";
	private static final Pattern THREAD_ID_PARAM_PATTERN = Pattern.compile(THREAD_ID_PARAM);

	//Fixed Substitutions are built-in replacement strings. These do not change during the test plan.
	//These are calculated ahead of time for better performance.
	//Currently there is only Thread Number, more may be added later.
	private Map<Pattern, String> fixedSubstitutions;
	
	//Variable Substitutions are values that change during test plan execution. These must be extracted
	//from a response and re-inserted into subsequent requests.
	private List<ActionVariable> variableSubstitutions;
	
	//Holds the retrieved variable values
	private final Map<String, String> variableValues = new HashMap<String, String>();
	
	public ActionSubstituter(int uniqueThreadNumber, List<ActionVariable> variableSubstitutions)
	{
		this.variableSubstitutions = variableSubstitutions;
		initFixedSubstitutions(uniqueThreadNumber);
	}
	
	private void initFixedSubstitutions(int uniqueThreadNumber)
	{
		fixedSubstitutions = new HashMap<Pattern, String>();
		fixedSubstitutions.put(THREAD_ID_PARAM_PATTERN, "" + uniqueThreadNumber);
	}
	
	/**
	 * Applies fixed substitutions during the recording phase.
	 * 
	 */
	public void applyFixedSubstitutions(HTTPAction action)
	{
		if(!action.isHasSubstitutions()) return;
		
		if(action.getPath() != null)
			action.setPath(Utils.applyRegexSubstitutions(action.getPath(), fixedSubstitutions));
		
		if(action.getQueryString() != null)
			action.setQueryString(Utils.applyRegexSubstitutions(action.getQueryString(), fixedSubstitutions));
		
		//Body only applies to post and put
		if(action.getContent() != null && ("PUT".equals(action.getMethod()) || "POST".equals(action.getMethod())))
			action.setContent(Utils.applyRegexSubstitutions(action.getContent(), fixedSubstitutions));
	}
	
	/**
	 * Applies variable names in the recording phase
	 * 
	 */
	public void applyVariableNames(PlayerHTTPAction action)
	{
		if(!action.isHasVariables()) return;
		
		for (ActionVariable variable : variableSubstitutions)
		{
			//Not all variables apply to all paths
			if(!variable.applyPathMatches(action.getPath())) continue;
			action.setContent(variable.substituteVariableName(action.getContent()));
		}
	}
	
	/**
	 * Applies variable values in the playback phase.
	 */
	public void applyVariableValues(PlayerHTTPAction action)
	{
		if(!action.isHasVariables()) return;
		
		for (ActionVariable variable : variableSubstitutions)
		{
			//We may not have a value for this variable
			String value = variableValues.get(variable.getVariableName());
			if (null == value) continue;
			
			//Not all variables apply to all paths
			if(!variable.applyPathMatches(action.getPath())) continue;
			
			action.setContent(variable.substituteVariableValue(action.getContent(), value));
		}
	}
	
	/**
	 * Retrieves variable values in the playback phase.
	 */
	public void retrieveVariableValues(PlayerHTTPAction action, String responseBody)
	{
		if(!action.isHasVariables()) return;
		
		for (ActionVariable variable : variableSubstitutions)
		{			
			//Not all variables apply to all paths
			if(!variable.retrievalPathMatches(action.getPath())) continue;
			
			action.setContent(variable.substituteVariableValue(action.getContent(), value));
		}
	}
	
	/**
	 * Clears out any previously stored variables.
	 * 
	 * This should be call at the end of every test plan execution.
	 * 
	 */
	public void resetVariables()
	{
		variableValues.clear();
	}
}
