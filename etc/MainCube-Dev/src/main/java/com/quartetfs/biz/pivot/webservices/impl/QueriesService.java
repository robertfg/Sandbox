/*
 * (C) Quartet FS 2007-2012
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.biz.pivot.webservices.impl;

import static com.quartetfs.fwk.Registry.create;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jws.WebService;

import com.quartetfs.biz.mondrian.pivolap.IPivolapContext;
import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.IActivePivotSchema;
import com.quartetfs.biz.pivot.IActivePivotSession;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.IProjection;
import com.quartetfs.biz.pivot.aggfun.IAggregationFunction;
import com.quartetfs.biz.pivot.cellset.ICellProcedure;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.classification.IClassifier;
import com.quartetfs.biz.pivot.context.IContextSnapshot;
import com.quartetfs.biz.pivot.context.drillthrough.IDrillthroughRow;
import com.quartetfs.biz.pivot.context.subcube.ISubCubeProperties;
import com.quartetfs.biz.pivot.context.subcube.impl.SubCubeProperties;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.axis.IAxisMember;
import com.quartetfs.biz.pivot.dto.AggregateDTO;
import com.quartetfs.biz.pivot.dto.CellDTO;
import com.quartetfs.biz.pivot.dto.CellSetDTO;
import com.quartetfs.biz.pivot.dto.DrillthroughHeaderDTO;
import com.quartetfs.biz.pivot.dto.DrillthroughResultDTO;
import com.quartetfs.biz.pivot.dto.DrillthroughRowDTO;
import com.quartetfs.biz.pivot.dto.MemberDTO;
import com.quartetfs.biz.pivot.dto.MultiselectionDTO;
import com.quartetfs.biz.pivot.dto.ObjectDTO;
import com.quartetfs.biz.pivot.dto.ObjectProperty;
import com.quartetfs.biz.pivot.dto.Path;
import com.quartetfs.biz.pivot.dto.ProjectionProperties;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.LocationUtil.ConvertLocationsResult;
import com.quartetfs.biz.pivot.logging.impl.MessagesServer;
import com.quartetfs.biz.pivot.query.IDrillthroughHeadersQuery;
import com.quartetfs.biz.pivot.query.IDrillthroughQuery;
import com.quartetfs.biz.pivot.query.IExpandLocationsQuery;
import com.quartetfs.biz.pivot.query.IGetAggregatesQuery;
import com.quartetfs.biz.pivot.query.IIndexerLookupQuery;
import com.quartetfs.biz.pivot.query.IMDXQuery;
import com.quartetfs.biz.pivot.query.impl.ActivePivotSyncUtils;
import com.quartetfs.biz.pivot.query.impl.ActivePivotSyncUtils.IAction;
import com.quartetfs.biz.pivot.query.impl.DrillthroughHeadersQuery;
import com.quartetfs.biz.pivot.query.impl.GetAggregatesQuery;
import com.quartetfs.biz.pivot.query.missing.IUnresolvedPath;
import com.quartetfs.biz.pivot.webservices.IQueriesService;
import com.quartetfs.biz.pivot.webservices.IQueriesServiceExecutor;
import com.quartetfs.biz.pivot.webservices.QueriesServiceException;
import com.quartetfs.fwk.IProperty;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.cache.CacheException;
import com.quartetfs.fwk.format.IFormatter;
import com.quartetfs.fwk.query.IQuery;
import com.quartetfs.fwk.query.QueryException;
import com.quartetfs.fwk.util.MessageUtil;
import com.quartetfs.pivot.anz.drillthrough.DrillThroughUtil;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.ListProperties;
import com.quartetfs.tech.indexer.IField;
import com.quartetfs.tech.type.IDataType;

/**
 * Implementation of the {@link IQueriesService} interface.
 * 
 * @author Quartet Financial Systems
 */
@WebService(
		name="QueriesService",
		serviceName = "QueriesService",
		endpointInterface = "com.quartetfs.biz.pivot.webservices.IQueriesService",
		targetNamespace="http://webservices.quartetfs.com"
)
public class QueriesService extends AManagerService implements IQueriesService {

	/** the logger **/
	private static Logger logger = Logger.getLogger(MessagesServer.LOGGER_NAME, MessagesServer.BUNDLE);
	
	// Used for formatting in DrillThrough
	protected final Pattern pattern = Pattern.compile("\\[([^\\[]+)\\]$"); // Extract the text between the last []

	//NBO hack for drillthrough headers aliasing
	//aliasing map for drillthrough header
	protected Properties drillthroughHeadersAliasing;
	
	/**
	 * The executor perform operations that are specific to the MDX layer
	 */
	protected IQueriesServiceExecutor executor;
	
	/**
	 * Key of the executor
	 */
	protected String executorKey = MdxQueriesServiceExecutor.PLUGIN_KEY;
	private DrillThroughUtil drillUtil;
	
	@Override
	public void setManager(IActivePivotManager manager) {
		super.setManager(manager);
		executor = Registry.getExtendedPlugin(IQueriesServiceExecutor.class).valueOf(executorKey).create(manager);
	}
	
	//NBO hack for drillthrough headers aliasing
	public void setDrillthroughHeadersAliasing(Properties drillthroughHeadersAliasing) {
		this.drillthroughHeadersAliasing = drillthroughHeadersAliasing;
	}
	
	/**
	 * Set the key of the QueriesService's executor
	 * @param key
	 */
	public void setExecutorKey(String key) {
		if(!Registry.getExtendedPlugin(IQueriesServiceExecutor.class).isValid(key))
			throw new IllegalArgumentException("Invalid key: "+ key);
		executorKey = key;
		if(manager != null)
			executor = Registry.getExtendedPlugin(IQueriesServiceExecutor.class).valueOf(executorKey).create(manager);
	}
	
	public IQueriesServiceExecutor getDelegate() {
		return executor;
	}
	
	/** @return the list of dimension names in the target pivot instance */
	@Override
	public List<String> retrieveDimensions(String pivotId) {
		//1- Retrieve the pivot instance
		IActivePivot pivot = checkAndRetrievePivot(pivotId);
		
		List<IDimension> dimensions = pivot.getDimensions();
		List<String> dimensionNames = new ArrayList<String>(dimensions.size());
		int dimensionCount = dimensions.size();
		
		// Skip the measure dimension (index 0)
		for(int d = 1; d < dimensionCount; d++) {
			dimensionNames.add(dimensions.get(d).getName());
		}
		
		return dimensionNames;
	}
	
	/** @return the list of default members for an ActivePivot instance */
	@Override
	public List<String> retrieveDefaultMembers(String pivotId) throws QueriesServiceException {
		return executor.retrieveDefaultMembers(pivotId); 
	}
	
	//TODO
   private String errLocal;
	/**
	 * Execute an MDX query.
	 */
	@Override
	public CellSetDTO execute(IMDXQuery query) throws QueriesServiceException {
		final long start = System.currentTimeMillis();
		if (logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, MessagesServer.QUERIES_SERVICE_BEGIN_EXECUTE, query);
		
		try {
			return executor.executeContextually(query);
		} catch(Exception e) {
			logger.log(Level.WARNING, MessageUtil.formMessage(MessagesServer.BUNDLE, MessagesServer.EXC_EXEC_QUERY, query.getType()), e);
			throw new QueriesServiceException(e);
		} finally {
			if (logger.isLoggable(Level.FINE))
				logger.log(Level.FINE, MessagesServer.QUERIES_SERVICE_END_EXECUTE, System.currentTimeMillis() - start);
		}
	}
	
	/**
	 * Execute a "Get Aggregates" query on an ActivePivot instance.
	 */
	@Override
	public List<AggregateDTO> execute(IGetAggregatesQuery query) throws QueriesServiceException {
		final long start = System.currentTimeMillis();
		if (logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, MessagesServer.QUERIES_SERVICE_BEGIN_EXECUTE, query);

		//1- Retrieve the pivot instance
		IActivePivot pivot = checkAndRetrievePivot(query.getPivotId());

		//2.1- Convert string locations to object locations.
		convertLocations(pivot, query);
		
		//2.2- Resolve all measure names 
		final Map<String,String> measureAliases = resolveMeasures(pivot, query);

		//3- Execute the query contextually
		final IContextSnapshot oldContext = 
			ServicesUtil.applyContextValues(pivot, query.getContextValues(), true);
		ICellSet cellSet = null;
		try {
		    cellSet = pivot.execute(query);
		} catch (Exception e) {
			logger.log(Level.WARNING, MessageUtil.formMessage(MessagesServer.BUNDLE, MessagesServer.EXC_EXEC_QUERY, query.getType()), e);
			throw new QueriesServiceException(MessagesServer.BUNDLE, MessagesServer.EXC_EXEC_QUERY, query.getType());
		} finally {
			ServicesUtil.replaceContextValues(pivot, oldContext);
		}

		//4- Create the list of results.
		List<AggregateDTO> results = new ArrayList<AggregateDTO>();
		if(cellSet != null && !cellSet.isEmpty()){
		    final Map<ILocation, Map<String, Object>> cells = new HashMap<ILocation, Map<String,Object>>();
		    cellSet.forEachCell(new ICellProcedure() {
                @Override
				public boolean execute(ILocation location, String measure, Object value) {
                    Map<String, Object> cell = cells.get(location);
                    if(cell == null) {
                        cell = new HashMap<String, Object>(1);
                        cells.put(location, cell);
                    }
                    String alias = measureAliases.get(measure);
                    
                    // Improving logging when there is no alias - PIVOT 1669
                    if (alias == null) 
                    	throw new QuartetRuntimeException(MessageUtil.formMessage(MessagesServer.BUNDLE, MessagesServer.NO_ALIAS_MEASURE, measure));
					
                    cell.put(alias, value);
                    return true;
                }
            });
			for(Entry<ILocation, Map<String,Object>> entry : cells.entrySet()){
				ILocation aggregateDTOLocation = entry.getKey();
				
				if (!(aggregateDTOLocation instanceof Location)) {
					// If the location is not a standard plain location,
					// convert it into a plain location for serialization
					// purposes (PIVOT-1461)
					aggregateDTOLocation = new Location(aggregateDTOLocation);
				}
				results.add(new AggregateDTO(aggregateDTOLocation,entry.getValue()));
			}
		}

		if (logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, MessagesServer.QUERIES_SERVICE_END_EXECUTE, System.currentTimeMillis() - start);

		return results;
	}

	/**
     * Retrieve the property headers for a drill through query.
     */
	@Override
	public List<IProperty> execute(IDrillthroughHeadersQuery query) throws QueriesServiceException {
		//1- Retrieve the pivot instance
		IActivePivot pivot = checkAndRetrievePivot(query.getPivotId());

		//2- Execute the query contextually
		final IContextSnapshot oldContext = 
			ServicesUtil.applyContextValues(pivot, query.getContextValues(), true);
		try {
			return pivot.execute(query);
		} catch (Exception e) {
			logger.log(Level.WARNING, MessageUtil.formMessage(MessagesServer.BUNDLE, MessagesServer.EXC_EXEC_QUERY, query.getType()), e);
			throw new QueriesServiceException(e, MessagesServer.BUNDLE, MessagesServer.EXC_EXEC_QUERY, query.getType());
		} finally {
			ServicesUtil.replaceContextValues(pivot, oldContext);
		}
	}

	/**
	 * Execute a drill through query on an ActivePivot instance.
	 */
	@Override
	public DrillthroughResultDTO execute(final IDrillthroughQuery query) throws QueriesServiceException {
		final long start = System.currentTimeMillis();
		logger.log(Level.INFO,"DrillThrough Location:" + query.getLocations().toString());
		
		if (logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, MessagesServer.QUERIES_SERVICE_BEGIN_EXECUTE, query);

		//1- Retrieve the pivot instance
		final IActivePivot pivot = checkAndRetrievePivot(query.getPivotId());

		//2- Convert string locations to object locations.
		convertLocations(pivot, query);
 		      			
		/** Added by chris **/

		SubCubeProperties subProps = (SubCubeProperties)query.getContextValues().get(0);
  		Set<List<?>> dimContainer = subProps.getAllGrantedMembers().get("Container");
  		List<String> container = (List<String>)dimContainer.iterator().next();
  			
		
		final List<String> dimensionToExtract = new ArrayList<String>();
		
		ListProperties headersFromDb = drillUtil.getDrillThroughHeaders(container.get(1));
		
		final Properties headerProperties = headersFromDb==null?drillthroughHeadersAliasing:headersFromDb; 
		
		Enumeration<Object> keys = headerProperties.keys();
		
		while(keys.hasMoreElements()){
			dimensionToExtract.add( (String)keys.nextElement());
		}
			
		
		if(container!=null && !container.get(1).equalsIgnoreCase(ANZConstants.VAR_CONTAINER)){
			dimensionToExtract.remove("HypoPL_scenario_AUD");
		}
		
		
logger.info("===========================================================================================================================================================");		
		if(container!=null && container.size()>=1 ){
			logger.info("Container Name:" + container.get(1));	
		}
		
		StringBuilder dimToExtract = new StringBuilder();
		StringBuilder dimAlias = new StringBuilder();
		
		for (String header : dimensionToExtract) {
			dimToExtract.append(header).append(",");
			
			dimAlias.append(headerProperties.get(header)).append(",");
		}  
		
		logger.info("Dim To Extract:" + dimToExtract.toString());

		logger.info("Header Alias:" + dimAlias.toString());
		logger.info("===========================================================================================================================================================");		
		
		
		
		/** Added by chris end **/
		
		//3- Execute the drillthrough query and retrieve the total number of rows relevant to this query atomically
		try {
			return ActivePivotSyncUtils.activePivotSyncExec(pivot, new IAction<DrillthroughResultDTO, IDrillthroughQuery>() {

				private static final long serialVersionUID = 9043249841255530367L;

				@Override
				public DrillthroughResultDTO execute(IActivePivotSession session, IDrillthroughQuery argument) throws QuartetException {
					
					
					// Execute the query contextually
					final IContextSnapshot oldContext = 
						ServicesUtil.applyContextValues(pivot, query.getContextValues(), true);

					try {
						// Retrieve the drillthrough rows
						final List<IDrillthroughRow> rows = pivot.execute(query);

						// Retrieve the drillthrough headers
						final DrillthroughHeadersQuery headersQuery = new DrillthroughHeadersQuery(true);
						headersQuery.setContextValues(query.getContextValues());
						final List<IProperty> headers = pivot.execute(headersQuery);					
						/**Added by chris **/
						Map<String,Integer> headerIndexMapping = new HashMap<String,Integer>();
						int headerIndex = 0;
						for (IProperty iProperty : headers) {						
							headerIndexMapping.put( iProperty.getName() , headerIndex);
							headerIndex++;
						}
					    /** Added by chris end **/	
						
						// Compute an upper bound of the total row count for that query (without max row limit)
						final int estimatedTotalRowCount[] = new int[] {0};
						GetAggregatesQuery countingQuery = new GetAggregatesQuery(query.getLocations(), Collections.singleton(IAggregationFunction.COUNT_ID));
						ICellSet countCellSet = pivot.execute(countingQuery);
						countCellSet.forEachCell(new ICellProcedure() {
							@Override
							public boolean execute(ILocation location, String measure, Object value) {
								estimatedTotalRowCount[0] += (Long)value;
								return true;
							}
						});

						final IPivolapContext pivolapContext = pivot.getContext().get(IPivolapContext.class);

						if (pivolapContext != null) {

							// If the pivolap context is defined,
							// we may have to apply formatters to the drillthrough result

							Map<String, IFormatter> formatters = new HashMap<String, IFormatter>();
							for (Entry<String, IFormatter> entry : pivolapContext.getFormatters().entrySet()) {
								// We use a regexp to extract the name of the column from the unique name
								final Matcher matcher = pattern.matcher(entry.getKey());
								if (matcher.find()) {
									formatters.put(matcher.group(1), entry.getValue());
								}
							}

							final IFormatter[] formattersArray = new IFormatter[headers.size()];
							// We keep only the formatters that we need
							for (int i = 0; i < formattersArray.length; ++i) {
								String headerName = headers.get(i).getName();
								formattersArray[i] = formatters.get(headerName);
							}

							// Format the rows
							for (IDrillthroughRow row : rows) {
								final Object[] content = row.getContent();
								for (int i = 0; i < content.length; ++i) {
									if (formattersArray[i] != null && content[i] != null) {
										try {
											content[i] = formattersArray[i].format(content[i]);
										} catch (RuntimeException e) {
											// In case of formatting exception, we fill the drillthrough with the error message
											String errorMessage = MessageUtil.formMessage(MessagesServer.BUNDLE, MessagesServer.EXC_FORMATTING, formattersArray[i], content[i]);
											logger.log(Level.WARNING, errorMessage, e);
											content[i] = IDrillthroughRow.ERROR;
										}
									}
								}
							} // End for
						} // End if (pivolapContext != null)


						// Build the result object
						List<DrillthroughRowDTO> drillthroughRows = new ArrayList<DrillthroughRowDTO>(rows.size());
						for (IDrillthroughRow row : rows) {
							Object[] content = null;
							if(row.getContent() != null) {
								int rowLength = row.getContent().length;
								content = new Object[dimensionToExtract.size()];
								int ctr = 0;
								for (String header : dimensionToExtract) {
									content[ctr] =  (row.getContent()[ headerIndexMapping.get(header)  ] == null ? "" : row.getContent()[ headerIndexMapping.get(header) ].toString());
									ctr++;
								} 
								
								
							} 
							drillthroughRows.add(new DrillthroughRowDTO(row.getOrdinal(), content));
						}

						List<DrillthroughHeaderDTO> drillthroughHeaders = new ArrayList<DrillthroughHeaderDTO>(headers.size());
						
						
						/** added by chris **/
						for(String header: dimensionToExtract) {
							String headerName = headerProperties.get(header) == null?header: (String)headerProperties.get( header );
							drillthroughHeaders.add(new DrillthroughHeaderDTO(headerName));
						}
						
						/** added by chris end **/
						
						

						return new DrillthroughResultDTO(drillthroughHeaders, drillthroughRows, estimatedTotalRowCount[0]);
						
					} catch (QueryException e) {
                        errLocal = e.getLocalizedMessage(); 
                        if(errLocal.indexOf("maximum")!=-1){ //You had exceed maximum number of AP records (xxx) allowed in drill through.
                        	int start = errLocal.indexOf("(");
                        	int end   = errLocal.indexOf(")"); 
                         	errLocal = "You had exceed maximum number of AP records (" + errLocal.substring( start + 1,end  ) + " ) allowed in drill through.";
                        } 
						throw new QueriesServiceException(e,  errLocal , errLocal, query.getType());
					} finally {
						ServicesUtil.replaceContextValues(pivot, oldContext);
					}
				}
  
				@Override
				public String toString() {
					return super.toString() + ": " + String.valueOf(query);
				}

			}, query);
		} catch(QueryException e) {
		//	throw new QueriesServiceException(e, e.getLocalizedMessage(), e.getLocalizedMessage(), query.getType());
			throw new QueriesServiceException(e, errLocal, errLocal, query.getType());
			
		} catch (QuartetException e) {
			logger.log(Level.WARNING, MessageUtil.formMessage(MessagesServer.BUNDLE, MessagesServer.EXC_EXEC_QUERY, query.getType()), e);
			throw new QueriesServiceException(e, MessagesServer.BUNDLE, MessagesServer.EXC_EXEC_QUERY, query.getType());
		} finally {
			if (logger.isLoggable(Level.FINE))
				logger.log(Level.FINE, MessagesServer.QUERIES_SERVICE_END_EXECUTE, System.currentTimeMillis() - start);
		}
	}

	/**
	 * The list of types which are susceptible to be edited.
	 */
	protected static final List<Class<?>> UPDATABLE_TYPES = Arrays.asList(new Class<?>[] {String.class,
			Double.class,
			Float.class,
			Integer.class,
			Long.class});


	/**
	 * Retrieve a record by key from an ActivePivot instance.
	 */
	@Override
	public ObjectDTO retrieveObject(String pivotId, String objectKey) throws QueriesServiceException {

		//1- Retrieve the pivot instance
		IActivePivot pivot = checkAndRetrievePivot(pivotId);

		//2- Retrieve the originating schema
		IActivePivotSchema schema = null;
		for(IActivePivotSchema s:manager.getSchemas().values()){
			if(pivot == s.retrieveActivePivot(pivotId)){
				schema = s;
				break;
			}
		}
		if(schema==null) {
			throw new QueriesServiceException(MessagesServer.BUNDLE, MessagesServer.EXC_NO_SCHEMA, (Object[]) null);
		}

		// Parse the object key string to its real data type
		IDataType<?> keyDataType = schema.getIndexer().getFields().get(IProjection.PROJECTION_OBJECT_KEY).getDataType();
		Object parsedKey = keyDataType.parseData(objectKey);

		//Retrieve the object's projections
		Map<String,Object> map = new HashMap<String, Object>();
		map.put(IProjection.PROJECTION_OBJECT_KEY, parsedKey);
		IIndexerLookupQuery indexerLookupQuery = create(IIndexerLookupQuery.class, map);
		Collection<IProjection> projections = null;
		try {
			projections = pivot.execute(indexerLookupQuery);
		} catch (Exception e) {
			throw new QueriesServiceException(e, MessagesServer.BUNDLE, MessagesServer.EXC_EXEC_QUERY, indexerLookupQuery.getType());
		}

		//NULL checking barrier
		if(projections==null || projections.isEmpty()) return null;

		//Retrieve object data
		Object object = null;
		try {
			object = schema.getCache().get(parsedKey);
		} catch (CacheException e) {
			throw new QueriesServiceException(e, MessagesServer.BUNDLE, MessagesServer.EXC_GET_OBJECT_FROM_CACHE, parsedKey);
		} catch (Exception e) {
			//nothing to do here
		}

		//		//NULL checking barrier
		//		if(object==null) 
		//			return null;

		//Create the objectDTO
		ObjectDTO dto = new ObjectDTO();
		dto.setKey(objectKey);

		//Retrieve properties which belong to the object
		final IClassifier classifier = schema.getClassifier();
		final Set<Entry<String, IField>> inputFields = classifier.getIntputFields().entrySet();
		final Set<Entry<String, IField>> projectionInputFields = new HashSet<Entry<String,IField>>();
		final Map<String, IField> outputFields = classifier.getOutputFields();

		
	    for(Entry<String, IField> entry : inputFields) {
	    	if(object == null) {
	    		// No object cache is defined, the field must be
	    		// retrieved from the projections.
	    		projectionInputFields.add(entry);
	    	} else {
				final IProperty property = entry.getValue().getProperty();
				// We have the original object,
				// test if the property was found on the object.
				try {
					Object propertyValue = property.getValue(object);
					boolean isReadOnly = !UPDATABLE_TYPES.contains(propertyValue.getClass());
					dto.getProperties().add(new ObjectProperty(property.getName(), propertyValue.toString(), isReadOnly));
					// The property was found on the object and we can modify it
				} catch (QuartetException e) {
					// The property was not found on the object and must be
				    // retrieved from the projections (calculator result)
				    projectionInputFields.add(entry);
				}
	    	}
		}
	    
		// Use the projections to introspect the remaining fields
		for(IProjection projection : projections){
			ProjectionProperties projectionProperties = new ProjectionProperties();
			projectionProperties.setKey(projection.getId().toString());

			for(Entry<String, IField> entry : projectionInputFields) {
			    final String fieldName = entry.getKey();
				final IField outputField = outputFields.get(fieldName);
				Object propertyValue = null;
				try {
					propertyValue = outputField.getProperty().getValue(projection.getContent());
					projectionProperties.getProperties().add(new ObjectProperty(fieldName, propertyValue.toString(), true));
				} catch (QuartetException e) {
					logger.log(Level.SEVERE, MessageUtil.formMessage(MessagesServer.BUNDLE, MessagesServer.EXC_GET_VALUE, fieldName), e);
				}
			}

			dto.getProjections().add(projectionProperties);
		}

		return dto;
	}

	/**
	 * Convert "String" locations to "Object" locations.
	 * 
	 * @param pivot The target pivot.
	 * @param query The target query to change.
	 */
	protected void convertLocations(IActivePivot pivot, IQuery<?> query) throws QueriesServiceException{
		// Convert the locations of this query
		final ConvertLocationsResult conversionResult;
		try{
			conversionResult = ServicesUtil.convertQueryLocations(pivot, query);
		} catch(Exception e) {
			throw new QueriesServiceException(
					e, 
					MessagesServer.BUNDLE, 
					MessagesServer.EXC_CONVERT_LOCATIONS, 
					(Object[]) null);
		}
		
		// Make sure that all the locations were successfully converted
		final Map<ILocation, List<IUnresolvedPath>> unresolvedLocations = conversionResult.getUnresolvedLocations();
		if (unresolvedLocations != null && !unresolvedLocations.isEmpty()) {
			final StringBuilder sb = new StringBuilder();
			for(final ILocation loc: unresolvedLocations.keySet())
				sb.append(" ").append(loc.toString());
			throw new QueriesServiceException(
					MessagesServer.BUNDLE, 
					MessagesServer.EXC_UNRESOLVED_LOC, 
					pivot.getId(), sb.toString());
		}
	}
	
	/**
	 * Resolve all measure names to real (unaliased) measure names
	 * 
	 * @param pivot The target pivot
	 * @param query The target query to change
	 * @return A map from the real name of the measures to their "original" name in the query (i.e. possibly aliased)
	 */
	protected Map<String,String> resolveMeasures(IActivePivot pivot, IQuery<?> query) throws QueriesServiceException {
		try{
			return ServicesUtil.resolveQueryMeasures(pivot, query);
		} catch(Exception e) {
			throw new QueriesServiceException(e, MessagesServer.BUNDLE, MessagesServer.EXC_CONVERT_LOCATIONS, (Object[]) null);
		}
	}

	/**
	 * Retrieve the children of a member following a dimension.
	 */
	@Override
	public MemberDTO[] retrieveChildMembers(String pivotId, String dimensionName, MemberDTO member) throws QueriesServiceException {
		IActivePivot pivot = checkAndRetrievePivot(pivotId);
		if (pivot == null)
			return null;


		// Retrieve the dimension
		IDimension dimension = null;
		for (IDimension d : pivot.getDimensions()){
			if (d.getName().equals(dimensionName)) {
				dimension = d;
				break;
			}
		}

		//If the dimension is wrong we return null
		if (dimension == null) return null;

		if (member == null){
			//If there is no members define we return all the members of the first level.
			return retrieveMembers(pivotId,dimensionName,dimension.getLevels().get(0).getName());
		}else{
			//If there is a member define we return the children of this.
			Path targetedMemberPath = member.getPath();
			//Retrieve the targetedMember
			IAxisMember axisMember = (IAxisMember) dimension.retrieveMembers(targetedMemberPath.getPath()).get(0);

			if (axisMember == null) return null;

			// Return DTO members
			Collection<IAxisMember> childMembers = axisMember.getChildren().values();
			MemberDTO[] wsMembers = new MemberDTO[childMembers.size()];
			int i = 0;
			for (IAxisMember childMember:childMembers){
				wsMembers[i] = buildMember(childMember);
				i++;
			}
			return wsMembers;
		}
	}

	/**
	 * Retrieve all the members in a specific level in a dimension.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public MemberDTO[] retrieveMembers(String pivotId, String dimensionName, String levelName) throws QueriesServiceException {
		IActivePivot pivot = checkAndRetrievePivot(pivotId);
		if (pivot == null)
			return null;
		IDimension dimension = null;
		for (IDimension d : pivot.getDimensions())
			if (d.getName().equals(dimensionName)) {
				dimension = d;
				break;
			}
		if (dimension == null)
			return null;
		for (ILevel curLvl : dimension.getLevels())
			if (curLvl.getName().equals(levelName)) {
				List<IAxisMember> axisMembers = (List<IAxisMember>) curLvl.getMembers();
				MemberDTO[] wsMembers = new MemberDTO[axisMembers.size()];
				for (int i = 0; i < axisMembers.size(); i++){
					wsMembers[i] = buildMember(axisMembers.get(i));
				}

				return wsMembers;
			}
		return null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public MemberDTO[] retrieveChildMembersCtx(String pivotId, String dimensionName, MemberDTO member, List<MultiselectionDTO> context) throws QueriesServiceException {
		if(context == null)
			return retrieveChildMembers(pivotId, dimensionName, member);
		
		IActivePivot pivot = checkAndRetrievePivot(pivotId);
		if (pivot == null)
			return null;
		
		Map<String, MultiselectionDTO> multiSelAsMap = new HashMap<String, MultiselectionDTO>();
		for(MultiselectionDTO msDto : context)
			multiSelAsMap.put(msDto.getDimension(), msDto);
		
		if(multiSelAsMap.containsKey(dimensionName))
			throw new QueriesServiceException("The discovered dimension cannot be part of the context");
		
		final ISubCubeProperties currSubCube = pivot.getContext().get(ISubCubeProperties.class);
		final ISubCubeProperties newSubCube;
		if(null != currSubCube)
			newSubCube = currSubCube.clone();
		else {
			newSubCube = Registry.create(ISubCubeProperties.class);
			newSubCube.setAccessGranted(true);
		}
		
		pivot.getContext().set(ISubCubeProperties.class, newSubCube);
		try {
			// Retrieve the dimension
			final List<IDimension> dims = pivot.getDimensions();
			int nbDims = dims.size();
			IDimension dimension = null;
			ILocation locationToExpand = null;
			for(int i = 1; i < nbDims; i ++) {
				final IDimension dim = dims.get(i);
				if (dim.getName().equals(dimensionName)) {
					if(member == null || member.getPath() == null)
						return retrieveMembersCtx(pivotId, dimensionName, dim.getLevels().get(0).getName(), context);
					
					if(member.getPath().getPath().length > dim.getLevels().size())
						return null;
					
					dimension = dim;
					
					final Object[][] locToExpAsArray = new Object[nbDims - 1][];
					//Retrieve equivalent members
					List<IAxisMember> axisMembers = (List<IAxisMember>) dim.retrieveMembers(member.getPath().getPath());
	
					//If null, break the LOOP
					if(axisMembers.isEmpty()){
						continue;
					}
	
					//Otherwise take the first member
					IAxisMember axisMember = axisMembers.get(0);
	
					//Fill the 
					List<IAxisMember> memberPath = axisMember.getPath();
					final Object[] pathAsArray = new Object[memberPath.size() + 1];
					for(int k = 0; k < memberPath.size(); k++){
						if(member.getPath().getPath()[k]!=null)
							pathAsArray[k] = memberPath.get(k).getDiscriminator();
						else
							pathAsArray[k] = null;
					}
					locToExpAsArray[dim.getOrdinal() - 1] = pathAsArray;
					locationToExpand = new Location(locToExpAsArray);
				} else if(multiSelAsMap.containsKey(dim.getName())) {
					MultiselectionDTO msDto = multiSelAsMap.get(dim.getName());
					for(int j = 0; j < msDto.getSelectedPaths().size(); j ++)
						newSubCube.grantMembers(dim.getName(), Arrays.asList(msDto.getSelectedPaths().get(j).getPath()));
				}
			}
		
			Collection<ILocation> expanded = pivot.execute(Registry.create(IExpandLocationsQuery.class, Collections.singleton(locationToExpand)));
			List<MemberDTO> wsMembers = new ArrayList<MemberDTO>();
			final int dimensionCoord = dimension.getOrdinal() - 1;
			for(ILocation expandedLoc : expanded) {
				final Object[] path = new Object[expandedLoc.getLevelDepth(dimensionCoord)];
				for(int i = 0; i < path.length; i ++)
					path[i] = expandedLoc.getCoordinate(dimensionCoord, i);
				IAxisMember axisMember = (IAxisMember) dimension.retrieveMembers(path).get(0);
				if(axisMember == null)
					return null;
				wsMembers.add(buildMember(axisMember));
			}
			return wsMembers.toArray(new MemberDTO[0]);
		} catch (QueryException e) {
			throw new QueriesServiceException(e);
		} finally {
			pivot.getContext().set(ISubCubeProperties.class, currSubCube);
		}
	}

	@Override
	public MemberDTO[] retrieveMembersCtx(String pivotId, String dimensionName, String levelName, List<MultiselectionDTO> context) throws QueriesServiceException {
		if(context == null)
			return retrieveMembers(pivotId, dimensionName, levelName);
		
		IActivePivot pivot = checkAndRetrievePivot(pivotId);
		if (pivot == null)
			return null;
		
		Map<String, MultiselectionDTO> multiSelAsMap = new HashMap<String, MultiselectionDTO>();
		for(MultiselectionDTO msDto : context)
			multiSelAsMap.put(msDto.getDimension(), msDto);
		
		if(multiSelAsMap.containsKey(dimensionName))
			throw new QueriesServiceException("The discovered dimension cannot be part of the context");
		
		final ISubCubeProperties currSubCube = pivot.getContext().get(ISubCubeProperties.class);
		final ISubCubeProperties newSubCube;
		if(null != currSubCube)
			newSubCube = currSubCube.clone();
		else {
			newSubCube = Registry.create(ISubCubeProperties.class);
			newSubCube.setAccessGranted(true);
		}
		
		pivot.getContext().set(ISubCubeProperties.class, newSubCube);
		try {
			// Retrieve the dimension
			final List<IDimension> dims = pivot.getDimensions();
			int nbDims = dims.size();
			IDimension dimension = null;
			ILocation locationToExpand = null;
			for(int i = 1; i < nbDims; i ++) {
				final IDimension dim = dims.get(i);
				if (dim.getName().equals(dimensionName)) {
					int levelDepth = 0;
					
					for(ILevel lvl : dim.getLevels()) {
						if(lvl.getName().equals(levelName)) {
							break;
						}
						levelDepth  ++;
					}
					
					dimension = dim;
					
					final Object[][] locToExpAsArray = new Object[nbDims - 1][];
					locToExpAsArray[dim.getOrdinal() - 1] = new Object[levelDepth + 1];
					locationToExpand = new Location(locToExpAsArray);
				} else if(multiSelAsMap.containsKey(dim.getName())) {
					MultiselectionDTO msDto = multiSelAsMap.get(dim.getName());
					for(int j = 0; j < msDto.getSelectedPaths().size(); j ++)
						newSubCube.grantMembers(dim.getName(), Arrays.asList(msDto.getSelectedPaths().get(j).getPath()));
				}
			}
		
			Collection<ILocation> expanded = pivot.execute(Registry.create(IExpandLocationsQuery.class, Collections.singleton(locationToExpand)));
			List<MemberDTO> wsMembers = new ArrayList<MemberDTO>();
			int dimensionCoord = dimension.getOrdinal() - 1;
			for(ILocation expandedLoc : expanded) {
				final Object[] path = new Object[expandedLoc.getLevelDepth(dimensionCoord)];
				for(int i = 0; i < path.length; i ++)
					path[i] = expandedLoc.getCoordinate(dimensionCoord, i);
				IAxisMember axisMember = (IAxisMember) dimension.retrieveMembers(path).get(0);
				if(axisMember == null)
					return null;

				wsMembers.add(buildMember(axisMember));
			}
			return wsMembers.toArray(new MemberDTO[0]);
		} catch (QueryException e) {
			throw new QueriesServiceException(e);
		} finally {
			pivot.getContext().set(ISubCubeProperties.class, currSubCube);
		}
	}

	/**
	 * Build a DTO member from an AxisMember.
	 * 
	 * @param axisMember a given axis member.
	 * @return The DTO member.
	 */
	protected MemberDTO buildMember(IAxisMember axisMember){
		MemberDTO member = new MemberDTO();
		member.setDisplayName(axisMember.getName());
		member.setDimensionName(axisMember.getLevel().getDimension().getName());
		member.setLevelName(axisMember.getLevel().getName());
		member.setDepth(axisMember.getDepth());

		List<IAxisMember> axisMemberPath = axisMember.getPath();
		String[] path = new String[axisMemberPath.size()];
		for(int j = 0;j < axisMemberPath.size();j++){
			path[j] = axisMemberPath.get(j).getName();
		}
		member.setPath(new Path(path));
		return member;
	}
	
	/**
	 * Generate the CellDTO expressing the conversion from one old CellDTO to a new value
	 * 
	 * @param previousCell the old CellDTO. May be null
	 * @param newValue the new value of the Cell. May be null
	 * @param cellFormattedValue used as formatted value. May be null
	 * @return the CellDTO expressing the conversion from one old CellDTO to a new value
	 */
	public static CellDTO toDTO(CellDTO previousCell, Object cellValue, String cellFormattedValue, int ordinal) {
		if(previousCell == null) {
			return new CellDTO(ordinal, cellValue, cellFormattedValue);
		} else {
			return new CellDTO(ordinal, cellValue, cellFormattedValue, previousCell.getValue());
		}
	}

	/**
	 * Generate the CellDTO expressing the conversion from one old CellDTO to a new value
	 * 
	 * @param previousCell the old CellDTO. May be null
	 * @param newValue the new value of the Cell. May be null
	 * @param formatter used to format the cell value. May be null
	 * @return the CellDTO expressing the conversion from one old CellDTO to a new value
	 */
	public static CellDTO toDTOwithFormatter(CellDTO previousCell, Object newValue, IFormatter formatter, int ordinal) {
		final String formattedValue;
		if (formatter != null) {
			// It is implied that formatters can handle null values
			formattedValue = formatter.format(newValue);
		} else {
			formattedValue = (newValue != null) ? newValue.toString() : "";
		}
		
		return toDTO(previousCell, newValue, formattedValue, ordinal);
	}


	/**
	 * Convert an array of objects into an array of strings.
	 * The toString() method of objects is used.
	 *
	 * @param objectArray
	 * @return array of strings
	 */
	public static String[] toStringArray(Object[] objectArray) {
		if(null == objectArray) return null;
		final String[] stringA = new String[objectArray.length];
		for(int i = 0; i < objectArray.length; i ++)
			stringA[i] = (objectArray[i] == null) ? "null" : objectArray[i].toString();
		return stringA;
	}

	public DrillThroughUtil getDrillUtil() {
		return drillUtil;
	}

	public void setDrillUtil(DrillThroughUtil drillUtil) {
		this.drillUtil = drillUtil;
	}
}