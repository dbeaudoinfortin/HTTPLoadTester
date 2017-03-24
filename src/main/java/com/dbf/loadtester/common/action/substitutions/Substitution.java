package com.dbf.loadtester.common.action.substitutions;

public interface Substitution
{
	/**
	 * Initialize the Substitution by pre-compiling any patterns.
	 */
	public void init() throws IllegalArgumentException;
}
