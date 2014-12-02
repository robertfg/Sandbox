package com.anz.rer.etl.transform;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.anz.rer.etl.cache.LookUp;
import com.anz.rer.etl.config.CsvReaderTaskConfig;
import com.anz.rer.etl.task.CsvReaderTask;
import com.anz.rer.etl.task.StampTask;
import com.anz.rer.etl.task.TransposeTask;
import com.anz.rer.etl.transform.impl.TransposeConfig;
import com.anz.rer.etl.utils.CsvObject;
import com.anz.rer.etl.utils.CsvObjectTransactionCoordinator;
import com.anz.rer.etl.utils.FileUtils;
import com.anz.rer.etl.utils.GzipWriterTask;

public class TransposerTask implements Callable<Long> {

  
	private File sourceFile;
  
    private CsvReaderTaskConfig csvReaderTaskConfig;
    private TransposeConfig transposeConfig;
    private int queueSize = 10;
    private LookUp cache;
    private String destDirectory;    
    private String archiveLocation;
    private CsvObjectTransactionCoordinator csvTranCoor;
    
    private final static Logger logger = Logger.getLogger(TransposerTask.class);
    
    public TransposerTask(File sourceFile,
			CsvReaderTaskConfig csvReaderTaskConfig, TransposeConfig transposeConfig,
			int queueSize, LookUp cache, String destDirectory, String archiveLocation) {
		
		this.sourceFile = sourceFile;
		this.csvReaderTaskConfig = csvReaderTaskConfig;
		this.transposeConfig = transposeConfig;
		this.queueSize = queueSize;
		this.cache = cache;
		logger.info("TransposerTask Cache ID:" + cache.hashCode());
		this.destDirectory = destDirectory;
		this.archiveLocation = archiveLocation;
	}

    

	@Override
	public Long call() throws Exception {
		long ioTime =0;
		   ExecutorService transposeService = newExecutorService(4) ;//fixed and hardcoded
		   try{
		   String tempFileName = String.format("%s.gz.tmp", sourceFile.getName());
			
		  
		   BlockingQueue<CsvObject> csvRecordsQueue =  new ArrayBlockingQueue<CsvObject>(queueSize,true);
		   BlockingQueue<CsvObject> transposeQueue  =  new ArrayBlockingQueue<CsvObject>(queueSize,true);
		   BlockingQueue<CsvObject> stampQueue      =  new ArrayBlockingQueue<CsvObject>(queueSize,true);
		    
		    transposeService.submit(new TransposeTask( csvRecordsQueue,transposeQueue, transposeConfig));
		   	transposeService.submit(new StampTask(  transposeQueue,stampQueue,cache));
		 
		   	/*to enable transaction coordinator
		   	 * Future<Long> totalTimeFuture = transposeService.submit( new GzipWriterTask(stampQueue,  
                    new File(sourceFile.getParent() +"\\" + tempFileName ),new CsvObjectTransactionCoordinator()));
		   	*/
		   	Future<Long> totalTimeFuture = transposeService.submit( new GzipWriterTask(stampQueue,  
                    new File(sourceFile.getParent() +"\\" + tempFileName )));
		   	
		   	
		    Future<Integer> totalNumberofLine = transposeService.submit(new CsvReaderTask( csvRecordsQueue, csvReaderTaskConfig, sourceFile));
		    int nLine = totalNumberofLine.get();
		    
		    String[] uvrDetail = FileUtils.readFileSpecificLine(sourceFile.getAbsolutePath(), ",", 1);
		    FileUtils.archiveFile( sourceFile.getAbsolutePath(),archiveLocation + File.separator);
		    ioTime=totalTimeFuture.get();	
		 
		  createRemoveFile(sourceFile.getName(),this.destDirectory, uvrDetail);
		    
		   deleteOldUvrFile(this.destDirectory,sourceFile.getName());
		    
		    // move uvr.gz to destination  directory
		    moveUvrToDestination(new File(sourceFile.getParent() + "\\"   + String.format("%s.gz.tmp", sourceFile.getName())));
		    
		   }catch(Exception e){
			   e.printStackTrace();
		   }finally{
			   transposeService.shutdown();  
			   
		   }
		
		return ioTime;
	}
	
	
	
	
	
	private boolean createRemoveFile(String fileName, String deleteDir, String[] uvrDetails){
		
		String params[]=fileName.split("#");
		String       COB = "COB="       + params[4] + "\n";
		if( params[2].equals("HYPO") ){
			//2034/8/
			//8
			//19/02/13
			String hypoDate =uvrDetails[1];
			 if(hypoDate.length()==7){
				 COB = "COB="  + "20" + hypoDate.substring(5) + hypoDate.substring(2,4) + "0"+hypoDate.substring(0,1)   + "\n";
			 } else {
				 COB = "COB="  + "20" + hypoDate.substring(6) + hypoDate.substring(3,5) + hypoDate.substring(0,2)   + "\n";
			 }
		}
		
		String CONTAINER = "CONTAINER=" + params[1] + "\n";
		
		if(params[1].equals("VAR AND P&L") ){
			CONTAINER= "CONTAINER=VaR and P&L" + "\n" + "psrName=" + params[0] ;
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

        logger.info( "Deleting old uvr file:" + destDir + oldUvrFile );
   
		File oldUvr = FileUtils.getFile(destDir, oldUvrFile);
		if(oldUvr != null){
			FileUtils.deleteQuietly(oldUvr);
		} else {
		      logger.info( "NO Old UVR Deleting old uvr file:" + oldUvrFile );
		}
		return true;
	}
	
	private boolean startPolling( String dir, String fileName ) {
		
		String[] oldUvrFileName = fileName.split("#");
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
	
	private File moveUvrToDestination(File csvOutputFile){
		
		String newfileName = String.format("%s_UVR.gz", csvOutputFile.getName()).replace(".apx.gz.tmp","") ;
		String destDir = this.destDirectory  + "\\"+ getDateSubDir(sourceFile.getName()) + "\\unified\\" ;  
		File newFile = new File( csvOutputFile.getParent() + "\\"  + newfileName);
		csvOutputFile.renameTo(newFile);
		FileUtils.archiveFile(newFile.getAbsolutePath(), destDir);
		return newFile;
	
	}

	private String getDateSubDir(String fileName){
		String date = fileName.split("#")[4];
		return date.substring(6) + date.substring(4,6) + date.substring(0,4);   

	}

	public String getDestDirectory() {
		return destDirectory;
	}


	private ExecutorService newExecutorService(int thread){
		
		return Executors.newFixedThreadPool(thread,new ThreadFactory() {
			AtomicInteger threadCtr = new AtomicInteger();
			@Override
			public Thread newThread(Runnable r) {
				 Thread t = new Thread(r,"TransposerTask-" + threadCtr.incrementAndGet());
				 return t;
			}
		});
	}
}
