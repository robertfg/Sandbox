package com.quartetfs.pivot.anz.postprocessing.impl;

import java.util.Collection;
import java.util.HashSet;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.pivot.anz.service.IDateService;

public class DateLocationHelper {
	private IDateService dateService;
	private AnalysisDimensionHelper analysisDimensionHelper;
	private IActivePivot pivot;
	

	public DateLocationHelper(IDateService service,AnalysisDimensionHelper helper,IActivePivot pivot) {
		this.dateService=service;
		this.analysisDimensionHelper=helper;
		this.pivot=pivot;
	}
	
	public Collection<ILocation> generatePreviousDateLocations(Collection<ILocation> locations, int[] others, int timeDimensionIdx) {
		Collection<ILocation> computedLocations = new HashSet<ILocation>();
		for (ILocation loc : locations) {
			ILocation location = analysisDimensionHelper.overrideOtherDiscriminatorLocation(loc,others);
			computedLocations.add(location);
			// for each location calculate the previous date location and add it in to set
			final Object[][] locationArray = location.arrayCopy();
			Object previousDate=getPreviousDate(locationArray,timeDimensionIdx);
			locationArray[timeDimensionIdx]=new Object[]{previousDate};
			ILocation newLocation=new Location(locationArray);
			computedLocations.add(newLocation);
		}
		return computedLocations;
	}
	
	
	private Object getPreviousDate(Object[][] locationArray, int timeDimensionIdx) {
		Object[]dateArray=locationArray[timeDimensionIdx];
		IDate currDate=(IDate)dateArray[0];
		return currDate==null?currDate:dateService.getPreviousDay( currDate,pivot, timeDimensionIdx); 
   }

	
}
