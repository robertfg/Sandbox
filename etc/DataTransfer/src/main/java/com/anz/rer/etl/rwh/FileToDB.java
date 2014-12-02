package com.anz.rer.etl.rwh;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.anz.rer.etl.csvToTable.CsvToTableImpl;
import com.anz.rer.etl.utils.FileUtils;
import com.quartetfs.pivot.anz.webservices.IDataManagement;
import com.quartetfs.pivot.anz.webservices.impl.DataManagementParamsDTO;




@ManagedResource
public class FileToDB extends CsvToTableImpl {
  
	private final static Logger logger = Logger.getLogger( FileToDB.class );
	private String preProcSql;
	private String postProcSql;
	private int nonVarSourceLength;
	private int varSourceLength;
	private int pnlSourceLength;
	private boolean debug;
	
	public static final String HASH_SEPARATOR= "#";
	public static final String DATA_EXTRACT_SEPARATOR = "!@!";
	
	private final String PUBLISH = "Published";
	//private String fName;
	
    
	
	private String wsUrl;
	private String wsUserName;
    private String wsPassword;
    
	
	public enum STATUS {
		Unknown, Ready, Processing, Error, Published, NoData;  
	}
	
	
	public FileToDB() {
		super();
	}

	public FileToDB(DataSource dataSource, int threadPool) {
		super(dataSource, threadPool);
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
			
		logger.info("validating filename...");
		super.setCurrentFileName(fileName.getName().toUpperCase());
		String  xmlConfiFileName = null;
		
		boolean retVal = true;
	
		if( super.getCurrentFileName().indexOf("NON-VAR")!=-1){
			xmlConfiFileName = fileName.getParent() + File.separator  + "config" + File.separator +  "Staging.FactRiskMeasureNonVaRDetail.XML";
			super.setTableName("Staging.FactRiskMeasureNonVarDetail");
			super.setCsvSourceLength( this.nonVarSourceLength );
			
		} else if(super.getCurrentFileName().indexOf("PNL")!=-1){
			xmlConfiFileName = fileName.getParent() + File.separator  + "config" + File.separator +  "Staging.FactVaRPnLVector.XML";
			super.setTableName("Staging.FactVaRPnLVector");
			super.setCsvSourceLength( this.pnlSourceLength );
	
		}else if(super.getCurrentFileName().indexOf("VAR")!=-1   ){
			xmlConfiFileName = fileName.getParent() + File.separator + "config" + File.separator + "Staging.FactVaR.XML";
			super.setTableName("Staging.FactVaR");
			super.setCsvSourceLength( this.varSourceLength );
			
		} 
		
		super.getFileResolver().setSrcFileName(fileName.getAbsolutePath());
		super.getFileResolver().setSrcConfigFileName(xmlConfiFileName);
		
		
		return retVal;
		
		
	}

	@Override
	public boolean preProcess() {
		  return super.preProcess();
	}

	private String getHeaderSql(String container){
	
		String sql = null;
    	String pnlContainer = container;
    	logger.info("Current FileName" + super.getCurrentFileName());
    	
    	 if(super.getCurrentFileName().indexOf("NON-VAR")!=-1) {
			 sql = " {call [Staging].[usp_LoadRiskMeasureHeader]  (" + this.getUniqueId(super.getCurrentFileName()) 
						  + "," + this.getCobDate(super.getCurrentFileName()) +",'" +  container + "') } ";
			
		} else if(super.getCurrentFileName().indexOf("PNL")!=-1){
			logger.info("PNL:" + super.getCurrentFileName());
			sql = " {call [Staging].[usp_LoadRiskMeasureHeader]  (" + this.getUniqueId(super.getCurrentFileName()) 
			  + "," + this.getCobDate(super.getCurrentFileName()) +",'"+ container +"') } ";
		
		} else if( super.getCurrentFileName().indexOf("VAR")!=-1){
			logger.info( "VAR:" + super.getCurrentFileName());
			logger.info( "container:" + container);
				
				if( container.equalsIgnoreCase("VAR_1D")){
					pnlContainer = "VAR_1D_AGG";
				} else if(container.equalsIgnoreCase("VAR_10D")){
					pnlContainer = "VAR_10D_AGG";
				} else if(container.equalsIgnoreCase("VAR_STRESS")){
					pnlContainer = "VAR_STRESS";
				} else if(container.equalsIgnoreCase("HYPO")){
					pnlContainer = "HYPO_AGG";
				} else if(container.equalsIgnoreCase("HYPO_1D")){
					pnlContainer = "HYPO_AGG";
				}
				
				
			 sql = " {call [Staging].[usp_LoadRiskMeasureHeader]  (" + this.getUniqueId(super.getCurrentFileName()) 
			  + "," + this.getCobDate(super.getCurrentFileName()) +",'"+ pnlContainer +"') } ";
		 
			 
		} 
    	
    	
    	 
	   return sql;
	}
    private boolean inserHeaderInfo(String sql){
       
		try{
		 logger.info("Executing:" + sql);	
    	 
		super.getDbUtils().executeSp(sql);
    	 
    	 logger.info(  "Done inserting to Header Table:" + sql);
    	 return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
    }
	@Override
	public boolean doProcess() {
		return super.doProcess();
		//return true;
	}

	@Override
	public boolean postProcess() {
		String sql = null;
		
		if(super.getCurrentFileName().indexOf("NON-VAR")!=-1){
			 sql =  "{ call [DW].[usp_LoadRiskMeasureNonVar] (" + this.getUniqueId(super.getCurrentFileName()) + ") }"; 
		} else if(super.getCurrentFileName().indexOf("PNL")!=-1){
	 		 logger.info("FNAME:" + super.getCurrentFileName());
	 		 if(this.getsData()!=null && this.getsData().length()>0){ 
	 			 sql =  "{ call [DW].[usp_LoadRiskMeasureVaRPnLVector] (" + this.getUniqueId(super.getCurrentFileName()) + ",'" + this.getsData().toString().split("!@!")[2] + "') }"; 
	 		 } else {
				 sql =  "{ call [DW].[usp_LoadRiskMeasureVaRPnLVector] (" + this.getUniqueId( super.getCurrentFileName()) + ",'null') }"; 
				 
	 			 logger.info("No database connection pnl date was null:" + sql );
	 		 }	 
		} else if( super.getCurrentFileName().indexOf("VAR")!=-1   ){
			 sql =  " {call [DW].[usp_LoadFactVaR]  (" + this.getUniqueId(super.getCurrentFileName()) + ")}";
 		}
		
		
		try{ // && this.getsData().toString().split(super.getDelimeter()).length > 0 
			if(this.getsData()!=null && this.getsData().length()>0 ) { 
				  logger.info("Data row size:" + this.getsData());
					
				if( this.inserHeaderInfo( this.getHeaderSql( this.getContainer(super.getCurrentFileName()) ) )) {	
					  if(!debug){
						  logger.info("-------------Will transfer data from staging to fact table -----------------------");
					   super.getDbUtils().executeSp(sql);
					   
					  } else {
						  logger.info("-------------Will NOT transfer data from staging to fact table -----------------------");
					  }
				 }else {
					  logger.info("------------- Exception encounter while inserting data to header table  -----------------------");
				 }  
			  
			 } else {
				logger.info(  "Nothing was published to DWH:" + this.getHeaderSql( this.getContainer( super.getCurrentFileName())) );
			 }
		} catch(org.springframework.jdbc.BadSqlGrammarException e){
			logger.info("The UniqueID:" + this.getUniqueId(super.getCurrentFileName()) + " exist more than ones in Staging.FactRiskMeasureHeader table.");
		} catch(Exception e){
			e.printStackTrace();
		}

		if( this.getRequestId(super.getCurrentFileName())!=null && !this.getRequestId(super.getCurrentFileName()).equalsIgnoreCase("NULL") ) {
			try{
				if(this.getsData()==null || this.getsData().length()==0){
					super.getDbUtils().executeSp( spUpdateExtractStatus( STATUS.Error));
					logger.info(  "No data pushed to DWH ");
				} else {	
					super.getDbUtils().executeSp( spUpdateExtractStatus( STATUS.Published ));
					logger.info(  "Data already pushed to DWH ");
				}
			} catch(SQLException e){
				e.printStackTrace();
				return false;
			} 
			String portfolioName ="";
			//removeDataInSubCube(this.getContainer(fName),this.getCobDate(fName), portfolioName );
		} else {
		    // removeDataInSubCube(this.getContainer(fName),this.getCobDate(fName), null );
		} 
		
		logger.info(  "done executing post process stored procedure:-" + sql);
		
		
		
//		try {
//		   File afile = new File( super.getFileResolver().getSrcFileName());
//		   logger.info("Moving file to:" + afile.getParent()  + File.separator + "archive   Directory"   );	
//		   FileUtils.moveFileToDirectory(afile, new File( afile.getParent()  + File.separator + "archive"), false);
//		   logger.info("File was moved successful!");
//		} catch (Exception e) {
//			e.printStackTrace();
//		} 
		return true;
	}
	
	private String spUpdateExtractStatus(STATUS  status){
		String  sql =  "{ call [DW].[UpdateSignoffAndExclude] (" + this.getRequestId(super.getCurrentFileName()) + ",'"+ status + "') }"; 
	 	return sql;
	}
	
	private String getUniqueId(String fname){
		fname = fname.replace(".APX", "");
		return fname.split(HASH_SEPARATOR)[5];
	}

	private String getCobDate(String fname){
		fname = fname.replace(".APX", "");
		return fname.split(HASH_SEPARATOR)[4];
	}
	
	private String getContainer(String fname){
		fname = fname.replace(".APX", "");
		return fname.split(HASH_SEPARATOR)[1];
	}
 
	private String getRequestId(String fname){
		fname = fname.replace(".APX", "");
		return fname.split(HASH_SEPARATOR)[3];
	}
	
  private void removeDataInSubCube( String containerName, String cobDate, String portfolioName){

      
	  JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setUsername(wsUserName);    
		factory.setPassword(wsPassword);  
		factory.setServiceClass( IDataManagement.class );
		//wsUrl = "http://localhost:8087/cube/webservices/DataManageMentService";
		
		      
		factory.setAddress(wsUrl);
		
		factory.getInInterceptors().add(new LoggingInInterceptor());
		factory.getOutInterceptors().add(new LoggingOutInterceptor());
			
//			try {
//			factory.setDataBinding(new JAXBDataBindingFactory().create());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
			 
		IDataManagement dataManageMent = (IDataManagement) factory.create();
		
		 DataManagementParamsDTO dataManagementParamsDTO = new DataManagementParamsDTO(); 
		 dataManagementParamsDTO.setsDate(cobDate);
		 dataManagementParamsDTO.setFileName(null);
		 dataManagementParamsDTO.getConditions().put("M_PTFOLIO", containerName);
		 
		dataManageMent.delete(dataManagementParamsDTO);
	  
	  
		
	}
	public static void main(String[] args){
		
		/*String x[] = "a!^!b!^!c".split(  "!\\^!" );
		
		logger.info("!\\^!" );
		
		FileToDB fDb = new FileToDB(null,0);
		
		fDb.setDelimeter("!@!");
		fDb.setRowDelimeter("\r\n|\r|\n|\n\r");
		fDb.setSkipFirstLine(true);
		
		
		fDb.setFileResolver(new PatternFileResolver(null));
	             
				 fDb.validate( new File("C:\\project\\data\\ApExtract\\V1AL0#VaR and P&L#PNL#20120229#1332421300031.apx"));
				 fDb.preProcess();
				 fDb.doProcess();*/
				
		
		/*SimpleDateFormat format = new SimpleDateFormat();
		
		format.p*/
		
	}

	public void setNonVarSourceLength(int nonVarSourceLength) {
		this.nonVarSourceLength = nonVarSourceLength;
	}

	public int getNonVarSourceLength() {
		return nonVarSourceLength;
	}

	public void setVarSourceLength(int varSourceLength) {
		this.varSourceLength = varSourceLength;
	}

	public int getVarSourceLength() {
		return varSourceLength;
	}

	public void setPnlSourceLength(int pnlSourceLength) {
		this.pnlSourceLength = pnlSourceLength;
	}

	public int getPnlSourceLength() {
		return pnlSourceLength;
	}
    
	
	@ManagedOperation(description="Reload Configuration without restarting the cube")
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setWsUrl(String wsUrl) {
		this.wsUrl = wsUrl;
	}

	public String getWsUrl() {
		return wsUrl;
	}

	public void setWsUserName(String wsUserName) {
		this.wsUserName = wsUserName;
	}

	public String getWsUserName() {
		return wsUserName;
	}

	public String getWsPassword() {
		return wsPassword;
	}

	public void setWsPassword(String wsPassword) {
		this.wsPassword = wsPassword;
	}
}
