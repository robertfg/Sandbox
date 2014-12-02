/*
 * (C) Quartet FS 2013
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.service;

/**
 * 
 * Data services of the Sandbox application.
 * 
 * @author Quartet FS
 *
 */
public interface IDataService {

	/**
	 * 
	 * Send a real-time update of trade records.
	 * 
	 * @param nbNew number of new trades in the update
	 * @param nbUpdates number of existing trades updated by this update
	 *
	 */
	void sendTradeUpdate(int nbNew, int nbUpdates);

}