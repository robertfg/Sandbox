package com.anz.rer.etl.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import com.anz.rer.etl.task.VectorTask;


public class GzipWriterTask implements Callable<Long> {

	private BlockingQueue<CsvObject> taskQueue;
	private OutputStream outputStream;
	private final static Logger logger = Logger.getLogger(GzipWriterTask.class);
    private CsvObjectTransactionCoordinator csvTranCoor;
	
	public GzipWriterTask(BlockingQueue<CsvObject> taskQueue, File exportFile  ) {
		this.taskQueue = taskQueue;
		try {
		   	
			outputStream = new GZIPOutputStream( new BufferedOutputStream(new FileOutputStream(exportFile)));
			logger.debug("Creating file:" + exportFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	public GzipWriterTask(BlockingQueue<CsvObject> taskQueue, File exportFile,	CsvObjectTransactionCoordinator csvTranCoor  ) {
		this.csvTranCoor = csvTranCoor;
		this.taskQueue = taskQueue;
		try {
			outputStream = new GZIPOutputStream( new BufferedOutputStream(new FileOutputStream(exportFile)));
			logger.debug("Creating file:" + exportFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
	}


	@Override
	public Long call() throws Exception {
		long totalTime = 0;
		CsvObject csvObject;

		while ((csvObject = taskQueue.take()) != null) {
			if (csvObject.isDone()){
				logger.debug("Shutting down GzipWriterTask....");
				outputStream.flush();				
				outputStream.close();	
				break;
			}
			totalTime+=call(csvObject);
			
			 
			
			/*  to enable transaction coordinator
			 * if(!csvObject.getRows().isEmpty()){
				 totalTime+=call(csvObject);
				 if(csvTranCoor.transactionDone(csvObject)){
					    logger.debug("Shutting down GzipWriterTask....");
						outputStream.flush();				
						outputStream.close();	
						break;
				 }
			 }*/
			 
		}
		logger.info("Total time to write:" +  csvObject.getName() + " " + totalTime + "ms" );
		return totalTime;
	}
	
	private Long call(CsvObject object) throws IOException {
		long start = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder(1000);
		for (Object[] row : object.getRows()) {
	      for (Object colValue : row) {
				
				sb.append(getColValue(colValue)).append(",");	
			}
	        	sb.append("\n");	
		}
		outputStream.write(sb.toString().getBytes());
		return System.currentTimeMillis() - start;
	}
	
	private Object getColValue(Object colValue) {
		if (colValue instanceof double[]) {
			double[] colValArray = (double[]) colValue;
			StringBuilder sb = new StringBuilder(colValArray.length * 5);
			for (double val : colValArray) {
				sb.append(val).append("|");
			}
			return sb.toString();
		}
		
		return (colValue == null || colValue.equals("null") || colValue.equals("N/A")) ? "" : colValue;
	}
}
