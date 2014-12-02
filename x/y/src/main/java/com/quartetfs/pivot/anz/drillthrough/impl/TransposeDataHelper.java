package com.quartetfs.pivot.anz.drillthrough.impl;

import static com.quartetfs.pivot.anz.drillthrough.DrillThroughConstants.DEFAULT_PREFIX;
import static com.quartetfs.pivot.anz.drillthrough.DrillThroughConstants.DEFAULT_TOTAL_HEADER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import com.quartetfs.biz.pivot.dto.DrillthroughHeaderDTO;
import com.quartetfs.biz.pivot.dto.DrillthroughResultDTO;
import com.quartetfs.biz.pivot.dto.DrillthroughRowDTO;
import com.quartetfs.biz.pivot.logging.impl.MessagesServer;
import com.quartetfs.pivot.anz.comparator.impl.CustomTenorComparator;
import com.quartetfs.pivot.anz.drillthrough.DrillThroughConstants.Headers;
import com.quartetfs.pivot.anz.drillthrough.SpreadType;
import com.quartetfs.pivot.anz.drillthrough.TransposeRequestInput;
import com.quartetfs.tech.point.impl.Point;
/**
 * Helper class to transpose the DrillthroughResultDTO;
 *
 */
public class TransposeDataHelper 
{
	private static Logger logger = Logger.getLogger(MessagesServer.LOGGER_NAME, MessagesServer.BUNDLE);
	private TransposeRequestInput transposeRequest;
	
	public TransposeDataHelper(TransposeRequestInput transposeRequest)
	{
		this.transposeRequest=transposeRequest;		
	}
	
	
	@SuppressWarnings("unchecked")
	public DrillthroughResultDTO transpose(DrillthroughResultDTO result ) 
	{
		StopWatch stopWatch = new StopWatch();	
		
		CustomTenorComparator customTenor = new CustomTenorComparator();
		customTenor.setFirstObjects( Arrays.asList(new String[]{"1d","t/n"}));
		                      
		
		Set<String> tenors=new TreeSet<String>(customTenor);	
		Map<Headers,Integer> headerIdx=transposeRequest.getColumnDetails().getHeaderIdx();
		
		stopWatch.start("Group By SpreadType");
		Map<SpreadType,List<DrillthroughRowDTO>> spreadTypeRows = groupBySpreadType(result, tenors);
		stopWatch.stop();
		
		for(Map.Entry<SpreadType,List<DrillthroughRowDTO>> entry: spreadTypeRows.entrySet())
		{
			stopWatch.start(String.format("Tranpose For spreadType %s", entry.getKey()));
			entry.getKey().transpose(entry.getValue(),transposeRequest);
			stopWatch.stop();
		}
		
		applyTenorFilter(tenors);
		
		List<DrillthroughRowDTO> newRows=new ArrayList<DrillthroughRowDTO>();
		
		stopWatch.start("Prepare Transpose Result");
		int ordinal=0;
		// build final list  at this time we have accumulated all the deals and we need to create final output
		Set<Integer> skipColsIndex = createSkipColHeader();
		
		for (Entry<Point, DrillThroughRowContent> entry:transposeRequest.getTransposedData().entrySet())
		{
			Object[] rawData=entry.getValue().getContent();
				List<Object> newData=populateNewRow(tenors, skipColsIndex, entry.getValue(), rawData);
				newRows.add(new DrillthroughRowDTO(ordinal++, newData.toArray()));
		 }
		stopWatch.stop();
		
		List<DrillthroughHeaderDTO> headers=result.getHeaders();
		
		//remove M_result and transpose column
		DrillthroughHeaderDTO dealHeader=headers.get(headerIdx.get(Headers.DEALNUMBER));
		DrillthroughHeaderDTO measureHeader=headers.get(headerIdx.get(Headers.MEASURE));
		DrillthroughHeaderDTO transposeHeader=headers.get(headerIdx.get(Headers.TRANSPOSE));
		//DrillthroughHeaderDTO scenarioTermHeader=headers.get(headerIdx.get(Headers.SCENARIOTERMUNDERLYING));
		DrillthroughHeaderDTO Hypothetical=headers.get(5);
				
		headers.remove(dealHeader);
		headers.remove(measureHeader);		
		headers.remove(transposeHeader);
	//	headers.remove(scenarioTermHeader);
		headers.remove(Hypothetical);
	
		
		
		// add deal number first, followed by Total and terms
		headers.add(0,dealHeader);
		headers.add(1,DEFAULT_TOTAL_HEADER);
		int offset=2;
		for(String tenor:tenors){
				headers.add(offset++, new DrillthroughHeaderDTO(DEFAULT_PREFIX+tenor));
		}  
		logger.info("Transpose Timing:" + stopWatch);
		DrillthroughResultDTO newResultDTO=new DrillthroughResultDTO(headers, newRows, ordinal);
		logger.info("New row count:" + newResultDTO.getEstimatedTotalRowCount());
		return newResultDTO;
	} 


	private Set<Integer> createSkipColHeader() {
		Map<Headers,Integer> headerIdx=transposeRequest.getColumnDetails().getHeaderIdx();
		Set<Integer> excludeColsIndex = new HashSet<Integer>();
		excludeColsIndex.add(headerIdx.get(Headers.DEALNUMBER));
		excludeColsIndex.add(headerIdx.get(Headers.MEASURE));
		excludeColsIndex.add(headerIdx.get(Headers.TRANSPOSE));
		//excludeColsIndex.add(headerIdx.get(Headers.SCENARIOTERMUNDERLYING));
		
		return excludeColsIndex;
	}


	private void applyTenorFilter(Set<String> tenors) {   
		//If there is term filter then apply it
		if(StringUtils.hasText(transposeRequest.getFilterDetails().getTermFilter()))
		{
			logger.info(String.format("Orignal Tenor %s", tenors));
			tenors.clear();
			tenors.add(transposeRequest.getFilterDetails().getTermFilter());
			logger.info(String.format("After Tenor Filter %s", tenors));
		}
	}


	private Map<SpreadType, List<DrillthroughRowDTO>> groupBySpreadType(DrillthroughResultDTO result,Set<String> tenors) 
	{
		Map<Headers,Integer> headerIdx=transposeRequest.getColumnDetails().getHeaderIdx();
		Map<SpreadType, List<DrillthroughRowDTO>> spreadTypeRows = new HashMap<SpreadType,List<DrillthroughRowDTO>>();
		boolean errorLogged=false;
		for(DrillthroughRowDTO row : result.getRows())
		{
								
			tenors.add(row.getContent()[headerIdx.get(Headers.TRANSPOSE)].toString());
			String spreadType=row.getContent()[headerIdx.get(Headers.SPREADTYPE)].toString();
			SpreadType spreadTypeValue = SpreadType.NA;
			
			try
			{
				//spreadTypeValue =  SpreadType.NA;
				spreadTypeValue= isNASpreadType(spreadType) ? SpreadType.NA :SpreadType.valueOf(spreadType);
				
			}
			catch(IllegalArgumentException e)
			{
				if(!errorLogged)
				{
					logger.log(Level.SEVERE, String.format("Invalid value for spread type:%s", spreadType),e);
					errorLogged=true;
				}
			}		
			
			List<DrillthroughRowDTO> spreadRows = spreadTypeRows.get(spreadTypeValue);
		
			if(spreadRows==null)
			{
				spreadRows = new ArrayList<DrillthroughRowDTO>();
				spreadTypeRows.put(spreadTypeValue, spreadRows);
			}
			spreadRows.add(row);
			
		}
		return spreadTypeRows;
	}


	private boolean isNASpreadType(String spreadType) 
	{
		return !StringUtils.hasText(spreadType) || "N/A".equalsIgnoreCase(spreadType) || "Bond".equalsIgnoreCase(spreadType) || "SWAP".equalsIgnoreCase(spreadType) || "OIS".equalsIgnoreCase(spreadType) ;		
	
	}	
	
	/**
	 * THis helper method creates new Data Row from rawData for a given key
	 * @param tenors= all tenors encountered in dataset in sort order
	 * @param headerIdx= index of Term and M_Reult 
	 * @param entry= tenor/pnl values.
	 * @param rawData= Old Data format
	 * @param newData= To be filled by this method new data format with transpose 
	 */
	private List<Object> populateNewRow(Set<String> tenors, Set<Integer> skipColsIndex,DrillThroughRowContent rowContent, Object[] rawData) 
	{
		Map<Headers,Integer> headerIdx=transposeRequest.getColumnDetails().getHeaderIdx();
		List<Object> newData=new ArrayList<Object>();
			//add Deal number first
			newData.add(rawData[headerIdx.get(Headers.DEALNUMBER)]);
			Double sum=0.0d;
		
			for(Object tenor:tenors){
					Object pnl=rowContent.getTransposeColValue(tenor);
					if (pnl==null){
						pnl="";
					}else{
						sum +=(Double)pnl;
					}
						newData.add(pnl);
			}
			// sum is second column always
			newData.add(1,sum);			
			
			//now loop through old data[] and create new data list skip index already populated		
			skipColsIndex.add(5);  // remove hypothetical pnl in reference from headers.remove(Hypothetical);
		for (int i=0;i<rawData.length;i++)
		{
			if (!skipColsIndex.contains(i))
			{
				newData.add(rawData[i]);; 
			}
			
		 }
		return newData;
	}	
}