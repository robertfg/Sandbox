/*
 * (C) Quartet FS 2007-2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.service.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.pivot.sandbox.impl.MessagesSandbox;

public class HistoricalDatesService {
	/** the logger **/
	private static Logger logger = Logger.getLogger(MessagesSandbox.LOGGER_NAME, MessagesSandbox.BUNDLE);

	private int nbHistoricalDates=0;

	public HistoricalDatesService(){}

	public int getNbHistoricalDates() {
		return nbHistoricalDates;
	}

	public void setNbHistoricalDates(int nbHistoricalDates) {
		if (nbHistoricalDates < 0){
			this.nbHistoricalDates=0;
			logger.log(Level.WARNING, MessagesSandbox.WARNING_NB_HIST_DATES_ZERO);
		}
		else 
			this.nbHistoricalDates = nbHistoricalDates;
	}

}
