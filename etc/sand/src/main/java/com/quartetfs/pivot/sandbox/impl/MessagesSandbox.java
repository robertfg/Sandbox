/*
 * (C) Quartet FS 2007-2009
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.impl;

import java.util.logging.Logger;

import mazz.i18n.annotation.I18NMessage;
import mazz.i18n.annotation.I18NResourceBundle;

/**
 * <b>MessagesSandbox</b>
 *
 * This class is used for logging purpose
 * @author Quartet Financial Systems
 */
@I18NResourceBundle(baseName = "activepivot-sandbox")
public class MessagesSandbox {
	/**
	 * The default name of the logger(s) that will use those
	 * messages, like in <code>Logger.getLogger(String name)>/code>.
	 */
	public static final String LOGGER_NAME = "quartetfs.activepivot.sandbox";

	/**
	 * The message bundle name for this messages, this is the name
	 * of the generated messages resource file, and must be the same
	 * as the i18nlog baseName.
	 */
	public static final String BUNDLE = "activepivot-sandbox";

	/** @return logger for the given class */
	public static Logger getLogger(Class<?> cls) { return Logger.getLogger(LOGGER_NAME, BUNDLE); }
	
	//Generic
	@I18NMessage("[{0}] {1} file loaded")
	public static final String INFO_FILE_LOADED = "INFO_FILE_LOADED";
	@I18NMessage("[{0}] {1} file was not loaded correctly.")
	public static final String SEVERE_FILE_NOT_LOADED = "SEVERE_FILE_NOT_LOADED";
	@I18NMessage("Unexpected Exception")
	public static final String UNEXPECTED_EX = "UNEXPECTED_EX";

	//TradeSource
	@I18NMessage("[TradeSource] Trades are not generated check tradeCount value")
	public static final String SEVERE_TRADES_NOT_GENERATED = "SEVERE_TRADES_NOT_GENERATED";
	@I18NMessage("[TradeSource] Source initialized")
	public static final String INFO_SOURCE_INITIALIZED = "INFO_SOURCE_INITIALIZED";
	@I18NMessage("[TradeSource] Source started")
	public static final String INFO_SOURCE_STARTED = "INFO_SOURCE_STARTED";
	@I18NMessage("[TradeSource] Source stopped")
	public static final String INFO_SOURCE_STOPPED = "INFO_SOURCE_STOPPED";
	@I18NMessage("[TradeSource] Source paused")
	public static final String INFO_SOURCE_PAUSED = "INFO_SOURCE_PAUSED";
	@I18NMessage("[TradeSource] Source resumed")
	public static final String INFO_SOURCE_RESUMED = "INFO_SOURCE_RESUMED";
	@I18NMessage("[TradeSource] **************** time spent: {0} ms to process: {1} objects sent by {2}")
	public static final String INFO_TRADE_SOURCE_REPORT = "INFO_TRADE_SOURCE_REPORT";
	@I18NMessage("[TradeSource] {0} random trades will be generated.")
	public static final String INFO_NB_TRADES_GENERATED = "INFO_NB_TRADES_GENERATED";
	@I18NMessage("[TradeSource] Looping over the trade list and sending the trades to the pivot.")
	public static final String INFO_LOOPING_START = "INFO_LOOPING_START";
	@I18NMessage("[TradeSource] Submission type not recognized (0, 1 and 2 are accepted), use default: single submission (submission type 0)")
	public static final String INFO_DEFAULT_SUBMISSION = "INFO_DEFAULT_SUBMISSION";


	//ObjectsFeeder
	@I18NMessage("[ObjectsFeeder] the schema {0} can not be retrieved")
	public static final String SEVERE_SCHEMA_NOT_RETRIEVED = "SEVERE_SCHEMA_NOT_RETRIEVED";
	@I18NMessage("[ObjectsFeeder] the indexer can not be retrieved")
	public static final String SEVERE_INDEXER_NOT_RETRIEVED = "SEVERE_INDEXER_NOT_RETRIEVED";
	@I18NMessage("[ObjectsFeeder] An object already indexed with the same key is being inserted, its key is: {0}")
	public static final String WARN_OBJ_EXISTS = "WARN_OBJ_EXISTS";
	@I18NMessage("[ObjectsFeeder] Replacing the object with key: {0}")
	public static final String INFO_REPLACING_OBJ = "INFO_REPLACING_OBJ";
	@I18NMessage("[ObjectsFeeder] TransactionException")
	public static final String EXC_TRANSACTION = "EXC_TRANSACTION";
	@I18NMessage("[ObjectsFeeder] Exception during rollback")
	public static final String EXC_ROLLBACK = "EXC_ROLLBACK";
	@I18NMessage("[ObjectsFeeder] Transaction rollbacked")
	public static final String INFO_ROLLBACK = "INFO_ROLLBACK";
	@I18NMessage("[ObjectsFeeder] UnsupportedQueryException while processing the key: {0}")
	public static final String EXC_UNSUPPORTED_QUERY = "EXC_UNSUPPORTED_QUERY";
	@I18NMessage("[ObjectsFeeder] QueryException while processing the key: {0}")
	public static final String EXC_QUERY = "EXC_QUERY";

	//HistoricalDatesService
	@I18NMessage("[HistoricalDatesService] nbHistoricalDates could not be negative, its value is has been set to zero.")
	public static final String WARNING_NB_HIST_DATES_ZERO = "WARNING_NB_HIST_DATES_ZERO";

	//ForexService
	@I18NMessage("[ForexService] Forex Service started")
	public static final String INFO_FOREX_SERVICE_STARTED = "INFO_FOREX_SERVICE_STARTED";
	@I18NMessage("[ForexService] Forex Real Time Thread already started, cannot start")
	public static final String INFO_FOREX_SERVICE_ALREADY_STARTED = "INFO_FOREX_SERVICE_ALREADY_STARTED";
	@I18NMessage("[ForexService] Value for {0} set to {1}")
	public static final String INFO_QUOTATION_CHANGED = "INFO_QUOTATION_CHANGED";
	
	//ForexHandler
	@I18NMessage("[ForexHandler] Reference currency context is not set.")
	public static final String FOREX_HANDLER_CONTEXT_NOT_SET = "FOREX_HANDLER_CONTEXT_NOT_SET";
	
	//ContinuousForexPostProcessor
	@I18NMessage("[ContinuousForexPostProcessor] Reference currency context is not set.")
	public static final String FOREX_PP_CONTEXT_NOT_SET = "FOREX_PP_CONTEXT_NOT_SET";

	@I18NMessage("Unable to configure logging from \"{0}\"")
	public static final String UNABLE_TO_CONFIGURE_LOGGING = "UNABLE_TO_CONFIGURE_LOGGING";

	@I18NMessage("Exception while computing the impacted location'")
	public static final String EXC_COMPUTING_IMPACTED_LOCATION = "EXC_COMPUTING_IMPACTED_LOCATION";
}
