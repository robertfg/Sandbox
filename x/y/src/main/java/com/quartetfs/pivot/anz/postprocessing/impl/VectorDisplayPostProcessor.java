package com.quartetfs.pivot.anz.postprocessing.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.ILocationPattern;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cube.hierarchy.axis.IAxisMember;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.postprocessing.IPrefetcher;
import com.quartetfs.biz.pivot.postprocessing.impl.APostProcessor;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesRetriever;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.pivot.anz.service.impl.VectorLabelService;
import com.quartetfs.pivot.anz.utils.ANZConstants;
/**
 * This is Generic post processor to display content of any vector, following dependencies needs to be provided
 * 1. Analysis Dimension name which pwill provide Labels of vector
 * 2. Container name 
 * 3. Entry in Vector Label Service corresponding to the container  
 *
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.postprocessing.IPostProcessor", key = VectorDisplayPostProcessor.PLUGIN_KEY)
public class VectorDisplayPostProcessor extends APostProcessor<Object> implements IPrefetcher {
	private static final long serialVersionUID = 8229509990542025333L;
	public final static String PLUGIN_KEY = "VECTOR_DISPLAY_PP";
	private VectorLabelService vectorLabelService;
	private String containerName;

	private int dimensionOrdinal[];
	private int timeDimensionIdx[];
	private AnalysisDimensionHelper analysisDimensionHelper=new AnalysisDimensionHelper();

	public VectorDisplayPostProcessor(final String name, final IActivePivot pivot) {
		super(name, pivot);
	}



	@Override
	public void init(final Properties properties) throws QuartetException {
		super.init(properties);
		analysisDimensionHelper.init(properties, pivot);
		List<int[]>dimensions=analysisDimensionHelper.getFirstDiscriminatorIndices();
		dimensionOrdinal= dimensions.get(0);
		String dateDimensionStr=(String)properties.get(ANZConstants.LABEL_DIMENSION);
		timeDimensionIdx= analysisDimensionHelper.getDimensionOrdinal(dateDimensionStr,pivot);
		containerName=(String)properties.get(ANZConstants.LABEL_CONTAINER);
		getPrefetchers().addAll( Collections.<IPrefetcher> singletonList(this) );
	}

	@Override
	public void evaluate(final ILocation locationReceived, final IAggregatesRetriever retriever) throws QuartetException {
		final Object firstdiscriminator = locationReceived.getCoordinate(dimensionOrdinal[0], dimensionOrdinal[1]);
		if( firstdiscriminator!=null && firstdiscriminator.equals(ANZConstants.UNAVAILABLE)){
			return;		
		}

		final ILocationPattern pattern=locationReceived.createWildCardPattern();
		final int index = pattern.getPatternIndex(dimensionOrdinal[0], dimensionOrdinal[1]);
		final ILocation locationToQuery=analysisDimensionHelper.overrideLocation(locationReceived);
		// lets get the current date
		final IDate currentDate=getCurrentDate(locationReceived.arrayCopy());

		final Set<String> selectedValues = new HashSet<String>();

		@SuppressWarnings("unchecked")
		final List<IAxisMember> members = (List<IAxisMember>) pivot.getDimensions().get(dimensionOrdinal[0] + 1).retrieveMembers(LocationUtil.copyPath(locationReceived, dimensionOrdinal[0]));
		for(IAxisMember m : members) {
			if(m.getDiscriminator() instanceof String && !m.getDiscriminator().equals(ANZConstants.UNAVAILABLE) )
				selectedValues.add((String) m.getDiscriminator());
		}

		final String singleValue;
		if(index < 0) {//we don't have wildcard on analysis dimension, we received here a value as a member
			if(selectedValues.size() != 1){return;/* throw new IllegalStateException();*/}//should be one value
			singleValue = selectedValues.iterator().next();//get the value we're looking at
		}else{
			singleValue=null;
		}
		// this should be the labels for vector
		final List<String> labels=vectorLabelService.get(currentDate, containerName);
		if (labels==null){
			return;
		}
		final ICellSet cellSet = retriever.retrieveAggregates(Collections.singletonList(locationToQuery), Collections.singletonList(underlyingMeasures[0]));

		cellSet.forEachLocation(new ILocationProcedure() {
			@Override
			public boolean execute(final ILocation location, final int rowId) {
				double[] vector=(double[]) cellSet.getCellValue(location, underlyingMeasures[0]);
				IDate cobDate 	  = (IDate)location.getCoordinate(timeDimensionIdx[0],0);	
				if (vector==null){
					return true;
				}
				if (index>=0){// range location or more than one label selected
					final Object[] tuple =  pattern.extractValues(location);
					List<String>labels=vectorLabelService.get(cobDate, containerName);
					for(String value : selectedValues) {
						final Object[] tupleCloned = tuple.clone();
						tupleCloned[index] = value;
						Object result=getObjectToWrite(vector,value,labels);
						if (result!=null){
							retriever.write(pattern.generate(tupleCloned), result);
						}	
					}

				}else{// point location
					final Object[][] locationArray = location.arrayCopy();
					locationArray[dimensionOrdinal[0]][dimensionOrdinal[1]] = singleValue;
					Object value=getObjectToWrite(vector,singleValue,labels);
					if (value!=null){
						retriever.write(new Location(locationArray),value);
					}	
				}
				return true;
			}
		});
	}

	private Object getObjectToWrite(double[] vector, String dimensionValue,List<String> labels){
		int idx=labels.indexOf(dimensionValue);
		return idx>-1?vector[idx]:null;
	}

	private IDate getCurrentDate(Object[][] locationArray){
		Object date[] = locationArray[timeDimensionIdx[0]];
		return (IDate) date[0];
	}


	@Override
	public String getType() {
		return PLUGIN_KEY;
	}


	@Override
	public Collection<ILocation> computeLocations(final Collection<ILocation> locations) {

		final Set<ILocation> locs = new HashSet<ILocation>();
		for (final ILocation l : locations) {
			locs.add(overrideLocation(l));
		}
		return locs;
	}


	@Override
	public Collection<String> computeMeasures(final Collection<ILocation> locations) {
		return Arrays.asList(underlyingMeasures[0]);
	}


	protected ILocation overrideLocation(ILocation location) {
		return analysisDimensionHelper.overrideLocation(location);
	}

	public void setVectorLabelService(VectorLabelService vectorLabelService) {
		this.vectorLabelService = vectorLabelService;
	}

}
