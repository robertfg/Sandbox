package com.quartetfs.pivot.anz.datasource.factory;

import java.util.Properties;

import com.quartetfs.pivot.anz.datasource.impl.PSRPublisherConfigInfo;
import com.quartetfs.pivot.anz.datasource.impl.PSRPublisherService;
import com.quartetfs.pivot.anz.datasource.impl.PSRReducerService;
import com.quartetfs.pivot.anz.service.IDateService;
import com.quartetfs.pivot.anz.service.IValueHolderService;
import com.quartetfs.pivot.anz.service.impl.PSRDetail;
import com.quartetfs.pivot.anz.service.impl.PSRService;
import com.quartetfs.pivot.anz.service.impl.VectorLabelService;
import com.quartetfs.pivot.anz.utils.CubeEventKeeper;

public interface DataSourceServiceFactory {
	
	public enum BeanName
	{
		psrDetail,
		dateService,
		psrService, 
		VectorPSRS, 
		vectorLabelService,
		valueHolderService,
		PSRReducerService,
		cubeEventKeeper,
		PSRPublisherService,
		PSRPublisherConfigInfo,
		psrFileTriggerProperties,
		PublisherFactory
	} 
	
	public PSRDetail getPSRDetails(String name);
	public IDateService getDateService(String name);
	public PSRService getPSRService(String name);
	public VectorLabelService getVectorLabelService(String name);
	public IValueHolderService getValueHolderService(String name);
	public PSRReducerService getReducerService(String name);
	public CubeEventKeeper getCubeEventKeeper(String name);
	public PSRPublisherService getPublisherService(String name);
	public PSRPublisherConfigInfo getPublisherConfig(String name);
	public Properties getFileTriggerProperties(String name);
	public PublisherFactory getPublisherFactory(String name);
}
