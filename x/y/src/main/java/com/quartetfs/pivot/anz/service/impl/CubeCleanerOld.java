/*
 * (C) Quartet FS 2007-2011
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.util.StopWatch;

import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.IActivePivotSchema;
import com.quartetfs.biz.pivot.IProjection;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.IPair;
import com.quartetfs.fwk.filtering.ICondition;
import com.quartetfs.fwk.filtering.impl.AndCondition;
import com.quartetfs.fwk.filtering.impl.EqualCondition;
import com.quartetfs.fwk.filtering.impl.InCondition;
import com.quartetfs.fwk.filtering.impl.SingleAccessor;
import com.quartetfs.fwk.filtering.impl.SubCondition;
import com.quartetfs.fwk.impl.Pair;
import com.quartetfs.fwk.util.impl.MappedTuple;
import com.quartetfs.fwk.util.impl.MappedTupleFactory;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.model.impl.Deal;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.RebuildCubeFromJmx;
import com.quartetfs.tech.indexer.IField;
import com.quartetfs.tech.indexer.IProcedure;
import com.quartetfs.tech.indexer.IReader;

public class CubeCleanerOld {

	private IActivePivotManager activePivotManager;
	private String schemaName;
	private IActivePivotSchema schema;
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	private int removalBatchSize;
	private boolean doRebuild;

	private String jmxUserName;
	private String jmxPassword;
	private String jmxHost;
	private String jmxPort;
	private String jmxBeanName;
	private String jmxMethodName;
	private Set<String> schemaCols;
	private Set<String> excludeKeys = new HashSet<String>(Arrays.asList(ANZConstants.COB,ANZConstants.CONTAINER_NAME));
	private long printBatchSize;
	private String[] tuppleToRead;
	private Map<String,Integer>varPsrIndex;
	private Map<String,String>idexerTuple;
	
	
	public void init(Properties props){
		this.removalBatchSize = Integer.parseInt(props.getProperty(ANZConstants.BATCH_SIZE_PROPS, ANZConstants.DEFAULT_BATCH_SIZE));
		this.doRebuild = Boolean.parseBoolean(props.getProperty(ANZConstants.REBUILD_PROPS, ANZConstants.DEFAULT_REBUILD));
		//jmx stuff
		this.jmxUserName = props.getProperty(ANZConstants.JMX_USERNAME , null);
		this.jmxPassword = props.getProperty(ANZConstants.JMX_PASSWORD , null);
		this.jmxHost     = props.getProperty(ANZConstants.JMX_HOST , null);
		this.jmxPort      = props.getProperty(ANZConstants.JMX_PORT ,null);
		this.jmxBeanName  = props.getProperty(ANZConstants.JMX_BEANNAME ,null);
		this.jmxMethodName  = props.getProperty(ANZConstants.JMX_METHOD,null);
		printBatchSize=5*removalBatchSize;

	}
	
	private void loadSchemaCols()
	{
		if(schemaCols==null)
		{
			schemaCols = new HashSet<String>();			
			schemaCols.addAll(schema.getIndexer().getFields().keySet());
		}
	}
	
	public void removeByCondition(final IDate cobDate, final String fileName,Properties props){
		boolean isCleanupSucceeded=true;
		String strRebuild = null;
		int oldRemovalBatchSize = removalBatchSize;
		long recordDeleteCount=0;
		try{
			 strRebuild = props.getProperty("rebuild",null);
			 if(strRebuild!=null && strRebuild.equalsIgnoreCase("now")){
				 doRebuild = true;
				 isCleanupSucceeded = rebuildIndex(true);
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
			
			loadSchemaCols();
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
			List<ConcurrentMap<String,Object>> facts=new ArrayList<ConcurrentMap<String,Object>>(removalBatchSize);

			
			do
			{
				schema.getTransactionManager().startTransaction();				
				//recycle
				facts.clear();
				String psrExt        =  props.getProperty("psrName",null); 
				String containerName =  props.getProperty(ANZConstants.CONTAINER_NAME,null);
				/* Will do an update if container is equal to VAR */
				BatchProcedure batchProcedure = new BatchProcedure(removalBatchSize, facts, psrExt, tuppleToRead,containerName);	
				
				schema.getIndexer().execute(Collections.<IPair<ICondition, IProcedure<IProjection>>>singleton(new Pair<ICondition, IProcedure<IProjection>>(condition, batchProcedure)));
				int size=facts.size();
				countRemoved += size;
                 
				int ctr = 0;
				/*doing update only on the row for VAR CONTAINER*/
				if( psrExt!=null && containerName!=null && containerName.equalsIgnoreCase(ANZConstants.VAR_CONTAINER) ){
					Iterator<ConcurrentMap<String,Object>> iter = facts.iterator();
					while (iter.hasNext()) {
						ConcurrentMap<String,Object> fact = iter.next();	
						 Long key = (Long)fact.get("objectKey");	
						  Object[] tuple  = new Object[25];
                                 tuple[0] = fact.get("container");
                                 tuple[1] = fact.get("container");
						tuple[4] = fact.get("1DVaRPL_AUD");
						tuple[5] = fact.get("10DVaRPL_AUD");
						tuple[6] = fact.get("HypoPL_scenario_AUD");
						tuple[ this.varPsrIndex.get(psrExt.substring(0,5)) ] = null;
					
						Deal deal = createDeal(fact);
						
						deal.setPsrExtA( (String) fact.get("psrName")  );
						deal.setPsrExtB( (String) fact.get("psrExtB")  );
						deal.setPsrExtH( (String) fact.get("psrExtH")  );
						
						
						 if(psrExt!=null){
							 if(psrExt.startsWith("V1")){
								 deal.setPsrExtA("");
							 }else if(psrExt.startsWith("VX")){
								 deal.setPsrExtB("");
							 } else if(psrExt.startsWith("B1")){
								 deal.setPsrExtH("");
							 }
						 }
						
						tuple[ANZConstants.FACT_DEAL_INDEX] = deal;
						schema.put( key ,tuple);
						
					  
					}
				} else {
					Iterator< ConcurrentMap<String,Object>> iter = facts.iterator();
					List<Object> keysToRemove=new ArrayList<Object>(removalBatchSize);
					while (iter.hasNext()) {
						Map<String,Object> fact = iter.next();
						Object keys = fact.get("objectKey");
						keysToRemove.add(keys);
					
					}
					schema.removeAll(keysToRemove);
				}
				schema.getTransactionManager().commit();
				recordDeleteCount+=size;									
				if(recordDeleteCount%printBatchSize==0)
				{
					LOGGER.info(String.format("%s records deleted.", recordDeleteCount));
					recordDeleteCount=0;
				}			
				
			} while (!facts.isEmpty());			
			
			if(recordDeleteCount > 0)
			{
				LOGGER.info(String.format("%s records deleted.",recordDeleteCount));
			}
			stopWatch.stop();
			isCleanupSucceeded=countRemoved>0?true:false;
			
			
			
			if(isCleanupSucceeded)
			{
				LOGGER.log(Level.INFO, MessagesANZ.CLEANUP_INFO, new Object[]{cobDate, countRemoved});
				LOGGER.info("Delete Time:" + stopWatch);
			}
			else
			{
				LOGGER.log(Level.INFO,"No data found for criteria");
			}
			//end remove
			rebuildIndex(isCleanupSucceeded);
			

		} catch (Throwable t) {
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
		}finally{
			 if(strRebuild!=null){ 
				 doRebuild = false;
			 }
			 removalBatchSize = oldRemovalBatchSize;
			generateFile(fileName, isCleanupSucceeded,recordDeleteCount);
		}
	}

	private Deal createDeal(Map<String,Object> tuples){
		
	
		String portfolio   = (String) tuples.get("M_PTFOLIO");
		String baseCCY     = (String) tuples.get("M_PL_CUR");
		String hierarchy[] =  new String[] {
							  (String) tuples.get("Level20"),				              
							  (String) tuples.get("Level19"),
				              (String) tuples.get("Level18"),
				              (String) tuples.get("Level17"),
				              (String) tuples.get("Level16"),
				              (String) tuples.get("Level15"),
				              (String) tuples.get("Level14"),
				              (String) tuples.get("Level13"),
				              (String) tuples.get("Level12"),
				              (String) tuples.get("Level11"),
				              (String) tuples.get("Level10"),
				              (String) tuples.get("Level9"),
				              (String) tuples.get("Level8"),
				              (String) tuples.get("Level7"),
				              (String) tuples.get("Level6"),
				              (String) tuples.get("Level5"),
				              (String) tuples.get("Level4"),
				              (String) tuples.get("Level3"),
				              (String) tuples.get("Level2"),
				              (String) tuples.get("Level1")
				          	};
		
		String geoHierarchy[]  = new String[] {
	             (String) tuples.get("GeoL8"),
	             (String) tuples.get("GeoL7"),
	             (String) tuples.get("GeoL6"),
	             (String) tuples.get("GeoL5"),
	             (String) tuples.get("GeoL4"),
	             (String) tuples.get("GeoL3"),
	             (String) tuples.get("GeoL2"),
	             (String) tuples.get("GeoL1")
								};
		
		String currPairHierarchy[] = new String[] {
				 (String) tuples.get("CurrPairL5"),
				 (String) tuples.get("CurrPairL4"),
				 (String) tuples.get("CurrPairL3"),
				 (String) tuples.get("CurrPairL2"),
				 (String) tuples.get("CurrPairL1")
				
		};
		
		String currHierarchy[] = new String[] {
				 (String) tuples.get("CurrL5"),
				 (String) tuples.get("CurrL4"),
				 (String) tuples.get("CurrL3"),
				 (String) tuples.get("CurrL2"),
				 (String) tuples.get("CurrL1")
				
		};
		
		String equityHierarchy[] = new String[] {
				  new String(""),
				 (String) tuples.get("equityTier"),
				 (String) tuples.get("equityRegion"),
				 (String) tuples.get("equityType")
		};
		
		String currGrouping[] = new String[] {
				  new String(""),
				 (String) tuples.get("CurrencyFamily"),
				 (String) tuples.get("CurrencyGroup"),
				 (String) tuples.get("CurrencyMajMin"),
				 (String) tuples.get("CurrencyGlobalPrec"),
				 (String) tuples.get("CurrencyOnOff")
		};
		
		String family = (String) tuples.get("M_TRN_FMLY");
		String group  = (String)tuples.get("M_TRN_GRP");
		String type   = (String)tuples.get("M_TRN_TYPE");
		String mcurr = (String)tuples.get("M_CUR");
		String instrument = (String)tuples.get("M_INSTRUMENT");
		
		IDate date = (IDate)tuples.get("M_DATE");
		long dealNumber = (Long)tuples.get("M_DEALNUM");
	
		
		Object[] tuple = new Object[ idexerTuple.size()];
		List<String> mappedTupleFactoryKeys = new ArrayList<String>(idexerTuple.size());
		Map<String,Integer> propertyIndex = new HashMap<String,Integer>();
		int tCtr = 0;
		for (Map.Entry<String,String> tupMapping : idexerTuple.entrySet() ) {
			tuple[ tCtr] = tuples.get( tupMapping.getKey());
			propertyIndex.put(tupMapping.getValue(), tCtr);
			mappedTupleFactoryKeys.add(tupMapping.getValue());
			tCtr++;	
		}
		propertyIndex.put(null, tCtr);
		//FIXME refactor the whole class
//		MappedTuple mappedTuple = null;//new MappedTuple( tuple, propertyIndex );
		MappedTupleFactory mtf = new MappedTupleFactory(mappedTupleFactoryKeys);
		MappedTuple mappedTuple = mtf.create(tuple);
		
		
		Deal deal = new Deal(date, dealNumber,portfolio, baseCCY, hierarchy, geoHierarchy,
				mappedTuple);
		
		     deal.setFamily(family);
			 deal.setGroup(group);
			 deal.setType(type);
			 deal.setInstrument(instrument);
			 deal.setMcurr(mcurr);
			 deal.setCurrPairHierarchy(currPairHierarchy);
			 deal.setCurrencyHierarchy(currHierarchy);
			 deal.setEquityHierarchy(equityHierarchy);
			 deal.setCurrencyGrouping(currGrouping);
			 
		return deal;
		
	}
	
	
	
	private boolean rebuildIndex(boolean isCleanupSucceeded) {
		//rebuild if needed
		if (isCleanupSucceeded && doRebuild)
		{
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


	private Map<Object, Object> buildCriteriaMap(Properties props) 
	{
		Map<Object,Object> values = new HashMap<Object,Object>();
		Set<Object> invalidFields = new HashSet<Object>();		
		for( Object key : props.keySet())
		{
			excludeKeys.add("removalSize");
			excludeKeys.add("rebuild");
			
			if(excludeKeys.contains(key)) continue;
			if(schemaCols.contains(key))
			{
				values.put(key, props.get(key));
			}
			else
			{
				invalidFields.add(key);
			}
		}
		
		if(!invalidFields.isEmpty())
		{
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
		
		for(Map.Entry<Object, Object> entry : values.entrySet())
		{  
			if(entry.getKey().toString().equalsIgnoreCase("removalSize")
					|| entry.getKey().toString().equalsIgnoreCase("rebuild")){
				continue;
			}
			/* handling scenario conatiner with 1 portfolio in 2 psr, except for var container which ignore the psrName*/
			if(entry.getKey().toString().equalsIgnoreCase("psrName")) {
			   if( containerNames!=null && !containerNames.equalsIgnoreCase(ANZConstants.VAR_CONTAINER)  ){
				   conditions.add(new SubCondition(new SingleAccessor(entry.getKey().toString()), new InCondition(entry.getValue().toString())));
			   } else{ /*containerName= VAR and PNL*/
				   if( entry.getValue().toString().startsWith("V1") ){
					   conditions.add(new SubCondition(new SingleAccessor("psrName" ), new InCondition("1D")));
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

	private void generateFile(final String fileName, final boolean status, long count) {

		String suffix = status ? ANZConstants.FILE_EXT_SUCCESS : ANZConstants.FILE_EXT_FAILURE;
		// create new .DONE file if successfull else create ERROR file if zero deletion or encounter error 

	    if(count == -1){
	    	suffix = ANZConstants.FILE_EXT_FAILURE;
	    }else if(count == 0){
	    	suffix = ANZConstants.FILE_EMPTY;
	    }else if(count>=10 ){
	    	suffix = ANZConstants.FILE_EXT_SUCCESS;
	    }	
		StringBuilder newFileName = new StringBuilder();
		newFileName.append(fileName).append(".").append(System.currentTimeMillis()).append(suffix);//avoid issue if we run twice

		File file = new File(newFileName.toString());

		try {
			file.createNewFile();
			file = new File(fileName);
			
			LOGGER.log(Level.INFO, "Remove file rename FROM:" + fileName + " TO:" +  fileName +  ANZConstants.FILE_EXT_SUCCESS );
			if(status){
				file.renameTo( new File( fileName +  ANZConstants.FILE_EXT_SUCCESS ) );
				LOGGER.log(Level.INFO, MessagesANZ.SUCCESS_DONE_FILE_CREATION, new Object[]{fileName, newFileName.toString()});
			} else {
				if(count==0){
				   file.renameTo( new File( fileName +  ANZConstants.FILE_EMPTY ) );
				}else {
					file.renameTo( new File( fileName +  ANZConstants.FILE_EXT_FAILURE ) );	
				}
				LOGGER.log(Level.INFO, MessagesANZ.ERR_FILE_CREATED, new Object[]{fileName, newFileName.toString()});
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,MessagesANZ.FAIL_DONE_FILE_CREATION,e);
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

	private class BatchProcedure implements IProcedure<IProjection>{
		private static final long serialVersionUID = -7783482034930707471L;
		private int batchSize;
		private List<ConcurrentMap<String,Object>> facts;
		private String psrExt;
		private String containerName;
		
		public BatchProcedure(int batchSize, List<ConcurrentMap<String,Object>> facts, String psrExt, String[] tuppleToRead, String containerName){
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
			
			String[] tupleToRead =  null; 	
		   if( psrExt == null) {
			   reader.setTuplePattern(new String[] {IProjection.PROJECTION_OBJECT_KEY});
		   } else {
			   if(containerName!=null && containerName.equals(ANZConstants.VAR_CONTAINER)){
				   tupleToRead = new String[schema.getIndexer().getFields().size()];
				   Map<String,IField> fields = schema.getIndexer().getFields();
					int ctr = 0;
				    for ( Map.Entry<String, IField>field : fields.entrySet()) {
					        tupleToRead[ctr] = field.getKey(); 
							ctr++;
					}
				   reader.setTuplePattern( tupleToRead );   
			   } else {
				   reader.setTuplePattern(new String[] {IProjection.PROJECTION_OBJECT_KEY});
			   }
		   }
		   
		 	while(reader.hasNext() && facts.size() < batchSize) {
				reader.next();
				Object[] tmp = null;
			 ConcurrentMap<String,Object> fact = new 	 ConcurrentHashMap<String,Object>();
				  
				  if(psrExt!=null  && containerName!=null &&  containerName.equals(ANZConstants.VAR_CONTAINER)){
				      tmp = reader.readTuple();
				      for (int i = 0; i < tupleToRead.length; i++) {
				    	  if(tmp[i]!=null){
				    	    fact.put(tupleToRead[i], tmp[i]);
				    	  }
				    	  
					  }
				  } else {
					 //we're just interested by the objectKey
					   tmp = new Object[1];
					   reader.readTuple(tmp);
					   fact.put("objectKey", tmp[0]);
				   }
				   facts.add(fact);
			}
			return (facts.size() < batchSize);
		}

		@Override
		public void complete() {}

	}
	
	public static void main(String[] args){
		
		CubeCleanerOld c = new CubeCleanerOld();
	//	c.generateFile("C:\\Project\\text.txt", false);
	}

	public void setTuppleToRead(String[] tuppleToRead) {
		this.tuppleToRead = tuppleToRead;
	}

	public String[] getTuppleToRead() {
		return tuppleToRead;
	}

	public Map<String, Integer> getVarPsrIndex() {
		return varPsrIndex;
	}

	public void setVarPsrIndex(Map<String, Integer> varPsrIndex) {
		this.varPsrIndex = varPsrIndex;
	}

	public Map<String, String> getIdexerTuple() {
		return idexerTuple;
	}

	public void setIdexerTuple(Map<String, String> idexerTuple) {
		this.idexerTuple = idexerTuple;
	}
	
}
