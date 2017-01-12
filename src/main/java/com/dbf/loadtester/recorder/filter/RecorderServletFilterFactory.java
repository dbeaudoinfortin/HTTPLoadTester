package com.dbf.loadtester.recorder.filter;

import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.util.ImmediateInstanceHandle;

public class RecorderServletFilterFactory implements InstanceFactory<RecorderServletFilter>
{
	private RecorderServletFilterOptions options;
	
	public RecorderServletFilterFactory(RecorderServletFilterOptions options)
	{
		this.options = options;
	}
	
	@Override
	public InstanceHandle<RecorderServletFilter> createInstance() throws InstantiationException
	{
		return new ImmediateInstanceHandle<RecorderServletFilter>(new RecorderServletFilter(options));
	}

}
