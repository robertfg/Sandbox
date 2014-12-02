/*package com.anz.rer.etl.mxPosExtract;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import com.anz.rer.etl.csvToTable.CsvToTableImpl;
import com.anz.rer.etl.directory.impl.PatternFileResolver;
import com.anz.rer.etl.utils.FileUtils;

public class MrePositionLoader extends CsvToTableImpl {
    
	private final static Logger logger = Logger.getLogger( MrePositionLoader.class );

	private String preProcSql;
	private String postProcSql;
	private List<String> ignoreList = new ArrayList<String>();
	private Long jobId;
	private String unzipFile;
	
	public MrePositionLoader() {
		super();
		
	}

	public MrePositionLoader(DataSource dataSource, int threadPool) {
		super(dataSource, threadPool);
	}
  
	@Override
	public boolean postProcess() {
	boolean retval=	super.postProcess();
		
		if(retval){
			logger.info("doing post processor");
			logger.info("executing:" + this.postProcSql);
			
			try{
			super.getDbUtils().executeSp(this.postProcSql);
			String sql = "{call [ETL].[UpdateJobStatus]('" + jobId.toString()+ "','Ready for Cache',1) }";
			super.getDbUtils().executeSp(sql);
		    FileUtils.deleteQuietly(new File(this.unzipFile));
			retval = true;
			}catch(SQLException e){
				e.printStackTrace();
				retval = false;
			}
		}
		logger.info("executing stored procedure:DONE" );
		
		return retval;
	}



	@Override
	public boolean preProcess() {
	    boolean retval =	super.preProcess();
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
		
		logger.info( fileName.getName() );
		super.setCurrentFileName(fileName.getName());
		
		// fileName should be MRE_POSITION_20120229.txt
	    String bussDate = super.getBussNameFromFileName(fileName.getName(), "_", ".gz",super.getBussDate());
	    
	    
	    try {
	        if(ignoreList.contains(bussDate)){
	        	return false;
	        }
	    	Integer.parseInt(bussDate);
	        
	     }catch (NumberFormatException e) {
	    	 ignoreList.add(bussDate);
	    	 return false;
	     }
	    
	    String sql = "select * from ETL.vw_Job where jobTypeID = '3' and status in ( 'Not Started','Failed') and businessDate='" + bussDate +"'";
	    
	    logger.info("checking if file is in job status table");
	    
	    List<Map<String, Object>> job = null;
		try {
			job = super.getDbUtils().executeSql(sql);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		} 
	    
	    if(job!=null && job.size()>0){
	    	 jobId = (Long)job.get(0).get("JobID");
		    super.getFileResolver().resolveSrcFileName();
		   
		    
		    try {
		    	    if(fileName.getName().endsWith(".gz")){
				     unzipFile = FileUtils.unzip(fileName, fileName.getName().replace(".gz", ".txt")); 
				     super.getFileResolver().setSrcFileName( unzipFile);
		    	    }else {
		    	    	super.getFileResolver().setSrcFileName( fileName.getAbsolutePath());
		    	    	unzipFile = fileName.getAbsolutePath();
		    	    }
		    	    
				    String configFileName =   (String)((PatternFileResolver)super.getFileResolver()).getProperties().get("etl.csvToTable.src.fileName.config");
				    super.getFileResolver().setSrcConfigFileName( fileName.getParent() + File.separator + configFileName  );
				    return true;
			} catch (IOException e) {
				
				e.printStackTrace();
				return false;
			}
		    
		    
		    
		 
	    } else {
	    	logger.info("file not in table ETL.vw_Job listed to be process");
		    	
	    	return false;
	    }
	    
		
	}
	
	
	


	@Override
	public boolean doProcess() {
		boolean retval = super.doProcess(); 
			
		if(!retval){
			String sql = "{call [ETL].[UpdateJobStatus]('" + jobId.toString()+ "','Failed',1) }";
			retval = true; 

			try {
				super.getDbUtils().executeSp(this.postProcSql);
			} catch (SQLException e) {
				e.printStackTrace();
				retval = false;
			}
		}
		
		return retval;
		
	}

	
}
*/