package com.anz.util.lookup.impl;

import javax.sql.DataSource;

import com.anz.util.lookup.ILookUp;

public class DBLookUp implements ILookUp{
    private String sql;
    private DataSource dataSource;
    
    
	@Override
	public Integer call() throws Exception {
		System.out.println("Im Now getting a DBLookUp");
		return 1;
	}

	/**
	 * @param sql the sql to set
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * @return the sql
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @return the dataSource
	 */
	public DataSource getDataSource() {
		return dataSource;
	}
	
	
      
}
