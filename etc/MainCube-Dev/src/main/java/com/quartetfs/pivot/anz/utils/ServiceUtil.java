package com.quartetfs.pivot.anz.utils;

public class ServiceUtil {
    private static ServiceUtil instance = null;
	private QueryHelper queryHelper;
   
	protected ServiceUtil() {
	}
 
	public static ServiceUtil getInstance(){
		
		if(instance==null){
			instance = new ServiceUtil();
		}
		return instance;
	}
	
	public void setQueryHelper(QueryHelper queryHelper) {
		this.queryHelper = queryHelper;
	}

	public QueryHelper getQueryHelper() {
		return queryHelper;
	}
	
}
