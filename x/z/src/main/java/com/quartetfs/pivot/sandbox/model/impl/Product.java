/*
 * (C) Quartet FS 2007-2009
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.model.impl;


import java.io.Serializable;

import com.quartetfs.fwk.IClone;

/**
 * 
 * A financial product.
 * 
 * @author Quartet FS
 *
 */
public class Product implements IClone<Product>, Serializable{

	/** serialVersionUID */
	private static final long serialVersionUID = -3147979015813031848L;
	
	/** Product identifier */
	protected int id;
	protected String productType;
	protected String productName;
	protected double productBaseMtm;
	protected String underlierCode;
	protected String underlierCurrency;
	protected String underlierType;
	protected double underlierValue;
	protected double bumpedMtmUp;
	protected double bumpedMtmDown;

	//empty constructor
	public Product() { }

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append(" (");
		sb.append("id=").append(getId());
		sb.append(", productType=").append(getProductType());
		sb.append(", productName=").append(getProductName());
		sb.append(", productBaseMtm=").append(getBaseMtm());
		sb.append(", underlierCode=").append(getUnderlierCode());
		sb.append(", underlierValue=" ).append(getUnderlierValue());
		sb.append(", underlierCurrency=").append(getUnderlierCurrency());
		sb.append(", underlierType=").append(getUnderlierType());
		sb.append(")");
		return sb.toString();
	}

	@Override
	public Product clone(){
		try {
			Product clone= (Product) super.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public int getId() { return id; }
	public void setId(int productId) { this.id = productId; }
	public String getProductName() { return productName; }
	public void setProductName(String productName) { this.productName = productName; }
	public String getProductType() { return productType; }
	public void setProductType(String productType) { this.productType = productType; }
	public double getBumpedMtmDown() { return bumpedMtmDown; }
	public void setBumpedMtmDown(double bumpedMtmDown) { this.bumpedMtmDown = bumpedMtmDown; }
	public double getBumpedMtmUp() { return bumpedMtmUp; }
	public void setBumpedMtmUp(double bumpedMtmUp) { this.bumpedMtmUp = bumpedMtmUp; }
	public double getBaseMtm() { return productBaseMtm; }
	public void setProductBaseMtm(double productBaseMtm) { this.productBaseMtm = productBaseMtm; }
	public String getUnderlierCode() { return underlierCode; }
	public void setUnderlierCode(String underlierCode) { this.underlierCode = underlierCode; }
	public String getUnderlierCurrency() { return underlierCurrency; }
	public void setUnderlierCurrency(String underlierCurrency) { this.underlierCurrency = underlierCurrency; }
	public String getUnderlierType() { return underlierType; }
	public void setUnderlierType(String underlierType) { this.underlierType = underlierType; }
	public double getUnderlierValue() { return underlierValue; }
	public void setUnderlierValue(double underlierValue) { this.underlierValue = underlierValue; }

	@Override
	public int hashCode() {	return id; }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Product other = (Product) obj;
		if (id != other.id)
			return false;
		return true;
	}
}