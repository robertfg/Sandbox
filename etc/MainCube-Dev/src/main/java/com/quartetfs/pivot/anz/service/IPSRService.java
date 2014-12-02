/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.service;

import java.util.Collection;

/**
 * PSR service
 * @author Quartet Financial Systems
 */
public interface IPSRService {

	void offer(String psr, String container);
	
	long getStamp();
	
	Collection<String> getPSRs();
	
	String getContainerName(String psr);
	
	void errorWithFileLoad(String fileName, Throwable firstError);
	void errorWithFileLoad(String fileName, String firstError);
	boolean isFileLoadedWithError(String absoluteFilePath);
	
	String getCurrentFilePsrName();
	
}
