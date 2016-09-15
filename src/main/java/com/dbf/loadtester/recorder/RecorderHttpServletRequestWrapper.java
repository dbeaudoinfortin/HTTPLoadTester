package com.dbf.loadtester.recorder;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ReadListener;
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
		return new RepeatableServletInputStream(new ByteArrayInputStream(requestBody));
	}
	
	public class RepeatableServletInputStream extends ServletInputStream
    {
        private ByteArrayInputStream  byteArrayInputStream;

        public RepeatableServletInputStream(ByteArrayInputStream  byteArrayInputStream)
        {
            this.byteArrayInputStream = byteArrayInputStream;
        }

        @Override
        public boolean isFinished() {
            return byteArrayInputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {}

        @Override
        public int read() throws IOException
        {
            return byteArrayInputStream.read();
        }
        @Override
        public void reset()
        {
            byteArrayInputStream.reset();
        }

    }
	
	public byte[] getRequestBody()
	{
		return requestBody;
	}
}
