package com.quartetfs.pivot.anz.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.context.subcube.ISubCubeProperties;
import com.quartetfs.biz.pivot.context.subcube.impl.SubCubeProperties;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.impl.LocationUtil.ConvertLocationsResult;
import com.quartetfs.biz.pivot.query.IGetAggregatesQuery;
import com.quartetfs.biz.pivot.query.impl.GetAggregatesQuery;
import com.quartetfs.biz.pivot.webservices.IQueriesServiceExecutor;
import com.quartetfs.biz.pivot.webservices.QueriesServiceException;
import com.quartetfs.biz.pivot.webservices.impl.MdxQueriesServiceExecutor;
import com.quartetfs.fwk.IPair;
import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.impl.Pair;
import com.quartetfs.fwk.query.QueryException;
import com.quartetfs.fwk.query.UnsupportedQueryException;
import com.quartetfs.pivot.anz.impl.MessagesANZ;

/**
 * Helps to manipulate locations and query complexity on a target pivot
 * 
 * @author QuartetFS
 */
public class QueryHelper {
	
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	/** The current pivot */
	protected final IActivePivot pivot;

	/** The dimensions' ordinals */
	protected final Map<String,Integer> dimensionOrdinalsCache = new HashMap<String, Integer>();
	private Map<Integer,Map<Integer,String>> dimensionIndexCache = new HashMap<Integer,Map<Integer,String>>();
	

	/** The levels' ordinals */
	protected final Map<String,int[]> levelOrdinalsCache = new HashMap<String, int[]>();

	protected IQueriesServiceExecutor mdxQueryService;

	protected String mdxQueryServiceKey = MdxQueriesServiceExecutor.PLUGIN_KEY;
	
	
	/**
	 * Give the pivot to the helper
	 * 
	 * @param pivot
	 */
	public QueryHelper(IActivePivot pivot) {
		super();
		this.pivot = pivot;
		for(IDimension dimension:pivot.getDimensions()){
			dimensionOrdinalsCache.put(dimension.getName(),dimension.getOrdinal());
			
			Map<Integer,String> levelIndex = new HashMap<Integer,String>();
			dimensionIndexCache.put(dimension.getOrdinal(),  levelIndex);
			
			for(ILevel level:dimension.getLevels()){
				int[] ordinals = new int[]{dimension.getOrdinal(),level.getOrdinal()};
				levelOrdinalsCache.put(level.getName()+"@"+dimension.getName(), ordinals);
				levelOrdinalsCache.put(level.getName(), ordinals);
				levelIndex.put(level.getOrdinal(), level.getName()+"@"+dimension.getName());
			}
			
		}
		
	
	}
	
	public QueryHelper(IActivePivotManager manager, String pivotName)
	{
		this(manager.getActivePivots().get(pivotName));	
		mdxQueryService = Registry.getExtendedPlugin(IQueriesServiceExecutor.class).valueOf(mdxQueryServiceKey).create(manager);
	}

	/**
	 * Execute a getAggreates query based on a pivot instance and some query paramaters
	 * 
	 * @param pivot the target pivot instance
	 * @param queryParameters map which links level to target discriminators.
	 * Levels (keys) are described as string to parse with the form "dimensionName"@"levelName".
	 * Discriminators (values) can be wildcards (null ref), collections or single quantity.
	 * @return the results of the query
	 * @throws QueryException
	 * @throws UnsupportedQueryException
	 */
	public ICellSet getAggregatesQuery(Map<String,Object> queryParameters,Collection<String> measures) throws QueryException{
		ILocation location = computeLocation(queryParameters);
	//	LOGGER.info(String.format("Executing query for location %s", location));
		
		GetAggregatesQuery aggQuery = new GetAggregatesQuery(Collections.singleton(location), measures);
		aggQuery.setPivotId(pivot.getId());
		ICellSet cellSet =  pivot.execute( aggQuery);
		aggQuery = null;
		location = null;
		return cellSet;
		
		
	}
	
	public ICellSet getAggregatesQuery(List<Map<String,Object>> queryParameters,Collection<String> measures) throws QueryException{
		List<ILocation> locations = computeLocations(queryParameters);
		LOGGER.info(String.format("Executing query for location %s", locations));
		
		GetAggregatesQuery aggQuery = new GetAggregatesQuery(locations, measures );
		aggQuery.setPivotId(pivot.getId());
		
		ICellSet cellSet = pivot.execute(aggQuery);
		
        locations.clear();
		locations = null;
		aggQuery = null;
		
		return cellSet;
	}
	
	
	public ICellSet getCellSet(Set<ILocation> locations,Collection<String> measures) throws QueryException{		
		GetAggregatesQuery query = new GetAggregatesQuery(locations, measures);
					query.setPivotId(pivot.getId());
		 ICellSet cellSet =  pivot.execute(query);
		 query = null;
		 locations = null;
		 return cellSet;
		 
		 
	}	
	
	public ISubCubeProperties createSubcube(Set<ILocation> locations) throws QueryException
	{
		ISubCubeProperties subCube = new SubCubeProperties();
		subCube.setAccessGranted(true);	
		for(ILocation loc : locations)
		{
			Object[][] arrayValue = loc.arrayCopy();	
			int dimCount=loc.getDimensionCount();
			for(int cnt=0;cnt<dimCount;cnt++)
			{
				Object[] locPart = arrayValue[cnt];
				if(needSubcube(locPart))
				{
					List<String> path = new ArrayList<String>();
					for(Object o : locPart)
					{
						path.add(o == null ? ILevel.ALLMEMBER : o.toString());
					}
					subCube.grantMembers(pivot.getDimensions().get(cnt+1).getName(),path);
				}
			}
		}
		
				  
				 
		
		
		return subCube;
	}

	private boolean needSubcube(Object[] locPart) {
		return locPart.length > 1 || locPart[0] != null;
	}
	
	public List<ILocation> computeLocations(List<Map<String, Object>> queryParameters) 
	{
		List<ILocation> locations = new ArrayList<ILocation>();
	     for ( Map<String,Object> location : queryParameters) {
	    	 locations.add(computeLocation(location) );
		}
		  return locations;
	
	}
	
	/**
	 * Compute the equivalent location for a given pivot and query parameters
	 * 
	 * @param pivot
	 * @param queryParameters
	 * @return a location
	 */
	public ILocation computeLocation(Map<String, Object> queryParameters) 
	{
		
		Map<Integer,List<IPair<int[], Object>>> paramPairMap = new HashMap<Integer,List<IPair<int[], Object>>>();
		List<IPair<int[], Object>> ordinalsToDiscriminator = null;
		
		for(Entry<String,Object> entry:queryParameters.entrySet())
		{
			int[] ordinals = retrieveLevelOrdinals(entry.getKey());
			
			if(ordinals==null){
				LOGGER.severe("Please check containerMapping.csv for the dimension name:" + entry.getKey()  );
			}
			
			 ordinalsToDiscriminator = paramPairMap.get(ordinals[0]);
			if(ordinalsToDiscriminator==null)
			{
				ordinalsToDiscriminator = new ArrayList<IPair<int[],Object>>();
				paramPairMap.put(ordinals[0], ordinalsToDiscriminator);
			}
			ordinalsToDiscriminator.add(new Pair<int[], Object>(ordinals, entry.getValue()));
		}

		//Create the location
		Object[][] arrayLocation = new Object[pivot.getDimensions().size() - 1][];
		
		
		
		for(int i=0;i<arrayLocation.length;i++)
		{
			int ordinal = i+1;
			 ordinalsToDiscriminator = paramPairMap.get(ordinal);
			if(ordinalsToDiscriminator!=null)
			{
				for(IPair<int[], Object> pair:ordinalsToDiscriminator)
				{
					reallocated(arrayLocation, i, pair);	
					Object qualifier = pair.getRight();
					if(qualifier.equals(ILocation.WILDCARD)){
						arrayLocation[i][pair.getLeft()[1]] = null;
					}else{
						arrayLocation[i][pair.getLeft()[1]] = qualifier;
					}
				}
				continue;
			}			
			//If not processed yet just set a null parameter
			arrayLocation[i] = new Object[]{null};
		}
		
		ILocation location = new Location(arrayLocation);
		arrayLocation = null;
		paramPairMap = null;
		ordinalsToDiscriminator = null;
		return location;
	}

	private void reallocated(Object[][] arrayLocation, int i, IPair<int[], Object> pair)
	{
		int newLength=pair.getLeft()[1]+1; 
		if(arrayLocation[i]==null)
		{
			//Allocate value 
			arrayLocation[i] = new Object[newLength];
		}
		else if(arrayLocation[i].length<newLength)
		{
			//Already allocated - reallocated and copy existing values
			Object[] oldValue = arrayLocation[i];
			arrayLocation[i] = new Object[newLength];						
			//Copy old values 
			for(int cnt=0;cnt<oldValue.length;cnt++)
			{
				arrayLocation[i][cnt] = oldValue[cnt];
			}
			oldValue = null;
		}
	}	

	/**
	 * Compute the location based on strings
	 * 
	 * @param queryParameters map which links level to target discriminators.
	 * @return The location with objects as coordinates
	 */
	public ILocation computeLocationWithStrings(Map<String,Object> queryParameters){
		//Create the location
		ILocation location = computeLocation(queryParameters);

		//Convert it to object coordinates
		ConvertLocationsResult results = LocationUtil.convertLocations(pivot.getDimensions(), Collections.singleton(location));
		Set<ILocation> locations = results.getConvertedLocations();
		if(locations.size()>0){
			return locations.iterator().next();
		}else{
			throw new QuartetRuntimeException("Cannot convert location="+location);
		}
	}

	/**
	 * Retrieve the ordinal of a dimension
	 * 
	 * @param dimensionName
	 * @return The ordinal of the dimension
	 */
	public int retrieveDimensionOrdinal(String dimensionName){
		return dimensionOrdinalsCache.get(dimensionName);
	}

	/**
	 * Retrieve a dimension by its name
	 * 
	 * @param dimensionName
	 * @return The dimension if it exists
	 */
	public IDimension retrieveDimension(String dimensionName){
		Integer ordinal = retrieveDimensionOrdinal(dimensionName);
		if(ordinal==null) {
			return null;
		}
		return pivot.getDimensions().get(ordinal);
	}

	/**
	 * Retrieve the ordinals of the dimension and level submitted
	 * 
	 * @param pivot the target pivot instance
	 * @param level this argument takes the form of "levelName" or "levelName"@"dimensionName" parsed by the method
	 * @return the ordinals of the dimension and level submitted
	 */
	public int[] retrieveLevelOrdinals(String level){
		return levelOrdinalsCache.get(level);
	}

	/**
	 * Retrieve the location's value associated to a level
	 * 
	 * @param pivot
	 * @param levelAtDimension
	 * @param location
	 * @return The value as an object
	 */
	public Object retrieveValue(String levelAtDimension,ILocation location){
	
		int[] ordinals = retrieveLevelOrdinals(levelAtDimension);
	    if(ordinals==null){
	    	LOGGER.info("Config misMatch between marketRiskDimension.xml and  AP-Extract-SubCubeConfig.xml for Dimension Name:" + levelAtDimension + "- at apx-data-output-mapping ");
	    	return "";
	    }
		if(location.getLevelDepth(ordinals[0] - 1) <= ordinals[1])
		{
			return "";
		}
		return location.getCoordinate(ordinals[0] - 1, ordinals[1]);
	}
	
	
	public Map<Integer, Map<Integer, String>> getDimensionIndexCache() {
		return dimensionIndexCache;
	}
	
	public IActivePivot getPivot() {
		return pivot;
	}
	
	public List<String> retrieveDefaultMembers() throws QueriesServiceException {
		return mdxQueryService.retrieveDefaultMembers(pivot.getId()); 
	}
	
	
}
