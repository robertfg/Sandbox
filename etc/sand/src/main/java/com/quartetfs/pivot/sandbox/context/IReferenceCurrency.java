/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.context;

import com.quartetfs.biz.pivot.context.IContextValue;
import com.quartetfs.fwk.IClone;

/**
 * Context value storing a reference currency.
 * 
 * @author Quartet Financial Systems
 *
 */
public interface IReferenceCurrency extends IContextValue, IClone<IContextValue>{

	/** @return the reference currency */
	String getCurrency();

}
