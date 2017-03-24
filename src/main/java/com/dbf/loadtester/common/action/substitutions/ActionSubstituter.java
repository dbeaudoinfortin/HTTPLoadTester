package com.dbf.loadtester.common.action.substitutions;

import java.util.ArrayList;
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
	private static final Pattern THREAD_ID_PARAM_PATTERN = Pattern.compile("<THREAD_ID>");

	//Fixed Substitutions are built-in replacement strings. These do not change during the test plan.
	//These are calculated ahead of time for better performance.
	//Currently there is only Thread Number, more may be added later.
	private Map<Pattern, String> fixedSubstitutions;
	
	//Variable Substitutions are values that change during test plan execution. These must be extracted
	//from a response and re-inserted into subsequent requests.
	private List<VariableSubstitution> variableSubstitutions;
	
	//Holds the retrieved variable values
	private final Map<String, String> variableValues = new HashMap<String, String>();
	
	public ActionSubstituter(int uniqueThreadNumber, List<VariableSubstitution> variableSubstitutions)
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
	 * Applies fixed substitutions during the playback phase.
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
	 * Applies variable values to a given HTTP Action in the playback phase.
	 */
	public void applyVariableValues(PlayerHTTPAction action)
	{
		for (VariableSubstitution variable : action.getReplacementVariables())
		{
			//We may not have a value for this variable
			String value = variableValues.get(variable.getVariableName());
			if (null == value) continue;
			action.setContent(variable.replaceVariable(action.getContent(), value));
		}
	}
	
	/**
	 * Retrieves variable values from a given HTTP Action in the playback phase.
	 */
	public void retrieveVariableValues(PlayerHTTPAction action, String responseBody)
	{
		for (VariableSubstitution variable : action.getRetrievalVariables())
		{			
			String value = variable.retrieveVariable(responseBody);
			if(null == value) continue;
			variableValues.put(variable.getVariableName(), value);
		}
	}
	
	/**
	 * Indicates all Variables that a given HTTP Action will retrieve
	 */
	public List<VariableSubstitution> retrievalVariablesForAction(PlayerHTTPAction action)
	{
		List<VariableSubstitution> retrievalVariables = new ArrayList<VariableSubstitution>(variableSubstitutions.size());
		for (VariableSubstitution variable : variableSubstitutions)
		{
			//Not all variables apply to all paths
			if(variable.retrievalPathMatches(action.getPath())) retrievalVariables.add(variable);
		}
		return retrievalVariables;
	}
	
	/**
	 * Indicates all Variables that a given HTTP Action will replace
	 */
	public List<VariableSubstitution> replacementVariablesForAction(PlayerHTTPAction action)
	{
		List<VariableSubstitution> replacementVariables = new ArrayList<VariableSubstitution>(variableSubstitutions.size());
		for (VariableSubstitution variable : variableSubstitutions)
		{
			//Not all variables apply to all paths
			if(variable.replacementPathMatches(action.getPath())) replacementVariables.add(variable);
		}
		return replacementVariables;
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
