package com.quartetfs.pivot.anz.postprocessing.impl;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Required;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.ILocationPattern;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.APostProcessor;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesRetriever;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.pivot.anz.service.impl.DateService;

/**
 * Difference of PnL : current COB - previous COB
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = ChangeInMeasure.PLUGIN_KEY)
public class ChangeInMeasure extends APostProcessor<Object> implements IPrefetcher{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -2533059314746154714L;
	private AnalysisDimensionHelper analysisDimensionHelper=new AnalysisDimensionHelper();
	public static final String PLUGIN_KEY="CHANGE_RESULT";
	private DateService dateService;
	private int[]timeDimensionIdx;
	private DateLocationHelper dateLocationHelper;

	public ChangeInMeasure(String name, IActivePivot pivot) {
		super(name, pivot);
	}


	@Override
	public void init(Properties properties) throws QuartetException {
		super.init(properties);
		analysisDimensionHelper.init(properties, pivot);
		getPrefetchers().addAll( Collections.<IPrefetcher> singletonList(this) );
		timeDimensionIdx=analysisDimensionHelper.getFirstDiscriminatorIndices().get(0);
		dateLocationHelper=new DateLocationHelper(dateService, analysisDimensionHelper, pivot);
	}


	private ILocation getLocationForPrevDate(IDate currentDate, Object [][]locationArray){
		final Object prevDate = dateService.getPreviousDay(currentDate, pivot,timeDimensionIdx[0]);
		locationArray[timeDimensionIdx[0]] = new Object[] { prevDate };
		return new Location(locationArray);
	}
	private IDate getCurrentDate(Object[][] locationArray){
		Object date[] = locationArray[timeDimensionIdx[0]];
		return (IDate) date[0];
	}

	@Override
	public void evaluate(ILocation location,final IAggregatesRetriever retriever) throws QuartetException {
		final ILocationPattern pattern=location.createWildCardPattern();
		ILocation locationToQuery=analysisDimensionHelper.overrideOtherDiscriminatorLocation(location,null);
		final ICellSet cellSet = retriever.retrieveAggregates(Collections.singletonList(locationToQuery),Collections.singletonList(underlyingMeasures[0]));
		Object[][] locationArray = locationToQuery.arrayCopy();
		IDate currentDate =getCurrentDate(locationArray);
		// if currentdate is null mens we have wild card so all dates would be covered in original cell set else, we get the prev date and fetch cell set
		final ICellSet prevDateCellSet=currentDate==null?cellSet:retriever.retrieveAggregates(Collections.singletonList(getLocationForPrevDate(currentDate,locationArray)),Collections.singletonList(underlyingMeasures[0]));
		
		cellSet.forEachLocation(new ILocationProcedure() {
			@Override
			public boolean execute(ILocation location, int rowId) {
				Object[][] locationArray = location.arrayCopy();
				IDate currentDate = getCurrentDate(locationArray);

				//get Previous Date from date service and create a new location with that
				Object previousDate=null;
				previousDate=dateService.getPreviousDay(currentDate, pivot,timeDimensionIdx[0]);
				
				//SubCubeProperties subProps = (SubCubeProperties)query.getContextValues()
				if (previousDate==null){
					return true;
				}
				Object tuple[]=pattern.extractValues(location);
				ILocation locationToWrite=pattern.generate(tuple);
				locationArray[timeDimensionIdx[0]] = new Object[] { previousDate };
				ILocation newLocation = new Location(locationArray);
				// get current value 
								
				Double currentValue = (Double) cellSet.getCellValue(rowId,underlyingMeasures[0]);
				// get original location from pattern and replace date with current location
					// if previous date is not null it means we have some previous value we need to get that from retriever, the location we are asking from retriever should already be provided in prefecthing
						Object prevValue=prevDateCellSet.getCellValue(newLocation,underlyingMeasures[0]);
						// if prev value is valid calculate change else do nothing
						if (prevValue!=null && prevValue instanceof Double && currentValue!=null){
							retriever.write(locationToWrite, ((Double)currentValue) - (((Double)prevValue)) );
						
						}
				return true;
			}
		});
	}

	@Override
	public String getType() {
		return PLUGIN_KEY;
	}

	@Required
	public void setDateService(DateService dateService) {
		this.dateService = dateService;
	}

	@Override
	public Collection<ILocation> computeLocations(Collection<ILocation> locations) {
		return dateLocationHelper.generatePreviousDateLocations(locations, null, timeDimensionIdx[0]);
	}

	@Override
	public Collection<String> computeMeasures(Collection<ILocation> locations) {
		return Arrays.asList(underlyingMeasures[0]);
	}


}
