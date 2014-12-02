package com.quartetfs.pivot.anz.service.export;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;
import com.quartetfs.biz.pivot.query.QueryTimeoutException;
import com.quartetfs.biz.pivot.query.impl.GetAggregatesQuery;
import com.quartetfs.pivot.anz.dto.ExportContainerMapping;
import com.quartetfs.pivot.anz.extraction.ExtractUtils;
import com.quartetfs.pivot.anz.extraction.SimpleRowBuilderTask;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.service.export.ExtractObject.ExtractType;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.DBUtils;
import com.quartetfs.pivot.anz.utils.QueryHelper;
import com.quartetfs.pivot.anz.webservices.impl.ExtractParamsDTO;

public class ExtractNonVarTask implements Callable<ExportTaskThreadInfo> /*Runnable */{
	
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	public ExtractNonVarTask(ExtractUtils extractUtils, ExtractParamsDTO extractParamsDTO,BlockingQueue<ExtractObject> fileWriterQueue,
			ExtractAPQueryPool extractAPQueryPool, int retryTime, int retryCount, DBUtils dbUtils ) {
			
			this.extractUtils = extractUtils;
			this.extractParamsDTO =  extractParamsDTO;
			this.fileWriterQueue = fileWriterQueue;
			this.extractAPQueryPool = extractAPQueryPool;
			this.retryTime = retryTime; 
			this.dbUtils = dbUtils;
			this.retryCount = retryCount;
			
	}

	private ExtractUtils extractUtils; 
	private ExtractParamsDTO extractParamsDTO;
	private BlockingQueue<ExtractObject> fileWriterQueue;
	private ExtractAPQueryPool extractAPQueryPool;
	private int retryTime;
	private  DBUtils dbUtils;
	private int retryCount;
	private int retCount;
	
	
	public void run(){
		extract(this.extractParamsDTO, extractUtils.getNonVarCsvOutputMapping() );
	}
	
	/*@Override*/
	public ExportTaskThreadInfo call() throws Exception {
		return extract(this.extractParamsDTO, extractUtils.getNonVarCsvOutputMapping() );
	}
	
	
	public ExportTaskThreadInfo extract(ExtractParamsDTO extractParamsDTO, List<ExtractHeader> nonVarCsvOutputMapping) 
	{
		
		ExportContainerMapping containerMapping = extractUtils.getMappingManager().getMappings().get(extractParamsDTO.getContainer());
		LOGGER.info(String.format("Exporting Data for params %s", extractParamsDTO));
		final AtomicLong count = new AtomicLong(0);
		try{  
			
			 QueryHelper queryHelper = new QueryHelper(extractUtils.getPivot());
			
			List<Map<String,Object>> queryParameters = extractUtils.generateMultipleQueryParameters(extractParamsDTO, containerMapping);
		
			//LOGGER.info("Query Used for QUERY Location Count:" + queryParameters.size());
			
			extractParamsDTO.setPsrName( extractUtils.getContainerNameToPsrName().get( extractParamsDTO.getContainer()) );
			extractParamsDTO.setPsrCode(extractParamsDTO.getPsrName());
			
			LOGGER.log(Level.INFO, "QueryPameters:" + queryParameters.toString() );
			ExportTaskThreadInfo tInfo =  extract( queryHelper, queryParameters, count, containerMapping,extractParamsDTO, 
					          extractParamsDTO.getFileBatchId(),nonVarCsvOutputMapping);
			
			queryHelper = null;
			queryParameters = null;
			nonVarCsvOutputMapping = null;
			extractParamsDTO = null;
			
			LOGGER.log(Level.INFO, "Releasing all resources............................");
			return tInfo;
			
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, MessagesANZ.EXTRACTION_ISSUE,e);		
			if(e instanceof  QueryTimeoutException   ){	
				try {
					dbUtils.spUpdateExtractStatus( extractParamsDTO.getCobDate(), extractUtils.getDbContainer(extractParamsDTO), "CubeDataExtractError" );
					if(extractParamsDTO.isDebug()){extractParamsDTO.setDebug(false);}
					if(this.retCount<retryCount){
				    	Thread.sleep( retryTime );
						this.retCount = retCount+1;
						this.extractAPQueryPool.getQueryQueue().put(this);
				    }
					
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}else {
				e.printStackTrace();
			}
			
			
		}
		return new ExportTaskThreadInfo(-1l, Long.valueOf(extractParamsDTO.getFileBatchId()) );
		
	}
	
	private ExportTaskThreadInfo extract( QueryHelper queryHelper, List<Map<String,Object>> queryParameters, final AtomicLong count,
			ExportContainerMapping containerMapping,ExtractParamsDTO extractParamsDTO, 
			String batchId,List<ExtractHeader> nonVarCsvOutputMapping ) throws Exception 
	{
	
		
		
		LOGGER.info("extract:" +containerMapping.getContainer() + "_");
		List<String> measures=extractUtils.transformMeasure(containerMapping.getContainer(),containerMapping.getMeasures());
		
		
		if(extractParamsDTO.isDebug()){throw new QueryTimeoutException( new GetAggregatesQuery( queryHelper.computeLocations(queryParameters) , measures), 480);}
		   
		ICellSet cellSet = queryHelper.getAggregatesQuery(queryParameters,measures);
		String header = extractUtils.getNonVarHeader();
		
		queryParameters = null;
		measures = null;
		ExportTaskThreadInfo tInfo=null;
		if (cellSet != null){
			 tInfo = writeData(queryHelper, count, cellSet, extractParamsDTO,header,"NON-VAR#" + batchId,
					ANZConstants.FILE_EXTRACTION_ORG, ANZConstants.FILE_EXTRACTION_TMP);
		}
		
		cellSet = null;
		queryHelper = null;
		extractParamsDTO = null;
		header = null;
		
		
		return tInfo ;//new ExportTaskThreadInfo(1, Long.valueOf(extractParamsDTO.getFileBatchId()) );
	
	}
	
	private ExportTaskThreadInfo writeData(final QueryHelper queryHelper,final AtomicLong count, final ICellSet cellSet,
		final ExtractParamsDTO extractParamsDTO, String header, String extractType, String findInFile,String replaceInFile) throws IOException {
		
		long startTime = System.currentTimeMillis();
	    long endTime   = System.currentTimeMillis();

	    String filePath = extractUtils.buildFilePath( extractParamsDTO,  extractType, replaceInFile, extractParamsDTO.getFileBatchId() ) ;	
		
	    try {
        	final SimpleRowBuilderTask nonVarDataBuilder = new SimpleRowBuilderTask();
     		
     		cellSet.forEachLocation(new ILocationProcedure() {
	 			 @Override
					public boolean execute(ILocation location, int rowId) {
	 				 nonVarDataBuilder.mapResultValue(location, queryHelper, cellSet, rowId, extractParamsDTO.getFileBatchId(), extractUtils.getNonVarCsvOutputMapping());
	 				 return true;
					} 
			 	}   
     		);
     		
     		 endTime = ((System.currentTimeMillis() - startTime) );
     		
     		 LOGGER.info("AP Extraction Time extractID:" + extractParamsDTO.getFileBatchId() + " " 
					+ endTime + " ms" + " Record Size:" + nonVarDataBuilder.getRows().size() + " PsrType:" + extractParamsDTO.getPsrName() );
     		  
     		fileWriterQueue.put( new ExtractObject( Long.valueOf(extractParamsDTO.getFileBatchId()), nonVarDataBuilder.getRows(),
     				              extractParamsDTO.isDone(), ExtractType.NON_VAR, filePath , extractParamsDTO.getTotalPartitionRequest()) );
     		 
    	    /*only for stand alone dev testing*/
     	   /* 	fileWriterQueue.put(  new ExtractObject( Long.valueOf(extractParamsDTO.getFileBatchId()) ,true) );*/
     		 
     		filePath = null;
     		header = null;
     		extractType = null;
     		findInFile = null;
     		replaceInFile = null;	
     		
     		
     		
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, MessagesANZ.EXTRACTION_ISSUE, e);
			e.printStackTrace();
		} 


		return new ExportTaskThreadInfo(endTime, Long.valueOf(extractParamsDTO.getFileBatchId()) );
	}
	
}
