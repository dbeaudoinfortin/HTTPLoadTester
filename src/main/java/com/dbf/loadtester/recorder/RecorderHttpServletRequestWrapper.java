package com.dbf.loadtester.recorder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;

/**
 * Wraps an HttpServletRequest so that the input stream (request body) is stored in memory and 
 * can be read multiple times.
 *
 */
public class RecorderHttpServletRequestWrapper extends HttpServletRequestWrapper
{
	private final byte[] requestBody;
	
	public RecorderHttpServletRequestWrapper (HttpServletRequest request) throws IOException
	{     
		super(request);
		requestBody = IOUtils.toByteArray(request.getInputStream());
	}

	@Override
	public ServletInputStream getInputStream()
	{
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(requestBody);
		ServletInputStream inputStream = new ServletInputStream()
		{
			@Override
			public int read() throws IOException
			{
				return byteArrayInputStream.read();
			}
		};
        return inputStream;  
	}
	
	public byte[] getRequestBody()
	{
		return requestBody;
	}
}
