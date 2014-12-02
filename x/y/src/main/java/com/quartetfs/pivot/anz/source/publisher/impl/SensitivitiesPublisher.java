/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.source.publisher.impl;




import com.quartetfs.pivot.anz.service.impl.PSRDetail.PublisherType;
import com.quartetfs.pivot.anz.source.IRunParseData;

/**
 * <b>SensitivitiesPublisher<b/><br/>
 * Decide if the data should be published into the Sensitivities cube.<br/>
 * To do that it relies on a list of PSR names.
 * 
 * @author Quartet Financial Systems
 */
public class SensitivitiesPublisher extends SlicePublisher {
	public SensitivitiesPublisher() {
		super();
	}

	@Override
	public boolean publish(final IRunParseData runParseData) throws Throwable{
		//boolean publish = false;
		//if (getPsrNames().contains(runParseData.getPSRName())) {
		//	publish = super.publish(runParseData);
		//}
		//return publish;
		
		return super.publish(runParseData);		
	}

@Override
	public PublisherType getType() {		
		return PublisherType.Sensitivities;
	}

}
