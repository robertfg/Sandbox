package com.anz.rer.etl.csvToTable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

public class BcpTransactionCoordinator {
	
	private final static Logger logger = Logger.getLogger(BcpTransactionCoordinator.class);
	
	private ConcurrentMap<String, BcpData > transaction = new ConcurrentHashMap<String, BcpData >();   
	
	public synchronized boolean transactionDone(BcpData bcpData){
		
		BcpData temp = transaction.get( bcpData.getName());
		boolean done = false;
		
		if( temp!=null  ){
			
			//logger.info( "existing - " + temp.getName() +  ":get tran count=" +  temp.getTranCtr());
			//logger.info( "existing - " + temp.getName() +  ":set tran count + 1 " );
			   
			temp.setTranCtr( temp.getTranCtr() + 1  );
			//logger.info( "existing - " + temp.getName() +  ":get new tran count:" +  temp.getTranCtr() );
			
			transaction.replace(bcpData.getName(), temp);
			//logger.info("cheking:" + temp.getTranCtr() + ":" + temp.getTotalPartition());
			if(temp.getTranCtr() == temp.getTotalPartition()){
				  transaction.remove( bcpData.getName() );
				   return true;
			}
		} else {
		  
		   //logger.info( bcpData.getName() +  ":get tran count=" +  bcpData.getTranCtr());
		   //logger.info( bcpData.getName() +  ":set tran count + 1 " );
		   bcpData.setTranCtr( bcpData.getTranCtr() + 1  );
		   //logger.info( bcpData.getName() +  ":get new tran count:" +  bcpData.getTranCtr() );
			
		   transaction.put(bcpData.getName(), bcpData);
		   if(bcpData.getTranCtr() == bcpData.getTotalPartition()){
			   transaction.remove( bcpData.getName() );
			   return true;
		   }
		   
		}
		return done;
	} 
	
}       
