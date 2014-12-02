/*
 * (C) Quartet FS 2007-2012
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.util.StopWatch;

import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.IActivePivotSchema;
import com.quartetfs.biz.pivot.IProjection;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.IPair;
import com.quartetfs.fwk.IProperty;
import com.quartetfs.fwk.filtering.ICondition;
import com.quartetfs.fwk.filtering.impl.AndCondition;
import com.quartetfs.fwk.filtering.impl.EqualCondition;
import com.quartetfs.fwk.filtering.impl.InCondition;
import com.quartetfs.fwk.filtering.impl.SingleAccessor;
import com.quartetfs.fwk.filtering.impl.SubCondition;
import com.quartetfs.fwk.impl.Pair;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.ExecuteJmxBeanMethod;
import com.quartetfs.pivot.anz.utils.RebuildCubeFromJmx;
import com.quartetfs.tech.indexer.IField;
import com.quartetfs.tech.indexer.IProcedure;
import com.quartetfs.tech.indexer.IReader;

public class CubeCleaner {

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);

	private IActivePivotManager activePivotManager;
	private String schemaName;
	private IActivePivotSchema schema;
	private int removalBatchSize;
	private boolean doRebuild;

	//jmx
	private String jmxUserName;
	private String jmxPassword;
	private String jmxHost;
	private String jmxPort;
	private String jmxBeanName;
	private String jmxMethodName;
	private String directoryToWatch;
	private Set<String> excludeKeys = new HashSet<String>(Arrays.asList(ANZConstants.COB,ANZConstants.CONTAINER_NAME));
	private long printBatchSize;
	private Map<String,Integer>varPsrIndex;


	private ConcurrentMap<String, DataBinding> binding= new ConcurrentHashMap<String, DataBinding>();
	private String[] idexerColumnsArray;
	private Set<String> idexerColumnsSet;
	private static final String EXPRESSION_SEPARATOR="/";
	private static final String TUPLE="tuple";
	private FastDateFormat dateFormat;

	public void setVarPsrIndex(Map<String, Integer> varPsrIndex) {
		this.varPsrIndex = varPsrIndex;
	}

	public void setActivePivotManager(IActivePivotManager activePivotManager) {
		this.activePivotManager = activePivotManager;
		if(null != schemaName)
			this.schema = activePivotManager.getSchemas().get(schemaName);
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
		if(null != activePivotManager)
			this.schema = activePivotManager.getSchemas().get(schemaName);
	}

	public void init(Properties props){
		this.removalBatchSize = Integer.parseInt(props.getProperty(ANZConstants.BATCH_SIZE_PROPS, ANZConstants.DEFAULT_BATCH_SIZE));
		this.doRebuild = Boolean.parseBoolean(props.getProperty(ANZConstants.REBUILD_PROPS, ANZConstants.DEFAULT_REBUILD));

		//jmx stuff
		this.jmxUserName   = props.getProperty(ANZConstants.JMX_USERNAME , null);
		this.jmxPassword   = props.getProperty(ANZConstants.JMX_PASSWORD , null);
		this.jmxHost       = props.getProperty(ANZConstants.JMX_HOST , null);
		this.jmxPort       = props.getProperty(ANZConstants.JMX_PORT ,null);
		this.jmxBeanName   = props.getProperty(ANZConstants.JMX_BEANNAME ,null);
		this.jmxMethodName = props.getProperty(ANZConstants.JMX_METHOD,null);
		printBatchSize=removalBatchSize;

		//init metamodel structures
		if (binding.isEmpty() || idexerColumnsArray == null || idexerColumnsSet == null){
			retrieveMetaModel();
		}

		this.dateFormat = FastDateFormat.getInstance("yyyyMMdd");
		this.directoryToWatch = props.getProperty("directoryToWatch",null);
	}

	public String getDirectoryToWatch() {
		return directoryToWatch;
	}

	public void removeByCondition(final IDate cobDate, final String fileName,Properties props,ConcurrentHashMap<String, ConcurrentHashMap<String, Date> > alreadyProcessedRemoveFiles){
		boolean isCleanupSucceeded=true;
		String strRebuild = null;
		int oldRemovalBatchSize = removalBatchSize;
		long recordDeleteCount=0;
		try{
			strRebuild = props.getProperty("rebuild",null);
			
			if(strRebuild==null){
				strRebuild = props.getProperty(ANZConstants.REBUILD_PROPS,null);
			}
			
			if(strRebuild!=null && strRebuild.equalsIgnoreCase("now")){
				doRebuild = true;
				isCleanupSucceeded = rebuildIndex(true,cobDate,props.getProperty("removeLimits","false"));
				doRebuild = false;
				return;
			} else if( strRebuild!=null && strRebuild.equalsIgnoreCase("true")){
				doRebuild = true;
			}
			String removalSize = props.getProperty("removalSize",null);

			if(removalSize!=null){
				try{
					removalBatchSize = Integer.parseInt(removalSize);
				} catch(NumberFormatException e){
					LOGGER.info(String.format("Removal batch size not valid number %s", removalSize));
				}catch(Exception e){
					LOGGER.info(String.format("Removal batch size not valid number %s", removalSize + e.getLocalizedMessage()));
				}
			}

			long countRemoved=0;
			StopWatch stopWatch = new StopWatch(String.format("Delete for %s", cobDate));
			stopWatch.start("Build Criteria");

			Map<Object, Object> values = buildCriteriaMap(props);
			//build the condition
			ICondition condition=buildCondition(cobDate, props.getProperty(ANZConstants.CONTAINER_NAME),values);

			LOGGER.info(String.format("Condition used for delete %s",props));
			stopWatch.stop();			
			stopWatch.start("Start Delete");
			//remove keys by removalBatchSize batch
			List<Map<String,Object>> facts=new ArrayList<Map<String,Object>>(removalBatchSize);


			do {
				schema.getTransactionManager().startTransaction();				
				//recycle
				facts.clear();

				String psrExt        =  props.getProperty(ANZConstants.PSR_NAME,null); 
				String containerName =  props.getProperty(ANZConstants.CONTAINER_NAME,null);
				/* Will do an update if container is equal to VAR */
				BatchProcedure batchProcedure = new BatchProcedure(removalBatchSize, facts, psrExt, containerName);	

				schema.getIndexer().execute(Collections.<IPair<ICondition, IProcedure<IProjection>>>singleton(new Pair<ICondition, IProcedure<IProjection>>(condition, batchProcedure)));
				int size=facts.size();
				countRemoved += size;

				/*doing update only on the row for VAR CONTAINER*/
				if( psrExt!=null && containerName!=null && containerName.equalsIgnoreCase(ANZConstants.VAR_CONTAINER) ){
					for (Map<String,Object> fact: facts){
						Object[] tuple = (Object[]) fact.get(TUPLE);

						tuple[this.varPsrIndex.get(psrExt.substring(0,5))]= null;//set the figure we want to remove to null

						//hard coded here as we know that expression is 2/string, so it's a map according to the transform(...) below,
						//this could be refactored in something cleaner, but I kept same logic as before the refactoring
						@SuppressWarnings("unchecked")
						Map<String,Object> m = (Map<String, Object>) tuple[2];
						
						if(psrExt.startsWith("V1")){
							m.put("psrExtA", ANZConstants.EMPTY_STRING);
						}else if(psrExt.startsWith("VX")){
							m.put("psrExtB", ANZConstants.EMPTY_STRING);
						} else if(psrExt.startsWith("B1")){
							m.put("psrExtH", ANZConstants.EMPTY_STRING);
						}
						//add the tuple in the transaction, we update the tuple withtout the removed VaR vector, 1D or 10D for instance
						schema.put(fact.get(IProjection.PROJECTION_OBJECT_KEY),tuple);
					}
				} else {
					Iterator<Map<String,Object>> iter = facts.iterator();
					List<Object> keysToRemove=new ArrayList<Object>(removalBatchSize);
					while (iter.hasNext()) {
						Map<String,Object> fact = iter.next();
						Object keys = fact.get(IProjection.PROJECTION_OBJECT_KEY);
						keysToRemove.add(keys);
					}
					//add the keys to be removed into the transaction
					schema.removeAll(keysToRemove);
				}
				//commit
				schema.getTransactionManager().commit();

				recordDeleteCount+=size;									
				if(recordDeleteCount%printBatchSize==0){
					LOGGER.info(String.format("%s records deleted.", recordDeleteCount));
					recordDeleteCount=0;
				}	
			  updateRemovalFileState(fileName,"LAST_EXEC_TIME",new Date(),alreadyProcessedRemoveFiles);

			} while (!facts.isEmpty());			

			if(recordDeleteCount > 0){
				LOGGER.info(String.format("%s records deleted.",recordDeleteCount));
			}
			stopWatch.stop();
			isCleanupSucceeded=countRemoved>0?true:false;



			if(isCleanupSucceeded){
				LOGGER.log(Level.INFO, MessagesANZ.CLEANUP_INFO, new Object[]{cobDate, countRemoved});
				LOGGER.info("Delete Time:" + stopWatch);
			}else{
				LOGGER.log(Level.INFO,"No data found for criteria");
			}
			//end remove
			rebuildIndex(isCleanupSucceeded,cobDate,props.getProperty("removeLimits","false"));
		} catch (Exception t) {
			recordDeleteCount = -1;
			t.printStackTrace();
			isCleanupSucceeded=false;
			LOGGER.log(Level.SEVERE,MessagesANZ.ERR_TRANSACTION,t);
			try {
				if (schema.getTransactionManager().isTransactionStarted()){
					schema.getTransactionManager().rollback();
				}
			} catch(Exception e) {
				LOGGER.log(Level.SEVERE,MessagesANZ.ROLLBACK_PROBLEM,e);
			}
		} finally{
			if(strRebuild!=null){ 
				doRebuild = false;
			}
			removalBatchSize = oldRemovalBatchSize;
			generateFile(fileName, isCleanupSucceeded,recordDeleteCount);
		}
	}

	public void removeByCondition( final String fileName,Properties props, ConcurrentHashMap<String, ConcurrentHashMap<String, Date> > alreadyProcessedRemoveFiles) throws Exception {
		
		boolean isCleanupSucceeded=true;
		
		int oldRemovalBatchSize = removalBatchSize;
		long recordDeleteCount=0;
		int countRemoved = 0;
		
		try{
		
			StopWatch stopWatch = new StopWatch(String.format("Delete for %s", "Finance PNL Removal"));
			stopWatch.start("Build Criteria");

			Map<Object, Object> values = buildCriteriaMap(props);
			//build the condition
			ICondition condition=buildCondition( props.getProperty(ANZConstants.CONTAINER_NAME),values);

			LOGGER.info(String.format("Condition used for delete %s",props));
			stopWatch.stop();			
			stopWatch.start("Start Delete");
			//remove keys by removalBatchSize batch
			List<Map<String,Object>> facts=new ArrayList<Map<String,Object>>(removalBatchSize);

			do {
				
				schema.getTransactionManager().startTransaction();				
				//recycle
				facts.clear();

				String psrExt        =  props.getProperty(ANZConstants.PSR_NAME,null); 
				String containerName =  props.getProperty(ANZConstants.CONTAINER_NAME,null);
				/* Will do an update if container is equal to VAR */
				BatchProcedure batchProcedure = new BatchProcedure(removalBatchSize, facts, psrExt, containerName);	

				schema.getIndexer().execute(Collections.<IPair<ICondition, IProcedure<IProjection>>>singleton(new Pair<ICondition, IProcedure<IProjection>>(condition, batchProcedure)));
				int size=facts.size();
				countRemoved += size;

					Iterator<Map<String,Object>> iter = facts.iterator();
					List<Object> keysToRemove=new ArrayList<Object>(removalBatchSize);
					while (iter.hasNext()) {
						Map<String,Object> fact = iter.next();
						Object keys = fact.get(IProjection.PROJECTION_OBJECT_KEY);
						keysToRemove.add(keys);
					}
					schema.removeAll(keysToRemove);
				
				//commit
				schema.getTransactionManager().commit();

				recordDeleteCount+=size;									
				if(recordDeleteCount%printBatchSize==0){
					LOGGER.info(String.format("%s records deleted.", recordDeleteCount));
					recordDeleteCount=0;
				}			
			    updateRemovalFileState(fileName,"LAST_EXEC_TIME",new Date(),alreadyProcessedRemoveFiles);	
			} while (!facts.isEmpty());			

			if(recordDeleteCount > 0){
				LOGGER.info(String.format("%s records deleted.",recordDeleteCount));
			}
			stopWatch.stop();
			isCleanupSucceeded=countRemoved>0?true:false;

			if(isCleanupSucceeded){
				LOGGER.log(Level.INFO, MessagesANZ.CLEANUP_INFO, new Object[]{ "Finance PNL removed:", countRemoved});
				LOGGER.info("Delete Time:" + stopWatch);
			}else{
				LOGGER.log(Level.INFO,"No data found for criteria");
			}
		
		} catch (Exception t) {
			recordDeleteCount = -1;
			t.printStackTrace();
			
			isCleanupSucceeded=false;
			LOGGER.log(Level.SEVERE,MessagesANZ.ERR_TRANSACTION,t);
			try {
				if (schema.getTransactionManager().isTransactionStarted()){
					schema.getTransactionManager().rollback();
				}
			} catch(Exception e) {
				LOGGER.log(Level.SEVERE,MessagesANZ.ROLLBACK_PROBLEM,e);
			}
			throw t;
		}finally{
			removalBatchSize = oldRemovalBatchSize;
			generateFile(fileName, isCleanupSucceeded,recordDeleteCount);
		}
	}
	
	
	private void retrieveMetaModel() {
		
		idexerColumnsSet = new LinkedHashSet<String>();
		idexerColumnsSet.add(IProjection.PROJECTION_OBJECT_KEY);
		idexerColumnsSet.addAll(schema.getClassifier().getIntputFields().keySet());

		this.idexerColumnsArray=idexerColumnsSet.toArray(new String[idexerColumnsSet.size()]);

		Map<String,IField> inputFields = schema.getClassifier().getIntputFields();
		for (Map.Entry<String, IField> inputField : inputFields.entrySet()){
			IProperty  underlyingProperty = inputField.getValue().getProperty();
			binding.put(inputField.getKey(), new DataBinding(underlyingProperty.getName(),underlyingProperty.getExpression().split(EXPRESSION_SEPARATOR)));
		}
		
	}

	private boolean rebuildIndex(boolean isCleanupSucceeded, IDate limitDate, String removeLimits) {
		
		if(removeLimits!=null&& removeLimits.equalsIgnoreCase("true")){
		  boolean removeLimit =  removeLimitDetailsByDate(dateFormat.format(limitDate.javaDate()));
		}
		
		//rebuild if needed
		if (isCleanupSucceeded && doRebuild){
			StopWatch stopWatch = new StopWatch(String.format("Rebuild Index"));
			stopWatch.start("Build Criteria");
			LOGGER.log(Level.INFO, MessagesANZ.START_REBUILD);
			boolean isRebuildSucceeded = executeJmxRebuild(); // rebuild the indexer;
			stopWatch.stop(); 
			if (!isRebuildSucceeded){
				LOGGER.log(Level.SEVERE, MessagesANZ.FAIL_REBUILD);
				return false;
			}else{
				LOGGER.log(Level.INFO, MessagesANZ.END_REBUILD, new Object[]{ (stopWatch)});
				return true;
			}
		}
			
		return false;
	}

	public boolean removeLimitDetailsByDate(IDate limitDate,String fileName,ConcurrentHashMap<String, ConcurrentHashMap<String, Date> > alreadyProcessedRemoveFiles){
		boolean removeLimit =  removeLimitDetailsByDate(dateFormat.format(limitDate.javaDate()));
			if(removeLimit){
				generateFile(fileName, true,1);
				 updateRemovalFileState(fileName,"LAST_EXEC_TIME",new Date(),alreadyProcessedRemoveFiles);
				 LOGGER.log(Level.SEVERE, "FAIL LIMITS DATA REMOVAL");
				 return true;
			}else{
				generateFile(fileName, false,-1);
				updateRemovalFileState(fileName,"LAST_EXEC_TIME",new Date(),alreadyProcessedRemoveFiles);
				LOGGER.log(Level.SEVERE, "FAIL LIMITS DATA REMOVAL");
				return false;
			}
	}
	
	
	public boolean rebuildIndex(String fileName,ConcurrentHashMap<String, ConcurrentHashMap<String, Date> > alreadyProcessedRemoveFiles) {
		
		
			StopWatch stopWatch = new StopWatch(String.format("Rebuild Index"));
			stopWatch.start("Build Criteria");
			
			LOGGER.log(Level.INFO, MessagesANZ.START_REBUILD);
			boolean isRebuildSucceeded = executeJmxRebuild(); // rebuild the indexer;
			
			stopWatch.stop(); 
			
			if (!isRebuildSucceeded){
				generateFile(fileName, false,-1);
				LOGGER.log(Level.SEVERE, MessagesANZ.FAIL_REBUILD);
				updateRemovalFileState(fileName,"LAST_EXEC_TIME",new Date(),alreadyProcessedRemoveFiles);	
				return false;
			}else{
				generateFile(fileName, true,1);
				LOGGER.log(Level.INFO, MessagesANZ.END_REBUILD, new Object[]{ (stopWatch)});
			   updateRemovalFileState(fileName,"LAST_EXEC_TIME",new Date(),alreadyProcessedRemoveFiles);	
				return true;
			}
		
	}

	private Map<Object, Object> buildCriteriaMap(Properties props){
		Map<Object,Object> values = new HashMap<Object,Object>();
		Set<Object> invalidFields = new HashSet<Object>();		
		for( Object key : props.keySet()){
			excludeKeys.add("removalSize");
			excludeKeys.add("rebuild");
			excludeKeys.add("removeLimits");
			

			if(excludeKeys.contains(key)) continue;
			if(idexerColumnsSet.contains(key)){
				values.put(key, props.get(key));
			}else{
				invalidFields.add(key);
			}
		}

		if(!invalidFields.isEmpty()){
			LOGGER.warning(String.format("Fields %s does't exists in schema. ", invalidFields));
		}
		return values;
	}

	private ICondition buildCondition(final IDate cobDate,final String containerNames,Map<Object,Object> values){
		//build a condition in order to iterate over all projection for the given  cob date and/or ConatinerNames

		List<ICondition> conditions = new ArrayList<ICondition>();

		conditions.add(new SubCondition(new SingleAccessor(ANZConstants.COBDATE_IDX_COL_NAME), new EqualCondition(cobDate)));

		if (containerNames!=null ){
			conditions.add(new SubCondition(new SingleAccessor(ANZConstants.CONTAINER_IDX_COL_NAME), new InCondition(containerNames)));			
		}

		for(Map.Entry<Object, Object> entry : values.entrySet()){  
			if(entry.getKey().toString().equalsIgnoreCase("removalSize")
					|| entry.getKey().toString().equalsIgnoreCase("rebuild")){
				continue;
			}
			/* handling scenario conatiner with 1 portfolio in 2 psr, except for var container which ignore the psrName*/
			if(entry.getKey().toString().equalsIgnoreCase(ANZConstants.PSR_NAME)) {
				if( containerNames!=null && !containerNames.equalsIgnoreCase(ANZConstants.VAR_CONTAINER)  ){
					conditions.add(new SubCondition(new SingleAccessor(entry.getKey().toString()), new InCondition(entry.getValue().toString())));
				} else{ /*containerName= VAR and PNL*/
					if( entry.getValue().toString().startsWith("V1") ){
						conditions.add(new SubCondition(new SingleAccessor(ANZConstants.PSR_NAME), new InCondition("1D")));
					} else if( entry.getValue().toString().startsWith("VX") ){
						conditions.add(new SubCondition(new SingleAccessor("psrExtB" ), new InCondition("10D")));
					} else if( entry.getValue().toString().startsWith("B1") ){
						conditions.add(new SubCondition(new SingleAccessor("psrExtH" ), new InCondition("B1")));
					} 
				}
			} else {
				conditions.add(new SubCondition(new SingleAccessor(entry.getKey().toString()), new InCondition(entry.getValue().toString())));	
			}
		}
		ICondition[] conds = new ICondition[conditions.size()];
		conditions.toArray(conds);

		return new AndCondition(conds);
	}

	private ICondition buildCondition(final String containerNames,Map<Object,Object> values){
		//build a condition in order to iterate over all projection for the given  cob date and/or ConatinerNames

		List<ICondition> conditions = new ArrayList<ICondition>();


		if (containerNames!=null ){
			conditions.add(new SubCondition(new SingleAccessor(ANZConstants.CONTAINER_IDX_COL_NAME), new InCondition(containerNames)));			
		}

		for(Map.Entry<Object, Object> entry : values.entrySet()){  
			if(entry.getKey().toString().equalsIgnoreCase("removalSize")
					|| entry.getKey().toString().equalsIgnoreCase("rebuild")){
				continue;
			}
				conditions.add(new SubCondition(new SingleAccessor(entry.getKey().toString()), new InCondition(entry.getValue().toString())));	
		}
		ICondition[] conds = new ICondition[conditions.size()];
		conditions.toArray(conds);

		return new AndCondition(conds);
	}

	public void generateFile(final String fileName, final boolean status, long count) {
	
		String suffix = status ? ANZConstants.FILE_EXT_SUCCESS : ANZConstants.FILE_EXT_FAILURE;
		// create new .DONE file if successfull else create ERROR file if zero deletion or encounter error 

		if(count == -1){
			suffix = ANZConstants.FILE_EXT_FAILURE;
		}else if(count == 0){
			suffix = ANZConstants.FILE_EMPTY;
		}else if(count>=1 ){
			suffix = ANZConstants.FILE_EXT_SUCCESS;
		}	
		StringBuilder newFileName = new StringBuilder();
					  newFileName.append(fileName).append(".").append(System.currentTimeMillis()).append(suffix);//avoid issue if we run twice

		File file = new File(newFileName.toString());

		try {
			//file.createNewFile();
			file = new File(fileName);

			if(status){
				file.renameTo( new File( fileName + "." + System.currentTimeMillis() + ANZConstants.FILE_EXT_SUCCESS ) );
				LOGGER.log(Level.INFO, "SUCCESS FILE CREATED:" + fileName + "." + System.currentTimeMillis() + ANZConstants.FILE_EXT_SUCCESS  );
				
			} else {
				if(count==0){
					file.renameTo( new File( fileName + "." + System.currentTimeMillis() +  ANZConstants.FILE_EMPTY ) );
					LOGGER.log(Level.INFO, "EMPTY FILE CREATED:" + fileName + "." + System.currentTimeMillis() + ANZConstants.FILE_EMPTY  );
				}else if(count==-1) {
					file.renameTo( new File( fileName + "." + System.currentTimeMillis() +  ANZConstants.FILE_EXT_FAILURE ) );
					LOGGER.log(Level.INFO, "ERROR FILE CREATED:" + fileName + "." + System.currentTimeMillis() + ANZConstants.FILE_EXT_FAILURE  );
				}else if(count == -2){
					file.renameTo( new File( fileName + "." + System.currentTimeMillis() +  ".ALP" ) );
					LOGGER.log(Level.INFO, "ALREADY PROCESSED FILE CREATED:" + fileName + "." + System.currentTimeMillis() + ".ALP"  );
				}
			
			}
		/*} catch (IOException e) {
			LOGGER.log(Level.SEVERE,MessagesANZ.FAIL_DONE_FILE_CREATION,e);*/
		} catch(Exception e){
			LOGGER.severe(e.getLocalizedMessage());
		}
		
		
	}

	private boolean executeJmxRebuild(){
		try {
			if(null != this.jmxBeanName || null != this.jmxMethodName){
				RebuildCubeFromJmx rebuildCubeFromJmx =new RebuildCubeFromJmx( this.jmxUserName, this.jmxPassword, this.jmxHost, this.jmxPort);
				rebuildCubeFromJmx.invoke( this.jmxBeanName , this.jmxMethodName);
				LOGGER.log(Level.INFO, MessagesANZ.JMX_REBUILD_SUCCESSFUL);
				return true;
			} else {
				LOGGER.log(Level.SEVERE, MessagesANZ.JMX_BEAN_METHOD_CANNOT_BE_NULL);
			} 
		} catch (IllegalStateException e){
			LOGGER.log(Level.SEVERE, MessagesANZ.JMX_OPERATION_PROBLEM, new Object[]{e.getCause().toString()});
		}
		return false;
	}
	
	private boolean removeLimitDetailsByDate(String strDate){
		String jmxBeanNameLimits   = "anz:name=LimitEventManager";
		String jxmMethodNameLimits = "removeLimitDetails";

		Object[] paramValue = { strDate };
		String[] paramName  = { "java.lang.String" }; 
		
		return executeJmxMethod(jmxBeanNameLimits,jxmMethodNameLimits,paramValue,paramName, "Limit details for:" + strDate + " was successfully removed.");
	}
	
	private boolean executeJmxMethod(String jmxBeanNameLimits, String jxmMethodNameLimits, Object[] params, String[] signature, String successMsg){
		
		
		try {
			LOGGER.log(Level.INFO,"Limits Remove BEAN Name:" + jmxBeanNameLimits );
			LOGGER.log(Level.INFO,"Limits Remove Method Name:" + jxmMethodNameLimits);
			
			if(null != jmxBeanNameLimits || null != jxmMethodNameLimits){
				
				ExecuteJmxBeanMethod jmxExec =new ExecuteJmxBeanMethod( this.jmxUserName, this.jmxPassword, this.jmxHost, this.jmxPort);
				jmxExec.invoke( jmxBeanNameLimits , jxmMethodNameLimits,params,signature ); 
				LOGGER.log(Level.INFO, successMsg );
				return true;
			} else {
				LOGGER.log(Level.SEVERE, MessagesANZ.JMX_BEAN_METHOD_CANNOT_BE_NULL);
			} 
		} catch (IllegalStateException e){
			LOGGER.log(Level.SEVERE, MessagesANZ.JMX_OPERATION_PROBLEM, new Object[]{e.getCause().toString()});
		}
		return false;
		
	}
	
	private synchronized void  updateRemovalFileState(String fileName,String status, Date dateTime,ConcurrentHashMap<String, ConcurrentHashMap<String, Date> > alreadyProcessedRemoveFiles){
		try{
			ConcurrentHashMap<String,Date> state =alreadyProcessedRemoveFiles.get( fileName);
			if( state == null ){
				 ConcurrentHashMap initState =  new ConcurrentHashMap();
				 				   initState.put(status, dateTime);
				 				   alreadyProcessedRemoveFiles.put(fileName, state);
			} else {
			    state.put(status, dateTime);
				alreadyProcessedRemoveFiles.put(fileName,  state);
			}
		}catch(Exception e){
			LOGGER.info(e.getLocalizedMessage());
		}
	}
	

	/**
	 * BatchProcedure in charge of retrieving the tuples from the indexer, runs as an indexer procedure
	 *
	 */
	private class BatchProcedure implements IProcedure<IProjection>{
		private static final long serialVersionUID = -7783482034930707471L;
		private int batchSize;
		//		private List<ConcurrentMap<String,Object>> facts;
		private List<Map<String,Object>> facts;
		private String psrExt;
		private String containerName;

		public BatchProcedure(int batchSize, List<Map<String,Object>> facts, String psrExt, String containerName){
			this.batchSize = batchSize;
			this.facts     = facts;
			this.psrExt    = psrExt;
			this.containerName = containerName;
		}
		@Override
		public boolean supportsParallelExecution() {
			return false;
		}

		@Override
		public boolean execute(IReader<IProjection> reader) {
			if( psrExt == null) {
				reader.setTuplePattern(new String[] {IProjection.PROJECTION_OBJECT_KEY});
			} else {
				if(containerName!=null && containerName.equals(ANZConstants.VAR_CONTAINER)){
					reader.setTuplePattern( idexerColumnsArray );   
				} else {
					reader.setTuplePattern(new String[] {IProjection.PROJECTION_OBJECT_KEY});
				}
			}

			while(reader.hasNext() && facts.size() < batchSize) {
				reader.next();
				Map<String,Object> fact = new HashMap<String,Object>();

				if(psrExt!=null  && containerName!=null &&  containerName.equals(ANZConstants.VAR_CONTAINER)){
					fact = transform(reader.readTuple());
				} else {
					//we're just interested by the objectKey
					Object[] tmp = new Object[1];
					reader.readTuple(tmp);
					fact.put(IProjection.PROJECTION_OBJECT_KEY, tmp[0]);
				}
				facts.add(fact);
			}
			return (facts.size() < batchSize);
		}

		@Override
		public void complete() {}

	}

	private static boolean isNumeric(String str)	{
		return str.matches("\\d+");
	}

	/**
	 * examples of patterns we may have in the schema
	 * 0 (start with int)
	 * 2/date (start with int then string)
	 * 2/currPairHierarchy/4 (int/string/int)
	 * 2/attributes/transactiondate (int/string/string)
	 * 
	 * !!! if you have a new pattern you have to implement it in this method
	 */
	@SuppressWarnings("unchecked")
	private Map<String,Object> transform(Object[] row){
		//the transformed object that will be sent to the schema in order to be introspected according to the available expressions
		Object[] transformedObject = new Object[ANZConstants.FACT_SIZE];
		Map<String,Object> objectToBeSubmitted = new HashMap<String,Object>();
		objectToBeSubmitted.put(IProjection.PROJECTION_OBJECT_KEY, row[0]);//row[0] is the object key
		objectToBeSubmitted.put(TUPLE, transformedObject);

		for(int i=1;i<idexerColumnsArray.length;i++){//start from 1 as 0 is for the key
			String fieldName = idexerColumnsArray[i];//get the field
			DataBinding dataBinding = binding.get(fieldName);//get the binding
			//System.out.println(dataBinding.toString() +" : "+row[i]);//trace
			String[] expression = dataBinding.getExpression();//get the expression

			//check that we're not having a not supported pattern
			if (expression.length>3 || !isNumeric(expression[0])){
				throw new IllegalStateException("Not supported expression:["+expression+"] fix the transform method in CubeCleaner class.");
			}

			//pattern (int)
			if (expression.length == 1 && isNumeric(expression[0])){
				transformedObject[Integer.parseInt(expression[0])] = row[i];
			}
			//pattern (int/string)
			else if (expression.length == 2 && !isNumeric(expression[1])){
				int rootOffset=Integer.parseInt(expression[0]);
				Map<String, Object> map = (Map<String, Object>) transformedObject[rootOffset];
				if (map==null){
					map=new HashMap<String,Object>();
					transformedObject[rootOffset]=map;
				}
				map.put(expression[1], row[i]);
			}
			//pattern  (int/string/string) or (int/string/int)
			else if (expression.length == 3 && !isNumeric(expression[1])){
				if (!isNumeric(expression[2])){//pattern  (int/string/string)
					int rootOffset=Integer.parseInt(expression[0]);
					Map<String,Map<String, Object>> firstMap = (Map<String,Map<String, Object>>) transformedObject[rootOffset];
					if (firstMap==null){
						firstMap=new HashMap<String,Map<String, Object>>();
						transformedObject[rootOffset]=firstMap;
					}

					Map<String, Object> secondMap= firstMap.get(expression[1]);
					if (secondMap == null){
						secondMap=new HashMap<String,Object>();
						firstMap.put(expression[1], secondMap);
					}
					secondMap.put(expression[2], row[i]);
				}else{//pattern  (int/string/int)
					int rootOffset=Integer.parseInt(expression[0]);
					Map<String,Object[]> map = (Map<String,Object[]>) transformedObject[rootOffset];
					if (map==null){
						map=new HashMap<String,Object[]>();
						transformedObject[rootOffset]=map;
					}

					Object[] array= map.get(expression[1]);
					if (array == null){
						array=new Object[20];//as the max length is 20 according to our schema definition
						map.put(expression[1], array);
					}
					array[Integer.parseInt(expression[2])]=row[i];
				}
			}
			//not supported cases
			else{
				throw new IllegalStateException("Not supported expression:["+expression+"] fix the transform method in CubeCleaner class.");
			}
		}
		return objectToBeSubmitted;
	}


	/**
	 * DataBinding holds the expression of each field
	 */
	private class DataBinding{
		private String[] expression;
		private String name;

		public DataBinding(String name, String[] expression){
			this.name=name;
			this.expression=expression;
		}
		@Override
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append(name).append(" - ").append(Arrays.toString(expression));
			return sb.toString();

		}
		public String[] getExpression() {
			return expression;
		}
	}

}
