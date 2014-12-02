package com.quartetfs.pivot.anz.datasource.factory;

import java.util.HashMap;
import java.util.Map;

import com.quartetfs.pivot.anz.service.impl.PSRDetail.PublisherType;
import com.quartetfs.pivot.anz.source.publisher.IRunParseDataPublisher;

public class PublisherFactory {
	private Map<PublisherType,IRunParseDataPublisher> publisherMap = new HashMap<PublisherType, IRunParseDataPublisher>();
	
	public PublisherFactory(IRunParseDataPublisher[] publisher)
	{
		for(IRunParseDataPublisher pub : publisher)
		{
			publisherMap.put(pub.getType(), pub);
		}
	}
	
	public IRunParseDataPublisher create(PublisherType type)
	{
		return publisherMap.get(type);
	}
}
