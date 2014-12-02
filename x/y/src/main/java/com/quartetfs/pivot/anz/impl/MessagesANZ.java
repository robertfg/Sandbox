package com.quartetfs.pivot.anz.impl;

import mazz.i18n.annotation.I18NMessage;
import mazz.i18n.annotation.I18NResourceBundle;

/**
 * (C) Quartet FS 2007-2009 ALL RIGHTS RESERVED. This material is the
 * CONFIDENTIAL and PROPRIETARY property of Quartet Financial Systems Limited.
 * Any unauthorized use, reproduction or transfer of this material is strictly
 * prohibited
 * 
 * <b>MessagesANZ</b>
 * 
 * This class is used for logging purpose
 * 
 * @author Quartet Financial Systems
 */
@I18NResourceBundle(baseName = "activepivot-anz")
public final class MessagesANZ {

	private MessagesANZ() { 
 
	}
 
	/**
	 * The default name of the logger(s) that will use those messages, like in
	 * <code>Logger.getLogger(String name)>/code>.
	 */
	public static final String LOGGER_NAME = "quartetfs.activepivot.anz";

	/**
	 * The message bundle name for this messages, this is the name of the
	 * generated messages resource file, and must be the same as the i18nlog
	 * baseName.
	 */
	public static final String BUNDLE = "activepivot-anz";


	@I18NMessage("=> Start parsing of file: {0}.")
	public static final String START_PARSING_FILE = "START_PARSING_FILE";
	
	@I18NMessage("=> Parsing of file completed: {0}.")
	public static final String END_PARSING_FILE = "END_PARSING_FILE";
	
	@I18NMessage("=> Start reducing of file: {0}.")
	public static final String START_REDUCING = "START_REDUCING";
	
	@I18NMessage("=> Reducing of file {0} completed in {1} ms.")
	public static final String END_REDUCING = "END_REDUCING";
	
	@I18NMessage("Non VR file {0} discarded.")
	public static final String NON_VR_FILE = "NON_VR_FILE";
	
	@I18NMessage("PSR name of file is not part of allowed PSRs:{0}.")
	public static final String NOT_ALLOWED_PSR = "NOT_ALLOWED_PSR";

	@I18NMessage("Cannot parse PSR name of file:{0}.")
	public static final String CANNOT_PARSE_PSR = "CANNOT_PARSE_PSR";

	@I18NMessage("the file {0} has been already processed, we skip it then.")
	public static final String ALREADY_PROCESSED = "ALREADY_PROCESSED";
	
	@I18NMessage("Publish of PSR {0} Container {1} to the publisher {2} took {3} ms.")
	public static final String PUBLISH_TRACE = "PUBLISH_TRACE";
	
	@I18NMessage("=> Start publishing PSR: {0}.")
	public static final String START_PUBLISH = "START_PUBLISH";
	
	@I18NMessage("=> Publishing of PSR {0} completed in {1} ms.")
	public static final String END_PUBLISHING = "END_PUBLISHING";
	
	@I18NMessage("=> Start commit PSR: {0}.")
	public static final String START_COMMIT = "START_COMMIT";
	
	@I18NMessage("=> Commit of PSR {0} completed in {1} ms.")
	public static final String END_COMMIT  = "END_COMMIT";
	
	@I18NMessage("Error when  adding data to schema.")
	public static final String ERROR_ADDING_DATA  = "ERROR_ADDING_DATA";
	
	@I18NMessage("Rollback transaction for PSR: {0}.")
	public static final String ROLLBACK_FOR_PSR  = "ROLLBACK_FOR_PSR";
	
	@I18NMessage("Rollback current transaction.")
	public static final String ROLLBACK_CURRENT  = "ROLLBACK_CURRENT";
	
	@I18NMessage("Problem rollbacking the transaction.")
	public static final String ROLLBACK_PROBLEM  = "ROLLBACK_PROBLEM";
	
	@I18NMessage("Transaction error.")
	public static final String ERR_TRANSACTION  = "ERR_TRANSACTION";
	
	@I18NMessage("Issue while loading global properties file.")
	public static final String ISSUE_GLOBAL_PROPERTIES  = "ISSUE_GLOBAL_PROPERTIES";
	
	@I18NMessage("Wrong varType,available values are:{0}.")
	public static final String WRONG_VAR_TYPE  = "WRONG_VAR_TYPE";
	
	@I18NMessage("Wrong Vector Containers,available values are:{0}.")
	public static final String WRONG_VECTOR_CONTAINER  = "WRONG_VECTOR_CONTAINER";
	
	
	@I18NMessage("Drillthrough props not available for the webservice.")
	public static final String WS_NO_DT_PROPS  = "WS_NO_DT_PROPS";
	
	@I18NMessage("Wrong confidenceLevel,available values are:{0}.")
	public static final String WRONG_CONFIDENCE_LEVEL  = "WRONG_CONFIDENCE_LEVEL";
	
	@I18NMessage("From and to are not consistent, from:[{0}] to:[{1}]")
	public static final String FROM_TO_INCONSISTENCY  = "FROM_TO_INCONSISTENCY";
	
	@I18NMessage("{0} is null.")
	public static final String NULL_DTO  = "NULL_DTO";
	
	@I18NMessage("Underlying drillthrough query is null.")
	public static final String NULL_DT_QUERY  = "NULL_DT_QUERY";
	
	@I18NMessage("Only a list containing one location is expected.")
	public static final String LOCATION_ISSUE = "LOCATION_ISSUE";
	
	@I18NMessage("Range location forbidden.")
	public static final String RANGE_LOCATION_FORBIDDEN  = "RANGE_LOCATION_FORBIDDEN";
	
	@I18NMessage("Multiselect not allowed for COB Date dimension.")
	public static final String MULTISELECT_COB_FORBIDDEN  = "MULTISELECT_COB_FORBIDDEN";

	@I18NMessage("Multiselect not allowed for Container dimension for Tarnspose Drill Through")
	public static final String MULTISELECT_CONTAINER_FORBIDDEN  = "MULTISELECT_CONTAINER_FORBIDDEN";
	
	@I18NMessage("[{0}] Results size:{1}.")
	public static final String RESULTS_SIZE  = "RESULTS_SIZE";
	
	@I18NMessage("[{0}] Size to send to the WS client:{1}.")
	public static final String RESULTS_TO_SEND  = "RESULTS_TO_SEND";
	
	@I18NMessage("The retrieved vector is null for the location:{0}.") 
	public static final String VECTOR_NULL  = "VECTOR_NULL";
	
	@I18NMessage("Error while parsing hierarchy from string :{0}. Incorrect heirarchy depth,expected:{1},Actual depth:{2}")
	public static final String ERR_PARSE_HIERARCHY="ERR_PARSE_HIERARCHY";
	
	@I18NMessage("Modified file(s): {0}.")
	public static final String MODIFIED_FILES="MODIFIED_FILES";
	
	@I18NMessage("New file(s): {0}.")
	public static final String NEW_FILES="NEW_FILES";
	
	@I18NMessage("file [{0}] skipped as no properties defined in the file.")
	public static final String NO_PROPS="NO_PROPS";
	
	@I18NMessage("file [{0}] skipped as the following property is not available : {1}.")
	public static final String PROP_NOT_AVAILABLE="PROP_NOT_AVAILABLE";
	
	@I18NMessage("[{0}] is not a valid date, expected yyyymmdd pattern.")
	public static final String NOT_VALID_DATE="NOT_VALID_DATE";
	
	@I18NMessage("Objects to be cleaned expected [{0}] for the date [{1}].")
	public static final String OBJECTS_EXPECTED="OBJECTS_EXPECTED";
	
	@I18NMessage("Cob date [{0}] cleaned, removed objects count [{1}].")
	public static final String CLEANUP_INFO="CLEANUP_INFO";
	
	@I18NMessage("Rebuild of the indexes starts.")
	public static final String START_REBUILD="START_REBUILD";
	
	@I18NMessage("Rebuild of the indexes done in [{0}] ms.")
	public static final String END_REBUILD="END_REBUILD";
	
	@I18NMessage("Rebuild failed.")
	public static final String FAIL_REBUILD="FAIL_REBUILD";
	
	@I18NMessage("Can not rename {0} to {1}.")
	public static final String FAIL_RENAME="FAIL_RENAME";
	@I18NMessage("File {0} renamed to {1}.")
	public static final String SUCCESS_RENAME="SUCCESS_RENAME";
	
	@I18NMessage("File {0} create DONE file to {1}.")
	public static final String SUCCESS_DONE_FILE_CREATION="SUCCESS_DONE_FILE_CREATION";
	
	@I18NMessage("File {0} create ERR file to {1}.")
	public static final String ERR_FILE_CREATED="ERR_FILE_CREATED";
		
	@I18NMessage("Fail to create DONE file.")
	public static final String FAIL_DONE_FILE_CREATION="FAIL_DONE_FILE_CREATION";
	
	
	@I18NMessage("Error while reducing for file: {0}")
	public static final String ERR_REDUCING="ERR_REDUCING";

	@I18NMessage("Error when starting parsing of File, incorrect state. Current Parse Data is not null")
	public static final String ERR_PARSE_STARTING = "ERR_PARSE_STARTING";
	
	@I18NMessage("Error when completeing parsing of file.")
	public static final String ERR_COMPLETEING = "ERR_COMPLETEING";
	
	@I18NMessage("Error when commiting data to cube for file {0}.")
	public static final String ERR_COMMIT = "ERR_COMMIT";
	
	@I18NMessage("Error creating file {0}")
	public static final String ERR_UNABLE_TOCREATE="ERR_UNABLE_TOCREATE";
	
	@I18NMessage("Parsing error in file: [{0}], not able to parse data related to the deal:[{1}].")
	public static final String DEAL_PARSING_ERR="DEAL_PARSING_ERR";
	
	@I18NMessage("PostProcessor {0} failed while calling retrieveAggregates method.")
	public static final String PP_ERR="PP_ERR";
	
	@I18NMessage("WebService issue while calling the method {0}.")
	public static final String WS_ERR="WS_ERR";
	
	@I18NMessage("Error while loading the file {0}.")
	public static final String PROP_LOADING_ERR="PROP_LOADING_ERR";
	
	@I18NMessage("Error while closing the file {0}.")
	public static final String PROP_CLOSING_ERR="PROP_CLOSING_ERR";
	
	@I18NMessage("Error while compeleting  parsing of  file {0}.")
	public static final String PROP_COMPLETEING_ERR="PROP_COMPLETEING_ERR";
	
	@I18NMessage("Error in parsing of  varDates file {0}.")
	public static final String ERR_PARSE_VARDATES="ERR_PARSE_VARDATES";

	@I18NMessage("Error in parsing of  file {0}, continue with next file. Check error logs for details")
	public static final String UNKNOWN_FILE_ERR = "UNKNOWN_FILE_ERR";
	
	@I18NMessage("Parser callback from incomplete file ignoring {0}, Check error logs for details")
	public static final String SKIP_INVALID_RECEIVE = "SKIP_INVALID_RECEIVE";
		 
	@I18NMessage("DealDetailQueryDTO is null.")
	public static final String DEAL_DETAIL_QUERY_NULL_DTO  = "DEAL_DETAIL_QUERY_NULL_DTO";
	
	@I18NMessage("DealDetailQueryDTO DealNumber is Zero(0).")
	public static final String DEAL_DETAIL_QUERY_DEAL_NUMBER_ZERO  = "DEAL_DETAIL_QUERY_DEAL_NUMBER_ZERO";

	@I18NMessage("DealDetailQueryDTO COBDate is null.")
	public static final String DEAL_DETAIL_QUERY_NULL_COBDATE  = "DEAL_DETAIL_QUERY_NULL_COBDATE";

	@I18NMessage("DealDetailQueryDTO Max Result is Zero(0).")
	public static final String DEAL_DETAIL_QUERY_MAX_RESULT_ZERO  = "DEAL_DETAIL_QUERY_MAX_RESULT_ZERO";
	
	@I18NMessage("Issue while parsing the COB Date {0}.")
	public static final String DATE_NOT_VALID  = "DATE_NOT_VALID";

	@I18NMessage("PostProcessor {0} failed while doing computation.")
	public static final String IR_VEGA_PP_ERR="IR_VEGA_PP_ERR";
	
	@I18NMessage("Missing params: COB Date={0}, Container={1}.")
	public static final String EXTRACTION_MISSING_PARAMS="EXTRACTION_MISSING_PARAMS";
	
	@I18NMessage("Extraction started for params: COB Date={0}, Container={1}.")
	public static final String EXTRACTION_INFO_PARAMS="EXTRACTION_INFO_PARAMS";
	
	@I18NMessage("Can not extract all the fields from the location:{0}.")
	public static final String EXTRACTION_FIELDS_ISSUE="EXTRACTION_FIELDS_ISSUE";
	
	@I18NMessage("Time taken : {0} (ms) in order to extact {1} entries in the file {2}.")
	public static final String EXTRACTION_TIME="EXTRACTION_TIME";
	
	@I18NMessage("Issue while extracting data.")
	public static final String EXTRACTION_ISSUE="EXTRACTION_ISSUE";
	
	@I18NMessage("IOException inside the procedure.")
	public static final String EXTRACTION_IO_ISSUE="EXTRACTION_IO_ISSUE";
	
	@I18NMessage("Unexpected comparator value:")
	public static final String UNEXPECTED_COMPARATOR_VALUE="UNEXPECTED_COMPARATOR_VALUE";
	
	@I18NMessage("Error encountered during JMX operation:{0}")
	public static final String JMX_OPERATION_PROBLEM = "JMX_OPERATION_PROBLEM";
	
	@I18NMessage("Invocation of data rebuild from JMX was successful")
	public static final String JMX_REBUILD_SUCCESSFUL = "JMX_REBUILD_SUCCESSFUL";
	
	@I18NMessage("Method in Bean cannot be found:{0}")
	public static final String JMX_BEAN_METHOD_NOT_FOUND = "JMX_BEAN_METHOD_NOT_FOUND";
	 
	@I18NMessage("Cannot connect to the JMX Host Please check JMX Address and Port {0}:{1}")
	public static final String JMX_HOST_CONNECTION_PROBLEM = "JMX_HOST_CONNECTION_PROBLEM";
	
	@I18NMessage("Bean Name cannot be found:{0}")
	public static final String JMX_BEAN_NOT_FOUND = "JMX_BEAN_NOT_FOUND";
	
	@I18NMessage("JMX Authentication failed! Invalid username or password")
	public static final String JMX_AUTHENTICATION_FAILED = "JMX_AUTHENTICATION_FAILED";
	
	@I18NMessage("JMX Bean and Method name cannot be NULL ")
	public static final String JMX_BEAN_METHOD_CANNOT_BE_NULL = "JMX_BEAN_METHOD_CANNOT_BE_NULL";
	
	
	
	
}
