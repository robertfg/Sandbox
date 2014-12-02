package com.anz.rer.etl.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GlobalStatusUtil {


	private ConcurrentMap<String,String> status = new ConcurrentHashMap<String,String>();

	public ConcurrentMap<String, String> getStatus() {
		return status;
	}

	public void setStatus(ConcurrentMap<String, String> status) {
		this.status = status;
	}
	
	
	
}
