/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.webservices.impl;

import java.util.Collections;
import java.util.List;

public class VarDealValueDTO {
	private long dealNumber;
	private double value;
	private List<Object> attributesValue = Collections.emptyList();
    
	public VarDealValueDTO(){};

	public VarDealValueDTO(long dealNumber, double value, List<Object> attributesValue){
		this.dealNumber=dealNumber;
		this.value=value;
		this.attributesValue = attributesValue;
	}
	public long getDealNumber() { 
		return dealNumber;
	}

	public void setDealNumber(long dealNumber) {
		this.dealNumber = dealNumber;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public List<Object> getAttributesValue() {
		return attributesValue;
	}

	public void setAttributesValue(List<Object> attributesValue) {
		this.attributesValue = attributesValue;
	}

	
}
