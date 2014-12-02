package com.quartetfs.pivot.anz.postprocessing.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.Util;
import com.quartetfs.pivot.anz.utils.ANZConstants;

public class AnalysisDimensionHelper {
	
	//used to store the dim and level indexes we focus on
	private List<int[]> firstDiscriminatorIndices=new ArrayList<int[]>();
	//other dimension that have an impact (Scenario dim in our case)
	private List<int[]> otherDiscriminatorIndices=new ArrayList<int[]>();
	
	 
	public void init(Properties properties, IActivePivot pivot){
		populateDiscriminators(firstDiscriminatorIndices,ANZConstants.FIRST_DISCRIMINATOR_LEVEL,properties,pivot);
		populateDiscriminators(otherDiscriminatorIndices,ANZConstants.OTHER_DISCRIMINATOR_LEVEL,properties,pivot);
	}
	
	
	private void populateDiscriminators(List<int[]> target,String propertyName,Properties properties, IActivePivot pivot){
		String proprty=properties.getProperty(propertyName);
		if (proprty!=null){
			final String[] otherDiscriminatorLevels = properties.getProperty(propertyName).split(ANZConstants.OTHER_DISCRIMINATORS_SEPARATOR);
			for (String other:otherDiscriminatorLevels){
				final String[] otherLevel=other.split(ANZConstants.LEVEL_DIM_SEPARATOR);
				int[] discrimnatorLevel=Util.findLevelOrdinals(pivot.getDimensions(), otherLevel[1].trim(),otherLevel[0].trim());
				discrimnatorLevel[0]--;//we decrement it as measures are in the dimension 0 of the cube
				target.add(discrimnatorLevel);
			}
		}	
	}
	
	
	public int[] getDimensionOrdinal(String dimensionpath,IActivePivot pivot){
		final String[] levels = dimensionpath.split(ANZConstants.LEVEL_DIM_SEPARATOR);
		int[] discrimnatorLevel=Util.findLevelOrdinals(pivot.getDimensions(), levels[1].trim(),levels[0].trim());
		discrimnatorLevel[0]--;
		return discrimnatorLevel;
	}
	
	public ILocation overrideLocation(ILocation location){
		Object[][] overriddenLocationArray = location.arrayCopy();
		if (getFirstDiscriminatorIndices()!=null){
			for (int[] firstDiscriminator:firstDiscriminatorIndices){
				overriddenLocationArray[firstDiscriminator[0]][firstDiscriminator[1]] = ANZConstants.DEFAULT_DISCRIMINATOR;
			}
		}
		
		overrideOtherDiscrimnatorLocation(overriddenLocationArray,null);
		return new Location(overriddenLocationArray);
	}
	
	
	
	public ILocation overrideOtherDiscriminatorLocation(ILocation location, int [] others){
		Object[][] overriddenLocationArray = location.arrayCopy();
		overrideOtherDiscrimnatorLocation(overriddenLocationArray,others);
		return new Location(overriddenLocationArray);
		
	}
	
	private void overrideOtherDiscrimnatorLocation(Object[][] location, int[]others) {
		if (otherDiscriminatorIndices != null) {
			for (int otherDiscriminator[] : otherDiscriminatorIndices) {
				
				if( hasValue( new Location( location ), otherDiscriminator[0]) ) {
				 location[otherDiscriminator[0]][otherDiscriminator[1]] = ANZConstants.DEFAULT_DISCRIMINATOR;
				}
			}
		}
		if (others!=null){
				location[others[0]][others[1]] = ANZConstants.DEFAULT_DISCRIMINATOR;
		
		}
	}
	
	public List<int[]> getFirstDiscriminatorIndices() {
		return firstDiscriminatorIndices;
	}
	
	private boolean hasValue(ILocation location,int dimOrdinal) 
	{
		return location.getLevelDepth(dimOrdinal) >=2;
	}


	public List<int[]> getOtherDiscriminatorIndices() {
		return otherDiscriminatorIndices;
	}
	
	
}
