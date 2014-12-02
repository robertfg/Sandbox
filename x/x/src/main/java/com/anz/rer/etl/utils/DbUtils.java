package com.anz.rer.etl.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.anz.rer.etl.csvToTable.BcpData;

public class DbUtils {
	private final static Logger logger = Logger.getLogger( DbUtils.class );

	private DataSource dataSource;
	private List<String> excludeToSubCube;
	
	private static final Map<String,String> varContainerMapping = new HashMap<String,String>();	
	static {
			varContainerMapping.put("B1AL0", "HYPO");
			varContainerMapping.put("VXAL0", "VAR_10D_AGG");
			varContainerMapping.put("V1AL0", "VAR_1D_AGG");
			varContainerMapping.put("VSAL0", "VAR_STRESS_AGG");
			varContainerMapping.put("VFAL0", "VAR_1540_AGG");
			
	}
	
	
	
	public DbUtils(DataSource dataSource) {
		this.dataSource = dataSource;
	    logger.info("###########################################################################################################");
	    try {
	    	logger.info("DB Connection Info:" + dataSource.getConnection().toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	    logger.info("###########################################################################################################");
	}
 
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public boolean executeSp(String spStmt) throws SQLException {
		 Connection con = null;
		 PreparedStatement ps = null;
		 
	  try{
			 con = this.getConnection();
			  
			 
			 con.setAutoCommit(false);
			 ps = con.prepareCall(spStmt);
			 logger.info("ConnID:" + con.hashCode() + "Executing:" + spStmt);       
			 ps.execute();
			 con.commit();
			 logger.info(  spStmt + ":SUCCESS");
		     return true;
	    } catch (SQLException e) {
	    	throw e;
	    } catch(Exception e){
	    	logger.info( "Exception while executing:" + spStmt + ":" + e.getLocalizedMessage() );
	    	e.printStackTrace();
	    	//con.rollback();
	    	con.setAutoCommit(true);
	    	throw new SQLException(e.getLocalizedMessage()); 
	    	
		} finally {
				try {
					if (con != null) {
						con.close();
					}
					if (ps != null) {
						ps.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		
	}

	
	
	public boolean executeStoredProcedure(String spStmt)  throws SQLException  {
		 Connection con = null;
		 PreparedStatement ps = null;
		 
	  try{
			 con = this.getConnection();
			 con.setAutoCommit(false);
			 ps = con.prepareCall(spStmt);
			 ps.execute();
			 con.commit();
		     return true;
		      
	    } catch(Exception e){
	    	logger.info( "Exception while executing:" + spStmt + ":" + e.getLocalizedMessage() );
	    	e.printStackTrace();
	    	con.rollback();
	    	return false;
		} finally {  
				try {
					if (con != null) {
						con.close();
					}
					if (ps != null) {
						ps.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		
	}
	
	public  List<Map<String,Object>> executeSql(String sql) throws SQLException{
		
		 Connection con = null;
		 Statement stmt = null;
		 ResultSet rs =  null;
		
		 try{
		 con = this.getConnection();	 
		 stmt = con.createStatement();
		 
		 rs   = stmt.executeQuery(sql);
		 
	     List<Map<String,Object>> records = new ArrayList<Map<String,Object>>();
		 
		 ResultSetMetaData rsMetaData = rs.getMetaData();
		 logger.info("SQL:" + sql );
		 
			while(rs.next()){
				Map<String,Object> record = new HashMap<String,Object>();
			    int numberOfColumns = rsMetaData.getColumnCount();
			    for (int i = 1; i <= numberOfColumns; i++) {
			      record.put(rsMetaData.getColumnName(i), rs.getObject(i) );
			    }
			  records.add(record);
			}
			logger.info("SQL Return / size:" +  records.size() );
			return records;
		 
		 } finally{
			 if(rs!=null) {rs.close();}
			 if(stmt!=null){stmt.close();}
			 if(con!=null){
				 con.close();
			 }
		     logger.info("all resources are closed");	 
		 }
	}
	
	public boolean executeSpNoTransaction(String spStmt) throws SQLException {
		 Connection con = null;
		 PreparedStatement ps = null;
		 
	  try{
			 con = this.getConnection();
			 ps = con.prepareCall(spStmt);
			 ps.execute();
		     return true;
		      
	    } catch(Exception e){
	    	logger.info( "Exception while executing.:" + spStmt + ":" + e.getLocalizedMessage() );
	    		e.printStackTrace();
	    		logger.info( "Throwing.:" + e.getLocalizedMessage() );
	    		throw new SQLException( e.getLocalizedMessage() );
		} finally {
				try {
					if (con != null) {
						con.close();
					}
					if (ps != null) {
						ps.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		
	}
	
	public boolean updateStatusInDB(BcpData bcpData, String status){
		String containerName  = bcpData.getContainerName();//  this.getContainer(bcpData.getName());
		String cobDate =        bcpData.getCobDate();     // this.getCobDate(bcpData.getName());
		
		if(status==null){
			status = bcpData.getStatus();
		}
		
		if( containerName !=null && updatable(bcpData) ) {
			 logger.info( "Container PsrCode:" + bcpData.getPsrCode() +  ", Updating status in DB:" +  bcpData.getName());
			 if(containerName.equalsIgnoreCase( "VAR AND P&L" )){
		     	containerName = varContainerMapping.get( bcpData.getPsrCode() );
	    	 	logger.info( "Inside:updateStatusInDB=" + containerName);
				  
			 }else if(containerName.equals("VAR_STRESS")){
				 containerName = "VAR_STRESS_AGG";
			 }else if(containerName.equals("VAR_1540")){
				 containerName = "VAR_1540_AGG";
			 }
			
			try { 
		    	this.executeSp("{ call [DW].[UpdateSignoffAndExclude] (" +cobDate + ",'"+ containerName + "','" + status+ "') }");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		 return true;
	}  
	
	public boolean rollBackVar(BcpData bcpData){
		String containerName  = bcpData.getContainerName();//  this.getContainer(bcpData.getName());
		String cobDate =        bcpData.getCobDate();     // this.getCobDate(bcpData.getName());
		
		
		if( containerName !=null  ) {
			 logger.info( "Container PsrCode:" + bcpData.getPsrCode() +  ", RollingBack status in DB:" +  bcpData.getName());
			 containerName = varContainerMapping.get( bcpData.getPsrCode() );
			 
			try { 
						this.executeSp("{ call [DW].[usp_CleanupFactTablesVaR] (" +cobDate + ",'"+ containerName + "') }");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		 return true;
	}
	
	
	private boolean updatable(BcpData bcpData){
		String updatetableContainer = bcpData.getName().split("#")[2];
	  	return true;//(!updatetableContainer.equals("VAR-VAR_1D") &&  !updatetableContainer.equals("VAR-VAR_10D"));
	}
	
	public String getSignOffStatus(String containerName,String cobDate){
		
		String sql = " select top 1 status  " + 
				     " from DW.vw_SignOffAndExclude "+
				     " where containerName = '" + containerName + "' and cobDate=" + cobDate; 
		try {
			List<Map<String,Object>> status = this.executeSql(sql);
			if(status!=null && status.size()>0){
			  return (String)status.get(0).get( "status");
			}
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			return null;
		} 
		
		return null;
	}

	public  void execute(String sql) throws SQLException{
		
		 Connection con = null;
		 Statement stmt = null;
		 ResultSet rs =  null;
		
		 try{
		 con = this.getConnection();	 
		 stmt = con.createStatement();
		 stmt.execute(sql);	 	 
		 } finally{			
			 if(stmt!=null){stmt.close();}
			 if(con!=null){
				 con.close();
			 }
		     logger.info("all resources are closed");	 
		 }
	}
	
	public boolean executeSp(String spStmt, String param) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = getConnection();

			con.setAutoCommit(false);
			ps = con.prepareCall(spStmt);
			logger.info("ConnID:" + con.hashCode() + "Executing:" + spStmt);
			ps.setString(1, param);
			ps.execute();
			con.commit(); 
			logger.info(spStmt + ":SUCCESS");
			return true;
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			logger.info("Exception while executing:" + spStmt + ":"
					+ e.getLocalizedMessage());
			e.printStackTrace();
			con.setAutoCommit(true);
			return false;
		} finally {
			try {
				if (con != null) {
					con.close();
				}
				if (ps != null)
					ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public synchronized ConcurrentHashMap<Object,ConcurrentHashMap<String,Object>>  executeSql(String sql, String key) throws SQLException{
		  
		 Connection con = null;
		 Statement stmt = null;
		 ResultSet rs =  null;
		
		 try{
		 con = this.getConnection();	 
		 stmt = con.createStatement();
		 
		 rs   = stmt.executeQuery(sql);
		 
		 ConcurrentHashMap<Object, ConcurrentHashMap<String,Object>> records = new ConcurrentHashMap<Object,ConcurrentHashMap<String,Object>>();
		 
		 ResultSetMetaData rsMetaData = rs.getMetaData();
		 logger.info("SQL:" + sql );
		 
			while(rs.next()){
				ConcurrentHashMap<String,Object> record = new ConcurrentHashMap<String,Object>();
				Object fKey = rs.getObject(key);
				
				 
				int numberOfColumns = rsMetaData.getColumnCount();
			    for (int i = 1; i <= numberOfColumns; i++) {
			      record.put(rsMetaData.getColumnName(i), rs.getObject(i) );
			    }
			    
			  records.put( fKey , record);
			}
			logger.info("SQL Return / size:" +  records.size() );
			return records;
		 
		 } finally{
			 if(rs!=null) {rs.close();}
			 if(stmt!=null){stmt.close();}
			 if(con!=null){
				 con.close();
			 }
		     logger.info("all resources are closed");	 
		 }
	}
	
	
	public synchronized Map<String,Object> executeSql(String sql, String key, String value) throws SQLException{
		  
		 Connection con = null;
		 Statement stmt = null;
		 ResultSet rs =  null;
		
		 try{
		 con = this.getConnection();	 
		 stmt = con.createStatement();
		 
		 rs   = stmt.executeQuery(sql);
		 
	     Map<String,Object> records = new HashMap<String,Object>();
		 
		 logger.info("SQL:" + sql );
		 
			while(rs.next()){
			    
				records.put(rs.getString(key),rs.getObject(value));
			    
			}
			logger.info("SQL Return / size:" +  records.size() );
			return records;
		 
		 } finally{
			 if(rs!=null) {rs.close();}
			 if(stmt!=null){stmt.close();}
			 if(con!=null){
				 con.close();
			 }
		     logger.info("all resources are closed");	 
		 }
	}

	public List<String> getExcludeToSubCube() {
		return excludeToSubCube;
	}

	public void setExcludeToSubCube(List<String> excludeToSubCube) {
		this.excludeToSubCube = excludeToSubCube;
	}
	
	

	
}
