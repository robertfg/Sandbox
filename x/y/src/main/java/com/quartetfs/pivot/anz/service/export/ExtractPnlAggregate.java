package com.quartetfs.pivot.anz.service.export;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.quartetfs.pivot.anz.utils.ANZUtils;

public class ExtractPnlAggregate {

	
	
	
	public static Map<String,Object[]> aggregate(List<Object[]> indexerDatas){
		 Map<String,Object[]> pnls = new ConcurrentHashMap<String,Object[]>(); 
		
		 for (Object[] indexerData : indexerDatas) {
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
	   return pnls;
	   
	}
	/*
	private File renameTempFile(File exportFile) 
	{
		String newfileName = String.format("%s.APX", fileToken);
		File newFile = new File(newfileName.replace(".TMP", "").toUpperCase() );
		Validate.isTrue(exportFile.renameTo(newFile), "File rename failed");
		return newFile;
	}
	
    private Map<String,Object[]>  aggregate(  List<Object[]> csvs){
	   Map<String,Object[]> pnls = new ConcurrentHashMap<String,Object[]>();
	   
	   for (Object[] csv : csvs) {
		   String key = csv[1] + "-" + csv[3];
		   Object[] oldValueObj =  pnls.get( key );
		   
		   if( oldValueObj == null  ){
			   pnls.put( key, new Object[]{ csv[0],csv[1],csv[2],csv[3]  });
		   
		   } else {
			   double[] addedVector = this.addTwoVector( ((String)oldValueObj[2]).split("|")  , ((String)csv[2]).split("|"));
			   pnls.remove( key);
			   pnls.put( key, new Object[]{ csv[0],addedVector,csv[2],oldValueObj[3]  });
			   
		   }   
	   }
	   
	   return pnls;
	}
    
    private double[] addTwoVector(String[] a, String[] b){
	     double[] aggregatedValue=new double[a.length];
 	
	     for (int i = 0; i < b.length; i++) {
	    	 aggregatedValue[i] = Double.valueOf( a[i] ) + Double.valueOf( b[i] ) ;
	 	 }
	    return aggregatedValue;
	   
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
			((FileOutputStream)outputStream).getChannel()
			.truncate( ((FileOutputStream)outputStream).getChannel().size() - 1);
			
			outputStream.flush();				
			outputStream.close();	
			
			
			
			File newFile = renameTempFile(exportFile);				
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
		while(reader.hasNext()) 
		{
			reader.next();
			Object[] tmp = new Object[noOfCols];
			reader.readTuple(tmp);
			rows.add(tmp);
			//LOGGER.info(Arrays.toString(tmp));
			chunkCounter++;
			if(chunkCounter==writeBatchSize) {
				enqueueMessage(rows,batchId, refDates,false);
				//Enqueue for writing
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
	}*/
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
