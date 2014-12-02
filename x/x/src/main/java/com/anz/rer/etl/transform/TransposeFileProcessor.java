package com.anz.rer.etl.transform;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.anz.rer.etl.cache.LookUp;
import com.anz.rer.etl.config.CsvReaderTaskConfig;
import com.anz.rer.etl.directory.IFileProcessor;
import com.anz.rer.etl.transform.impl.TransposeConfig;


public class TransposeFileProcessor implements IFileProcessor {
	private File sourceFile;
    
	private CsvReaderTaskConfig csvReaderTaskConfig;
	private Map<String,TransposeConfig> transposeConfigurations;
    private Map<String,String> psrTransposeConfigMapping;
	
    
	private String destDirectory;
    private String archiveLocation;
    
	private LookUp cache;
    
    private ExecutorService executorService;
   
	private final static Logger logger = Logger.getLogger(TransposeFileProcessor.class);
    
    
    public CsvReaderTaskConfig getCsvReaderTaskConfig() {
		return csvReaderTaskConfig;
	}

	public void setCsvReaderTaskConfig(CsvReaderTaskConfig csvReaderTaskConfig) {
		this.csvReaderTaskConfig = csvReaderTaskConfig;
	}

    public Map<String, TransposeConfig> getTransposeConfigurations() {
		return transposeConfigurations;
	}

	public void setTransposeConfigurations(
			Map<String, TransposeConfig> transposeConfigurations) {
		this.transposeConfigurations = transposeConfigurations;
	}

	
	public TransposeFileProcessor(int threadPoolSize,LookUp cache, String archiveLocation) {
		    executorService = Executors.newFixedThreadPool(threadPoolSize,new ThreadFactory() {
		    AtomicInteger threadCtr = new AtomicInteger();
		    @Override
			public Thread newThread(Runnable r) {
				 Thread t = new Thread(r,"TransposeThread-" + threadCtr.incrementAndGet());
				 return t;
			}
		});
		    
		 this.cache = cache;  
		 logger.info("TransposeDirectoryProcessot Cache ID:" + cache.hashCode()); 
		 this.archiveLocation =archiveLocation;
	}

	@Override
	public boolean validate(File fileName) {
		sourceFile = fileName;
		return true;
	}

	@Override
	public boolean preProcess() {
		return true;
	}

	@Override
	public boolean doProcess() {
		//TransposeConfig transposeConfig = transposeConfigurations.get("nonVarTansposeConfig" );
		
		String psrName = sourceFile.getName().split("#")[2];
		TransposeConfig transposeConfig = transposeConfigurations.get(this.getPsrTransposeConfigMapping().get(psrName) );
				
		executorService.submit( new TransposerTask(sourceFile,  csvReaderTaskConfig,
				               transposeConfig, 10,cache,destDirectory,archiveLocation) );
	    
		/*Future<Long> totalTimeFuture = executorService.submit( new TransposerTask(sourceFile,  csvReaderTaskConfig, transposeConfig, 10,cache) );
	    
		try {
			long ioTime=totalTimeFuture.get(); 
			File newFile = renameTempFile(new File(sourceFile.getParent() + "\\"   + String.format("%s.gz.tmp", sourceFile.getName())));	
			logger.info("UVR file created:" + sourceFile.getParent() + "\\"   + String.format("%s.gz.tmp", sourceFile.getName()));
		
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
		
			e.printStackTrace();
		}	
		*/
		return true;
	}
	
	
	
		

	@Override
	public boolean postProcess() {
		return true;
	}

	public Map<String,String> getPsrTransposeConfigMapping() {
		return psrTransposeConfigMapping;
	}

	public void setPsrTransposeConfigMapping(
			Map<String,String> psrTransposeConfigMapping) {
		this.psrTransposeConfigMapping = psrTransposeConfigMapping;
	}
	
	

}