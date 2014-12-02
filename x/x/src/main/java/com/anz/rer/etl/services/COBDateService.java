package com.anz.rer.etl.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class COBDateService  {
	
	private final static Logger logger = Logger.getLogger(COBDateService.class);
	

	private String prevCobDate ;
	private Date cobDate;
	private String nextCobDate;
	private String prevSrcCobDate ;
	private String srcCobDate;
	private String nextSrcCobDate;
 
	private String sql;
	private JdbcTemplate jdbcTemplate;
	
	
	public String getSql() {
		return sql;
	}


	public void setSql(String sql) {
		this.sql = sql;
	}
	
	public COBDateService(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public void loadDates( String inPattern, String srcPattern, 
							String outPattern){
		
		SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
		
		//inPattern    = "ddMMyy";//properties.getProperty( "murex.date.src.format" ); 
		//outPattern   = "ddMMyyyy";//properties.getProperty( "etl.uvr.date.format" );     
		//srcPattern   = "yyyyMMdd"; //properties.getProperty( "database.cob.date.format" );
		
		
		while(rs.next()){
			try {
				 
				prevSrcCobDate = this.deriveDate( rs.getString("PreviousBusinessDate"), srcPattern, inPattern); 
				srcCobDate     = this.deriveDate( rs.getString("CurrentBusinessDate"),  srcPattern, inPattern); 			
				nextSrcCobDate = this.deriveDate( rs.getString("NextBusinessDate")   ,  srcPattern, inPattern);
			
				
				prevCobDate    = this.deriveDate( rs.getString("PreviousBusinessDate"),srcPattern, outPattern); 
				cobDate        =  this.stringToDate (rs.getString("CurrentBusinessDate"), srcPattern, outPattern); 	
				
				nextCobDate    = this.deriveDate( rs.getString("NextBusinessDate")   , srcPattern, outPattern); 
				 
			} catch (InvalidResultSetAccessException e) {
				logger.error("Failed to initialise COB date information: " +e.getMessage());
				e.printStackTrace();
			} catch (ParseException e) {
				logger.error("Failed to initialise COB date information: " +e.getMessage());
				e.printStackTrace();
			} 	
		}	
	}

	public String getPrevCobDate() {
		return prevCobDate;
	}
	public Date getCobDate() {
		return cobDate;
	}
	public String getNextCobDate() {
		return nextCobDate;
	}
	

	public String getPrevSrcCobDate() {
		return prevSrcCobDate;
	}	
	public String getSrcCobDate() {
		return srcCobDate;
	}
	public String getNextSrcCobDate() {
		return nextSrcCobDate;
	}	
	
	public String deriveDate(String inDate, 
			String inDatePattern,
			String outDatePattern) throws ParseException {
		
		SimpleDateFormat sdf = new SimpleDateFormat(inDatePattern);
		Date tmpDate = sdf.parse(inDate); // converted to date
		sdf.applyPattern(outDatePattern); 
		return sdf.format(tmpDate);
	}
	
	private Date stringToDate(String inDate, 
			String inDatePattern,
			String outDatePattern){
	
		SimpleDateFormat sdf = new SimpleDateFormat(inDatePattern);
		try {
			return sdf.parse(inDate);
		} catch (ParseException e) {
		
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	public String deriveDate(Date inDate, 
			String datePattern) throws ParseException {
		
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		
		return sdf.format(inDate);
	}
	
	
	
	
	
	
	
	
	

}
