/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.service.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.quartetfs.pivot.anz.service.IPSRService;

public class PSRService implements IPSRService {
	private final AtomicLong stamp = new AtomicLong(-1);
	private final ConcurrentMap<String, String> psrToContainer = new ConcurrentHashMap<String, String>();
	private final ConcurrentMap<String, String> filesWithError = new ConcurrentHashMap<String, String>();
	private  String currentFilePsrName;

	@Override
	public void offer(final String psr, final String container) {
		System.out.println("psr:" + psr + ",container:" + container );
		psrToContainer.putIfAbsent(psr, container);
		stamp.incrementAndGet();
	}

	@Override
	public long getStamp() {
		return stamp.get();
	}

	@Override
	public Collection<String> getPSRs() {
		return psrToContainer.keySet();
	}

	@Override
	public String getContainerName(final String psr) {
		return psrToContainer.get(psr);
	}
	
	@Override
	public void errorWithFileLoad(String fileName, Throwable firstError){
			filesWithError.putIfAbsent(fileName, parseExceptionTrace(firstError));
	}
	
	@Override
	public void errorWithFileLoad(String fileName, String firstError) {
		filesWithError.putIfAbsent(fileName, firstError);
	}
	
	private String parseExceptionTrace(Throwable e){
		StringWriter sw=new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	@Override
	public boolean isFileLoadedWithError(String fileName){
		return filesWithError.containsKey(fileName);
	}

	public String getCurrentFilePsrName() {
		return currentFilePsrName;
	}

	public void setCurrentFilePsrName(String currentFilePsrName) {
		this.currentFilePsrName = currentFilePsrName;
	}

	

}
