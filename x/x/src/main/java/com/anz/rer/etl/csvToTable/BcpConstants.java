package com.anz.rer.etl.csvToTable;

public class BcpConstants {
	   
	public static String BCP            = "file-staging";
	public static String BCP_RETRY      = "file-staging-retry";
	
	public static String HEADER   = "insert-to-header";
	public static String HEADER_RETRY   = "insert-to-header-retry";
	
	public static String FACT     = "staging-fact";
	public static String FACT_RETRY     = "staging-fact-retry";
	
	public static String CLEANUP  = "do-cleanup";
	
	public static final String NA ="N/A";
    

}
