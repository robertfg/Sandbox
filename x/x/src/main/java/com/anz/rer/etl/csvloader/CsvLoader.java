package com.anz.rer.etl.csvloader;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.anz.rer.etl.directory.impl.PatternFileResolver;
import com.anz.rer.etl.utils.FileUtils;

public class CsvLoader extends CsvLoaderImpl {
    
	private final static Logger logger = Logger.getLogger(  CsvLoader.class );

	private String preProcSql;
	private String postProcSql;
	private List<String> ignoreList = new ArrayList<String>();
	private Long jobId;
	private String unzipFile;
	private String valiDateSql;
	private String successStatus;
	private Set<String> excludeFileName;
	private String name;
	private String jobIdSplitDelimeter = "_";
	private int    jobIdLocation = 4;
	private String oldStatus;
    private String murexVersion;
	private final String ADDED_BY = "ETL_RWH";

	
	
	public CsvLoader() {
		super();
		
	}
	public CsvLoader(DataSource dataSource, int threadPool) {
		super(dataSource, threadPool);
		logger.info( "CsvLoader Id:" + this.hashCode());
		
	}
  
  
	@Override 
	public boolean postProcess() {
	boolean retval=	super.postProcess();
		
		if(retval){
			logger.info("doing post processor");
		      if (this.postProcSql != null) {
		        logger.info("executing:" + this.postProcSql + " for Job ID: " + this.jobId + " on Murex Version: " + this.murexVersion);
		        try
		        {
		          if (this.postProcSql.indexOf("?") != -1)
		          {
		            if (!getDbUtils().executeSp(this.postProcSql, this.murexVersion)) {
		              this.successStatus = "Failed";
		            }
		          }
		          else if (!getDbUtils().executeSp(this.postProcSql))
		            this.successStatus = "Failed";
		        }
		        catch (SQLException e)
		        {
		          e.printStackTrace();
		          retval = false;
		          this.successStatus = "Failed";
		        }
		      }
			
			String sql = "{call [ETL].[UpdateJobStatus]('" + jobId.toString()+ "','"+ this.successStatus +"', '"+ ADDED_BY + "'  )}";
			logger.info("Setting status from postProcess:" + sql);
			successStatus = oldStatus;
			
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
			logger.info("executing stored procedure:DONE" );
		} else{
			logger.info("SP will not be executed" );
					
		}
		
		
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
		
		oldStatus = getSuccessStatus();
		super.setCurrentFileName(fileName.getName());
		
  
	    String bussDate = this.getBussNameFromFileName(fileName.getName(), "_", ".gz", getBussDate());
	           bussDate = bussDate.replace(".gz", "");
	           bussDate = bussDate.replace(".csv", "");
	           
	    String apJobID  = this.getJobIdFromFileName(fileName.getName(), "_", ".gz",getJobIdLocation() );
	    		
	    if(apJobID!=null && (apJobID.indexOf(".gz") !=-1 || apJobID.indexOf(".csv") !=-1 || apJobID.indexOf("null") !=-1) ){
	    	apJobID = null;
	    }
	    try {
	        if(ignoreList.contains(bussDate)){
	        	return false;
	        }
	    	Integer.parseInt(bussDate);
	     }catch (NumberFormatException e) {
	    	 ignoreList.add(bussDate);
	    	 return false;
	     }
	  
	     String sql =  valiDateSql + " and businessDate='" + bussDate + "' ";
	     if(apJobID!=null){
	    	 sql  += " and jobID=" + apJobID;
	     }
	     logger.info("checking if file is in job status table:" + sql);
	    
	    List<Map<String, Object>> job = null;
		try {
			job = super.getDbUtils().executeSql(sql);
		} catch (SQLException e1) {
			
			e1.printStackTrace();
		}
	    
	    if(job!=null && job.size()>0){
	    	 jobId = (Long)job.get(0).get("JobID");
	    	 murexVersion = (String)job.get(0).get("MurexVersion");
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
			logger.info("doProcess:" + ret); 
			String sql = "{call [ETL].[UpdateJobStatus]('" + jobId.toString()+ "','"+ super.getStatus() + "', '"+ ADDED_BY + "' ) }";
			try {
				super.getDbUtils().executeSp(sql);
				ret = false;
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

	protected String getJobIdFromFileName(String jobIdFromFileName, String fileNamePadStr, String fileExtension, int jobIdLocation){
		try{
		
		return jobIdFromFileName.split(jobIdSplitDelimeter)[jobIdLocation];
		} catch(Exception e){
			logger.warn("No JobID in FileName - Current JobID config Position:" + e.getMessage() );
			jobIdFromFileName = null;
			e.printStackTrace();
		}
		return jobIdFromFileName;
		  
	 
	}

	public String getJobIdSplitDelimeter() {
		return jobIdSplitDelimeter;
	}

	public void setJobIdSplitDelimeter(String jobIdSplitDelimeter) {
		this.jobIdSplitDelimeter = jobIdSplitDelimeter;
	}

	public int getJobIdLocation() {
		return jobIdLocation;
	}

	public void setJobIdLocation(int jobIdLocation) {
		this.jobIdLocation = jobIdLocation;
	}
	
}
