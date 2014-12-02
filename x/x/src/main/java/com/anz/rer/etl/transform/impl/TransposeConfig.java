package com.anz.rer.etl.transform.impl;

import java.util.Map;

public class TransposeConfig {

	private Map<Integer,Integer> csvToUvrMappingConfig;
	private int uvrLength;
	
	public Map<Integer, Integer> getCsvToUvrMappingConfig() {
		return csvToUvrMappingConfig;
	}
	
	public void setCsvToUvrMappingConfig(Map<Integer, Integer> csvToUvrMappingConfig) {
		this.csvToUvrMappingConfig = csvToUvrMappingConfig;
	}
	
	public int getUvrLength() {
		return uvrLength;
	}
	public void setUvrLength(int uvrLength) {
		this.uvrLength = uvrLength;
	}

	
}
