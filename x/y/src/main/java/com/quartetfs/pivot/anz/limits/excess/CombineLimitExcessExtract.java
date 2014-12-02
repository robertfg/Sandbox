package com.quartetfs.pivot.anz.limits.excess;

import gnu.trove.map.TIntObjectMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StopWatch;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.context.IContextSnapshot;
import com.quartetfs.biz.pivot.context.subcube.ISubCubeProperties;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.LocationSet;
import com.quartetfs.biz.pivot.webservices.impl.ServicesUtil;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.query.QueryException;
import com.quartetfs.pivot.anz.limits.bo.LimitDetail;
import com.quartetfs.pivot.anz.limits.service.LimitUtil;

public class CombineLimitExcessExtract implements LimitExcessExtract {
	
	private static final Logger LOGGER = Logger.getLogger(NormalLimitExcessExtract.class.getSimpleName());
	private LimitUtil util;
	private Map<String,Double> limitValuesMap =  new HashMap<String,Double>(); 
	
	public CombineLimitExcessExtract(LimitUtil util) {
		super();
		this.util = util;		
	}

	@Override
	public Map<String,Double> extract(IDate limitDate) throws QueryException {	
		return extract(limitDate,null);
	

	}

	private Set<ILocation> rebuildLocation(LimitDetail limit,Set<ILocation> locations) 
	{
		Object[][] locArray = locations.iterator().next().arrayCopy();
		for(String dimension : limit.getCombileLimitLevels())
		{
			int index = util.getHelper().retrieveDimensionOrdinal(dimension.split("@")[1]);
			locArray[index-1] = new Object[]{null};
		}
		locations.clear();
		locations.add(new Location(locArray));
		return locations;
	}
	
	private void match(final String measure,final LimitDetail limit, Set<ILocation> locations,ISubCubeProperties subCube) throws QueryException 
	{
		IContextSnapshot oldContextValues = ServicesUtil.applyContextValues(util.getHelper().getPivot(), Arrays.asList(subCube), true);
		final ICellSet cellSet =util.getHelper().getCellSet(locations, Collections.singletonList(measure));		
		cellSet.forEachLocation( new ILocationProcedure() {					
			@Override
			public boolean execute(ILocation location, int rowId) {	
				limitValuesMap.put(limit.getId(),(Double)cellSet.getCellValue(rowId, measure));	
				return true;
			}
		});
		ServicesUtil.replaceContextValues(util.getHelper().getPivot(), oldContextValues);
	}

	@Override
	public Map<String, Double> extract(IDate limitDate,
			List<LimitDetail> limitDetails) throws QueryException {
		LOGGER.info("combine limit excess extract started");
		StopWatch stopWatch = new StopWatch("Combine excess extract");
		
			final List<LimitDetail> limits = limitDetails==null?util.findLimits(limitDate, true):limitDetails;
	
			//	if(CollectionUtils.isEmpty(limits)) continue;		
			stopWatch.start("location build for " + limitDate);
			for(LimitDetail limit : limits)
			{
				Set<ILocation> locations = new LocationSet();				
				for(TIntObjectMap<TIntObjectMap<Object>> locValues : limit.getLocationValues())
				{
					locations.add(util.computeLocation(locValues));
				}				
				ISubCubeProperties subCube = util.getHelper().createSubcube(locations);		
				locations=rebuildLocation(limit, locations);			
				//match(limit.getMeasureName(),limit,locations);	
			}
			stopWatch.stop();
	
		LOGGER.info(stopWatch.toString());
		LOGGER.info("combine limit excess extract done");	
		return limitValuesMap;
	}
}
