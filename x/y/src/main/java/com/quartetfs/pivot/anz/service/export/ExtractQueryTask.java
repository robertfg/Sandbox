package com.quartetfs.pivot.anz.service.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.pivot.anz.dto.ExportContainerMapping;
import com.quartetfs.pivot.anz.extraction.ExtractUtils;
import com.quartetfs.pivot.anz.extraction.IRowBuilder;
import com.quartetfs.pivot.anz.extraction.VaRRowBuilder;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.utils.QueryHelper;
import com.quartetfs.pivot.anz.webservices.impl.ExtractParamsDTO;

public class ExtractQueryTask {

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	private ExtractUtils extractUtils;
	private ExtractParamsDTO extractParamsDTO;
  
	
	
	
	private void getData(){
		ExportContainerMapping containerMapping = getContainerMapping(false);
		
	}
	private ExportContainerMapping getContainerMapping( boolean isPNL) {
		
		String prefix = isPNL ? "pnl." : "var.";

		List<String> levelsList     = extractUtils.getCrossJoinProperty(prefix,	 extractParamsDTO.getVarType(), ".levels");
		List<String> measuresList   = extractUtils.getCrossJoinProperty(prefix,  extractParamsDTO.getVarType(), ".measures");
		List<String> dimensionList  = extractUtils.getCrossJoinProperty(prefix,  extractParamsDTO.getVarType(), ".dimensions");

		return new ExportContainerMapping(	extractParamsDTO.getContainer(), levelsList, measuresList,
				dimensionList);

		//return doQuery(extractParamsDTO, containerMapping, isPNL, destQueue);

	}

	private List<String[]> doQuery(ExtractParamsDTO extractParamsDTO,
			ExportContainerMapping containerMapping, boolean isPNL,
			BlockingQueue<ExtractObject> destQueue) {

		long startTime = System.currentTimeMillis();
		final AtomicLong count = new AtomicLong(0);
		List<String[]> origVarData = null;

		try {
			final QueryHelper queryHelper = new QueryHelper(extractUtils.getPivot());
			Set<ILocation> locations = this.buildLocations(queryHelper,	containerMapping, isPNL);

			ICellSet cellSet = queryHelper.getCellSet(locations,containerMapping.getMeasures());

			List<String> levelsForOutput = containerMapping.getDefaultLevelsForOutput();
						 levelsForOutput.addAll(extractUtils.getLevels(containerMapping.getDimensions(), extractUtils.getPivot()));
 
						 
			// write header
			List<String> headerAttributes = new ArrayList<String>(levelsForOutput);
			headerAttributes.addAll(containerMapping.getMeasures());

			String header = extractUtils.buildVarHeader(headerAttributes);

			origVarData = getAggregatedResults(	queryHelper, cellSet,new VaRRowBuilder(levelsForOutput, containerMapping
							.getMeasures()), extractParamsDTO, header,
					destQueue);

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, MessagesANZ.EXTRACTION_ISSUE, e);
		} finally {
			LOGGER.log(Level.INFO, MessagesANZ.EXTRACTION_TIME, new Object[] {(System.currentTimeMillis() - startTime),
					"PNL:" + isPNL + ":" + count, "" });
		}
		return origVarData;
	}

	private Set<ILocation> buildLocations(QueryHelper queryHelper,
			ExportContainerMapping containerMapping, boolean isPNL) {

		Set<ILocation> locations = new HashSet<ILocation>();

		if (extractParamsDTO.getLocationPath() == null) {
			Map<String, Object> queryParameters = extractUtils
					.generateQueryParameters(extractParamsDTO, containerMapping);
			// generate locations
			List<List<Map<String, Object>>> allDimensions = new ArrayList<List<Map<String, Object>>>();
			for (String dimension : containerMapping.getDimensions()) {
				allDimensions.add(extractUtils.getAllLevelPath(dimension,extractUtils.getPivot()));
			}

			Iterator<List<Map<String, Object>>> iterator = allDimensions
					.iterator();
			List<Map<String, Object>> first = iterator.next();

			while (iterator.hasNext()) {
				List<Map<String, Object>> other = iterator.next();
				first = extractUtils.crossjoin(first, other, queryParameters);
			}

			//LOGGER.info("Query Used for Location:" + queryParameters);
			for (Map<String, Object> mapping : first) {
				mapping.putAll(queryParameters);
				ILocation location = queryHelper.computeLocation(mapping);
				locations.add(location);
			}

		} else {
			List<Map<String, Object>> queryParameters = extractUtils.generateVarQueryParameters(extractParamsDTO,containerMapping, isPNL);

			for (Map<String, Object> queryParameter : queryParameters) {
				ILocation location = queryHelper
						.computeLocation(queryParameter);
				locations.add(location);
			}
		//	LOGGER.info("Query Used for Location:" + queryParameters);
		}

		return locations;

	}
	
	
	 private List<String[]>  getAggregatedResults(final QueryHelper queryHelper, final ICellSet cellSet,
				final IRowBuilder rowBuilder,final ExtractParamsDTO extractParamsDTO, String header,
				BlockingQueue<ExtractObject> destQueue) throws IOException {
			
			long startTime = System.currentTimeMillis();
			
				VarData vData  = new VarData(queryHelper, cellSet, rowBuilder, extractParamsDTO);
				
			try {
				 cellSet.forEachLocation( vData); 
			   //  destQueue.put( new ExtractObject(-2, null, true)) ;
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, MessagesANZ.EXTRACTION_ISSUE, e);
				e.printStackTrace();
			} finally {
				LOGGER.info("Time to iterate over cellSet and populate VarData Object:" + extractParamsDTO.getLocationPath() 
			   			+ " Var Data:" + (System.currentTimeMillis() - startTime) + " ms");
			}
			return vData.getVarData();
		}
}
