package com.quartetfs.pivot.anz.service.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.springframework.util.StopWatch;

import com.quartetfs.biz.pivot.IActivePivotSchema;
import com.quartetfs.biz.pivot.IProjection;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.IPair;
import com.quartetfs.fwk.filtering.ICondition;
import com.quartetfs.fwk.filtering.impl.AndCondition;
import com.quartetfs.fwk.filtering.impl.EqualCondition;
import com.quartetfs.fwk.filtering.impl.InCondition;
import com.quartetfs.fwk.filtering.impl.OrCondition;
import com.quartetfs.fwk.filtering.impl.SingleAccessor;
import com.quartetfs.fwk.filtering.impl.SubCondition;
import com.quartetfs.fwk.impl.Pair;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.ANZUtils;
import com.quartetfs.pivot.anz.webservices.impl.DataExportDTO;
import com.quartetfs.tech.indexer.IProcedure;
import com.quartetfs.tech.indexer.IReader;

public class GetPnlDataFromIndexerTask implements Callable<Void> {

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	private String extractionDirectory;
	private IActivePivotSchema schema;
	private DataExportDTO extractParamsDTO;
	private String requestToken;
	private IDate cobDate;
	private ExecutorService executorService;
	private String[] cols;
	private int queueCapacity;
	private int writeBatchSize;
	private BlockingQueue<TaskPnlObject> taskQueue;
	private long recCounter;
		
	public GetPnlDataFromIndexerTask(IActivePivotSchema schema,IDate cobDate,DataExportDTO extractParamsDTO,String requestToken, String extractionDirectory,
			int taskQueueSize,int exportBatchSize)
	{
		this.schema= schema;
		this.extractParamsDTO = extractParamsDTO;
		this.requestToken = requestToken;
		this.cobDate = cobDate;		
		this.executorService=newExecutorService(requestToken);
		this.extractionDirectory=extractionDirectory;
		this.queueCapacity=taskQueueSize;
		this.writeBatchSize=exportBatchSize;
		this.taskQueue = new ArrayBlockingQueue<TaskPnlObject>(queueCapacity,true);
		this.cols =  extractParamsDTO.getColumnToExtract(); // new String[indexerCols.size()];
	
	}
	 

	private ExecutorService newExecutorService(final String requestToken) 
	{
		return Executors.newSingleThreadExecutor(new ThreadFactory() {
 			AtomicInteger threadCtr = new AtomicInteger();
 			@Override
 			public Thread newThread(Runnable r) {
 				 Thread t = new Thread(r,"export-writer - pnl" +  "-" + threadCtr.incrementAndGet());
 				 return t;
 			}
 		});	 
	}
	
	@Override
	public Void call() throws Exception {
		try
		{
			LOGGER.info(String.format("Starting export for %s", extractParamsDTO ));
			StopWatch stopWatch= new StopWatch();
			ICondition filter = buildQueryFilter();
			
			LOGGER.info("Filter Created, execute from indexer....");
					
			stopWatch.start("Indexer Query Time");
			schema.getIndexer().execute(Collections.<IPair<ICondition, IProcedure<IProjection>>> singleton
							(new Pair<ICondition, IProcedure<IProjection>>(filter,
									 new ExportDataProcedure(requestToken, extractParamsDTO.getRefDates().toString(), 
									extractParamsDTO.isFromFM() ))));
			stopWatch.stop();
			
			LOGGER.info(String.format("Total Time to query the Indexer:%s",stopWatch));
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Error",e);
		}
		
		return null;
	}




	private ICondition buildQueryFilter() 
	{
		List<ICondition> conditions = new ArrayList<ICondition>();
		conditions.add(new SubCondition(new SingleAccessor(ANZConstants.COBDATE_IDX_COL_NAME), new EqualCondition(cobDate)));
		
		if(extractParamsDTO.hasContainerName())
		{
			conditions.add(new SubCondition(new SingleAccessor(ANZConstants.CONTAINER_IDX_COL_NAME), new InCondition(extractParamsDTO.getContainerName())));
		}
		if(extractParamsDTO.hasPortfolio())
		{
			List<ICondition> portfolios = new ArrayList<ICondition>();
			for(String name : extractParamsDTO.getPortfolio())
			{
				portfolios.add(new SubCondition(new SingleAccessor(ANZConstants.PORTFOLIO_IDX_COL_NAME), new EqualCondition(name)));
			}
			ICondition[] portFolios = new ICondition[portfolios.size()];				
			conditions.add(new OrCondition(portfolios.toArray(portFolios)));			
		}		
		
		
		if(extractParamsDTO!=null && extractParamsDTO.getFieldFilter()!=null && extractParamsDTO.getFieldFilter().size()>0){
			
			for(Map.Entry<String, String> filter:extractParamsDTO.getFieldFilter().entrySet() ){
				if( filter.getValue()!=null   ) {
					if( filter.getValue().toUpperCase().indexOf("OR") !=-1) {
						String[] orValue = filter.getValue().toUpperCase().split("OR");
						List<ICondition> orFilter = new ArrayList<ICondition>();
						 for (int i = 0; i < orValue.length; i++) {
							orFilter.add(new SubCondition(new SingleAccessor( filter.getKey() ), new EqualCondition( orValue[i].trim() )));
						 }     
							ICondition[] orFilters = new ICondition[orFilter.size()];				
							conditions.add(new OrCondition(orFilter.toArray(orFilters)));			
					} else {
						conditions.add(new SubCondition(new SingleAccessor( filter.getKey() ), new InCondition( filter.getValue().trim() )));
					}
				}
			}
		}
		
		
		ICondition[] allCondition = new ICondition[conditions.size()];
		conditions.toArray(allCondition);
		ICondition finalCondition = new AndCondition(allCondition);
		
		conditions = null;
		allCondition = null;
		return finalCondition;
	}
	
	
	
	private class ExportDataProcedure  implements IProcedure<IProjection>
	{
		private static final long serialVersionUID = 3026327021495695393L;
		private String fileToken;
		private int noOfCols = cols.length;
		private String refDates;
		private boolean fromFM;
		
		public ExportDataProcedure(String fileToken, String refDates, boolean fromFM)
		{
			this.fileToken=fileToken;
			this.refDates = refDates;
			this.fromFM = fromFM;
		}
		@Override
		public void complete() {
						
		}

		private void createRefDate(String fileDirectory, String batchId, String refDates){
			
			OutputStream outputStream;
			try {
				String fileName = fileDirectory + "//" + batchId + "_VarRefDates.ref";
				LOGGER.info( "Creating refDates:" + fileName);
				   
				outputStream = new FileOutputStream(fileName);
			    outputStream.write(refDates.getBytes());
				outputStream.flush();				
				outputStream.close();	
				
				LOGGER.info( "Done creating refDates:" + fileName);
		
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
					
		}
		@Override
		public boolean execute(IReader<IProjection> reader) {
			
			String tempFileName = String.format("%s", fileToken);
			String batchId      = tempFileName.split("#")[3];
			File exportFile = new File(tempFileName);
			OutputStream outputStream=null;
			try   
			{
				 
				createRefDate(exportFile.getParent(),batchId, refDates);
				
				LOGGER.info(String.format("Creating file %s",exportFile.getAbsolutePath()));
			    	
				outputStream = new FileOutputStream(exportFile);					
				 
				Future<Long> totalTimeFuture = executorService.submit( new FilePnlWriterTask(taskQueue, outputStream));
				 
				extractData(reader,batchId, refDates);			
				taskQueue.put( new TaskPnlObject(true));    // End message
				
				reader = null;
				
				
				long ioTime=totalTimeFuture.get();	
				((FileOutputStream)outputStream).getChannel().truncate( ((FileOutputStream)outputStream).getChannel().size() - 1);
				
				outputStream.flush();				
				outputStream.close();	
				
				File newFile = renameTempFile(exportFile,fromFM);				
				float seconds = (float)ioTime/1000f;				
				LOGGER.info(String.format("Total Record %s , Total IO Time %s ms , IO Time %s records/second",recCounter,ioTime,recCounter/seconds));				
				LOGGER.info(String.format("File %s created",newFile.getAbsolutePath()));
				
			}
			catch(Exception e)
			{
				LOGGER.log(Level.SEVERE,"Error while exporting data",e);
			}
			finally
			{
				executorService.shutdownNow();
			}
			
			return true;
		}
		
	
		
		private void extractData(IReader<IProjection> reader, String batchId, String refDates) throws InterruptedException 
		{
			
			StopWatch stopWatch = new StopWatch();
			reader.setTuplePattern(cols);
			
			List<Object[]> rows = new ArrayList<Object[]>();
			int chunkCounter=0;
			stopWatch.start("Data from Indexer");
			
			List<Object[]> header = new ArrayList<Object[]>();
			header.add( new Object[]{"?UniqueID","BaseCCY","ScenarioPnLVector","PositionID"});
			enqueueMessage(  header,batchId, refDates,true);
			
			List<Object[]> indexData = new ArrayList<Object[]>();
			while(reader.hasNext()) 
			{
				reader.next();
				Object[] tmp = new Object[noOfCols];
				reader.readTuple(tmp);
				
				Object[] clone = tmp.clone();
				clone[1] = ((double[]) tmp[1]).clone();
			    indexData.add(clone);
			}
			reader = null;
			
			Map<String,Object[]> pnls = new HashMap<String,Object[]>();
		    
			for (Object[] pnl : indexData) {
				aggregate(pnls,pnl);	
			}
			indexData =null;
			  System.out.println( pnls.size());
			
			for(Map.Entry<String,Object[]> pnl: pnls.entrySet()){
				rows.add(pnl.getValue());
				chunkCounter++;
				if(chunkCounter==writeBatchSize) {
					enqueueMessage(rows,batchId, refDates,false);
					chunkCounter=0;
					rows = new ArrayList<Object[]>();
				
				}
				recCounter++;	
		    }
			
			
			if(!rows.isEmpty())
			{
				enqueueMessage(rows,batchId, refDates,false);
			}
			
			stopWatch.stop();
			LOGGER.info(String.format("Time taken for execute query %s",stopWatch));
			pnls = null;
			
		}
		
		private void aggregate( Map<String,Object[]> pnls , Object[] indexerData){
			
			String key = indexerData[0] + "-" + indexerData[2];
			Object[] oldValueObj =  pnls.get( key );
			
		   if( oldValueObj == null  ){
			   pnls.put( key, indexerData);
		   } else {
			   double[] addedVector = ANZUtils.addTwoArrays(  oldValueObj[1], indexerData[1]);
			   pnls.remove( key);
			   pnls.put(key, new Object[]{ indexerData[0],addedVector,indexerData[2]  } );
			   
		   }
		}
		
		
		private File renameTempFile(File exportFile,boolean fromFM) 
		{
			String newfileName = "";
			 if(fromFM){
				 newfileName = String.format("%s.TFM", fileToken);
								 
			 } else {
				 newfileName =  String.format("%s.APX", fileToken);
			 }
			   
			File newFile = new File(newfileName.replace(".TMP", "").toUpperCase() );
			
			Validate.isTrue(exportFile.renameTo(newFile), "File rename failed");
			return newFile;
		}
		
		private void enqueueMessage(List<Object[]> rows, String batchId, String refDates, boolean putHeader) throws InterruptedException 
		{
			taskQueue.put( new TaskPnlObject(rows,batchId, refDates,putHeader ));
		}

		@Override
		public boolean supportsParallelExecution() {
			return false;
		}		
	}
	
	public static class TaskPnlObject 
	{
		private boolean isPoison;
		private List<Object[]> rows;
		private String batchId;
		private String refDates;
		
		private boolean putHeader;
		
		
		
		public TaskPnlObject(boolean isPoison, List<Object[]> row, String batchId, String refDates,boolean putHeader) {
			super();
			this.isPoison = isPoison;
			this.rows = row;
			this.setBatchId(batchId);
			this.setRefDates(refDates);
			this.putHeader = putHeader;
		}
		
		public TaskPnlObject(List<Object[]> row,String batchId, String refDates, boolean putHeader) {
			super();
			this.rows = row;
			this.setBatchId(batchId);
			this.setRefDates(refDates);
			this.setPutHeader(putHeader);
		}
		
		public TaskPnlObject(boolean isPoison) {
			this(isPoison,null,null,null,false);			
		}
		
		public boolean isPoison() {
			return isPoison;
		}
		
		public List<Object[]> getRows() {
			return rows;
		}

		public String getBatchId() {
			return batchId;
		}

		public void setBatchId(String batchId) {
			this.batchId = batchId;
		}

		public String getRefDates() {
			return refDates;
		}

		public void setRefDates(String refDates) {
			this.refDates = refDates;
		}

		public boolean isPutHeader() {
			return putHeader;
		}

		public void setPutHeader(boolean putHeader) {
			this.putHeader = putHeader;
		}
		
	}
	
	

}
