package com.anz.rer.etl.mxRate;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.anz.rer.etl.csvToTable.CsvToTableImpl;
import com.anz.rer.etl.directory.impl.DirectoryWatcherImpl;
import com.anz.rer.etl.transform.impl.Transformers;
import com.anz.rer.etl.transform.impl.TrimDownRecords;
import com.anz.rer.etl.utils.CsvUtils;

public class OldMxRateLoader extends CsvToTableImpl {
    
	private final static Logger logger = Logger.getLogger( OldMxRateLoader.class );
	private String preProcSql;
	private String postProcSql;
	
	public OldMxRateLoader() {
		super();
		
	} 

	public OldMxRateLoader(DataSource dataSource, int threadPool) {
		super(dataSource, threadPool);
	}
  
	@Override
	public boolean postProcess() {
	boolean retval=	super.postProcess();
		
		if(retval){
			logger.info("doing post processor");
			logger.info("executing:" + this.postProcSql);
			retval = true; 

			try {
				super.getDbUtils().executeSp(this.postProcSql);
			} catch (SQLException e) {
				e.printStackTrace();
				retval = false;
			}
		}
		logger.info("executing stored procedure:DONE" );
		return retval;
	}



	@Override
	public boolean preProcess() {
	    boolean retval = super.preProcess();
	    return retval;
	}

	public String getPreProcSql() {
		return preProcSql;
	}

	public void setPreProcSql(String preProcSql) {
		this.preProcSql = preProcSql;
	}

	public String getPostProcSql() {
		return postProcSql;
	}

	public void setPostProcSql(String postProcSql) {
		this.postProcSql = postProcSql;
	}

	@Override
	public boolean validate(File fileName) {
		boolean retval =  super.validate(fileName);
		if( retval){
			retval = true;
		}
		return retval;
	}

	@Override
	public boolean doProcess() {
		logger.info(super.getFileResolver().getSrcFileName());  
		List<String[]> csvs = CsvUtils.loadCsv( super.getFileResolver().getSrcFileName() , ";", false,"#");
	    TrimDownRecords tdrec = new TrimDownRecords();
		csvs =  Transformers.trimRecords( tdrec , csvs);
		return super.doBatchInsert(csvs);	  
	}

}
