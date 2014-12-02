package com.quartetfs.pivot.anz.service.export;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.aggfun.IAggregationFunction;
import com.quartetfs.biz.pivot.cellset.ICellProcedure;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.impl.LocationSet;
import com.quartetfs.biz.pivot.query.impl.GetAggregatesQuery;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.impl.DateParser;
import com.quartetfs.pivot.anz.dto.ExportContainerMapping;
import com.quartetfs.pivot.anz.extraction.ExtractUtils;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.utils.QueryHelper;
import com.quartetfs.pivot.anz.webservices.impl.DataExportDTO;
import com.quartetfs.pivot.anz.webservices.impl.ExtractParamsDTO;

public class GetPnlDataFromIndexerTest {

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	private ExtractUtils extractUtils;
	private ExportContainerMapping containerMapping;
	private IActivePivotManager activePivotManager;
	
	public GetPnlDataFromIndexerTest(ExtractUtils extractUtils,ExportContainerMapping containerMapping,IActivePivotManager activePivotManager){
		this.extractUtils = extractUtils;
		this.containerMapping = containerMapping;
		this.activePivotManager = activePivotManager;
	}
	
	
	public void test1DayVarPnlExtract(ExtractParamsDTO extractParamsDTO, String token){
		 DateParser parser  =(DateParser) Registry.getPlugin(IParser.class).valueOf("date[yyyyMMdd]"); 
		 
		ExportPnlDataHelper exportPnl = new ExportPnlDataHelper();
		
		exportPnl.setExportBatchSize(1000);
		exportPnl.setExtractionDirectory("C:\\temp");
		exportPnl.setTaskQueueSize(5);
		exportPnl.setActivePivotManager(activePivotManager);
		
		
	//	schema, cobDate, extractParamsDTO, requestToken, extractionDirectory,taskQueueSize,exportBatchSize
		
		
		DataExportDTO dataExportDTO =  createDataExportDto(extractParamsDTO,containerMapping, true );
		
		exportPnl.enqueue(dataExportDTO, Registry.create(IDate.class, parser.parse(dataExportDTO.getCobDate()).getTime()),token);
		
	}
	
	private DataExportDTO createDataExportDto( ExtractParamsDTO extractParamsDTO,ExportContainerMapping containerMapping,  boolean isPNL){
		
		StringBuilder posId = getPositionIDs(extractParamsDTO,containerMapping,isPNL );
		
		Map<String,String> fieldFilter = new HashMap<String,String>();
			fieldFilter.put("positionId", posId.substring(0,posId.length()-3));

	   
				fieldFilter.put("psrName", "1D");
		 				
	
		 String measureName =  "1DVaRPL_AUD" ;
		 
		 
		 DataExportDTO dataExportDTO = new DataExportDTO();
         dataExportDTO.setCobDate(extractParamsDTO.getCobDate()) ;
         dataExportDTO.setContainerName( extractParamsDTO.getContainer() );
         
         dataExportDTO.setFieldFilter(fieldFilter );
         dataExportDTO.setColumnToExtract( new String[]{ "M_PL_CUR", measureName,"positionId" } );
         dataExportDTO.setRefDates(new StringBuilder("20130402"));
         
         return dataExportDTO;
	
	}
	
	private StringBuilder getPositionIDs( ExtractParamsDTO extractParamsDTO, ExportContainerMapping containerMapping,  boolean isPNL){
		
		Set<ILocation> locations = new LocationSet();	
		QueryHelper queryHelper = null;
		GetAggregatesQuery countingQuery = null;
		List<String> locationPaths = null;
		ICellSet countCellSet =null;
		 
		try {
			   queryHelper = new QueryHelper( extractUtils.getPivot());
			 locationPaths = extractParamsDTO.getLocationPaths();
				
				for (String locationPath : locationPaths) {
					String[] params = locationPath.split("\\|");
					extractParamsDTO.setLocationPath(params[0]);
					extractParamsDTO.setDimensionFilter(params[1]);
					locations.addAll(extractUtils.buildLocations(queryHelper, containerMapping, isPNL,extractParamsDTO));
				}
			 
			 LOGGER.info("UniqueID=" + extractParamsDTO.getFileBatchId() + ",Total number of locations to be query:" + locations.size());
			// Compute an upper bound of the total row count for that query (without max row limit)
				final int estimatedTotalRowCount[] = new int[] {0};
				final StringBuilder positionID = new StringBuilder();
				final Set<String> posIdSet = new TreeSet<String>();
				
				 //countingQuery = new GetAggregatesQuery( locations,  Collections.singleton(IAggregationFunction.COUNT_ID));
				 
				 countingQuery = new GetAggregatesQuery( locations,  Collections.singleton("1DVaRPLVector_AUD.SUM"));
				 
				 
				 countCellSet =  extractUtils.getPivot().execute(countingQuery);
				
				countCellSet.forEachCell(new ICellProcedure() {
					QueryHelper queryHelper = new QueryHelper( extractUtils.getPivot());
					 
					@Override
					public boolean execute(ILocation location, String measure, Object value) {
						try{
						System.out.println(queryHelper.retrieveValue( "Base Currency", location));
					    //estimatedTotalRowCount[0] += (Long)value;
						posIdSet.add((String) queryHelper.retrieveValue( "Position ID", location));
						}catch(Exception e){
							e.printStackTrace();
						}
						return true;
					}
				});
				
		       for (String posId : posIdSet) {
					positionID.append(posId).append(" or ");
			    }
		       
		  	   LOGGER.info("UniqueID=" + extractParamsDTO.getFileBatchId() + ",Total number of position to be query:" + posIdSet.size() + ",positionID.size:" + positionID.length() );
		  	   return positionID;
			 
		
		
	
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, MessagesANZ.EXTRACTION_ISSUE, e);
		} finally{
			       locations = null;
			       queryHelper = null;
			       countingQuery = null;
			       locationPaths = null;
			        countCellSet  = null;
			       
			      
		}
		return null;
   }
}
