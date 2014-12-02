package com.anz.rer.etl.utils;

import org.apache.log4j.Logger;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static Logger log = Logger.getLogger(ExceptionHandler.class);
	
	@Override
	public void uncaughtException(Thread t, Throwable ex) {
		log.error("Uncaught exception in thread:"   + t.getName() + "\n LocalMess:" + ex.getLocalizedMessage() + "\n Message:" + ex.getMessage() , ex);
	}

	 public static void registerExceptionHandler() {
		    Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		    System.setProperty("sun.awt.exception.handler", ExceptionHandler.class.getName());
	  }
	 
	public static void main(String[] args){
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		
		String x = null;
		
		System.out.println( x.split("xx"));
	}
}
