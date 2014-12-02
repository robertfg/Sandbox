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

public class VarDrillthroughDTO {
	
	private List<VarDealValueDTO> deals = Collections.emptyList();
	private int scenarioIndex;
	private String scenarioDate;
	private Set<String> attributesHeader = Collections.emptySet();
	
	public VarDrillthroughDTO(){};

	public VarDrillthroughDTO(List<VarDealValueDTO> deals, int scenarioIndex){
		this.deals=deals;
		this.scenarioIndex=scenarioIndex;
	}

	public List<VarDealValueDTO> getDeals() {
		return deals;
	}

	public void setDeals(List<VarDealValueDTO> deals) {
		this.deals = deals;
	}

	public int getScenarioIndex() {
		return scenarioIndex;
	}

	public void setScenarioIndex(int scenarioIndex) {
		this.scenarioIndex = scenarioIndex;
	}

	public String getScenarioDate() {
		return scenarioDate;
	}

	public void setScenarioDate(String scenarioDate) {
		this.scenarioDate = scenarioDate;
	}

	public Set<String> getAttributesHeader() {
		return attributesHeader;
	}

	public void setAttributesHeader(Set<String> attributesHeader) {
		this.attributesHeader = attributesHeader;
	}

	
	
}
