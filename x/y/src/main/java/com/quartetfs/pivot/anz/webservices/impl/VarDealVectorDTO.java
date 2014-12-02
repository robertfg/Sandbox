/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.webservices.impl;

import java.util.Collections;
import java.util.List;

public class VarDealVectorDTO {
	private long dealNumber;
	private double[] vector;
	private List<Object> attributesValue = Collections.emptyList();
	
	public VarDealVectorDTO(){};

	public VarDealVectorDTO(long dealNumber, double[] vector, List<Object> attributesValue){
		this.dealNumber=dealNumber;
		this.vector=vector;
		this.attributesValue = attributesValue;
	}
	public long getDealNumber() {
		return dealNumber;
	}

	public double[] getVector() {
		return vector;
	}
	public void setDealNumber(long dealNumber) {
		this.dealNumber = dealNumber;
	}
	public void setVector(double[] vector) {
		this.vector = vector;
	}

	public List<Object> getAttributesValue() {
		return attributesValue;
	}

	public void setAttributesValue(List<Object> attributesValue) {
		this.attributesValue = attributesValue;
	}

}
