package com.quartetfs.pivot.anz.service.export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
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
import java.util.zip.GZIPOutputStream;

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
import com.quartetfs.pivot.anz.webservices.impl.DataExportDTO;
import com.quartetfs.tech.indexer.IProcedure;
import com.quartetfs.tech.indexer.IReader;

public class GetDataFromIndexerTask implements Callable<Void> {

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
	private BlockingQueue<TaskObject> taskQueue;
	private long recCounter;
		
	public GetDataFromIndexerTask(IActivePivotSchema schema,IDate cobDate,DataExportDTO extractParamsDTO,String requestToken, 
			String extractionDirectory,
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
		this.taskQueue = new ArrayBlockingQueue<TaskObject>(queueCapacity,true);
		
		this.cols =  extractParamsDTO.getColumnToExtract(); 
	}
	 

	private ExecutorService newExecutorService(final String requestToken) 
	{
		return Executors.newFixedThreadPool(1,/* threadFactory) .newSingleThreadExecutor(*/new ThreadFactory() {
 			AtomicInteger threadCtr = new AtomicInteger();
 			@Override
 			public Thread newThread(Runnable r) {
 				 Thread t = new Thread(r,"export-writer -" + requestToken + "-" + threadCtr.incrementAndGet());
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
			stopWatch.start("Total Time for Execute & Export");
			
			 ExportDataProcedure e = new ExportDataProcedure(requestToken);
			 schema.getIndexer().execute(Collections.<IPair<ICondition, IProcedure<IProjection>>> singleton
							(new Pair<ICondition, IProcedure<IProjection>>(filter,e)));
			 stopWatch.stop();
			
			LOGGER.info(String.format("Total Time for export:%s",stopWatch));
			
		
			 
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
		return finalCondition;
	}
	
	
	
	private class ExportDataProcedure  implements IProcedure<IProjection>
	{
		private static final long serialVersionUID = 3026327021495695393L;
		private String fileToken;
		private int noOfCols = cols.length;
		private List<Object[]> results = new ArrayList<Object[]>();
		
		public ExportDataProcedure(String fileToken)
		{
			this.fileToken=fileToken;
		}
		@Override
		public void complete() {
			this.createExtractFile();
		}

		@Override
		public boolean execute(IReader<IProjection> reader) {
			
			try {
				extractData(reader);
			} catch (InterruptedException e) {}
			catch (Exception e){}
				return true;
		}
		
		public void createExtractFile(){
			
			String fileName = String.format("%s.gz.tmp", requestToken);
			File exportFile = new File(extractionDirectory+"\\"+ fileName);
			
			try   
			{
				LOGGER.info(String.format("Creating file %s",exportFile.getAbsolutePath()));
				GZIPOutputStream outputStream = new GZIPOutputStream( new BufferedOutputStream(new FileOutputStream(exportFile)));					
				 FileWriterTask fileWriter = new FileWriterTask( outputStream);
				 List<Object[]> headers = new ArrayList<Object[]>();
					headers.add(cols);
				 fileWriter.call( new TaskObject( headers ));
				 fileWriter.call( new TaskObject( results ));
											
				outputStream.flush();				
				outputStream.close();	
				
				File newFile = renameTempFile( new File(extractionDirectory+"\\"+ fileName));				
				LOGGER.info(String.format("Total Record %s , Total IO Time %s ms , IO Time %s records/second",recCounter,1,recCounter/1000));				
				LOGGER.info(String.format("File %s created",newFile.getAbsolutePath()));
				
				
			}catch(Exception e){}
			
			
			
		}
		
		
		private void extractData(IReader<IProjection> reader) throws InterruptedException 
		{
			StopWatch stopWatch = new StopWatch();
			reader.setTuplePattern(cols);
			
			List<Object[]> rows = new ArrayList<Object[]>();
			int chunkCounter=0;
			stopWatch.start("Data from Indexer");
			while(reader.hasNext()) 
			{
				reader.next();
				Object[] tmp = new Object[noOfCols];
				reader.readTuple(tmp);
				rows.add(tmp);
			
				chunkCounter++;
				if(chunkCounter==writeBatchSize)
				{
					chunkCounter=0;
				}
				
				recCounter++;
			}
			stopWatch.stop();
			LOGGER.info(String.format("Time taken for execute query %s",stopWatch));
			
			if(!rows.isEmpty())
			{
				results.addAll(rows);
			}
			
		}
	
		private File renameTempFile(File exportFile) 
		{
			String newfileName = String.format("%s.gz", fileToken);
			File newFile = new File(extractionDirectory+"\\"+newfileName);
			Validate.isTrue(exportFile.renameTo(newFile), "File rename failed");
			return newFile;
		}
		
	
		@Override
		public boolean supportsParallelExecution() {
			return false;
		}		
	}
	
	public static class TaskObject 
	{
		private boolean isPoison;
		private List<Object[]> rows;
		
		public TaskObject(boolean isPoison, List<Object[]> row) {
			super();
			this.isPoison = isPoison;
			this.rows = row;
		}
		
		public TaskObject(List<Object[]> row) {
			super();
			this.rows = row;
		}
		
		public TaskObject(boolean isPoison) {
			this(isPoison,null);			
		}
		
		public boolean isPoison() {
			return isPoison;
		}
		
		public List<Object[]> getRows() {
			return rows;
		}
		
	}
	
	

}
