package com.quartetfs.pivot.anz.webservices.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebService;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.MDC;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.impl.DateParser;
import com.quartetfs.fwk.query.QueryException;
import com.quartetfs.pivot.anz.limits.bo.LimitConfigInfo;
import com.quartetfs.pivot.anz.limits.bo.LimitDetail;
import com.quartetfs.pivot.anz.limits.bo.LimitMasterData;
import com.quartetfs.pivot.anz.limits.bo.LimitMasterData.LimitDataHolder;
import com.quartetfs.pivot.anz.limits.excess.LimitBatchExtract;
import com.quartetfs.pivot.anz.limits.excess.LimitExcessService;
import com.quartetfs.pivot.anz.limits.extract.LimitConsolidation;
import com.quartetfs.pivot.anz.limits.service.LimitUtil;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.QueryHelper;
import com.quartetfs.pivot.anz.webservices.ILimitService;
import com.quartetfs.pivot.anz.webservices.dto.ExtractExcessParam;
import com.quartetfs.pivot.anz.webservices.dto.ExtractLimitDTO;
import com.quartetfs.pivot.anz.webservices.dto.ExtractedLimitDTO;
import com.quartetfs.pivot.anz.webservices.dto.LimitDTO;
import com.quartetfs.pivot.anz.webservices.dto.LimitMasterDataDTO;

@WebService(name = "ILimitService", targetNamespace = "http://webservices.quartetfs.com/activepivot", 
endpointInterface = "com.quartetfs.pivot.anz.webservices.ILimitService", serviceName = "LimitService")
public class LimitService implements ILimitService {

	private static final Logger LOGGER = Logger.getLogger(LimitService.class.getSimpleName());
	private FastDateFormat dateFormat;
	private DateParser parser;	
	private LimitMasterData limitMasterData;
	private LimitConfigInfo configInfo; 
	private QueryHelper queryHelper;
	private LimitExcessService limitExcessService;
	private ExecutorService batchLimitExecutor; 
	private int matcherPartition = 5;
	private int matcherThread = 2;
	private LimitUtil limitUtil;
	
	
	public LimitService(LimitMasterData limitMasterData,QueryHelper helper,LimitConfigInfo configInfo,
			LimitExcessService limitExcessService, LimitUtil limitUtil)
	{
		this.dateFormat = FastDateFormat.getInstance("dd-MM-yyyy");
		this.parser =(DateParser) Registry.getPlugin(IParser.class).valueOf("date[dd-MM-yyyy]");
		this.limitMasterData = limitMasterData;
		this.configInfo = configInfo;		
		this.queryHelper=helper;
		this.limitExcessService=limitExcessService;
		
		matcherThread=matcherThread==-1?Runtime.getRuntime().availableProcessors():matcherThread;
		batchLimitExecutor = Executors.newFixedThreadPool(matcherThread, new ThreadFactory() {
			AtomicInteger threadCtr = new AtomicInteger();
			@Override
			public Thread newThread(Runnable r) {
				 Thread t = new Thread(r, "LimitBatchExtract" + ":" + threadCtr.incrementAndGet());
				 return t;
			}
		});
		
		this.limitUtil = limitUtil;
	}
	
	@Override
	public ExtractLimitDTO extractExcess(ExtractExcessParam param)
	{
		long startTime = System.currentTimeMillis();
		MDC.put(ANZConstants.CONTEXT, param.getExtractDate());
		LOGGER.info("Extracting excess using limit date:" + param.getLimitDate());
		LOGGER.info("Extracting excess for exposure date:" + param.getExtractDate());
		
		
		IDate limitDate = Registry.create(IDate.class, parser.parse(param.getLimitDate()).getTime());
		LimitDataHolder holder = limitMasterData.getLimits().get(limitDate);
		
		List<LimitDetail> limitDetails = new ArrayList<LimitDetail>();
		
		ExtractLimitDTO extractDTO = null;
		
		ExecutorCompletionService<Map<String,Map<String,Double>>> completionService = 
				new ExecutorCompletionService<Map<String,Map<String,Double>>>(batchLimitExecutor);
		if(holder==null) 
		{
			LOGGER.info(String.format("No limit master data found for %s", param.getExtractDate()));
			return null;
		}
		
		/* Get Limit Details from Holder */
		if(param.getContainerName()!=null){
			for(LimitDetail limit : holder.getLimitDetails())
			{	
				if(limit.getLocationValues().get(0).get(0).containsValue( param.getContainerName())){
					limitDetails.add(limit);		
				}
			}
		} else {
			limitDetails = holder.getLimitDetails();
		}
		
		/*Default the limits return value to NAN*/
		ConcurrentMap<String,Double> limitValuesMap =  new ConcurrentHashMap<String, Double>(); 
		for(LimitDetail limit : limitDetails)	{
			String limitId = limit.getId().split("_")[0].trim();
			limitValuesMap.put(limitId, Double.NaN);
		}
		
		try {
			// partition List<limits>
			
			LOGGER.info("Exposure extract start." );
			int partitionCount = 0;
			for (int start = 0; start < limitDetails.size(); start += matcherPartition) {
		        int end = Math.min(start + matcherPartition,limitDetails.size());
		        List<LimitDetail> limitDetail = limitDetails.subList(start, end);
		        completionService.submit(new LimitBatchExtract( limitDetail, limitUtil, param ));
		        partitionCount++; 
		    }
			
			Map<String,Map<String,Double>> apLimitExposureExtract = new HashMap<String,Map<String,Double>>();
			/*
			 * Extract exposure
			 */
			for(int cnt=0;cnt<partitionCount;cnt++) {
				Map<String,Map<String,Double>> limit;
				try {
					limit = completionService.take().get();
					if(limit!=null){
						apLimitExposureExtract.putAll(  limit );
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			LOGGER.info("Exposure extract end." + (System.currentTimeMillis() - startTime) + ":ms" );
			LimitConsolidation limitConsolidation = new LimitConsolidation(limitValuesMap, limitUtil);
			startTime = System.currentTimeMillis();
			LOGGER.info("Exposure consolidation start, ApLimitExposure Size:" + apLimitExposureExtract.size());
			limitValuesMap = limitConsolidation.consolidate(limitDetails, apLimitExposureExtract);
			LOGGER.info("Exposure consolidation end." + (System.currentTimeMillis() - startTime) + ":ms" );
			
			if(limitValuesMap.isEmpty()) {
				LOGGER.info(String.format("No data found for extract for %s",param.getExtractDate()));
				return null;
			}
			
			extractDTO = this.getLimitDTO( new ArrayList<String>(  Arrays.asList("limitid", "measurevalue")) , limitValuesMap);
			if( extractDTO!=null &&  extractDTO.getValues() !=null && extractDTO.getValues().size()>0 ){
				LOGGER.info("Exposure Extract Count:" + extractDTO.getValues().size());
			}else{ LOGGER.info("Exposure Extract Count extractDTO is NULL" ); }
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, "Problem in extracting excess", e);
		}
		return extractDTO;
	}
    
	@SuppressWarnings("unused")
	private File writeExtractResultToFile(ExtractExcessParam param,Map<Integer, Double> limitExtractValues) throws FileNotFoundException 
	{
		File filePath = new File(configInfo.getExtractDir(),param.getExtractDate()+"_" + System.currentTimeMillis() + "_limitexcess.csv");
		LOGGER.info("Creating file " + filePath.getAbsolutePath());
		PrintWriter writer = new  PrintWriter(filePath);
		writer.println("limitid,measurevalue");
		for(Map.Entry<Integer,Double> entry : limitExtractValues.entrySet())
		{
			writer.println(entry.getKey() + "," + entry.getValue());
		}			
		writer.close();
		LOGGER.info(String.format("%s Limit excess records for %s date written in file %s. ",limitExtractValues.size(),param.getExtractDate(),filePath.getAbsolutePath()));
		return filePath;
	}	

	
	@Override
	public ExtractLimitDTO extractLimit(LimitMasterDataDTO limitMaster) throws QueryException, IOException {
		
		Map<String,Set<String>> expandedLoc = new HashMap<String,Set<String>>();
		
		ExtractLimitDTO extractDTO = new ExtractLimitDTO();
		//extractDTO.setHeader( new ArrayList<String>(Arrays.asList("Date","Limit_ID","Attributes","Measure_Name","Value")));
		
		
		List<ExtractedLimitDTO> values = new  ArrayList<ExtractedLimitDTO>();
		 
		for(LimitDTO limit : limitMaster.getLimits())
		{
			extractLocations(limit,expandedLoc, limitMaster.getMeasureName());
		}
		
		for(Map.Entry<String, Set<String>> entry : expandedLoc.entrySet())
		{
			
			//File tmpFile = File.createTempFile(entry.getKey().replaceAll(Pattern.quote("-"), "") + "_" , "_limit.csv");
			//PrintWriter writer = new PrintWriter( tmpFile);
			
			int count=0;
			//writer.println("Date,Limit_ID,Attributes,Measure_Name,Value");
			Random r = new Random();
			for(String loc : entry.getValue())
			{
		//		writer.println(entry.getKey() + "," + count + "," + loc + "," + limitMaster.getMeasureName() +  "," + r.nextInt(10000));
	         /*  values.add( new String[]{String.valueOf( entry.getKey()) , String.valueOf(count), loc, limitMaster.getMeasureName(), String.valueOf(r.nextInt(10000)) } );
			 				count++;
			 */
 					count++;
	 				
			 				
			}			
		//	LOGGER.info(String.format("File created %s , no of limits %s", tmpFile.getAbsolutePath(),count));
	//		writer.close();
			
		}
		
		//extractDTO.setValues(values);
		return extractDTO;
	}
	
	private void extractLocations(LimitDTO limit,final Map<String,Set<String>> expandedLoc, String measureName) throws QueryException,IOException, FileNotFoundException 
	{
		Map<String,Object> queryParameters = new LinkedHashMap<String, Object>();
		final Set<String> wildCardLevels= new HashSet<String>();
		for(String entry : limit.getParams())
		{
			String[] values = entry.split("=");
			String key = values[0];
			Object value=values[1];
			
			boolean wildCard=false;
			
			if(value.equals(ILocation.WILDCARD))
			{
				String[] keyParts = key.split(ANZConstants.LEVEL_DIM_SEPARATOR);
				wildCardLevels.add(keyParts.length > 1 ? keyParts[1] : keyParts[0]);
				wildCard=true;
			}
			
			if(key.toLowerCase().indexOf("date") > -1)
			{
				if(!wildCard)
				{
					value = parser.parse(value.toString());
				}
			}			
			queryParameters.put(key, value);			
		}
		
		
		Collection<String> measure = new ArrayList<String>();
		measure.add(measureName);
		
		
		final int dateIndex = queryHelper.retrieveDimensionOrdinal("COB Date");		
		
		final ICellSet cellSet = queryHelper.getAggregatesQuery(queryParameters,measure);
		cellSet.forEachLocation(new ILocationProcedure() {			
			@Override
			public boolean execute(ILocation location, int rowId) {				
				final Object[][] loc = location.arrayCopy();
				Object[] date = loc[dateIndex-1];
				String dateText = dateFormat.format(((IDate)date[0]).javaDate());
				Set<String> locs = expandedLoc.get(dateText);
				if(locs==null)
				{
					locs = new HashSet<String>();
					expandedLoc.put(dateText, locs);
				}
				
				StringBuilder sb = new StringBuilder();
				for(String dimName : wildCardLevels)
				{
					int index = queryHelper.retrieveDimensionOrdinal(dimName);
					String locValue =  arrayTOString(loc[index-1]);					
					sb.append(dimName).append(configInfo.getLocationSeparator()).append(locValue).append(configInfo.getDimensionSeparator());
				}
				sb.deleteCharAt(sb.length()-1);
				locs.add(sb.toString());
				return true;
			}
		});		
		
		if(cellSet.isEmpty())
		{
			LOGGER.info("No locations found. Check parameters");
		}
		
	}	
	
	private String arrayTOString(Object[] loc)
	{
		StringBuilder sb = new StringBuilder();
		for(Object s : loc)
		{
			if(s instanceof IDate)
			{
				IDate d = (IDate) s;
				s = dateFormat.format(d.javaDate());
			}
			sb.append(s).append("\\");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	private ExtractLimitDTO getLimitDTO(List<String> header, Map<String, Double> limitExtractValues){
		ExtractLimitDTO extractDTO = new ExtractLimitDTO();
	//	extractDTO.setHeader(header);
		List<ExtractedLimitDTO> values = new  ArrayList<ExtractedLimitDTO>();
		for(Map.Entry<String,Double> entry : limitExtractValues.entrySet())
		{
			values.add( new ExtractedLimitDTO( entry.getKey(), entry.getValue() )  );
			 //String.valueOf( entry.getKey()) , String.valueOf( entry.getValue() )}
			
			
		}
	    extractDTO.setValues(values);
	    
	    return extractDTO;
		
	}

	public int getMatcherPartition() {
		return matcherPartition;
	}
	public void setMatcherPartition(int matcherPartition) {
		this.matcherPartition = matcherPartition;
	}
	public int getMatcherThread() {
		return matcherThread;
	}
	public void setMatcherThread(int matcherThread) {
		this.matcherThread = matcherThread;
	}
	public static void main(String[] args){
		List<Integer>limit = new ArrayList<Integer>();
		for (int i = 0; i < 100; i++) {
			limit.add(i);
		}
		int partitionCount = 0;
		for (int start = 0; start < limit.size(); start += 5) {
	        int end = Math.min(start + 5, limit.size());
	        List<Integer> subLimit = limit.subList(start, end);
	        System.out.println( subLimit.toString() );
	        partitionCount++;
	    }
		System.out.println( partitionCount );
	}
}
