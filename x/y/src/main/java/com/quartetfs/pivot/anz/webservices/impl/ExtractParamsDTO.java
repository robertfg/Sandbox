/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.webservices.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.impl.DateParser;
import com.quartetfs.pivot.anz.service.export.ExtractObject.ExtractType;

public class ExtractParamsDTO {

	private String cobDate;
	private String previousCobDate;
	
	private String container;
	private String dimensionName;
	private String dimensionFilter;
	
	
	private String locationPath;
	
	private String psrName;
	
	private String fileBatchId = String.valueOf(System.currentTimeMillis() + System.nanoTime());
	

	
	private String varType;
	
	private String psrCode;
	private String extractCode;
	private long    execTime;
	private boolean done;
	private int totalPartitionRequest;
	private ExtractType extractType;
	
	private IDate iDate;
	private DateParser parser;
	
	private boolean fromFM;
	private String jobId = "-1";
	
	private List<String> locationPaths = new ArrayList<String>();
	
	private String varRefDateType;
	private boolean debug;
	
    public ExtractParamsDTO() {
    	parser = (DateParser) Registry.getPlugin(IParser.class).valueOf("date[yyyyMMdd]");
    }
    
    
    public ExtractParamsDTO(String container, String locationPath, String fileBatchId, 
    		String cobDate,boolean done,
    		int totalPartitionRequest, ExtractType extractType) {
    	this();
    	this.container = container;
    	this.locationPath = locationPath;
    	this.fileBatchId = fileBatchId;
    	setCobDate(cobDate);
    	this.done = done;
    	this.totalPartitionRequest = totalPartitionRequest;
    	this.extractType = extractType;
    	 
    }	
    
    public ExtractParamsDTO(String container, String locationPath, String fileBatchId, String cobDate, 
    		String previousCobDate, boolean done, int totalPartitionRequest, ExtractType extractType) {
    	
    	this (container,  locationPath,  fileBatchId,  cobDate, done,totalPartitionRequest, extractType);
    	this.previousCobDate = previousCobDate;
    }	
    

	public String getCobDate() {
		return cobDate;
	}
	public void setCobDate(String cobDate) {
		this.cobDate = cobDate;
		iDate = Registry.create(IDate.class, parser.parse( cobDate).getTime());
	}
	public String getContainer() {
		return container;
	}
	public void setContainer(String container) {
		this.container = container;
	}
	
	public void setLocationPath(String locationPath) {
		this.locationPath = locationPath;
	}
	
	public void setDimensionName(String dimensionName) {
		this.dimensionName = dimensionName;
	}
	
	public String getDimensionName() {
		return dimensionName;
	}
	
	public String getLocationPath() {
		return locationPath;
	}
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
		.append("cobDate",cobDate)
		.append("containerName",container)
		.append("dimensionName",dimensionName)
		.append("locationPath",locationPath)
		.append("previousCobDate",previousCobDate)
		.toString();	
	}
	
	public String getPsrName() {
		return psrName;
	}
	public void setPsrName(String psrName) {
		this.psrName = psrName; 
	}
	
	/*public void setuId(String uId) {
		this.uId = uId;
	}
	public String getuId() {
		return uId;
	}
*/
	public void setFileBatchId(String fileBatchId) {
		this.fileBatchId = fileBatchId;
	}

	public String getFileBatchId() {
		return fileBatchId;
	}

	public String getVarType() {
		return varType;  
	}

	public void setVarType(String varType) {
		this.varType = varType;
	}

	public String getPsrCode() {
		return psrCode;
	}

	public void setPsrCode(String psrCode) {
		this.psrCode = psrCode;
	}

	public String getExtractCode() {
		return extractCode;
	}

	public void setExtractCode(String extractCode) {
		this.extractCode = extractCode;
	}

	public long getExecTime() {
		return execTime;
	}

	public void setExecTime(long execTime) {
		this.execTime = execTime;
	}

	public List<String> getLocationPaths() {
		return locationPaths;
	}

	public void setLocationPaths(List<String> locationPaths) {
		this.locationPaths = locationPaths;
	}

	public String getDimensionFilter() {
		return dimensionFilter;
	}

	public void setDimensionFilter(String dimensionFilter) {
		this.dimensionFilter = dimensionFilter;
	}

	public String getPreviousCobDate() {
		return previousCobDate;
	}

	public void setPreviousCobDate(String previousCobDate) {
		this.previousCobDate = previousCobDate;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public int getTotalPartitionRequest() {
		return totalPartitionRequest;
	}

	public void setTotalPartitionRequest(int totalPartitionRequest) {
		this.totalPartitionRequest = totalPartitionRequest;
	}

	public ExtractType getExtractType() {
		return extractType;
	}

	public void setExtractType(ExtractType extractType) {
		this.extractType = extractType;
	}
	
	public IDate getIDate(){
		
		return iDate;
	}


	public boolean isFromFM() {  
		return fromFM;
	}


	public void setFromFM(boolean fromFM) {
		this.fromFM = fromFM;
	}


	public String getJobId() {
		return jobId;
	}


	public void setJobId(String jobId) {
		this.jobId = jobId;
	}


	public String getVarRefDateType() {
		return varRefDateType;
	}


	public void setVarRefDateType(String varRefDateType) {
		this.varRefDateType = varRefDateType;
	}


	public boolean isDebug() {
		return debug;
	}


	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
