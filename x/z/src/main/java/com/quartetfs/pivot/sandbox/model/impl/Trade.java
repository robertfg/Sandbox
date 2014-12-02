/*
 * (C) Quartet FS 2007-2009
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.model.impl;


import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.Date;

import com.quartetfs.fwk.IClone;

/**
 * <b>TradeDefinition</b>
 * 
 * This class is used in order to build a simple trade.
 * @author Quartet Financial Systems
 *
 */
public class Trade implements IClone<Trade>, Serializable{

	/** serial version uid for the class. */
	private static final long serialVersionUID = -3147979015813031848L;

	/** The current JVM's host name */
	protected static final String HOST_NAME = ManagementFactory.getRuntimeMXBean().getName();
	
	protected long key; // the key uniquely identifies the trade
	protected int bookId;
	protected int productId;
	protected double productBaseMtm;
	protected int productQtyMultiplier;
	protected String underlierCode;
	protected double underlierValue;
	protected String underlierCurrency;
	protected String underlierType;
	protected double bumpedMtmUp;
	protected double bumpedMtmDown;
	protected Date date;
	protected String status;
	protected Object dateBucket;
	protected String futureBucket;
	protected String productType;
	protected String productName;
	protected String isSimulated;

	//empty constructor
	public Trade(){}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("key: [").append(getKey());
		sb.append("] productId: [").append(getProductId());
		sb.append("] status: [").append(getStatus());
		sb.append("] bookId: [").append(getBookId());
		sb.append("] isSimulated: [").append(getIsSimulated());
		sb.append("] date: [").append(getDate());
		sb.append("] productType: [").append(getProductType());
		sb.append("] productName: [").append(getProductName());
		sb.append("] productBaseMtm: [").append(getProductBaseMtm());
		sb.append("] underlierCode: [").append(getUnderlierCode());
		sb.append("] underlierValue: [" ).append(getUnderlierValue());
		sb.append("] underlierCurrency: [").append(getUnderlierCurrency());
		sb.append("] underlierType: [").append(getUnderlierType());
		sb.append("]");
		return sb.toString();
	}


	@Override
	public Trade clone(){
		try{
			Trade clone= (Trade)super.clone();
			return clone;
		}catch (CloneNotSupportedException e){
			return null;
		}
	}

	//getters and setters used to retrieve members 
	//while processing an instance of this class in calculator, thanks to reflection mechanism
	public int getBookId() {
		return bookId;
	}
	public void setBookId(int bookId) {
		this.bookId = bookId;
	}
	public double getBumpedMtmDown() {
		return bumpedMtmDown;
	}
	public void setBumpedMtmDown(double bumpedMtmDown) {
		this.bumpedMtmDown = bumpedMtmDown;
	}
	public double getBumpedMtmUp() {
		return bumpedMtmUp;
	}
	public void setBumpedMtmUp(double bumpedMtmUp) {
		this.bumpedMtmUp = bumpedMtmUp;
	}
	public double getProductBaseMtm() {
		return productBaseMtm;
	}
	public void setProductBaseMtm(double productBaseMtm) {
		this.productBaseMtm = productBaseMtm;
	}
	public int getProductId() {
		return productId;
	}
	public void setProductId(int productId) {
		this.productId = productId;
	}
	public int getProductQtyMultiplier() {
		return productQtyMultiplier;
	}
	public void setProductQtyMultiplier(int productQtyMultiplier) {
		this.productQtyMultiplier = productQtyMultiplier;
	}
	public String getUnderlierCode() {
		return underlierCode;
	}
	public void setUnderlierCode(String underlierCode) {
		this.underlierCode = underlierCode;
	}
	public String getUnderlierCurrency() {
		return underlierCurrency;
	}
	public void setUnderlierCurrency(String underlierCurrency) {
		this.underlierCurrency = underlierCurrency;
	}
	public String getUnderlierType() {
		return underlierType;
	}
	public void setUnderlierType(String underlierType) {
		this.underlierType = underlierType;
	}
	public double getUnderlierValue() {
		return underlierValue;
	}
	public void setUnderlierValue(double underlierValue) {
		this.underlierValue = underlierValue;
	}
	public long getKey() {
		return key;
	}
	public void setKey(long key) {
		this.key = key;
	}
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date valueDate) {
		this.date = valueDate;
	}

	public Object getDateBucket() {
		return dateBucket;
	}

	public void setDateBucket(Object dateBucket) {
		this.dateBucket = dateBucket;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getFutureBucket() {
		return futureBucket;
	}

	public void setFutureBucket(String futureBucket) {
		this.futureBucket = futureBucket;
	}

	public String getIsSimulated() {
		return isSimulated;
	}

	public void setIsSimulated(String isSimulated) {
		this.isSimulated = isSimulated;
	}

	public String getHostName() {
		return HOST_NAME;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int)(key ^ (key >>> 32));
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Trade other = (Trade) obj;
		if (key != other.key)
			return false;
		return true;
	}
}