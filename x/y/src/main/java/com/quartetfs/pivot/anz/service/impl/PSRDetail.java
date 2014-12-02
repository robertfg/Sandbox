/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.quartetfs.pivot.anz.utils.ANZConstants;
/**
 * Utility Class which keep list of ALL PSRs processed by cube.
 * Also keep mapping of VARPSR location in projection array submitted to cube 
 * 
 *
 */
public class PSRDetail {

	public enum PublisherType
	{
		Sensitivities,
		Var,
		Vector,
		InsertUpdate
	};
	
	private Map<String, Integer> varPSR = new HashMap<String, Integer>();
	private Map<String, List<String>> measure2psr = new HashMap<String, List<String>>();
	private Map<String, Integer> field2IndexPosition = new HashMap<String, Integer>();
	private Map<String, String>fieldsToClean=new HashMap<String, String>();
	private String[] indexerFields;
	
	private Set<String> nonVarPSR=new HashSet<String>();
	private Set<String> allPSRs=new HashSet<String>();
	private Set<String> vectorPSRs=new HashSet<String>();

	private Set<String> insertUpdatePSRs=new HashSet<String>();

	private Map<String,String> varPsrToDateNameMapping = new HashMap<String,String>();
	
	private Map<String,String> varPsrToContainerNameMapping = new HashMap<String,String>();
	
	
	public void setVarPSR(Map<String, Integer> varPSR) {
		this.varPSR = Collections.unmodifiableMap(varPSR);
		allPSRs.addAll(varPSR.keySet());
	}
	
	
	public void setMeasure2psr(Map<String, String> measure2psr) {
		for(Entry<String, String>entry:measure2psr.entrySet()){
			String value=entry.getValue();
			this.measure2psr.put(entry.getKey(), getValues(value));
		}
	}

	public void setIndexerFields(final String[] indexerFields) {
		this.indexerFields = indexerFields;
	
	}
	
	private List<String> getValues(String value){
		String values[]=value.split(ANZConstants.COMMA_SEPARATOR);
		return Arrays.asList(values);
	}

	// public methods
	public Map<String, Integer> retrievePsr2Idx() {
		return varPSR;
	}

	public List<String> retrievePSR(final String measure) {
		return measure2psr.get(measure);
	}

	public Set<String> retrieveVarPSRs() {
		return varPSR.keySet();
	}

	public String[] retrieveIndexerFields() {
		return indexerFields.clone();
	}
	
	public void setNonVarPSR(Set<String> nonVarPsr) {
		this.nonVarPSR = Collections.unmodifiableSet(nonVarPsr);
		this.allPSRs.addAll(nonVarPsr);
	}
	
	public Set<String> getNonVarPsr() {
		return nonVarPSR;
	}
	
	public Set<String> getAllPSRs(){
		return allPSRs;
	}
	
	public boolean isValidPSR(String psr){
		return allPSRs.contains(psr.trim());
	}
	
	public void setVectorPSR(Set<String> fxoPSR) {
		this.vectorPSRs = Collections.unmodifiableSet(fxoPSR);
		this.allPSRs.addAll(fxoPSR);
	}


	public Set<String> getVectorPSR() {
		return vectorPSRs;
	}
	
	public void setFieldToIndexPostion(Map<String, Integer> mapping){
		field2IndexPosition=mapping;
	}

	public Map<String, Integer> getField2IndexPosition() {
		return field2IndexPosition;
	}
	
	public void setFieldsToClean(Map<String, String> fieldsToClean) {
		this.fieldsToClean = fieldsToClean;
	}
	
	public Map<String, String> getFieldsToClean() {
		return fieldsToClean;
	}
	
	public void setInsertUpdatePSRs(Set<String> insertUpdatePSRs) {
		this.insertUpdatePSRs = Collections.unmodifiableSet(insertUpdatePSRs);
		this.allPSRs.addAll(insertUpdatePSRs);

	}


	public Set<String> getInsertUpdatePSRs() {
		return insertUpdatePSRs;
	}

	public PublisherType identifyPublisherType(String psrName)
	{
		if(getNonVarPsr().contains(psrName))
		{
			return PublisherType.Sensitivities;
		}
		else if(retrievePsr2Idx().containsKey(psrName))
		{
			return PublisherType.Var;
		}
		else if(getVectorPSR().contains(psrName))
		{
			return PublisherType.Vector;
		}
		else if( getInsertUpdatePSRs().contains(psrName))
		{
			return PublisherType.InsertUpdate;
		}
		return null;
	}


	public Map<String, String> getVarPsrToDateNameMapping() {
		return varPsrToDateNameMapping;
	}


	public void setVarPsrToDateNameMapping(
			Map<String, String> varPsrToDateNameMapping) {
		this.varPsrToDateNameMapping = varPsrToDateNameMapping;
	}


	public Map<String,String> getVarPsrToContainerNameMapping() {
		return varPsrToContainerNameMapping;
	}


	public void setVarPsrToContainerNameMapping(
			Map<String,String> varPsrToContainerNameMapping) {
		this.varPsrToContainerNameMapping = varPsrToContainerNameMapping;
	}


	
}
