/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.service;

import java.util.Collection;

/**
 * <b>ForexServiceListener</b>
 * 
 * @author Quartet Financial Systems
 *
 */
public interface IForexServiceListener {
    /**
     * Upon arrival of a message, the source listener is being called with this method.
     * @param currencies
     */
	public void onQuotationUpdate(Collection<String> currencies);
	
}
