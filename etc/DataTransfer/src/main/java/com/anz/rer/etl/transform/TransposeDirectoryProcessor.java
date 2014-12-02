package com.anz.rer.etl.transform;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.anz.rer.etl.cache.LookUp;
import com.anz.rer.etl.config.CsvReaderTaskConfig;
import com.anz.rer.etl.directory.impl.ADirectoryWatcher;
import com.anz.rer.etl.transform.impl.TransposeConfig;
import com.anz.rer.etl.webservice.RwhInfo;

public class TransposeDirectoryProcessor  extends ADirectoryWatcher {

private final static Logger logger = Logger.getLogger(TransposeDirectoryProcessor.class);
	
	/*IFileProcessor fileProcessor;*/

	private Set<String> alreadyProcessedFiles = new HashSet<String>();
	private String archiveDir;
	private String destDirectory;
	
	private int dateLoc;
	private String dateLocSeparator;
	
	private LookUp cache;
	
	
	private ExecutorService executorService;
	
	private Map<String,TransposeConfig> transposeConfigurations;
    private Map<String,String> psrTransposeConfigMapping;
    private CsvReaderTaskConfig csvReaderTaskConfig;
    private int queueSize = 40;
	private boolean  caching = false;
	private boolean  lock = false;
	
//	private ConcurrentMap<String,Boolean> cachedDate = new ConcurrentHashMap<String,Boolean>();
//	private ConcurrentMap<String,Boolean> startedCached = new ConcurrentHashMap<String,Boolean>();
	
	
//	private static final Object cachingLock = new Object();
	
  private ExecutorService cachingService = null;
	

	
	public TransposeDirectoryProcessor(Properties props, int threadPoolSize) {
		 
		     super(props.getProperty("etl.csvToTable.src.directory"),
			props.getProperty("etl.csvToTable.src.fileName.pattern"));
		    
		 archiveDir    = props.getProperty("etl.csvToTable.src.fileName.archive");
		 destDirectory = props.getProperty("etl.csvToTable.src.fileName.destination");
		 
		 executorService = Executors.newFixedThreadPool(threadPoolSize,new ThreadFactory() {
			    AtomicInteger threadCtr = new AtomicInteger();
			    @Override
				public Thread newThread(Runnable r) {
					 Thread t = new Thread(r,"TransposeFilePool-" + threadCtr.incrementAndGet());
					 return t;
				}
			});
		 
		 cachingService = Executors.newFixedThreadPool(1,new ThreadFactory() {
			    AtomicInteger threadCtr = new AtomicInteger();
			    @Override
				public Thread newThread(Runnable r) {
					 Thread t = new Thread(r,"TransposeFilePool-" + threadCtr.incrementAndGet());
					 return t;
				}
			});
		 
	}

	public TransposeDirectoryProcessor(String path, String filter) {
		super(path, filter);
		
	}

	public TransposeDirectoryProcessor(String path) {
		super(path);
	}

	
	@Override
	public void onChange(File file, String action) {
	    
		//GAMMA_BASIS*.*.apx|.*IR_GAMMA*.*.apx|.*FXO_DELGAMMA
		if(!action.equals("delete")){
		    
		      String archiveLocation = getArchiveSubDir( archiveDir,file,dateLoc, dateLocSeparator);
		      
		      
		      if(alreadyProcessedFiles.contains(file.getName())){
		    //	logger.info( String.format("File %s already put in queue:", file.getName()));
			 
		      } else {
		
				  if( checkDateIfAlreadyInCache(file) ){
					    logger.info("Processing:" + file.getName());
								  
						alreadyProcessedFiles.add( file.getName() );
					    TransposeConfig transposeConfig = transposeConfigurations.get(this.getPsrTransposeConfigMapping().get(file.getName().split("#")[2]) );
						logger.info("Submitting queue size:" + queueSize );
						executorService.submit( new TransposerTask( file,  csvReaderTaskConfig,transposeConfig
								                  ,queueSize, cache, destDirectory,archiveLocation) );
					
				  } else {
					  initializedCache(file);
				}
			}
		} else {

		} 
	}
	
	public void lock(){
		lock=true;
		logger.info("locking.............................");
	}

	public void releaseLock(){
		lock=false;
		logger.info("releasing the lock..............................");
	}
	
	public void aquireLock() {	
		
		logger.info("...............Acquiring the Lock............................");
		while(lock){
			try {
				logger.info("Cached is rebuilding....... ");
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				logger.error("Error while thread waiting to lock :" +e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void initializedCache(File file){
		       String cob = getCobDate(file.getName());
		
		 		 if(!cache.getCachedStarted().containsKey(Integer.valueOf(cob))){
					 logger.info("Starting to build cache for cobdate:" + cob);
					 logger.info("Putting the cobdate:" + cob + ", as Started");
					 cache.getCachedStarted().put(Integer.valueOf(cob), true);
					 ConcurrentHashMap<Integer,RwhInfo>  cDate = cache.getCacheFromDB(Integer.valueOf(cob));
					 cache.getCache().put(Integer.valueOf(cob),cDate );
					 logger.info(".............................. CACHED WAS ALREADY BUILD for COBDATE:" + cob + ".................................");
				 } else {
					 logger.info("Cache already started:" + cob + ",No NEED to start again");
					 cache.getCachedStarted().put(Integer.valueOf(cob), true);
				 } 
				
				 //	releaseLock();
	}
	
	private boolean checkDateIfAlreadyInCache(File file){
		
		if(cache!=null){
			String cob = getCobDate(file.getName());
			logger.info("Cached COB Date:" + cob);
			if(cache.getCachedDate().containsKey(Integer.valueOf(cob))) { 
				logger.info("Cached COB Date:" + cob + ",NOW contain ");
				
				Boolean cachedDone = cache.getCachedDone().get(Integer.valueOf(cob)); 
				logger.info("Cached COB Date:"+ cob + " IS DONE:" + cachedDone + "...");
				
				if( cachedDone!=null && cachedDone.equals(Boolean.TRUE) ){
					return true;
				}else{
					logger.info("Cached COB Date:" + cob + ",Is not yet DONE");
				} 
			}else {
				logger.info("Cached COB Date:" + cob + ",Doesnt contain");
				cache.getCachedDate().put(Integer.valueOf(cob), true);
				logger.info("Cached COB Date:" + cob + ",Now PUT");
				
			}
		} 
		   logger.info("cache object is still false");
			return false;
	}
	
	public String getCobDate( String fileName   ){
		String[] name = fileName.split("#",-1);
		
		String date   = name[4]; 
	 /*   String cobDate = null;
		 
		 if( name[2].equals("HYPO") ){
			 
			 String hypoDate =  (String)name[8];
			 
			 cobDate = hypoDate;
		 }else {
			
			 cobDate  = date;//.substring(6) + "-"+ date.substring(4,6) + "-" + date.substring(0, 4); 
		 }*/
		 return date;
	}

	
	private String getArchiveSubDir(String archiveDir,File file,int dateLoc, String dateLocSep ){
	    String fileName = file.getName().toString();
    	
		if(dateLoc > 0){
			String date = fileName.split(dateLocSep)[dateLoc];
			archiveDir += "//" + date.substring(6) + date.substring(4,6) + date.substring(0,4);   
		}
		return archiveDir;
	}
	
	public int getDateLoc() {
		return dateLoc;
	}

	public void setDateLoc(int dateLoc) {
		this.dateLoc = dateLoc;
	}

	public String getDateLocSeparator() {
		return dateLocSeparator;
	}

	public void setDateLocSeparator(String dateLocSeparator) {
		this.dateLocSeparator = dateLocSeparator;
	}

	public LookUp getCache() {
		
		return cache;
	}

	public void setCache(LookUp cache) {
		logger.info("TransposeDirectoryProcessot Cache ID:" + cache.hashCode()); 
		this.cache = cache;
	}

	

	public Map<String, TransposeConfig> getTransposeConfigurations() {
		return transposeConfigurations;
	}

	public void setTransposeConfigurations(
			Map<String, TransposeConfig> transposeConfigurations) {
		this.transposeConfigurations = transposeConfigurations;
	}

	public Map<String, String> getPsrTransposeConfigMapping() {
		return psrTransposeConfigMapping;
	}

	public void setPsrTransposeConfigMapping(
			Map<String, String> psrTransposeConfigMapping) {
		this.psrTransposeConfigMapping = psrTransposeConfigMapping;
	}

	public CsvReaderTaskConfig getCsvReaderTaskConfig() {
		return csvReaderTaskConfig;
	}

	public void setCsvReaderTaskConfig(CsvReaderTaskConfig csvReaderTaskConfig) {
		this.csvReaderTaskConfig = csvReaderTaskConfig;
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}


}
