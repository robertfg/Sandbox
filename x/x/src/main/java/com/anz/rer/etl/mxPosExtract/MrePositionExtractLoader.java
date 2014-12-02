package com.anz.rer.etl.mxPosExtract;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.anz.rer.etl.csvToTable.CsvToTableImpl;
import com.anz.rer.etl.directory.impl.PatternFileResolver;
import com.anz.rer.etl.utils.FileUtils;

public class MrePositionExtractLoader extends CsvToTableImpl {
    
	private final static Logger logger = Logger.getLogger(  MrePositionExtractLoader.class );

	private String preProcSql;
	private String postProcSql;
	private List<String> ignoreList = new ArrayList<String>();
	private Long jobId;
	private String unzipFile;
	private String valiDateSql;
	private String successStatus;
	private Set<String> excludeFileName;
	private String name;
	
	public MrePositionExtractLoader() {
		super();
		
	}

	public MrePositionExtractLoader(DataSource dataSource, int threadPool) {
		
		super(dataSource, threadPool);
		logger.info( "Id:" + this.hashCode());
		
	}
  
  
	@Override
	public boolean postProcess() {
	boolean retval=	super.postProcess();
		
		if(retval) {
			logger.info("doing post processor");
			if(this.postProcSql!=null){
				 try {
				
					 if(!getDbUtils().executeSp(this.postProcSql)){
						 retval = false;
						 this.successStatus = "Failed";
					 }
				
				} catch (SQLException e) {
					e.printStackTrace();
					retval = false;
					this.successStatus = "Failed";
				} catch(Exception e){
					e.printStackTrace();
					retval = false;
					this.successStatus = "Failed";
				}
				
			}
			String sql = "{call [ETL].[UpdateJobStatus]('" + jobId.toString()+ "','"+this.successStatus+"',1)}";
			try {
				super.getDbUtils().executeSp(sql);
			} catch (SQLException e) {
				e.printStackTrace();
				retval=false;
			}
			
			
			if(unzipFile!=null && retval == true){
				FileUtils.deleteQuietly(new File(this.unzipFile));
				retval = true;
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
		super.setCurrentFileName(fileName.getName());

	    String bussDate = this.getBussNameFromFileName(fileName.getName(), "_", ".gz", getBussDate());
	           bussDate = bussDate.replace(".gz", "");
	           bussDate = bussDate.replace(".csv", "");
	    
	    try {
	        if(ignoreList.contains(bussDate)){
	        	return false;
	        }
	    	Integer.parseInt(bussDate);
	     }catch (NumberFormatException e) {
	    	 logger.info("something wrong with the filename");
	    	 ignoreList.add(bussDate);
	    	 return false;
	     }
	  
	     String sql =  valiDateSql + " and businessDate='" + bussDate + "'";
	     logger.info("checking if file is in job status table:" + sql);
	    
	    List<Map<String, Object>> job = null;
		try {
			job = super.getDbUtils().executeSql(sql);
		} catch (SQLException e1) {
			
			e1.printStackTrace();
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
		    	    //	unzipFile = fileName.getAbsolutePath();
		    	    }
		    	    
				    String configFileName =   (String)((PatternFileResolver)super.getFileResolver()).getProperties().get("etl.csvToTable.src.fileName.config");
				    super.getFileResolver().setSrcConfigFileName( fileName.getParent() + File.separator + configFileName  );
				    return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
		boolean ret = super.doProcess(); 
			
		if(!ret){
			String sql = "{call [ETL].[UpdateJobStatus]('" + jobId.toString()+ "','Failed',1) }";
			try {
				super.getDbUtils().executeSp(sql);
				ret = true;
			} catch (SQLException e) {
				e.printStackTrace();
				ret = false;
			}	
		}
		
		return ret;
		
	}

	public void setValiDateSql(String valiDateSql) {
		this.valiDateSql = valiDateSql;
	}

	public String getValiDateSql() {
		return valiDateSql;
	}

	public void setSuccessStatus(String successStatus) {
		this.successStatus = successStatus;
	}

	public String getSuccessStatus() {
		return successStatus;
	}

	public void setExcludeFileName(Set<String> excludeFileName) {
		this.excludeFileName = excludeFileName;
	}

	public Set<String> getExcludeFileName() {
		return excludeFileName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	
}
