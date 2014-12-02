package com.anz.rer.etl.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.anz.rer.etl.config.CsvReaderTaskConfig;
import com.anz.rer.etl.utils.CsvObject;
import com.anz.rer.etl.utils.CsvUtils;

public class CsvReaderTask implements Callable<Integer>{

	private final static Logger logger = Logger.getLogger(CsvReaderTask.class);
    
	private BlockingQueue<CsvObject> csvRecordsQueue;
	private CsvReaderTaskConfig csvConfig;
	private File csvFile;
	
	public CsvReaderTask(BlockingQueue<CsvObject> csvRecordsQueue, CsvReaderTaskConfig csvConfig, File csvFile) {
		this.csvRecordsQueue = csvRecordsQueue;
		this.csvConfig = csvConfig;
		this.csvFile = csvFile;
	}

	@Override
	public Integer call() throws Exception {
		return process(csvRecordsQueue,  csvConfig,  csvFile);
	}

	
 public int process(BlockingQueue<CsvObject> csvRecordsQueue, CsvReaderTaskConfig csvConfig, File csvFile) {
      long start = System.currentTimeMillis();
		
	   logger.info("Starting CsvReaderTask:" +  csvFile.getAbsolutePath());
	   logger.info("CsvReaderTask Partition:" + csvConfig.getCsvPartition());
	    
	    int totalLineNumber = 0;
	    int totalPart = 0;
		try {
			totalLineNumber = CsvUtils.getNumberOfLine(csvFile.getAbsolutePath());
			 totalPart = this.getTotalPartition(totalLineNumber, csvConfig.getCsvPartition());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	   
		Scanner lineScan = null; 
		boolean skipLine = csvConfig.isSkipFirstLine();
		int count = 0;
		try {
		   Pattern rowDelim = Pattern.compile( csvConfig.getRowDelimeter() );
		   //Pattern colDelim = Pattern.compile( csvConfig.getColumnDelimeter());
		   
		   lineScan = new Scanner( new FileReader(  csvFile ) );
			if (lineScan != null) {
				lineScan.useDelimiter(rowDelim);
					int ctr = 0; 
					List<Object[]> csvLines = new ArrayList<Object[]>();
					while (lineScan.hasNext()) {
						        	 if(skipLine){
						        	     skipLine=false;
						        		 continue;	 
						        	 } else {
						        		 String row = lineScan.next() + " ";// rows.next();
						        		 if( !row.startsWith(csvConfig.getIgnoreString()) && row.trim().length()>0 ){
						                     csvLines.add( row.split( csvConfig.getColumnDelimeter(),-1 )  );		 
						        		     row = null;
						                     if(ctr== csvConfig.getCsvPartition()) {
							        			 CsvObject csvObj = new CsvObject();
							        			 csvObj.setName(csvFile.getName());
							        			 csvObj.setTotalPartition( totalPart );
							        			 csvObj.setTotalLineNumber(totalLineNumber);
							        			 
							        			           csvObj.setRows(csvLines);
							      	        			  csvRecordsQueue.put(csvObj);
							        			 
							      	        	  csvLines = new ArrayList<Object[]>();
							        			 
							        			 ctr=-1;
						                     } 
							        		 ctr++;
							        		 count++;
						        		 }
						        	 }
					}
					if(csvLines!=null && !csvLines.isEmpty()){
	        			 CsvObject csvObj = new CsvObject();
	        			 csvObj.setName(csvFile.getName());
	        			 csvObj.setRows( csvLines);
	        			 
	        			 csvObj.setTotalPartition( totalPart );
	        			 csvObj.setTotalLineNumber(totalLineNumber);
	        			 
	        			 logger.info("CsvReaderTask Putting to Queue:" + csvObj.getRows().size() );
         				 csvRecordsQueue.put(csvObj);
	        			 //csvObj = null;
					}
					
					logger.debug("CsvReaderTask signaling end process total line:" + count);
					CsvObject csvObj = new CsvObject(true);
	   			 			  csvObj.setName(csvFile.getName());
	   			 			  csvObj.setRows( new ArrayList<Object[]>());
	   			 			  if(csvObj.getRows()!=null){
	   			 				logger.debug("CsvReaderTask Put EOF current line obj:" + csvObj.getRows().size() );  
	   			 			  }else {
	   			 				logger.debug("CsvReaderTask Put EOF current no more line to process");	  
	   			 			  }
	   			 			  csvRecordsQueue.put( csvObj);
	   		}
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (NullPointerException e) {
			
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lineScan.close();
			
			 logger.info("CsvReaderTask End process Total record:" + count);
		}
		logger.info(csvFile.getName() +" parsing time for " + count + " number of lines:" + (System.currentTimeMillis() - start) + " ms");
		return count;
	}

	 private int getTotalPartition( int totalLine, int partitionSize ){
	 	int x = totalLine / partitionSize;
	 	int y = totalLine % partitionSize;
	 
	 	if(y > 0 ){
	 		x++;
	 	}
	 	return x;
	 }
  
   public static void main(String[] args){
	   
	   CsvReaderTaskConfig csvConfig = new CsvReaderTaskConfig();
	   csvConfig.setSkipFirstLine(false);
	   csvConfig.setRowDelimeter("(\\r\\n|\\r|\\n|\\n\\r)" );
	
		csvConfig.setColumnDelimeter(",");
		csvConfig.setIgnoreString("#");
		csvConfig.setCsvPartition(5000);
	   
	   
	   BlockingQueue<CsvObject> csvRecordsQueue =  new ArrayBlockingQueue<CsvObject>(100);
	   String fileName = "c:\\subcube\\archive\\SIGM1#GAMMA_BASIS#NON-VAR#null#20120731#2085841370765702.apx";
	          fileName = "C:\\temp\\ANZ\\SIGM0#IR_GAMMA#NON-VAR#2704901785946#20121029#2704901785946_xxx.APX";
	    
	   CsvReaderTask csvReaderTask = new CsvReaderTask(csvRecordsQueue,csvConfig,  new File(fileName) );
           int count = csvReaderTask.process(csvRecordsQueue, csvConfig,   new File(fileName));
	   System.out.println(count);
   }
}
