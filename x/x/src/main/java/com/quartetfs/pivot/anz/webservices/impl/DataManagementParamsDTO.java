package com.quartetfs.pivot.anz.webservices.impl;

import java.util.HashMap;
import java.util.Map;

public class DataManagementParamsDTO {

	private String sDate;
    private String fileName;
    
	 
	
	private Map<String,Object> conditions = new HashMap<String,Object>();

	public void setConditions(Map<String,Object> conditions) {
		this.conditions = conditions;
	}

	public Map<String,Object> getConditions() {
		return conditions;
	}

	public void setsDate(String sDate) {
		this.sDate = sDate;
	}

	public String getsDate() {
		return sDate;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
	
	
}
