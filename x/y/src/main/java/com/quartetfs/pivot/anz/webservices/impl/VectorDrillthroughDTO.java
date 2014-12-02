package com.quartetfs.pivot.anz.webservices.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class VectorDrillthroughDTO {
	private Set<String> attributesHeader = Collections.emptySet();
	private List<VectorDealValueDTO> deals = Collections.emptyList();
	private List<String> vectorLabels= Collections.emptyList();
	
	
	public VectorDrillthroughDTO(){};
	
	public Set<String> getAttributesHeader() {
		return attributesHeader;
	}
	
	public List<String> getVectorLabels() {
		return vectorLabels;
	}
	
	public List<VectorDealValueDTO> getDeals() {
		return deals;
	}
	
	public void setAttributesHeader(Set<String> attributesHeader) {
		this.attributesHeader = attributesHeader;
	}
	
	public void setDeals(List<VectorDealValueDTO> deals) {
		this.deals = deals;
	}
	
	public void setVectorLabels(List<String> vectorLabels) {
		this.vectorLabels = vectorLabels;
	}

}
