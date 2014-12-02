package com.quartetfs.pivot.anz.datasource.impl;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.springframework.util.StopWatch;

import com.quartetfs.pivot.anz.model.IVRParsingEntry;
import com.quartetfs.pivot.anz.source.impl.PSRData;

public class PSRReducerService {
	
	private static final Logger LOGGER = Logger.getLogger(PSRReducerService.class.getSimpleName());
	private ExecutorService reducerService;
	private int reduceBlock;
	private ConcurrentMap<String, FuruteWrapper> ftWrapper = new ConcurrentHashMap<String, FuruteWrapper>();
	
	private PSRReducerService(Properties prop)
	{
		int reducerThread = Integer.parseInt(prop.getProperty("reducerThreads"));
		this.reduceBlock = Integer.parseInt(prop.getProperty("reducerBlock"));
		this.reducerService = Executors.newFixedThreadPool(reducerThread);
	}
	
	public void submit(PSRData psrData,List<IVRParsingEntry> entry )
	{
		FuruteWrapper wrapper = createFutureWrapper(psrData);
		
		int length=entry.size();
		for(int from=0;from<length;)
		{
			int to = Math.min(from+reduceBlock, length);			
			ReduceTask rt = new ReduceTask(psrData, entry, from, to);
			wrapper.add(reducerService.submit(rt));
			from = to;
		}
	}

	private FuruteWrapper createFutureWrapper(PSRData psrData) {
		FuruteWrapper wrapper = new FuruteWrapper(psrData.getFileName());
		FuruteWrapper existingWrapper = ftWrapper.putIfAbsent(psrData.getFileName(), wrapper);
		if(existingWrapper!=null)
		{
			wrapper=existingWrapper;
		}
		return wrapper;
	}
	
	public void waitForReduce(String fileName) throws InterruptedException, ExecutionException
	{
		FuruteWrapper wrapper = ftWrapper.remove(fileName);
		Validate.notNull(wrapper, String.format("No Reducer task found for %s file", fileName));
		wrapper.get();		
	}
	
	
	
	private static class FuruteWrapper implements Future<Void>
	{	
		
		private ConcurrentLinkedQueue<Future<?>> futures=new ConcurrentLinkedQueue<Future<?>>();
		private String fileName;
		
		public FuruteWrapper(String fileName) {
			super();
			this.fileName = fileName;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return false;
		}

		@Override
		public Void get() throws InterruptedException, ExecutionException {
			StopWatch watch = new StopWatch("Reduce");
			watch.start(fileName);
			for(Future<?> ft : futures)
			{
				ft.get();
			}
			watch.stop();
			LOGGER.info(watch.toString());
			return null;
		}

		@Override
		public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,TimeoutException 
		{			
			throw new UnsupportedOperationException();
		}
		
		public void add(Future<?> ft)
		{
			futures.add(ft);
		}		
	}
	
	private static class ReduceTask implements Callable<Void>
	{
		private PSRData psrData;
		private List<IVRParsingEntry> entries;
		private int from;
		private int to;
		
		public ReduceTask(PSRData psrData, List<IVRParsingEntry> entries,int from, int to) 
		{
			this.psrData = psrData;
			this.entries = entries;
			this.from = from;
			this.to = to;
		}

		@Override
		public Void call() throws Exception {
			psrData.contribute(entries, from, to);
			return null;
		}
		
	}
}
