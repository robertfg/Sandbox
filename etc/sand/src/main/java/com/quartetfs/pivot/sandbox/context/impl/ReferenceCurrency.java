/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.context.impl;

import com.quartetfs.biz.pivot.context.IContextValue;
import com.quartetfs.biz.pivot.context.impl.AContextValue;
import com.quartetfs.pivot.sandbox.context.IReferenceCurrency;

/**
 * 
 * Reference currency context value.
 * 
 * @author Quartet Financial Systems
 *
 */
public class ReferenceCurrency extends AContextValue implements IReferenceCurrency{

	/** serialVersionUID */
	private static final long serialVersionUID = -2979293772294831695L;

	/** reference currency */
	protected String currency;

	/** constructor */
	protected ReferenceCurrency() {}

	/** constructor */
	public ReferenceCurrency(String ccy) {
		this.currency = ccy;
	}

	@Override
	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	@Override
	public String toString() {
		return "Reference Currency: " + currency;
	}
	
	@Override
	public Class<? extends IContextValue> getContextInterface() {
		return IReferenceCurrency.class;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
		* result
		+ ((currency == null) ? 0 : currency.hashCode());
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
		ReferenceCurrency other = (ReferenceCurrency) obj;
		if (currency == null) {
			if (other.currency != null)
				return false;
		} else if (!currency.equals(other.currency))
			return false;
		return true;
	}

}
