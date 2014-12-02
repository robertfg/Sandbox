package com.quartetfs.pivot.anz.webservices.impl;

import java.util.Collections;
import java.util.List;


public class VectorDealValueDTO {
	private  long dealNumber;
	private  List<Object> attributesValue = Collections.emptyList();
    private  double[] mresult;
    private  double[] mresultV;
    
    public VectorDealValueDTO(){};
    
    public VectorDealValueDTO(long dealNumber,List<Object> attributesValue,double[] mresult,double[] mresultV ) {
		this.dealNumber=dealNumber;
		this.attributesValue=attributesValue;
		this.mresult=mresult;
		this.mresultV=mresultV;
	}
    public double[] getMresult() {
		return mresult;
	}
    
    public double[] getMresultV() {
		return mresultV;
	}
     
     public long getDealNumber() {
		return dealNumber;
	}
     
     public List<Object> getAttributesValue() {
		return attributesValue;
	}
     
     public void setMresult(double[] mresult) {
		this.mresult = mresult;
	}
     
     public void setMresultV(double[] mresultV) {
		this.mresultV = mresultV;
	}
     public void setDealNumber(long dealNumber) {
		this.dealNumber = dealNumber;
	}
     public void setAttributesValue(List<Object> attributesValue) {
		this.attributesValue = attributesValue;
	}
    
}
