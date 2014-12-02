/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.webservices.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class VarDataExtractDTO {
	private List<VarDealVectorDTO> deals = Collections.emptyList();
	private String[] scenarioDates = null;;
	private Set<String> attributesHeader = Collections.emptySet();

	
	public List<VarDealVectorDTO> getDeals() {
		return deals;
	}

	public void setDeals(List<VarDealVectorDTO> deals) {
		this.deals = deals;
	}

	public String[] getScenarioDates() {
		return scenarioDates;
	}

	public void setScenarioDates(String[] scenarioDates) {
		this.scenarioDates = scenarioDates;
	}

	public Set<String> getAttributesHeader() {
		return attributesHeader;
	}

	public void setAttributesHeader(Set<String> attributesHeader) {
		this.attributesHeader = attributesHeader;
	}
}
