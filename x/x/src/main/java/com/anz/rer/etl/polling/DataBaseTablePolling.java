package com.anz.rer.etl.polling;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.anz.rer.etl.utils.DbUtils;
 
public class DataBaseTablePolling extends APollingTask{
    
	private String sqlTable;
	private DbUtils dbUtils;
	
	public DataBaseTablePolling(int interval, long startDelay) {
		super(interval, startDelay);
	}

	public DataBaseTablePolling(int interval, long startDelay, DbUtils dbUtils,  String sqlTable) {
		super(interval, startDelay);
		this.dbUtils = dbUtils;
		this.sqlTable = sqlTable;
	}

	
	@Override
	public void run() {
		try {
		  List<Map<String,Object>> table =	dbUtils.executeSql(sqlTable);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	

}
