package com.anz.rer.etl.worker;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.anz.rer.etl.cache.LookUp;
import com.anz.rer.etl.utils.CsvObject;
import com.anz.rer.etl.webservice.RwhInfo;

public class StampCsv {
	private final static Logger logger = Logger.getLogger(StampCsv.class);
	  
	private LookUp cache;
	
	public StampCsv(LookUp cache){
		this.cache = cache;
		logger.info("StampCsv Cache ID:" + cache.hashCode());
	}
	
	public CsvObject stampCsv(CsvObject csvObject){
		
		
		logger.info("Stamping start:" + csvObject.getName());
		long start = System.currentTimeMillis();
		try {
			   
			List<Object[]> rows = new ArrayList<Object[]>();
			
			for (Object[] csv : csvObject.getRows()) {
				
				String[] name = csvObject.getName().split("#",-1);
				String date   = name[4]; 
			
				 csv[0]  = "MainCube"; 
				 if( name[2].equals("HYPO") ){
					 
					 csv[3]  = name[2]; //;"containerName"; 
					 String hypoDate =  (String)csv[5];
					 
					 if(hypoDate.length()==7){
					   csv[5]  = "0"+ hypoDate.substring(0,1) + "-"+ hypoDate.substring(2,4) + "-20" + hypoDate.substring(5);
					 } else {
					   csv[5]  = hypoDate.substring(0,2) + "-"+ hypoDate.substring(3,5) + "-20" + hypoDate.substring(6); 
					 }
				 }else {
					 csv[3]  = name[1]; //;"containerName"; 
					 csv[5]  = date.substring(6) + "-"+ date.substring(4,6) + "-" + date.substring(0, 4); 
				 }
				// logger.info("2");	 
				
				 String positionID  = (String)csv[68];
				 RwhInfo trade = null;
				 if(positionID.equals("N/A")){ 
					 logger.info("=======================================================================================================");
					 logger.info("Postion ID is not a number:" + csv[68]);
					 logger.info("=======================================================================================================");
					 
				 }else {
				     try{
				    	 positionID = positionID.trim();
				         trade = cache.getByPositionID( Integer.valueOf(date), positionID );
				    	   
				         if(trade==null || trade.getPortfolioHierarchyPath().indexOf("N/A")!=-1){
				        	 logger.info("RwhInfo is null");
						     trade =  cache.getRwInfo(  Integer.valueOf(positionID), Integer.valueOf(date)  ) ;
					 	     logger.info("No RPH posID:" + positionID +",cobDate:" + date );
					 	  }
				         
				     } catch(Exception e){
				    	 logger.info("Postion ID problem:" + positionID + ",date:" + date + ",eMsg:"+ e.getLocalizedMessage());
				    	 e.printStackTrace();
				    	
				     } 
				 }
		
				 
				 
				 
				 csv[42] = name[0];//psrName
			
				 if(trade!=null) {
					
					 csv[44]   =   trade.getPortfolioHierarchyPath();
					 csv[6]    =   trade.getInstrumentCCY();			
					 csv[8]    =   trade.getPortfolio();
					 csv[9]    =   trade.getMxFamily();
					 csv[10]   =    trade.getMxGroup();
					 csv[11]   =   trade.getMxType();
					 csv[12]   =   trade.getInstrument();
					 csv[45]   =    trade.getGeographyHierarchyPath();
				
					 if(trade.getLegalEntityHierarchyPath()!=null){
						 csv[101]  =  trade.getLegalEntityHierarchyPath();
					 }else {
						 logger.info("legal entiry null---------------------");
						 	 
					 }
				 } else {
					  	 csv[0]  = "Not Cache"; 
					     csv[44] = " "; 
						 csv[8]  = "No-PortfolioName-" + name[1]; 
				  }
			
				 
				 rows.add(csv);
				
			}
		
			if(rows!=null && !rows.isEmpty())	{
				csvObject.setRows(rows);
			}
			
			logger.info("ending stampCsv rowSize:" + csvObject.getRows().size());
			
		}catch(Exception e){
			logger.info("-------------------------------------------------------------------------------------------------------------------------");
			e.printStackTrace();
			logger.info("-------------------------------------------------------------------------------------------------------------------------");
		}
		
		logger.info("Time to stamp: " + csvObject.getRows().size() + " rows, time:" + (System.currentTimeMillis() - start) + " ms., ObjName:" + csvObject.getName());
		return csvObject;
	}
	

	
	public static void main(String[] args){
		String t = "815818 ";
		
		System.out.println(t.trim() + ":");
		


	}
	
}

