package com.dbf.loadtester.player.management.server;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.dbf.loadtester.common.json.JsonEncoder;

@Provider
@Produces({ MediaType.APPLICATION_JSON })
public class JsonMessageBodyWriter implements MessageBodyWriter<Object>
{

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
	{
		return true;
	}

	@Override
	public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
	{
		return -1;
	}

	@Override
	public void writeTo(Object entity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException
	{
		JsonEncoder.toJson(entity, type, entityStream);
	}


}
