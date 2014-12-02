package com.quartetfs.pivot.anz.limits.extract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.fwk.query.QueryException;
import com.quartetfs.pivot.anz.limits.bo.LimitDetail;
import com.quartetfs.pivot.anz.limits.service.LimitUtil;
import com.quartetfs.pivot.anz.webservices.dto.ExtractExcessParam;

public class LimitExposureExtract {

	private LimitUtil util;
	private ExtractExcessParam excessParam;
	public LimitExposureExtract(){}
	public LimitExposureExtract(LimitUtil util,ExtractExcessParam excessParam)	{
		super();		
		this.util=util;
		this.excessParam = excessParam;
	}
	
	
	
	public Map<String,Map<String,Double>>extract( List<LimitDetail> limitDetails)
			throws QueryException {
	
		 
		final List<LimitDetail> limits = limitDetails;
	   
		if(!CollectionUtils.isEmpty(limits))				{
			Map<String,LimitDetail> locations =  new LinkedHashMap<String,LimitDetail>();	
			for(LimitDetail limit : limits)
			{
				locations.put(limit.getId()+"@@"+util.computeLocation(limit.getLocationValues().iterator().next()),limit);					
			}
			return match(locations);	
		}
		return null;
		
	}
	private Map<String,Map<String,Double>> match( final Map<String,LimitDetail> locations) throws QueryException 
	{
		HashSet<ILocation> locToSearch = new HashSet<ILocation>();
	    final Map<String,Map<String,Double>> result = new HashMap<String, Map<String,Double>>();
	    
		for(final Map.Entry<String,LimitDetail> entry : locations.entrySet())
		{
			final List<String> measures = Arrays.asList(entry.getValue().getMeasureName());		
			locToSearch.clear();
			locToSearch.add(updateLocation(entry.getKey().split("@@")[1].trim()));
			
			final ICellSet cellSet = util.getHelper().getCellSet(locToSearch, measures);
            
			cellSet.forEachLocation( new ILocationProcedure() {					
				
				
				@Override
				public boolean execute(ILocation location, int rowId) {
				//	System.out.println("actual location:" + location.toString());
					
			    	Map<String,Double> cellResult = new HashMap<String, Double>();
					for (String measure : measures) {
						cellResult.put(measure, (Double) cellSet.getCellValue(rowId,measure));
				    }
					result.put(/*util.generateKeyFromLocation(location, util.getDefaultMember())*/location.toString(), cellResult);
					
				//	System.out.println("generated key:" + util.generateKeyFromLocation(location, util.getDefaultMember()));
					
					return true;
				}
			});
		}
		
		return result;
	}	
	
	
	public ILocation updateLocation( String location){
		
		ILocation newLoc = util.rebuildLocation( new Location(location), excessParam.getExtractDate() );
		List<Map<String,Object>> dimsUpdate = new ArrayList<Map<String,Object>>();
		Map<String,Object> dims = new HashMap<String, Object>();
		
		
		if(excessParam.getSnapShot()==null){
		   excessParam.setSnapShot("EOD");
		}
		dims.put("Data Snapshot",excessParam.getSnapShot());
		dimsUpdate.add(dims);
		return util.updateLocationValue(newLoc,dimsUpdate );
		
	}
	

}
