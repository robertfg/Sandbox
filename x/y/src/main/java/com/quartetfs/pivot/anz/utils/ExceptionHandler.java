package com.quartetfs.pivot.anz.utils;



import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.pivot.anz.impl.MessagesANZ;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);

	
	@Override
	public void uncaughtException(Thread t, Throwable ex) {
	
		LOGGER.log(Level.SEVERE,"Uncaught exception in thread:"   + t.getName() + "\n LocalMess:" + ex.getLocalizedMessage() + "\n Message:" + ex.getMessage() , ex);
	
	}

	 public static void registerExceptionHandler() {
		    
			 
		    Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		    System.setProperty("sun.awt.exception.handler", ExceptionHandler.class.getName());
		    LOGGER.log( Level.INFO, "***************** Registering Global Error handler for UncaughtException *****************************************" );
			  
	  }
	 
	public static void main(String[] args){
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		
		String x = null;
		
		System.out.println( x.split("xx"));
	}
}
