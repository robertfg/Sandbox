package com.quartetfs.pivot.anz.service.export;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.IActivePivotSchema;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.pivot.anz.service.impl.DateService;
import com.quartetfs.pivot.anz.webservices.impl.DataExportDTO;

public class ExportPnlDataHelper {
	
	private ExecutorService executorService ;
	private IActivePivotSchema schema;
	private String extractionDirectory;
	private int taskQueueSize;
	private int exportBatchSize;
	private String defaultDimension;
	private DateService dateService;
	
	
	public ExportPnlDataHelper()
	{
		//Get it from config
		executorService = Executors.newFixedThreadPool(5,new ThreadFactory() {
			AtomicInteger threadCtr = new AtomicInteger();
			@Override
			public Thread newThread(Runnable r) {
				 Thread t = new Thread(r,"pnl-export-" + threadCtr.incrementAndGet());
				 return t;
			}
		});
	}
	
	
	public void enqueue(DataExportDTO extractParamsDTO,IDate cobDate,String requestToken)
	{
		GetPnlDataFromIndexerTask task = new GetPnlDataFromIndexerTask(schema, cobDate, extractParamsDTO, requestToken, extractionDirectory,taskQueueSize,exportBatchSize);
		executorService.submit(task);
	}
	
	public void setActivePivotManager(IActivePivotManager activePivotManager) {
		this.schema = (IActivePivotSchema)activePivotManager.getSchemas().get("MarketRiskSchema");
	}
	
	public void setExtractionDirectory(String extractionDirectory) {
		this.extractionDirectory = extractionDirectory;
	}
	
	public void setTaskQueueSize(int taskQueueSize) {
		this.taskQueueSize = taskQueueSize;
	}
	
	public void setExportBatchSize(int exportBatchSize) {
		this.exportBatchSize = exportBatchSize;
	}


	public DateService getDateService() {
		return dateService;
	}


	public void setDateService(DateService dateService) {
		this.dateService = dateService;
	}
	
}
