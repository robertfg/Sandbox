package com.quartetfs.pivot.anz.service.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellProcedure;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.impl.LocationSet;
import com.quartetfs.biz.pivot.query.QueryTimeoutException;
import com.quartetfs.biz.pivot.query.impl.GetAggregatesQuery;
import com.quartetfs.pivot.anz.dto.ExportContainerMapping;
import com.quartetfs.pivot.anz.extraction.ExtractUtils;
import com.quartetfs.pivot.anz.extraction.IRowBuilder;
import com.quartetfs.pivot.anz.extraction.VaRRowBuilder;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.service.export.ExtractObject.ExtractType;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.DBUtils;
import com.quartetfs.pivot.anz.utils.QueryHelper;
import com.quartetfs.pivot.anz.webservices.impl.ExtractParamsDTO;

public class ExtractVarTask implements Callable<ExportTaskThreadInfo> /*Runnable*/ {
	 
	public ExtractVarTask(	ExtractUtils extractUtils,	ExtractParamsDTO extractParamsDTO,
			BlockingQueue<ExtractObject> vectorizerQueue, BlockingQueue<ExtractObject> fileWriterQueue,
			ExtractAPQueryPool extractAPQueryPool, int retryTime, int retryCount, DBUtils dbUtils ) {
		
		this.extractUtils = extractUtils;
		this.extractParamsDTO    =  extractParamsDTO;
		this.vectorizerQueue     = vectorizerQueue;
		this.fileWriterQueue     = fileWriterQueue;
		this.extractAPQueryPool = extractAPQueryPool;
		this.retryTime = retryTime; 
		this.dbUtils = dbUtils;
		this.retryCount = retryCount;
		
	} 

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	private ExtractUtils extractUtils;
	private ExtractParamsDTO extractParamsDTO;
	private BlockingQueue<ExtractObject> vectorizerQueue;
	private BlockingQueue<ExtractObject> fileWriterQueue;
	private ExtractAPQueryPool extractAPQueryPool;
	private int retryTime;
	private  DBUtils dbUtils;
	private int retryCount;
	private int retCount;
	
	public void run(){
		extract(this.extractParamsDTO);
		
	}
	/*@Override*/
	public ExportTaskThreadInfo call() throws Exception {
		return extract(this.extractParamsDTO);
	}
	
	public ExportTaskThreadInfo extract(ExtractParamsDTO extractParamsDTO) {
		long totalTime = 0l;
		
			// psrName = VAR_1D;
			// containerName = VAR_1d to Var and PNL;
		    // psrCode = VXAL0
			// varType = VAR_1D
		LOGGER.info( "SignOff VAR param:" + extractParamsDTO.toString()  );
		extractParamsDTO.setPsrName(extractParamsDTO.getContainer());
		extractParamsDTO.setVarType(extractParamsDTO.getPsrName());
		extractParamsDTO.setPsrCode( extractUtils.getContainerNameToPsrName().get( extractParamsDTO.getPsrName() )  );
		
		if(!extractParamsDTO.getContainer().equalsIgnoreCase(ANZConstants.VAR_STRESS_CONTAINER) 
				&& !extractParamsDTO.getContainer().equalsIgnoreCase(ANZConstants.VAR_SIX_YEAR_CONTAINER) ){
			extractParamsDTO.setContainer( ANZConstants.VAR_CONTAINER );
		}
		
		if(extractParamsDTO.getPsrName().equals("HYPO")){
			if(extractParamsDTO.getExtractType().equals( ExtractType.HYPO)){
				totalTime+= this.getHypo(extractParamsDTO);		
			} else {
				totalTime+= this.getVar(extractParamsDTO);  
			}
		} else {
		   
			if( extractParamsDTO.getExtractType().equals(ExtractType.VAR_CONFIDENCE)
					|| extractParamsDTO.getExtractType().equals(ExtractType.VAR_SIX_YEAR_CONFIDENCE) ){
				LOGGER.info("Starting AGG VAR extract start ..");
			    totalTime+= this.getVar(extractParamsDTO);  
				
			} else if(extractParamsDTO.getExtractType().equals(ExtractType.VAR_PNL) 
					|| extractParamsDTO.getExtractType().equals(ExtractType.VAR_PNL_PORTFOLIO) 
					||extractParamsDTO.getExtractType().equals(ExtractType.VAR_SIX_YEAR_PNL) 
					|| extractParamsDTO.getExtractType().equals(ExtractType.VAR_SIX_YEAR_PNL_PORTFOLIO)  ){
				
				LOGGER.info("Starting PNL Vector extract..");
				totalTime+=this.getPnLVector(extractParamsDTO);  
			
			} else if(extractParamsDTO.getExtractType().equals(ExtractType.VAR_STRESS_PNL) 
					|| extractParamsDTO.getExtractType().equals(ExtractType.VAR_STRESS_PNL_PORTFOLIO) ){
				LOGGER.info("Starting VAR Stress PNL extract..");
				totalTime+=this.getPnLVector(extractParamsDTO);  
			
			}else if(extractParamsDTO.getExtractType().equals(ExtractType.VAR_STRESS_CONFIDENCE)){
				
				LOGGER.info("Starting AGG VAR STRESS extract start ..");
			    totalTime+= this.getVar(extractParamsDTO);  
				
			} else {
				LOGGER.info("Starting VAR Stress extract..");
				totalTime+=this.getVarStress(extractParamsDTO);  
			}
	
		}
		 return new ExportTaskThreadInfo(totalTime, Long.valueOf(extractParamsDTO.getFileBatchId()) );
	}
	
	 private long getPnLVector( ExtractParamsDTO extractParamsDTO ){
		
		  if(extractParamsDTO.getLocationPaths().size()==0 ){
			 createEmptyFile( "Empty file created for PNL extract due to NO Portfolio Child node OR No data was extracted from the CUBE" ); 
	    	 return -1;
		 } 
		 
		          String filePath = extractUtils.buildFilePath(extractParamsDTO, "PNL-" + extractParamsDTO.getVarType() + "#"
                  + extractParamsDTO.getFileBatchId() ,ANZConstants.FILE_EXTRACTION_TMP,extractParamsDTO.getFileBatchId());
   	  
		         try{ 
			 	     List<String[]> pnlData =  this.getPnl(extractParamsDTO, 
				    		extractUtils.getContainerMapping(true, extractParamsDTO.getVarType(),extractParamsDTO.getContainer()) ,true);
				    
			 	      if(pnlData!=null&&pnlData.size()>0){
			 	    	  extractUtils.createRefDate(extractUtils.getExtractionDirectory(), extractParamsDTO.getFileBatchId(), 
			 	    			  extractUtils.getRefDate(extractParamsDTO.getCobDate(), extractParamsDTO.getVarRefDateType()));
			 	    	  try {
			 	    		  
							fileWriterQueue.put(new ExtractObject(  Long.valueOf(extractParamsDTO.getFileBatchId()), 
									pnlData ,extractParamsDTO.isDone(), extractParamsDTO.getExtractType() , 
									filePath, extractParamsDTO.getTotalPartitionRequest(),extractParamsDTO.isFromFM()  ));
						
						  } catch (NumberFormatException e) {
							e.printStackTrace();
						  } catch (InterruptedException e) {  
							 e.printStackTrace();
						  }
						  return pnlData.size();
			 	      } else {
			 	    	 createEmptyFile( "Empty file created for PNL extract due to No data was extracted from the CUBE" ); 
			 	    	  return -1l;
			 	      }
		          }catch(Exception e){
					  handleException(e);
				  }
		         
		   	  return -1l;
		 	   
		      
	   }
	
	 
	 
	 private long getVar( ExtractParamsDTO extractParamsDTO ){
		 
			
		 try {
			   String filePath = extractUtils.buildFilePath(extractParamsDTO, "VAR-" + extractParamsDTO.getVarType() + "#" + extractParamsDTO.getFileBatchId(),
						 ANZConstants.FILE_EXTRACTION_TMP,extractParamsDTO.getFileBatchId());
			
			   if(extractParamsDTO.getExtractType().equals(ExtractType.HYPO_NODE)){
				    extractParamsDTO.setVarType("HYPO_NODE");
				    filePath = extractUtils.buildFilePath(extractParamsDTO, extractParamsDTO.getVarType() + "#" + extractParamsDTO.getFileBatchId(),
							 ANZConstants.FILE_EXTRACTION_TMP,extractParamsDTO.getFileBatchId());
				    
						if (extractParamsDTO.getPreviousCobDate() != null) {
							LOGGER.info("HYPO_NODE extract date:" + extractParamsDTO.getPreviousCobDate());
							extractParamsDTO.setCobDate(extractParamsDTO.getPreviousCobDate());
						} else {
							LOGGER.info("HYPO_NODE extract, No Previous date");
						}
			   } else if(extractParamsDTO.getExtractType().equals(ExtractType.VAR_STRESS_CONFIDENCE)){
				   extractParamsDTO.setContainer("VAR_STRESS");
			   }
				
			    LOGGER.info("FilePath:" + filePath);
				     
			 	List<String[]> apQueryResult =  doQuery(  extractParamsDTO, extractUtils.getContainerMapping(false, extractParamsDTO.getVarType(),extractParamsDTO.getContainer()), false);  
					   
			 	       vectorizerQueue.put( new ExtractObject(  Long.valueOf(extractParamsDTO.getFileBatchId()), apQueryResult ,
			 	    		                                        extractParamsDTO.isDone(),ExtractType.VAR_CONFIDENCE, filePath,
			 	    		                                        extractParamsDTO.getTotalPartitionRequest()) );
					   
			 	       return extractParamsDTO.getExecTime();	
			
		 	   } catch ( Exception e )  {
		 		  handleException(e);
		 	   } finally{
		        }
		 return -1l;
	  }

	 private long getHypo( ExtractParamsDTO extractParamsDTO ) {
	
	     String filePath = extractUtils.buildFilePath(extractParamsDTO, "HYPO#" + extractParamsDTO.getFileBatchId(),
	    		 ANZConstants.FILE_EXTRACTION_TMP,extractParamsDTO.getFileBatchId());
	     
	     try{
	    	
	    	if(extractParamsDTO.getPreviousCobDate()!=null) {
	    		LOGGER.info("HYPO extract date:" + extractParamsDTO.getPreviousCobDate());
				extractParamsDTO.setCobDate(extractParamsDTO.getPreviousCobDate());
			}else {
				LOGGER.info("HYPO extract, No Previous date");
			}
	    	
		    List<String[]> origVarData =  doQuery(  extractParamsDTO,  
		    	extractUtils.getContainerMapping(false, extractParamsDTO.getVarType(),extractParamsDTO.getContainer()),  true);  
		    
			  fileWriterQueue.put(new ExtractObject(  Long.valueOf(extractParamsDTO.getFileBatchId()), 
					    origVarData ,extractParamsDTO.isDone(), ExtractType.HYPO, filePath, extractParamsDTO.getTotalPartitionRequest() )  );
			  
			  
	      return extractParamsDTO.getExecTime();
	      
	     } catch (InterruptedException e) {
				 e.printStackTrace();
		   } catch ( Exception e )  {
			  handleException(e);
		   } 	
		return -1l;
	  }
      
	 private long getVarStress( ExtractParamsDTO extractParamsDTO ) {
		 String filePath = extractUtils.buildFilePath(extractParamsDTO, "VAR_STRESS#" + extractParamsDTO.getFileBatchId(),
	    		 ANZConstants.FILE_EXTRACTION_TMP,extractParamsDTO.getFileBatchId());
		    LOGGER.info("FilePath:" + filePath);
		       
		       try {
			   	   List<String[]> apQueryResult =  doQuery(  extractParamsDTO, extractUtils.getContainerMapping(false, extractParamsDTO.getVarType(),extractParamsDTO.getContainer()), false);  
			 	       vectorizerQueue.put( new ExtractObject(  Long.valueOf(extractParamsDTO.getFileBatchId()), apQueryResult ,
			 	    		                                        extractParamsDTO.isDone(),ExtractType.VAR_STRESS, filePath,
			 	    		                                        extractParamsDTO.getTotalPartitionRequest()) );
			 	       return extractParamsDTO.getExecTime();		
		 	   } catch ( Exception e )  {
				   handleException(e);
			   } finally{
		        }
		 return -1l;
	  }
 
	 private List< String[] > doQuery( ExtractParamsDTO extractParamsDTO, ExportContainerMapping containerMapping,  boolean isPNL) throws Exception{
			
		   
			List<String[]> apResultData = null;
			Set<ILocation> locations = new LocationSet();	
			ICellSet cellSet = null;
			List<String> headerAttributes = null;
			List<String> levelsForOutput = null;
			QueryHelper queryHelper = null;
			
			try {
				 
				 queryHelper = new QueryHelper( extractUtils.getPivot());
				 List<String> locationPaths = extractParamsDTO.getLocationPaths();
					
					for (String locationPath : locationPaths) {
						String[] params = locationPath.split("\\|");
						extractParamsDTO.setLocationPath(params[0]);
						extractParamsDTO.setDimensionFilter(params[1]);
						locations.addAll(extractUtils.buildLocations(queryHelper, containerMapping, isPNL,extractParamsDTO));
					}
				 
				 LOGGER.info("Total number of locations to be query:" + locations.size());
				 LOGGER.info("ExtractParamDTO container:" + extractParamsDTO.getContainer() + "," + "cobDate:" + extractParamsDTO.getCobDate() );
				 
			//	 if(extractParamsDTO.isDebug()){throw new QueryTimeoutException( new GetAggregatesQuery(locations, containerMapping.getMeasures()), 480);}
				 
				 
				 cellSet=queryHelper.getCellSet(locations,  containerMapping.getMeasures() );
				 LOGGER.info("QueryHelper CellSet Done");
				 
				 levelsForOutput = containerMapping.getDefaultLevelsForOutput();
				 levelsForOutput.addAll(extractUtils.getLevels(containerMapping.getDimensions(), extractUtils.getPivot()));
			
				// write header
				 headerAttributes=new ArrayList<String>(levelsForOutput); //401 510397
				 headerAttributes.addAll( containerMapping.getMeasures());
	
				 String header = extractUtils.buildVarHeader( headerAttributes   );
							 
				 apResultData = getAggregatedResults( queryHelper, cellSet, 
						     	new VaRRowBuilder(levelsForOutput, containerMapping.getMeasures() ), 
							    extractParamsDTO,header);
		
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, MessagesANZ.EXTRACTION_ISSUE, e);
				throw e;
				  
			} finally{
				       locations = null;
				         cellSet = null;
				headerAttributes = null;
				 levelsForOutput = null;
				     queryHelper = null;
			}
			return apResultData;
	   }
     
	
	private List<String[]> getPnl( final ExtractParamsDTO extractParamsDTO, ExportContainerMapping containerMapping,  boolean isPNL) throws Exception{
				
				Set<ILocation> locations = new LocationSet();	
				final QueryHelper queryHelper = new QueryHelper( extractUtils.getPivot());
				
				 List<String> locationPaths = null;
				 ICellSet countCellSet =null;
				 final List<String[]> pnls = new ArrayList<String[]>();
				// final Extract
				
				 //extractParamsDTO.getVarType()
				 String measureName = "";
				 final String levelDim =  (extractParamsDTO.getExtractType().equals( ExtractType.VAR_STRESS_PNL_PORTFOLIO  ) 
					  || extractParamsDTO.getExtractType().equals( ExtractType.VAR_PNL_PORTFOLIO ) 
					  || extractParamsDTO.getExtractType().equals( ExtractType.VAR_SIX_YEAR_PNL_PORTFOLIO ) )?"M_PTFOLIO":"Position ID";
					 
				 
				 try {
					 
				  	  locationPaths = extractParamsDTO.getLocationPaths();
						for (String locationPath : locationPaths) {
							String[] params = locationPath.split("\\|");
							extractParamsDTO.setLocationPath(params[0]);
							if(params!=null && params.length>1){
							  extractParamsDTO.setDimensionFilter(params[1]);
							}
							locations.addAll(extractUtils.buildLocations(queryHelper, containerMapping, isPNL,extractParamsDTO));
						}
					 
					   LOGGER.info("UniqueID=" + extractParamsDTO.getFileBatchId() + ",Total number of locations to be query:" + locations.size());
						
					   if( extractParamsDTO.getVarType().equals("VAR_1D")  ){
						   measureName = "1DVaRPLVector_AUD.SUM";
					   }else if( extractParamsDTO.getVarType().equals("VAR_10D")  ){
						   measureName = "10DVaRPLVector_AUD.SUM";
					   }else if( extractParamsDTO.getVarType().equals("VAR_STRESS")  ){
						   measureName = "StressVector_AUD.SUM";
					   } else if( extractParamsDTO.getVarType().equals("VAR_1540")  ){
						   measureName = "PLSixYearMeasure.SUM";
					   } 
					   
				   if(extractParamsDTO.isDebug()){throw new QueryTimeoutException( new GetAggregatesQuery(locations, Collections.singleton(measureName)), 480);}
					 	 countCellSet =  queryHelper.getCellSet(locations,  Collections.singleton(measureName) );
						 countCellSet.forEachCell(new ICellProcedure() {
							@Override
							public boolean execute(ILocation location, String measure, Object value) {
								try{
									String[] detail = new String[4];
								          detail[0] = extractParamsDTO.getFileBatchId();
								          detail[1] = (String)queryHelper.retrieveValue( "Base Currency", location);
										  detail[2] =  arrToStr(value); 
								          detail[3] = (String)queryHelper.retrieveValue( levelDim, location);
								          pnls.add(detail);   
								}catch(Exception e){
									e.printStackTrace();
								} 
								return true;
							} 
						
						   String arrToStr(Object arr){
							    double[] pnl = (double[])arr;
							    StringBuilder ret = new StringBuilder( "" + pnl[0]);
							    for (int i = 1; i < pnl.length; i++) {
								  ret.append(",").append( pnl[i]);
							    }
							   return ret.toString();
						     } 
						  });
						
				  	   LOGGER.info("UniqueID=" + extractParamsDTO.getFileBatchId() + ",Total number of pnl record:" + pnls.size()   );
				  	   return pnls;
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, MessagesANZ.EXTRACTION_ISSUE, e);
					throw e;
					
				} finally{
					       locations = null;
					       locationPaths = null;
					       countCellSet  = null;
				}
			
		   }
	
	  
	 
	
	 private List<String[]>  getAggregatedResults(final QueryHelper queryHelper, final ICellSet cellSet,
				final IRowBuilder rowBuilder,final ExtractParamsDTO extractParamsDTO, String header) throws IOException {
		 
		    LOGGER.info("Getting Aggregates..............");
		 
			long startTime = System.currentTimeMillis();
			List<String[]> results = null;
			
				VarData vData  = new VarData(queryHelper, cellSet, rowBuilder, extractParamsDTO);
			try {
				 cellSet.forEachLocation( vData);
				 results = vData.getVarData();
				 LOGGER.info("DONE for each query....................");
				 
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, MessagesANZ.EXTRACTION_ISSUE, e);
				e.printStackTrace();
			} finally {
				long endTime = System.currentTimeMillis() - startTime;
				 extractParamsDTO.setExecTime(endTime);
				 LOGGER.info("AP Extraction Time extractID:" + extractParamsDTO.getFileBatchId() + " " 
							+ ((endTime) ) + " ms" + " Record Size:" + vData.getVarData().size() + " VarType:" + extractParamsDTO.getPsrCode());
		     	
			}
			vData = null;
			return results ;
		}
	 
	
	 private void createEmptyFile(String message){
		 
		 String posIdZero = extractUtils.buildFilePath(extractParamsDTO, "PNL-" + extractParamsDTO.getVarType() + "#" 
                 + extractParamsDTO.getFileBatchId() ,ANZConstants.FILE_EXTRACTION_EXT,extractParamsDTO.getFileBatchId());
	            
		          File empty = new File(posIdZero);
		          
		          try
		          {
		        	  empty.createNewFile(); 
		        	  LOGGER.info( message);
		           } catch(IOException ioe)
		          {
		              LOGGER.info(ioe.getLocalizedMessage());
		          }
		 
	 }
	 
	 private void handleException(Exception e){
		 if(e instanceof  QueryTimeoutException   ){
				try {
					dbUtils.spUpdateExtractStatus( extractParamsDTO.getCobDate(), extractUtils.getDbContainer(extractParamsDTO), "CubeDataExtractError" );
					 if(extractParamsDTO.isDebug()){extractParamsDTO.setDebug(false);}
					 extractParamsDTO.setContainer(extractParamsDTO.getPsrName());
					 if(this.retCount<retryCount){
					    	Thread.sleep( retryTime );
							this.retCount = retCount+1;
							this.extractAPQueryPool.getQueryQueue().put(this);
					    }
					
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} else {
				e.printStackTrace();
			}
	 }
	 
	  
	  
}