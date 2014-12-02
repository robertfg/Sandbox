package com.anz.rer.etl.csvToTable;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.anz.rer.etl.utils.CsvUtils;
import com.anz.rer.etl.utils.DbUtils;

public class Bcp implements  Callable<Long> {

	    BlockingQueue<BcpData> sqlQueue; 
	    private BlockingQueue<BcpData> cleanUpQueue;
	    private File csvFile;
	    private BcpConfig bcpConfig;
	    private final static Logger logger = Logger.getLogger(Bcp.class);
	    private int dataPartition;
	    private String columnDelimeter;
	    private String ignoreString;
	    private long start;
	    private DbUtils dbUtils;
	    
	    private static final Map<String,String> varContainerMapping = new HashMap<String,String>();	
		static {
				varContainerMapping.put("B1AL0", "HYPO");
				varContainerMapping.put("VXAL0", "VAR_10D_AGG");
				varContainerMapping.put("V1AL0", "VAR_1D_AGG");
				varContainerMapping.put("VSAL0", "VAR_STRESS");
				varContainerMapping.put("VFAL0", "VAR_1540_AGG");
		}
		
	    public Bcp(BlockingQueue<BcpData> sqlQueue, File csvFile, BcpConfig bcpConfig,int dataPartition, String columnDelimeter, String ignoreString,
	    		BlockingQueue<BcpData>  cleanUpQueue, long start,  DbUtils dbUtils) {
			super();
			this.sqlQueue = sqlQueue;
			this.csvFile = csvFile;
			this.bcpConfig = bcpConfig;
			this.dataPartition = dataPartition;
			this.columnDelimeter = columnDelimeter;
			this.ignoreString = ignoreString;
			this.cleanUpQueue = cleanUpQueue;
			this.start = start;
			this.dbUtils = dbUtils;
			
		}

		
	    
		private BcpData getBcpDataInfo(File csvFile, int partitionSize){
	    	
			int lineCount = 0;
			try {
				 lineCount = CsvUtils.getNumberOfLine(csvFile.getAbsolutePath());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	    		 BcpData bcpData = new BcpData( csvFile.getName().toString().toUpperCase());
						 bcpData.setStart(System.currentTimeMillis());
						 bcpData.setFilePath(csvFile.getAbsolutePath().toString());
						 bcpData.setCsv(null);
						 bcpData.setTotalLineNumber( lineCount );
						 bcpData.setColumnPerRow(0);
						 bcpData.setTotalPartition(this.getTotalPartition(lineCount, partitionSize));
					
						 bcpData.setStatus("PublishingToStaging"); 
				         
				         updateStatusInDB(bcpData,null);
			              
			             
				 return bcpData;
	     }
		
	    
	    
	    private void putToQueueOpti(BcpData bcpData, int partition, long start, String delimeter, String ignoreString ){
	    	
	    	try{
	    	  /*  FileInputStream fis = new FileInputStream(new File(bcpData.getFilePath()));
			    FileChannel fc = fis.getChannel();
			    MappedByteBuffer mmb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			    byte[]  buffer = new byte[ 1024 ];
			    
			   	mmb.get(buffer);
			    fis.close();

			    BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer )));
	                 int ctr=0;    
	                 int id  = 1;
	                 String csv = null;
	                 List<String[]> csvLines = new ArrayList<String[]>();
	                 String[] header = null;
	                  
	                 boolean gotData = false;
			         for (csv = in.readLine(); csv != null; csv = in.readLine()) {
			        	   if(!csv.startsWith(ignoreString)){
			        		   ctr++;
			        		   if(ctr==partition) {
							 		csvLines.add(csv.split(delimeter)); 
							 		this.putInTheQueue( new BcpData(csvLines,id, bcpData.getColumnPerRow(), bcpData.getTotalPartition(), this.bcpConfig, bcpData.getName(),partition, start ));
							 		header = new String[ csvLines.get(0).length];
							 		header =  csvLines.get(0);
							 		
							 		csvLines = null;
							 		csvLines  = new ArrayList<String[]>();
							 		
							 		id++;
							 		ctr=0;
							 	} else {
									csvLines.add(csv.split(delimeter));
								}
			        		   gotData = true;
			        		  
			        	   }
			         }

			    in.close();*/
	    	
	    		     int ctr=0;    
	                 int id  = 1;
	                 List<String[]> csvLines = new ArrayList<String[]>();
	                 String[] header = null;
	                  
	            boolean gotData = false;
	    		BcpFileReader bcpReader = new BcpFileReader(bcpData.getFilePath());
	    
				for (String csv : bcpReader) {
					if(!csv.startsWith(ignoreString)){
		        		   ctr++;
		        		   if(ctr==partition) {
						 		csvLines.add(csv.split(delimeter, -1));  
						 		
						 		this.putInTheQueue( new BcpData( csvLines,id, bcpData.getColumnPerRow(), 
						 				bcpData.getTotalPartition(), bcpData.getName(),partition, start ));
						 		
						 		header = new String[ csvLines.get(0).length];
						 		header =  csvLines.get(0);
						 		csvLines = null;
						 		csvLines  = new ArrayList<String[]>();
						 		id++;
						 		ctr=0;
						 	} else {
								csvLines.add(csv.split(delimeter,-1));
							}
		        		   gotData = true;
		        	   }
				}
				bcpReader.Close();
				bcpReader = null;
		    	if (!csvLines.isEmpty()) {
		    		gotData = true;
		    		header = new String[ csvLines.get(0).length ];
			 		header =  csvLines.get(0);
			 		this.putInTheQueue( new BcpData(csvLines,id, bcpData.getColumnPerRow() ,bcpData.getTotalPartition(), bcpData.getName(),partition,start ));
				}
	    	
		    	logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>End Of File GOT DATA:" + gotData + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		    	
		    	if(!gotData){
		    		 BcpData emptyFile = new BcpData();
		    		 emptyFile.setStart(start);
		    		 emptyFile.setName(csvFile.getName().toString().toUpperCase());
		    		 emptyFile.setStatus("No Data");
		    		 try {
		    			logger.info("No Data:" + emptyFile.getName());
						cleanUpQueue.put(emptyFile);
						updateStatusInDB(emptyFile,null);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		    	} else {
		    		logger.debug("Sending EOF to QUEUE>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		    		
		             
		    		this.putInTheQueue( new BcpData( bcpData.getName(), true, header ));
		    	}
		    	
		    	bcpData = null;
	    	} catch(Exception e){
	    		e.printStackTrace();
	    	} finally{
	    		
	    		
	    	}	
	    }
	    
	     
	    private int getTotalPartition( int totalLine, int partitionSize ){
	    	int x = totalLine / partitionSize;
	    	int y = totalLine % partitionSize;
	    
	    	if(y > 0 ){
	    		x++;
	    	}
	    	return x;
	    }
	     
	    private void putInTheQueue(BcpData bcpData ){
	    	 try {
	    		// System.out.println("putin queue:" + bcpData.getName() + ":" + bcpData.getId());
	    		 logger.debug("putting in bucket:" + bcpData.getName() + ":" + bcpData.getId());
	    		 
	    		 sqlQueue.put(bcpData);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    }
	    
	    
	    
	    
	    
     public  static void readFile(String ignoreString, int partition, String delimeter, String filePath) {
	    	try{
	    	  FileInputStream fis = new FileInputStream(new File( filePath ));
			    FileChannel fc = fis.getChannel();
			    MappedByteBuffer mmb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			    byte[] buffer = new byte[(int)fc.size()];
			    mmb.get(buffer);
			    fis.close();

			    BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer)));
			    
	                 int ctr=0;    
	                 int id  = 1;
	                 String csv = null;
	                 List<String[]> csvLines = new ArrayList<String[]>();
	                 boolean gotData = false;
			         for (csv = in.readLine(); csv != null; csv = in.readLine()) {
			        	   if(!csv.startsWith(ignoreString)){
			        		   ctr++;
			        		   if(ctr==partition) {
							 		csvLines.add(csv.split(delimeter,-1)); 
							 		csvLines = null;
							 		csvLines  = new ArrayList<String[]>();
							 		id++;
							 		ctr=0;
							 	} else {
									csvLines.add(csv.split(delimeter,-1));
								}
			        		   gotData = true;
			        		  
			        	   }
			         }

			    in.close();
	    	
	    	
		    	if (!csvLines.isEmpty()) {
		    		gotData = true;
				}
	    	
		    	
		    	if(!gotData){
		    		
		    	}
		    	
		    	
	    	} catch(IOException ioEx){
	    		ioEx.printStackTrace();
	    	} catch(Exception e){
	    		e.printStackTrace();
	    	}	
	    }
	    
	    
	    public static void main(String[] args){
	    	
	    	String[] x = new String[2];
	    	x[0] = "Afasdfasdf";
	    	x[1] = "affffffffffffff";
	    	
	    
	    	
	    /*	 long start = System.currentTimeMillis();
	          Bcp.readFile("#", 10000, ",", "C:\\temp\\BigData\\SIGM0#IR_GAMMA#NON-VAR#2704901785946#20121029#2704901785946_xxx.APX");
   	         
	          System.out.println( System.currentTimeMillis() - start  );
	    */
	    }

		public void setDataPartition(int dataPartition) {
			this.dataPartition = dataPartition;
		}

		public int getDataPartition() {
			return dataPartition;
		}

		public void setIgnoreString(String ignoreString) {
			this.ignoreString = ignoreString;
		}

		public String getIgnoreString() {
			return ignoreString;
		}

		public void setColumnDelimeter(String columnDelimeter) {
			this.columnDelimeter = columnDelimeter;
		}

		public String getColumnDelimeter() {
			return columnDelimeter;
		}

		public void setStart(long start) {
			this.start = start;
		}

		public long getStart() {
			return start;
		}
		
		public boolean updateStatusInDB(BcpData bcpData, String status){
			
			String containerName = this.getContainer(bcpData.getName());
			String cobDate = this.getCobDate(bcpData.getName());
			
			if(status==null){
				status = bcpData.getStatus();
			}
			
			if( containerName !=null  ) {
				logger.info(  "Updating status in DB");
				 try { 
				if(containerName.equalsIgnoreCase( "VAR AND P&L" )){
		    		containerName = varContainerMapping.get( bcpData.getPsrCode() );
		    	}
				   	String  sql =  "{ call [DW].[UpdateSignoffAndExclude] (" +cobDate + ",'"+ containerName + "','" +status+ "') }"; 
				   	dbUtils.executeSp(sql);
					} catch (SQLException e) {
						e.printStackTrace();
					}
					logger.info(  "No data pushed to DWH ");
			}
			 return true;
		}
		
		

		private String getContainer(String fname){
			fname = fname.replace(".APX", "");
			return fname.split("#")[1];
		}
		
		private String getCobDate(String fname){
			fname = fname.replace(".APX", "");
			return fname.split("#")[4];
		}



		@Override
		public Long call() throws Exception {
			  logger.info("Starting process:" + csvFile.getName());	
		      try{
		    	  this.putToQueueOpti( this.getBcpDataInfo( csvFile,dataPartition) , dataPartition, start, columnDelimeter, ignoreString);
		      }catch(Exception e){
		    	  e.printStackTrace();
		      }
			return 1l;
		}		

}



