package com.anz.rer.etl.mxHierarchy;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.anz.rer.etl.directory.IFileProcessor;
import com.anz.rer.etl.directory.IFileResolver;
import com.anz.rer.etl.utils.CsvUtils;


public class MxHierarchyImpl implements IFileProcessor {

	private final static Logger logger = Logger.getLogger(MxHierarchyImpl.class);
	private IFileResolver fileResolver;

	private int insertThreshold;
	private String delimeter;
	private String xmlFormatFile;
	private String tableName;
	private boolean loadPreviousDateData;
	private boolean skipFirstLine;
	private PlatformTransactionManager txManager;
	private JdbcTemplate jdbcTemplate;
	
	private int lastNodeAK;
	
	private String rootNodeAK = "-1";
	private String rootParentNodeAK = "1976";
	
	private List< Map<String, Object>> hierarchyFromDB;
	
	private Map<String,String> hierarchyTypeMap = new HashMap<String,String>();
	
	
    

	public MxHierarchyImpl() {
	}
    
	public MxHierarchyImpl(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	
	 
	public Document processUsingXML(String rootNodeAK, String rootParentNodeAK, 
		List<String[]> csvHierarchy, String keyDelimeter, boolean skipFirstLine,
		Map<String,Map<String,Object>> tableHierarchy , String hierarchyID ) {
		 boolean needInserts = false;
		 
		   Document document = DocumentHelper.createDocument();
		   List<String[]> hierarchyNotPresentInTable =  new ArrayList<String[]>();
		   Map<String,String[]> temp = new HashMap<String,String[]>();
		   for (String[] csv : csvHierarchy ) {
		    //	System.out.println(csv);
		    	for (int i = 0; i < csv.length -1; i++) {
		    		 String cKey = csv[0].trim();
					 if(i>=0){
						 cKey = this.mergeCsvArr(csv,keyDelimeter, 1); 
					 }
					if( tableHierarchy.get(cKey)!=null ){
					//	System.out.println(cKey);
						temp.put(cKey, csv);
					} else {
						temp.put(cKey, csv);
			    	//	System.out.println(cKey);
			    		needInserts = true;
			    	}
		    	}
		 	}
		   for(Map.Entry<String, String[]> cvs: temp.entrySet() ){
			   hierarchyNotPresentInTable.add(cvs.getValue() );   
		   }
		   
		    
		    long start = System.currentTimeMillis();
		    
		    String nodeAK =   rootNodeAK; 
		    String parentNodeAK = rootParentNodeAK;
		    int gNodeAK = 0;
		    if(needInserts){
		    for(String[] notPresent: hierarchyNotPresentInTable ) {
		    	   String xPath="";
		    	   String nodeNameAK="";
		    	   for (int i = 0; i < notPresent.length -1 ; i++) {
		    		     if(notPresent[0]!=null && notPresent[0].trim().length()>0) {
		    		       xPath= this.buildXmlFullPath(notPresent, i); 
		    			   	nodeNameAK = getNodeNameAK(notPresent,i);
		    			    Node parentNode = document.selectSingleNode(xPath);
		    			     
		    			    if(parentNode==null){
		    			    	
		    			    	gNodeAK = this.getNodeAK( notPresent, i, parentNode, gNodeAK, tableHierarchy,hierarchyID);
		    			    	nodeAK = String.valueOf(gNodeAK); 
		    			         parentNodeAK = this.getParentNodeAK(notPresent, i, parentNode, document, hierarchyID);
		    			         this.addElement(document, "LEVEL_" + (i), nodeNameAK, nodeAK, parentNodeAK, notPresent, i );
		    			    } else {
		    			   
		    			    }
		    		     }
		    	   }
		    	  // System.out.println(document.asXML());
		    }}
		    
		
		   long end = System.currentTimeMillis() - start;
		   logger.info( "total time:" + end );
		   return document;
		
	}
	
	private String buildXmlFullPath(String[] csv, int ctr){
	
		String xPath = "";
		for (int j = 0; j <= ctr; j++) {
			xPath+="LEVEL_" + (j)+ "[@ID='" + csv[j].trim() + "']/";
		}
        if(xPath.lastIndexOf("/")>-1){
		   xPath = "//" + xPath.substring(0, xPath.length() - 1) ; 
        }
		return xPath;
	}
	
	private String buildCsvPath(String[] csv, int ctr, String separator){
		String csvPath = ""; 
		for (int j = 0; j <= ctr; j++) {
			csvPath+= csv[j].trim() + separator;
		}
        if(csvPath.lastIndexOf(separator)>-1){
		   csvPath =  csvPath.substring(0, csvPath.length() - 1) ; 
        }
		return csvPath;
	}
	
	private Node getPreviousNode(String[] csv, int ctr,Document document){
				
		String previousXpath = this.buildXmlFullPath(csv, ctr-1); 
		Node previousNode = null;
	   	if(document!=null) {
	     previousNode = document.selectSingleNode(previousXpath);
	   	} 
	    return previousNode;
	}

	private String getNodeNameAKwithROOT(String csv[],int ctr){
		String nodeNameAK ="";
		if(ctr==0){
   			nodeNameAK = "ROOT";
   		}else{
   			nodeNameAK = csv[ctr-1].trim();	
   		}
		return nodeNameAK;
	}
	
	private String getNodeNameAK(String csv[],int ctr){
		String nodeNameAK ="";
		nodeNameAK = csv[ctr].trim();	
		return nodeNameAK;
	}  
	
	private String getPreviousNodeID(String csv[], int ctr){
		return this.getNodeNameAK(csv, ctr -1 );
	}
	
		
	public List<Map<String, Object>> getHierarchyFromDB() {
		return hierarchyFromDB;
	}

	public void setHierarchyFromDB( List< Map<String, Object>>  hierarchyFromDB) {
		this.hierarchyFromDB = hierarchyFromDB;
	}

	private List< Map<String, Object>> getHierarchyFromDB( String sql) {
 		  hierarchyFromDB = this.executeSql(sql);
	  	  return hierarchyFromDB;
	}
	
	
	
	
	public Map<String,Map<String,Object>> getMapHierarchyFromTable( String sql, String comKey){
		List< Map<String, Object>> tableHierarchy = getHierarchyFromDB( sql );
		Map<String, Map<String,Object>> tHierarchy = new HashMap<String,Map<String,Object>>();
		
		for (Map<String, Object> map : tableHierarchy) {
		    String uniqueKey = ((String)map.get( comKey )).trim();
		    tHierarchy.put(uniqueKey, map);	
			
		}
		return tHierarchy;
	}
	 
	private Map<String,Map<String,Object>> executeSqlMap( String sql, String[] key, String keyDelimeter){
		
		List< Map<String, Object>> tableHierarchy = this.executeSql( sql );
		
		
		Map<String, Map<String,Object>> tHierarchy = new HashMap<String,Map<String,Object>>();
		
		for (Map<String, Object> map : tableHierarchy) {
			String uniqueKey =  "";
			for (int i = 0; i < key.length; i++) {
				uniqueKey+=((String)map.get(key[i])).trim() + keyDelimeter;
			}
			uniqueKey = uniqueKey.substring(0, uniqueKey.length() -1);
		    tHierarchy.put(uniqueKey, map);	
		}
		return tHierarchy;
	}
	
	public void setLastNodeAK(int lastNodeAK) {
		this.lastNodeAK = lastNodeAK;
	}

	private int getLastNodeAK(){
	   // add a check in NodeAK already Exist in the Table
		return lastNodeAK;
	}
	public void insertBatchSQL(final String[] sql){
		for (int i = 0; i < sql.length; i++) {
			logger.info(sql[i]);
		}
	   this.jdbcTemplate.batchUpdate(sql);
	}
	
	private int getNodeAK(String csv[],int ctr,Node node,
			int nodeAK,Map<String,Map<String,Object>> tableHierarchy,String hierachyID ){
	
		 String cKey = csv[0].trim();
		 if(ctr>0){
			 cKey = this.concatCsv(csv,"|", ctr);
		 }
		 
		 HierarchyInfo hInfo =  this.getHierarchyInfo( tableHierarchy, cKey, hierachyID) ;
	     if( hInfo!=null ){ 
	    	 return hInfo.getNodeAK();
	     }else {
	    	 lastNodeAK--;
		  return  lastNodeAK;
	   }

	
	}
	
	private String getParentNodeAK(String csv[],int ctr,Node node, Document document,String hierachyID ){
	
		String parentNodeAK = "";

		if(node!=null) {
			 parentNodeAK = node.valueOf("@nodeAK") ;
		 } else {
			
			 String key = this.buildCsvPath(csv, ctr, "|");
			
			 HierarchyInfo hInfo =  this.getHierarchyInfo(key,hierachyID);
			
			 
			if( hInfo!=null )  {
				parentNodeAK = String.valueOf( hInfo.getParentNodeAK() );
			} else {
				 if(ctr>0){
					 
					 Node previousNode = this.getPreviousNode(csv, ctr,document);
					 if(previousNode!=null){
						 parentNodeAK = previousNode.valueOf("@nodeAK");
					 }else {
						 lastNodeAK--;
						  return  String.valueOf(lastNodeAK);
						
					 }
				 } else {
					   parentNodeAK = "" ;
				 }
			}	 
		 }
		return parentNodeAK;
	}

	
	
	private Map<String,Map<String,Object>>  mapHierarchyInfo;
	
	
	public Map<String, Map<String, Object>> getMapHierarchyInfo() {
		return mapHierarchyInfo;
	}

	public void setMapHierarchyInfo(
			Map<String, Map<String, Object>> mapHierarchyInfo) {
		this.mapHierarchyInfo = mapHierarchyInfo;
	}

	public Map<String, Map<String, Object>> getMapHierarchyInfo(String sql, String comKey) {
		  mapHierarchyInfo  = getMapHierarchyFromTable(sql,comKey);
		
		return mapHierarchyInfo;
	}

	private HierarchyInfo getHierarchyInfo( Map<String, Map<String, Object>> hierarchyInfos, String key, String hierachyID )  {
		if(hierarchyInfos!= null ){
			
			Map<String, Object> record = hierarchyInfos.get(key);

	  	if( record  != null) {
			return new HierarchyInfo( Integer.valueOf( (Integer) record.get("NodeAK") ), 
					Integer.valueOf( (Integer) record.get("ParentNodeAK") ),
			       (String)record.get("NodeName"),hierachyID  );
	  	  }  
	  	  return null;       
		} else {
			return null;
		}
	}

	private HierarchyInfo getHierarchyInfo( String key,String hierachyID )  {
		if(getMapHierarchyInfo()!= null ){
			Map<String, Object> record = getMapHierarchyInfo().get(key);

	  	if( record  != null) {
			return new HierarchyInfo(Integer.valueOf( (Integer)record.get("NodeAK") ),
					Integer.valueOf( (Integer)record.get("ParentNodeAK") ), (String)record.get("NodeName"), hierachyID  );
	  	  }  
	  	  return null;       
		} else {
			return null;
		}
	}
	
	
	private String mergeCsvArr(String[] a, String separator, int exclude) {
		if (a == null || separator == null) {
	        return null;
	    }
	    StringBuffer result = new StringBuffer();
	    if (a.length > 0) {
	        result.append(a[0].toUpperCase().trim() );
	        for (int i=1; i < a.length - exclude; i++) {
	            result.append(separator);
	            result.append(a[i].toUpperCase().trim());
	        }
	    }
	    return result.toString();
	  }

	private String concatCsv(String[] a, String separator, int include) {
		if (a == null || separator == null) {
	        return null;
	    }
	    StringBuffer result = new StringBuffer();
	    if (a.length > 0) {
	        result.append(a[0].toUpperCase().trim() );
	        for (int i=1; i < include+1; i++) {
	            result.append(separator);
	            result.append(a[i].toUpperCase().trim());
	        }
	    }
	    return result.toString();
	  }
	
private void addElement(Document doc, String elementName, String nodeNameAK, String nodeAK, 
		String parentNodeAK, String[] csv, int ctr){
	
	 Element element = null;
	 if( doc.getRootElement()==null) {
		 doc.addElement(elementName )
	 		.addAttribute("nodeAK",  nodeAK)
	 		.addAttribute("parentNodeAK", parentNodeAK)
	 		.addAttribute("ID", nodeNameAK.trim());
	 } else {
		 
		  
		 String previousXpath = this.buildXmlFullPath(csv, ctr-1); 
			Node previousNode = null;
		   	if(doc!=null) {
		     previousNode = doc.selectSingleNode(previousXpath);
		     element = (Element) previousNode;
		   	} 
		 
		//  element = doc.elementByID(this.getPreviousNodeID(csv, ctr));
		  
		  
		  element.addElement(elementName)
	 		.addAttribute("nodeAK",  nodeAK)
	 		.addAttribute("parentNodeAK", parentNodeAK)
	 		.addAttribute("ID", nodeNameAK.trim() );
	 }
	}

public void processUsingMap(String rootNodeAK, String rootParentNodeAK, 
		List<String[]> csvInput, String delimeter,  
		Map<String,Map<String,Object>> tableHierarchy,String hierachyID  ){
	 
	  Map<String,HierarchyInfo> mapNotPresent	= new HashMap<String,HierarchyInfo>();  
	  
	 for (String[] csv : csvInput) {
		 String cKey = this.mergeCsvArr(csv,"|", 1);
	     HierarchyInfo hInfo =  this.getHierarchyInfo( tableHierarchy, cKey, hierachyID) ;
		   
	        if( hInfo !=null ){
	    //	  logger.info("already present:" + cKey ) ;
	    	} else {
	    		mapNotPresent.put(cKey, hInfo);
	    	}
	}
	 
	// logger.info("map insert:" + this.generateScriptStatementFromMap( mapNotPresent  ));
	 
}


private Map<String, HierarchyInfo>buildHierarchy(List<String[]> csvInput, Document document, String hierarchyID){
	
	Map<String, HierarchyInfo> hierarchyMap = new HashMap<String,HierarchyInfo>();
	
		 
	for (String[] csv : csvInput) {
		for (int i = 0; i < csv.length; i++) {
			
			String fullPath = this.buildCsvPath(csv, i, "|");
		   	int nodeAK = this.getLastNodeAK();
		   	String parentNodeAK = this.getParentNodeAK(csv, i, null, document, hierarchyID);
		   	String nodeNameAK = this.getNodeNameAK(csv, i);
		   	
			HierarchyInfo hInfo = new HierarchyInfo( nodeAK, Integer.valueOf( parentNodeAK), nodeNameAK, hierarchyID );
			 
			hierarchyMap.put(fullPath, hInfo);
			
		} 
		
	}
	return hierarchyMap;
	
}
 private String generateScriptStatement(Document xml, String hierarchyID){
	 StringBuilder insert = new StringBuilder();
	 Node root = xml.getRootElement();
	  
	//insert.append( this.generateScriptStatement( this.getHierarchyInfo(root)  ) );
	// TODO create this inserts
	// insert into Staging.HierarchyUpdateByUI values( 9,getUtcDate(),0 );
	insert.append(parseDocumentGenrateInserStnmt( xml , insert.toString(), hierarchyID));
	return insert.toString();
	
	
 }
 
 private HierarchyInfo getHierarchyInfo(Node node,String hierachyID ){
	 if(node!=null){
		 return new HierarchyInfo(   Integer.valueOf( node.valueOf("@nodeAK") ), 
				 Integer.valueOf( node.valueOf("@parentNodeAK") ), node.valueOf("@ID"),hierachyID );
	 }
	 return null;
 }
 
 public String parseDocumentGenrateInserStnmt(Document document, String insertStmnt, String hierarchyID) {
	 return tryThis(document, hierarchyID);
	 
 }

 public String tryThis(Document document, String hierarchyID){
	 
	  List<Node> nodeList = document.getRootElement().selectNodes("//*");
	  StringBuilder insertXML = new StringBuilder();
	   for (Node node : nodeList) {
		   if (node.getNodeType() == Node.ELEMENT_NODE) {
	           insertXML.append( this.generateScriptStatement(  this.getHierarchyInfo(node,hierarchyID)) );
	        }
	  }
	   return insertXML.toString();
 }
 
 
 public String generateInsertScriptFormXml(Element element, String insXml, String hierarchyID) {
	 
	 StringBuilder insertXml = new StringBuilder(insXml);   
     for ( int i = 0, size = element.nodeCount(); i < size; i++ ) { 
         Node node = element.node(i);
         
         if ( node instanceof Element ) {
        	 Node hInfo = (Element) node;
        	 insertXml.append( this.generateScriptStatement(  this.getHierarchyInfo(hInfo,hierarchyID) ) );
         } else {
            
         }
     }
     return insertXml.toString();
 }
 
 
 private  Map<String,Map<String,Object>> getLeafMapTable( String hierarchyID){
	 String leafSQL = " SELECT * FROM DW.DimPortfolio " ;
	 		           
	 
	 Map<String,Map<String,Object>> leafTable = this.executeSqlMap(leafSQL,new String[]{"Name"},"|"); //"LeafFullPath",
	 return leafTable;
 }

 private  Map<String,Map<String,Object>> getLeafMapTableOld( String hierarchyID){
	 String leafSQL = " SELECT ParentNode,ChildNode,ParentNodeID,ChildNodeID, replace(LeafFullPath,'ROOT|','') LeafFullPath, IsActive" +
	 		          " FROM DW.udf_GetLatestHierarchyLeafMap("+ hierarchyID +") "; 
	 
	 Map<String,Map<String,Object>> leafTable = this.executeSqlMap(leafSQL,new String[]{"ChildNode"},"|"); //"LeafFullPath",
	 return leafTable;
 }

 private void processLeafNode(List<String[]> csvHierarchy, String keyDelimeter, String hierarchyID,Map<String,Map<String,Object>> leafTable ){
	 List<String[]> leafTobeUpdated =  new ArrayList<String[]>();
	 Map<String,String[]> leafNotPresentMap =  new HashMap< String, String[]>(); 
	 StringBuffer selectLeafId = new StringBuffer();
	 for (String[] csv : csvHierarchy ) {
	    	for (int i = (csv.length - 1); i >= csv.length - 1;i--) {
	    		  
	    		 String cKey = csv[i].trim();
				if( leafTable.get(cKey)!=null ){
					
					String fullPath = this.mergeCsvArr(csv,keyDelimeter, 1);
					String leafFullPath = (String) leafTable.get(cKey).get("LeafFullPath"); 
				//	if(leafFullPath.equals(fullPath)) {
						leafTobeUpdated.add(csv); // need to be updated	
						selectLeafId.append("'" + cKey + "'," );
					//}else {
						//leafTobeUpdated.add(csv); // need to be updated	
						//selectLeafId.append("'" + cKey + "'," );
					//}
					
		    	} else {
		    		    leafNotPresentMap.put(csv[i].trim(), csv); // need to be inserted
		    			selectLeafId.append("'" + cKey + "'," );
		    			leafTobeUpdated.add(csv); // need to be updated
		    	}
	    	}
	 	}
	 // StringBuffer insertLeafx   = new StringBuffer();
	 
	  
	  for (Map.Entry<String,String[]> newLeaf : leafNotPresentMap.entrySet()) {
		  String creaPortFolio ="{call [DW].[CreatePortfolio]('" +newLeaf.getKey()+ "', 1," + this.getCurrentUserId() +  ")}";
		  logger.info(creaPortFolio);
  		  jdbcTemplate.execute(creaPortFolio);
	  }
	  Map<String,Map<String,Object>> leafPortFolioTable = null;
	  if(selectLeafId.length()>0){
		  selectLeafId = new StringBuffer(selectLeafId.toString().substring(0,selectLeafId.toString().length() -1));
		  String leafGetleafID = " select * from DW.DimPortfolio where Name in (" +  selectLeafId.toString() + ")";
		  logger.info( leafGetleafID );
		  this.getUpdateLatestHierarchy(hierarchyID);
		  leafPortFolioTable = this.getMapHierarchyFromTable(leafGetleafID,"Name");
	//	  logger.info( leafPortFolioTable.size() );
		  
	  } 
	  
	  
	  for (String[] csv : leafTobeUpdated) {
		  String leafName = csv[csv.length - 1].trim();
		  //logger.info( leafName + "x");
		  leafName = String.valueOf( (Integer) leafTable.get(leafName).get("ID")); //leafPortFolioTable.get(leafName).get("ID"));  
		  String key = this.buildCsvPath(csv,(csv.length-2), "|");
		  HierarchyInfo hInfo =  this.getHierarchyInfo(key,hierarchyID);
		  if(hInfo!=null){
			  String sProc = "{ call [DW].[UpdateHierarchyMap] (9,'" + hInfo.getNodeAK() + "','" + leafName + "','" + getCurrentDate() + "'," + this.getCurrentUserId() +  ")}";
			  		  logger.info(sProc);
			  		  jdbcTemplate.execute(sProc);
			  		
		  }
	  }
	  
	  logger.info("Done portfolio leaf processing");
	  
	 
	 
 }
 
 private String getCurrentUserId(){

    final String storedProc = "{ call sp_who2 }";
	 
	String sid =  jdbcTemplate.query(new PreparedStatementCreator() {
	     
		public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
	        return connection.prepareStatement(storedProc);
	      }
	    }, new PreparedStatementSetter() {
	    	public void setValues(PreparedStatement preparedStatement) throws SQLException {
	    	
	    	}
	    }, new ResultSetExtractor<String>() {

	  public String extractData(ResultSet rs) throws SQLException, DataAccessException {
	    	String uId = null;
	    	  while(rs.next()) {
	    		  uId = rs.getString("SPID");
	    		  
				}
	           return uId;
	      }
	    });	
	return sid;
	  
 }
 
 
 private  java.sql.Timestamp getCurrentDate() {
		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());  
		
	}
 
 private String generateScriptStatementFromListMap(List<Map<String,HierarchyInfo>> mapHierarchy){
	 StringBuilder insert = new StringBuilder();
	 
	 for (Map<String, HierarchyInfo> mapHInfos : mapHierarchy)  {
		 for (Map.Entry<String, HierarchyInfo> mapHInfo : mapHInfos.entrySet()) {
		      insert.append( this.generateScriptStatement( mapHInfo.getValue() ));
		 }
	 } 
	 return insert.toString();
 }
 
 private String generateScriptStatementFromMap(Map<String,HierarchyInfo> mapHierarchy){
	 StringBuilder insert = new StringBuilder();
	 
	 	 for (Map.Entry<String, HierarchyInfo> mapHInfo : mapHierarchy.entrySet()) {
		      insert.append( this.generateScriptStatement( mapHInfo.getValue() ));
		 }
	 return insert.toString();
 }
  
 private String generateScriptStatement(HierarchyInfo hInfo){
	 StringBuilder insert = new StringBuilder();
	 insert.append("INSERT INTO Staging.HierarchyDetailUpdateByUI (HierarchyTypeID, ChildNodeAK,ChildNode, ParentNodeAK)" );
	 insert.append("VALUES(" + hInfo.getHierarchyId() +  ",'" + hInfo.getNodeAK() + "','" + hInfo.getNodeName()+"','" + hInfo.getParentNodeAK() + "');");
	 return insert.toString();
	 
 }
 

 @SuppressWarnings("unused")
 private class HierarchyInfo {
		
		
		public HierarchyInfo(int nodeAK, int parentNodeAK, String nodeName, String hierarchyId) {
		super();
		this.nodeAK = nodeAK;
		this.parentNodeAK = parentNodeAK;
		this.nodeName = nodeName;
		this.hierarchyId = hierarchyId;
		
	}
		private int    nodeAK;
		private int    parentNodeAK;
		private String nodeName;
		private String hierarchyId;
		
	
		public String getHierarchyId() {
			return hierarchyId;
		}
		public void setHierarchyId(String hierarchyId) {
			this.hierarchyId = hierarchyId;
		}
		public int getNodeAK() {
			return nodeAK;
		}
		public void setNodeAK(int nodeAK) {
			this.nodeAK = nodeAK;
		}
		public int getParentNodeAK() {
			return parentNodeAK;
		}
		public void setParentNodeAK(int parentNodeAK) {
			this.parentNodeAK = parentNodeAK;
		}
		public String getNodeName() {
			return nodeName;
		}
		public void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}
		
	}



@Override
public boolean doProcess() {
   boolean retval = true;
   return retval;
   
	
}

@Override
public boolean postProcess() {
	boolean retval = true;
	   return retval;
	
}

@Override
public boolean preProcess() {
	  boolean retval = false;
	DefaultTransactionDefinition def = new DefaultTransactionDefinition();
	def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
	TransactionStatus status = txManager.getTransaction(def);
	
	try {
	
		/*TODO the hierarchyID can be determine from the header, if the header will become consistent
		 * from different hierachy source file
		 * */
	String hierarchyID = hierarchyTypeMap.get("LEGAL_ENTITY");

	List<Map<String,Object>> rootParentNodeAKdb =  getRootParentNodeAKdb( hierarchyID );
	
	Map<String,Map<String,Object>> leafTable = getLeafMapTable(hierarchyID);
	
	
	if(rootParentNodeAKdb!=null){
 		  rootParentNodeAK = String.valueOf(rootParentNodeAKdb.get(0).get("DimHierarchyNodeID"));
 		
 	  Map<String,Map<String,Object>> tableHierarchy = getUpdateLatestHierarchy( hierarchyID );
	  String csvFileName = this.fileResolver.getSrcFileName();
	  String delimeter = ";";
	  boolean skipFirstLine = true;
	  
	  List<String[]> csvHierarchy = this.loadCsvFile(csvFileName, delimeter,skipFirstLine);
	  String keyDelimeter = "|";
	   
	 Document doc =	 this.processUsingXML( "-1", rootParentNodeAK ,csvHierarchy,keyDelimeter,skipFirstLine,tableHierarchy, hierarchyID);
	 
	 if(doc!=null && doc.getRootElement()!=null){
		 String insertHeirarchyUpdateByUI = "INSERT INTO Staging.HierarchyUpdateByUI values(" + hierarchyID + ",getUtcDate()," + this.getCurrentUserId() +  ") ";
		 this.insertBatchSQL( new String[]{insertHeirarchyUpdateByUI});
         String insertsHierarchyDetailUpdateByUI = this.generateScriptStatement(doc, hierarchyID);
		 String inserts[] = insertsHierarchyDetailUpdateByUI.split(";");
		  this.insertBatchSQL(inserts);
		  String loadHierarchy =  "{call [DW].[LoadHierarchy] ("+hierarchyID+")}";
		  logger.info( loadHierarchy );
		  
		  jdbcTemplate.execute(loadHierarchy);
		  
		
	 }
	        processLeafNode(csvHierarchy, keyDelimeter,hierarchyID, leafTable );
    	 
		} else {
		}
		txManager.commit(status);
		retval = true;
		logger.info("loading hierarchy done........");
	}
	catch (Exception ex) {
		ex.printStackTrace();
	    txManager.rollback(status);
	    retval = false;
	}
	return retval;

}

private List<Map<String,Object>> getRootParentNodeAKdb( String hirarchyNodeType){
	String rootParentNodeAKSql = "Select * from dw.dimhierarchynode" +
								 "  where dimhierarchynodename = 'ROOT' and HierarchyTypeID = " + hirarchyNodeType;
	return  this.executeSql(rootParentNodeAKSql);

}
private Map<String,Map<String,Object>> getUpdateLatestHierarchy(String hierarchyTypeId){
	
	  String sql = "SELECT ParentNodeName,NodeName,ParentNodeAK,NodeAK,DimHierarchyTypeVersionID, "  +
      " replace(FullPath,'ROOT|','') as FullPath FROM [DW].[udf_GetLatestHierarchy] (" + hierarchyTypeId + ")" +
		   " order by ParentNodeAK ";

	  Map<String,Map<String,Object>> tableHierarchy = this.getMapHierarchyFromTable(sql,"FullPath");
	  this.setMapHierarchyInfo(tableHierarchy);
	  return tableHierarchy;
	  
}


@Override
public boolean validate(File fileName) { 
	fileResolver.resolveSrcFileName();
	return fileResolver.validateFileName(fileName);
}
@SuppressWarnings("rawtypes")
private  List<Map<String,Object>> executeSql(String sql){
	
	SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
    List<Map<String,Object>> records = new ArrayList<Map<String,Object>>();
	 
	SqlRowSetMetaData rsMetaData = rs.getMetaData();
	
	while(rs.next()){
		Map<String,Object> record = new HashMap<String,Object>();
	    int numberOfColumns = rsMetaData.getColumnCount();
	   
	    for (int i = 1; i <= numberOfColumns; i++) {
	      record.put(rsMetaData.getColumnName(i), rs.getObject(i) );
	    }
	  records.add(record);
	}
	
	return records;
}

public String getDelimeter() {
	return delimeter;
}

public void setDelimeter(String delimeter) {
	this.delimeter = delimeter;
}

public int getInsertThreshold() {
	return insertThreshold;
}

public void setInsertThreshold(int insertThreshold) {
	this.insertThreshold = insertThreshold;
}



public void setTableName(String tableName) {
	this.tableName = tableName;
}

public String getTableName() {
	return tableName;
}

public void setXmlFormatFile(String xmlFormatFile) {
	this.xmlFormatFile = xmlFormatFile;
}

public String getXmlFormatFile() {
	return xmlFormatFile;
}

public boolean isLoadPreviousDateData() {
	return loadPreviousDateData;
}

public void setLoadPreviousDateData(boolean loadPreviousDateData) {
	this.loadPreviousDateData = loadPreviousDateData;
}

public void setFileResolver(IFileResolver fileResolver) {
	this.fileResolver = fileResolver;
}

public IFileResolver getFileResolver() {
	return fileResolver;
}

public PlatformTransactionManager getTxManager() {
	return txManager;
}

public void setTxManager(PlatformTransactionManager txManager) {
	this.txManager = txManager;
}


public boolean isSkipFirstLine() {
	return skipFirstLine;
}

public void setSkipFirstLine(boolean skipFirstLine) {
	this.skipFirstLine = skipFirstLine;
}

private void doTransaction(){
	
	DefaultTransactionDefinition def = new DefaultTransactionDefinition();
	def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

	TransactionStatus status = txManager.getTransaction(def);
	try {
	    // execute your business logic here
	}
	catch (Exception ex) {
	    txManager.rollback(status);
	    
	}
	txManager.commit(status);
	
}

public List<String[]>  loadCsvFile (String csvFileName, String delimeter, boolean skipFirstLine) {
	//return CsvUtils.loadCsv(csvFileName,  delimeter, skipFirstLine);
	return null;
}

  
public String executeStoredProcedure(final String storedProc, final String param){
	
	 String whatToWatch =  jdbcTemplate.query(new PreparedStatementCreator() {
	     public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
	        return connection.prepareStatement(storedProc);
	      }
	    }, new PreparedStatementSetter() {
	    	public void setValues(PreparedStatement preparedStatement) throws SQLException {
	    		preparedStatement.setInt(1, Integer.valueOf(param));
	    	}
	    }, new ResultSetExtractor<String>() {
	    	
	    public String extractData(ResultSet rs) throws SQLException, DataAccessException {
	    	  String nextDate = null;
	    	  while(rs.next()) {
	    		  nextDate = rs.getString( 1  );	
				}
	           return nextDate;
	      }
	    });	
	return whatToWatch;
}

 
@SuppressWarnings("unchecked")
public void executeHierarchyUpdate(final String storedProc, final int hierarchyID,final int nodeAK, 
		final int leafID, final String userId){
	
	 jdbcTemplate.query( new PreparedStatementCreator() {
	     public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
	    	 
	        return connection.prepareStatement(storedProc);
	      }
	    }, new PreparedStatementSetter() {
	    	private  java.sql.Date getCurrentDate() {
	    		java.util.Date today = new java.util.Date();
	    		return new java.sql.Date(today.getTime());
	    	}
	    	
	    	public void setValues(PreparedStatement preparedStatement) throws SQLException {
	    		preparedStatement.setInt(1, Integer.valueOf(hierarchyID));
	    		preparedStatement.setInt(2, Integer.valueOf(nodeAK));
	    		preparedStatement.setInt(3, Integer.valueOf(leafID));
	    		preparedStatement.setDate(4,getCurrentDate());
	    		preparedStatement.setInt(5, Integer.valueOf( userId ));
	    		
	    	}
	    }, new ResultSetExtractor<Object>() {

			@Override
			public Object extractData(ResultSet arg0) throws SQLException,
					DataAccessException {
			
				 return null;
				
			}
	    	
	  
	    });	
	
}

public Map<String, String> getHierarchyTypeMap() {
	return hierarchyTypeMap;
}

public void setHierarchyTypeMap(Map<String, String> hierarchyTypeMap) {
	this.hierarchyTypeMap = hierarchyTypeMap;
}




}
