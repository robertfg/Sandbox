package com.quartetfs.pivot.anz.utils;

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
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.quartetfs.pivot.anz.impl.MessagesANZ;

public class DBUtils {
	protected DataSource dataSource;
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);

	//private JdbcTemplate jdbc;
	
	public DBUtils(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

    public void init(){
    	LOGGER.info("=========================================================================================");
    	try {
			
    		Connection conn = dataSource.getConnection();
    		if(conn!=null){
    			LOGGER.info("" + conn.toString());
    	     	conn.close();
    	     	conn = null;
    		}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	LOGGER.info("=========================================================================================");
    }
	
	
	
	
    public int spUpdateExtractStatus(String cobDate, String containerName, String status){
		 String  sql =  "{ call [DW].[UpdateSignoffAndExclude] (" +cobDate + ",'"+ containerName + "','" +status+ "') }"; 

		try {
			boolean retVal = executeSp(sql);
			sql = null;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
	 	return 1;
	 	
	}
		
	    
	public List<Map<String,Object>> executeSQL(String sqltmt) throws SQLException {
		Connection con = null;
		Statement statement = null;
		ResultSet result = null;
				 List<Map<String,Object>> records = new ArrayList<Map<String,Object>>();		
		try {
			con = dataSource.getConnection();
			statement = con.createStatement();
			   LOGGER.info("Executing:" + sqltmt);
			   result = statement.executeQuery(sqltmt);
			   
			   ResultSetMetaData rsMetaData = result.getMetaData();
				while(result.next()){
					Map<String,Object> record = new HashMap<String,Object>();
				    int numberOfColumns = rsMetaData.getColumnCount();
				    for (int i = 1; i <= numberOfColumns; i++) {
				      record.put(rsMetaData.getColumnName(i), result.getObject(i) );
				    }
				  records.add(record);
				}
				 LOGGER.info("Total number of record/s:" + records.size());  
		} catch (Exception e) {
				e.printStackTrace();
 		} finally {
			if(result!=null){
				result.close();
			}
			
			if(statement!=null){
				statement.close();
			}
			
			if(con!=null){
				
				con.close();
			}
		}
		
		result = null;
		statement = null;
		con = null;
		
		return records;
  }
	    
	    
		public boolean executeSp(String spStmt) throws SQLException {
			 Connection con = null;
			 PreparedStatement ps = null;
			 
		  try{
				 con = dataSource.getConnection();
				 con.setAutoCommit(false);
				 ps = con.prepareCall(spStmt);
				 LOGGER.info( "Executing:" + spStmt); 
				 ps.execute();
				 con.commit();
				 LOGGER.info(  spStmt + ":SUCCESS");
			     return true;
			      
		    } catch(Exception e){
		    	LOGGER.info( "Exception while executing:" + spStmt + ":" + e.getLocalizedMessage() );
		    	e.printStackTrace();
		    	con.rollback();
		    	con.setAutoCommit(true);
		    	return false;
			} finally {
					try {
						if (con != null) {
							con.close();
							con = null;
						}
						if (ps != null) {
							ps.close();
							ps = null;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
			}
			
		}
	
}
