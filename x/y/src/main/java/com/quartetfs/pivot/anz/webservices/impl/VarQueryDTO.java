/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.webservices.impl;

import com.quartetfs.biz.pivot.query.IDrillthroughQuery;

public class VarQueryDTO {
	
	private IDrillthroughQuery dtQuery;//used as wrapper to retriweve the locations & the contextValues
	private double confidenceLevel;//used by varDrillthrough
	private int from;//used by varDataExtract
	private int to;//used by varDataExtract
	private String varType;//distinguish between 1D & 10D var
	
	public String getVarType() {
		return varType;
	}
	public void setVarType(String varType) {
		this.varType = varType;
	}
	public IDrillthroughQuery getDtQuery() {
		return dtQuery;
	}
	public void setDtQuery(IDrillthroughQuery dtQuery) {
		this.dtQuery = dtQuery;
	}
	
	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public int getTo() {
		return to;
	}
	public void setTo(int to) {
		this.to = to;
	}
	public double getConfidenceLevel() {
		return confidenceLevel;
	}
	public void setConfidenceLevel(double confidenceLevel) {
		this.confidenceLevel = confidenceLevel;
	}


}
