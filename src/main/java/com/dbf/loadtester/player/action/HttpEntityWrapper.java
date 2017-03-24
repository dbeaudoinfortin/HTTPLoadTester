package com.dbf.loadtester.player.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;


/**
 * Wraps an HttpEntity so that response body is stored in memory and can be read multiple times.
 *
 */
public class HttpEntityWrapper implements HttpEntity
{
	private final HttpEntity entity;
	private String responseBody;
	
	public HttpEntityWrapper(HttpEntity entity)
	{
		this.entity = entity;
	}

	public boolean isRepeatable()
	{
		return entity.isRepeatable();
	}

	public boolean isChunked()
	{
		return entity.isChunked();
	}

	public long getContentLength()
	{
		return entity.getContentLength();
	}

	public Header getContentType()
	{
		return entity.getContentType();
	}

	public Header getContentEncoding()
	{
		return entity.getContentEncoding();
	}

	public InputStream getContent() throws IOException, UnsupportedOperationException
	{
		return entity.getContent();
	}

	public void writeTo(OutputStream paramOutputStream) throws IOException
	{
		entity.writeTo(paramOutputStream);
	}

	public boolean isStreaming()
	{
		return entity.isStreaming();
	}

	@SuppressWarnings("deprecation")
	public void consumeContent() throws IOException
	{
		entity.consumeContent();
	}

	public String getResponseBody()
	{
		return responseBody;
	}

	public void setResponseBody(String responseBody)
	{
		this.responseBody = responseBody;
	}
}
