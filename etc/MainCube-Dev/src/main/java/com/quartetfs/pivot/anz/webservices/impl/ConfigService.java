/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.webservices.impl;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.service.impl.VectorLabelService;
import com.quartetfs.pivot.anz.utils.CubeEventKeeper;
import com.quartetfs.pivot.anz.webservices.IConfigService;


	public class ConfigService implements IConfigService {

	private Properties globalProperties = null;
	private String version=null;
	private static final String ERR_MSG="issue while loading properties file";
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	private CubeEventKeeper eventKeeper;
	private VectorLabelService vectorLabelService;
	@Override
	public String config() {
		if (globalProperties.isEmpty()){
			LOGGER.log(Level.SEVERE, MessagesANZ.ISSUE_GLOBAL_PROPERTIES);
			return ERR_MSG;
		}else
			return globalProperties.toString();
	}

	@Override
	public String version() {
		return version;
	}

	
	public void setProperties(Properties props ){
		globalProperties=props;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String printAllEvents() {
		return eventKeeper.printAll();		
	}
	
	public void setEventKeeper(CubeEventKeeper eventKeeper) {
		this.eventKeeper = eventKeeper;
	}

	@Override
	public String printParseEvents() {
		return eventKeeper.printParseEvents();
		
	}

	@Override
	public String printCommitEvents() {
		return eventKeeper.printCommitEvents();
	}

	@Override
	public String printDeleteEvents() {
		return eventKeeper.printDeleteEvents();		
	}

	@Override
	public void cleanAllEvents() {
		 eventKeeper.clean();
	}

	@Override
	public String vectorLabels() {
		return vectorLabelService.toString();
	}
	
	public void setVectorLabelService(VectorLabelService vectorLabelService) {
		this.vectorLabelService = vectorLabelService;
	}
}
