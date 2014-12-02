package com.anz.rer.etl.vectorizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.anz.rer.etl.cache.LookUp;
import com.anz.rer.etl.config.CsvReaderTaskConfig;
import com.anz.rer.etl.directory.IFileProcessor;
import com.anz.rer.etl.transform.impl.TransposeConfig;
import com.anz.rer.etl.utils.FileUtils;

public class VectorizeFileProcessor implements IFileProcessor {
	private final static Logger logger = Logger.getLogger(VectorizeFileProcessor.class);
    
    
	private File sourceFile;
    
	private CsvReaderTaskConfig csvReaderTaskConfig;
    
    private Map<String,VectorConfig> vectorConfigurations;
    private Map<String,String> psrVectorConfigMapping;
    
    private Map<String,TransposeConfig> transposeConfigurations;
    private Map<String,String> psrTransposeConfigMapping;
    
    
    private ExecutorService executorService;
    
    private String destDirectory;
    private LookUp cache;
   
    
	public LookUp getCache() {
		return cache;
	}

	public void setCache(LookUp cache) { 
		this.cache = cache;
	}

	public VectorizeFileProcessor(int threadPoolSize,LookUp cache) {
		executorService = Executors.newFixedThreadPool(threadPoolSize,new ThreadFactory() {
			AtomicInteger threadCtr = new AtomicInteger();
			@Override
			public Thread newThread(Runnable r) {
				 Thread t = new Thread(r,"export-" + threadCtr.incrementAndGet());
				 return t;
			}
		} );
		this.cache = cache;
		logger.info("VectorizerFileProcessor Cache ID:" + cache.hashCode());
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
	    logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> vector doProcess <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ");
		String psrName = sourceFile.getName().split("#")[1];
		VectorConfig vectorConfig = vectorConfigurations.get( this.getPsrVectorConfigMapping().get(psrName) );
		TransposeConfig transposeConfig = transposeConfigurations.get(this.getPsrTransposeConfigMapping().get(psrName) );
		
		Future<Long> totalTimeFuture = executorService.submit( new VectorizerTask(sourceFile, vectorConfig, csvReaderTaskConfig, transposeConfig, 10, cache) );
		 try {
			 long ioTime  = totalTimeFuture.get();
			 createRemoveFile(sourceFile.getName(),this.destDirectory);
			 deleteOldUvrFile(this.destDirectory,sourceFile.getName());
		  	 File newFile = renameTempFile(new File(sourceFile.getParent() + "\\"+ String.format("%s.gz.tmp", sourceFile.getName())));	
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}	
		
		return true;
	}
	
	private File renameTempFile(File csvOutputFile) 
	{
		String newfileName = String.format("%s_UVR.gz", csvOutputFile.getName()).replace(".apx.gz.tmp","") ;
		String destDir = this.destDirectory  + "\\"+ getDateSubDir(sourceFile.getName()) + "\\unified\\" ;  
		File newFile = new File( csvOutputFile.getParent() + "\\"  + newfileName);
		csvOutputFile.renameTo(newFile);
		FileUtils.archiveFile(newFile.getAbsolutePath(), destDir);
		return newFile;
	}
	

	@Override
	public boolean postProcess() {
		return true;
	}

	public Map<String,VectorConfig> getVectorConfigurations() {
		return vectorConfigurations;
	}

	public void setVectorConfigurations(Map<String,VectorConfig> vectorConfigurations) {
		this.vectorConfigurations = vectorConfigurations;
	}

	public Map<String,String> getPsrVectorConfigMapping() {
		return psrVectorConfigMapping;
	}

	public void setPsrVectorConfigMapping(Map<String,String> psrVectorConfigMapping) {
		this.psrVectorConfigMapping = psrVectorConfigMapping;
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

	public String getDestDirectory() {
		return destDirectory;
	}

	public void setDestDirectory(String destDirectory) {
		this.destDirectory = destDirectory;
	}
	
	private String getDateSubDir(String fileName){
		String date = fileName.split("#")[4];
		//return date;
		return date.substring(6) + date.substring(4,6) + date.substring(0,4);   

	}
	
	public static void main(String args[]){
		
		System.out.println( "20121217".substring(6)); 
		System.out.println( "20121217".substring(4,6));
				System.out.println( "20121217".substring(0,4));
	}

	//this will be move to CubeCleanerUtil
	
private boolean createRemoveFile(String fileName, String deleteDir){
		
		String params[]=fileName.split("#");
		String       COB = "COB="       + params[4] + "\n";
		String CONTAINER = "CONTAINER=" + params[1] + "\n";
		
		if(params[1].equals("VaR and P&L") ){
		  CONTAINER +=  "psrName=" + params[0] ;
		}
		
		FileWriter ryt = null;
		try {
			fileName = fileName.toUpperCase().replace(".APX", ".DELETE");
			
			String removeFileName = deleteDir + "\\"  + fileName.toUpperCase().replace(".APX", ".DELETE");
			logger.info("creating remove file:" + removeFileName );
			ryt = new FileWriter( removeFileName );
			BufferedWriter out=new BufferedWriter(ryt);
			out.write(COB + CONTAINER);
			out.close();
			
			fileName = fileName.toUpperCase().replace(".DELETE", ".remove" );
			
			FileUtils.renameFile(  new File(removeFileName) , fileName);
			logger.info("creating remove file:" + fileName);
			startPolling( deleteDir ,  fileName );
			
			
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return false;
	}
	
	private boolean deleteOldUvrFile(String destDirectory, String fileName){
		//VSAL0#VAR_STRESS#PNL-VAR_STRESS#295495436348161#20130221#295495436348161#465708025606606.APX.gz.tmp_UVR.gz
		String destDir = destDirectory  + "\\"+ getDateSubDir(sourceFile.getName()) + "\\unified\\" ; 
		String[] oldUvrFileName = fileName.split("#");
	 	
		String oldUvrFile =  "*" + oldUvrFileName[0] + "#" + oldUvrFileName[1] + "#" + oldUvrFileName[2] + "#.*.#" + oldUvrFileName[4] + "#.*._UVR.*.gz";
	//	                                                                             "*SFDG0#FXO_DELGAMMA#NON-VAR#.*.#20130216#.*._UVR.*.tmp"

         logger.info( "Deleting old uvr file:" + oldUvrFile );
   
		File oldUvr = FileUtils.getFile(destDir, oldUvrFile);
		if(oldUvr != null){
			FileUtils.deleteQuietly(oldUvr);
		} else {
		      logger.info( "NO Oold UVR Deleting old uvr file:" + oldUvrFile );
		}
		return true;
				
		
	}
	

	private boolean startPolling( String dir, String fileName ) {
		
		String[] oldUvrFileName = fileName.split("#");
		//String oldUvrFile =  "*" + oldUvrFileName[0] + "#" + oldUvrFileName[1] + "#" + oldUvrFileName[2] + "#.*.#" + oldUvrFileName[4] + "#.*remove";
		
		String oldUvrFile =  "*" + oldUvrFileName[0] + "#" + oldUvrFileName[1] + "#" + oldUvrFileName[2] + "#" + oldUvrFileName[3] + "#" + oldUvrFileName[4] + "#.*remove";
		
		if( FileUtils.getFile(dir, oldUvrFile + ".DONE")!=null || FileUtils.getFile(dir, oldUvrFile + ".EMT")!=null  ){
	      return true;
		} else {
		
		  try {
			Thread.sleep(5000);
		    startPolling(dir, fileName);
		  } catch (InterruptedException e) {
			e.printStackTrace();
		  }
		}
		return true;
	}
	
	public void execute(File file) {
		
		  logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> vector doProcess <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< ");
			String psrName = file.getName().split("#")[1];
			VectorConfig vectorConfig 		= vectorConfigurations.get( this.getPsrVectorConfigMapping().get(psrName) );
			TransposeConfig transposeConfig = transposeConfigurations.get(this.getPsrTransposeConfigMapping().get(psrName) );
			
			Future<Long> totalTimeFuture = executorService.submit( new VectorizerTask(file, vectorConfig, csvReaderTaskConfig, transposeConfig, 10, cache) );
			 try {
				 long ioTime  = totalTimeFuture.get();
				 createRemoveFile(file.getName(),this.destDirectory);
				 deleteOldUvrFile(this.destDirectory,file.getName());
			  	 File newFile = renameTempFile(new File(file.getParent() + "\\"+ String.format("%s.gz.tmp", file.getName())));	
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}	
			
		
	}
	

}