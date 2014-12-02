package com.anz.rer.etl.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;



public class CsvObjectTransactionCoordinator {

	
private final static Logger logger = Logger.getLogger(CsvObjectTransactionCoordinator.class);
	
	private ConcurrentMap<String, CsvObject > transaction = new ConcurrentHashMap<String, CsvObject >();   
	
	public synchronized boolean transactionDone(CsvObject csvObject){
		
		CsvObject temp = transaction.get( csvObject.getName());
		boolean done = false;
		
		if( temp!=null  ){
			
			//logger.info( "existing - " + temp.getName() +  ":get tran count=" +  temp.getTranCtr());
			//logger.info( "existing - " + temp.getName() +  ":set tran count + 1 " );
			   
			temp.setTranCtr( temp.getTranCtr() + 1  );
			//logger.info( "existing - " + temp.getName() +  ":get new tran count:" +  temp.getTranCtr() );
			
			transaction.replace(csvObject.getName(), temp);
			//logger.info("cheking:" + temp.getTranCtr() + ":" + temp.getTotalPartition());
			if(temp.getTranCtr() == temp.getTotalPartition()){
				  transaction.remove( csvObject.getName() );
				   return true;
			}
		} else {
		  
		   //logger.info( bcpData.getName() +  ":get tran count=" +  bcpData.getTranCtr());
		   //logger.info( bcpData.getName() +  ":set tran count + 1 " );
			csvObject.setTranCtr( csvObject.getTranCtr() + 1  );
		   //logger.info( bcpData.getName() +  ":get new tran count:" +  bcpData.getTranCtr() );
			
		   transaction.put(csvObject.getName(), csvObject);
		   if(csvObject.getTranCtr() == csvObject.getTotalPartition()){
			   transaction.remove( csvObject.getName() );
			   return true;
		   }
		   
		}
		return done;
	} 

}
