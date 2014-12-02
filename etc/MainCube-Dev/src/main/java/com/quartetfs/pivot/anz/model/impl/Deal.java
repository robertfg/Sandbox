/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.model.impl;



import java.util.Map;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.pivot.anz.utils.ANZConstants;
/**
 * Deal implementation
 * 
 * @author Quartet Financial Systems
 */
public  class Deal {
	private final IDate date;
	private final long dealNum;
	private final String portfolio;
	private final String baseCCY;
	private String[] hierarchy;
	private String[] currPairHierarchy;
	private String[] currencyHierarchy;
	private String[] geoHierarchy;
	private String[] equityHierarchy;
	private String[] currencyGrouping;
	private String[] commodityHierarchy;
	private String psrExtH;
    private String[] issuerHierarchy;
    private String[] legalEntityHierarchy;
    private String[] dealCurrencyHierarchy;
    
    private String[] financeHierarchy;
    private String[] revenueHierarchy;
    
    


	private final  Map<String, Object> attributes;
	private String type;
	private String group;
	private String family;
	private String mcurr;
	private String instrument;
	private String containerName;
	private String psrName;
	private String psrExtA;
	private String psrExtB;
	
	public Deal(final IDate date, final long dealNum, final String portfolio,final String baseCCY,
			final String[] hierarchy, final String[] geoHierarchy, final Map<String, Object> attributes) {
		this.date = date;
		this.dealNum = dealNum;
		this.portfolio = portfolio;
		this.baseCCY = baseCCY;
		this.hierarchy = hierarchy;
		this.attributes = attributes;
		this.geoHierarchy=geoHierarchy;
	}


	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append(date).append("#");
		buffer.append(dealNum).append("#");
		buffer.append(portfolio).append("#");
		buffer.append(baseCCY);
		return buffer.toString();
	}

	@Override
	public Deal clone() {
		Deal clone = null;
		try {
			clone = (Deal) super.clone();
		} catch (final CloneNotSupportedException e) {
			clone = null;
		}
		return clone;
	}


	public String getPortfolio() {
		return portfolio;
	}


	public long getDealNum() {
		return dealNum;
	}


	public IDate getDate() {
		return date;
	}


	public String[] getHierarchy() {
		return hierarchy;
	}


	public Map<String, Object> getAttributes() {
		return attributes;
	}


	public boolean isHieratchyResolved() {
		return (portfolio!=null && portfolio.trim().length()>1 && 
				getHierarchy()!=null && getHierarchy().length>=ANZConstants.PORTFOLIOHIERARCHY_DEPTH);
	}


	public void assignHierarchy(String[] hierarchy) {
		this.hierarchy = hierarchy;
	}

	public String getBaseCCY() {
		return baseCCY;
	}



	public String getEquity() {
		return (String) attributes.get(ANZConstants.EQUITY);
	}

	public String getCurrency() {
		return (String) attributes.get(ANZConstants.CURRENCY);
	}


	public String getCurrencyPair() {
		return (String) attributes.get(ANZConstants.CURRENCY_PAIR);
	}



	public String getIrCurve() {
		return (String) attributes.get(ANZConstants.IR_CURVE);
	}



	public String getFxSpotPrice() {
		return (String) attributes.get(ANZConstants.FX_SPOT);
	}



	public String getEquityVol() {
		return (String) attributes.get(ANZConstants.EQUITY_VOL);
	}



	public String getFxVol() {
		return (String) attributes.get(ANZConstants.FX_VOL);
	}



	public String getShockDays() {
		return (String) attributes.get(ANZConstants.SHOCK_DAYS);
	}



	public String getTerm() {
		return (String) attributes.get(ANZConstants.TERM);
	}


	public String getStressScenario() {
		return (String) attributes.get(ANZConstants.STRESS_SCENARIOS);
	}


	public String[] getCurrPairHierarchy() {
		return currPairHierarchy;
	}

	public void setCurrPairHierarchy(String[] currPairHierarchy) {
		this.currPairHierarchy = currPairHierarchy;
	}

	public String[] getCurrencyHierarchy() {
		return currencyHierarchy;
	}

	public void setCurrencyHierarchy(String[] currencyHierarchy) {
		this.currencyHierarchy = currencyHierarchy;
	}


	public void setHierarchy(String[] hierarchy) {
		this.hierarchy = hierarchy;
	}


	public void setEquityHierarchy(String[] equityHierarchy) {
		this.equityHierarchy=equityHierarchy;
	}
	
	public String[] getEquityHierarchy() {
		return equityHierarchy;
	}

	public void setCurrencyGrouping(String[] currencyGroups) {
		this.currencyGrouping=currencyGroups;
	}

	public String[] getCurrencyGrouping() {
		return currencyGrouping;
	}
	
	public String[] getGeoHierarchy() {
		return geoHierarchy;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public void setFamily(String family) {
		this.family = family;
	}
	
	public String getFamily() {
		return family;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	public String getGroup() {
		return group;
	}
	
	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}
	
	public String getInstrument() {
		return instrument;
	}
	
	public void setMcurr(String mcurr) {
		this.mcurr = mcurr;
	}
	
	public String getMcurr() {
		return mcurr;
	}
	
	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}
	
	public String getContainerName() {
		return containerName;
	}
	
	public void setPsrName(String psrName) {
		this.psrName = psrName;
	}
	
	public String getPsrName() {
		return psrName;
	}


	public void setCommodityHierarchy(String[] commodityHierarchy) {
		this.commodityHierarchy = commodityHierarchy;
	}


	public String[] getCommodityHierarchy() {
		return commodityHierarchy;
	}
	
	public void setPsrExtA(String psrExtA) {
		this.psrExtA = psrExtA;
	}


	public String getPsrExtA() {
		return psrExtA;
	}


	public String getPsrExtB() {
		return psrExtB;
	}


	public void setPsrExtB(String psrExtB) {
		this.psrExtB = psrExtB;
	}
	
	public void setPsrExtH(String psrExtH) {
		this.psrExtH = psrExtH;
	}


	public String getPsrExtH() {
		return psrExtH;
	}


	public void setIssuerHierarchy(String[] issuerHierarchy) {
		this.issuerHierarchy = issuerHierarchy;
	}


	public String[] getIssuerHierarchy() {
		return issuerHierarchy;
	}


	public String[] getLegalEntityHierarchy() {
		return legalEntityHierarchy;
	}
 

	public void setLegalEntityHierarchy(String[] legalEntityHierarchy) {
		this.legalEntityHierarchy = legalEntityHierarchy;
	}


	public void setDealCurrencyHierarchy(String[] dealCurrencyHierarchy) {
		this.dealCurrencyHierarchy = dealCurrencyHierarchy;
	}


	public String[] getDealCurrencyHierarchy() {
		return dealCurrencyHierarchy;
	}


	public String[] getFinanceHierarchy() {
		return financeHierarchy;
	}


	public void setFinanceHierarchy(String[] financeHierarchy) {
		this.financeHierarchy = financeHierarchy;
	}
	
	public String[] getRevenueHierarchy() {
		return revenueHierarchy;
	}


	public void setRevenueHierarchy(String[] revenueHierarchy) {
		this.revenueHierarchy = revenueHierarchy;
	}
}
