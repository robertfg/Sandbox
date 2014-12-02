package com.quartetfs.pivot.anz.extraction;

import java.util.List;
import java.util.logging.Logger;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.service.export.ExtractHeader;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.QueryHelper;
import com.quartetfs.pivot.anz.webservices.impl.ExtractParamsDTO;
 
public class SimpleRowBuilder implements IRowBuilder {

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	private List<ExtractHeader> nonVarCsvMapping;
	
	public SimpleRowBuilder( ExtractUtils extractUtils   ) {
	 	this.nonVarCsvMapping = extractUtils.getNonVarCsvOutputMapping();
	} 
	
	
	@Override
	public String[] buildRow(ILocation location, QueryHelper queryHelper,ICellSet cellSet, int rowId,ExtractParamsDTO extractParamDto) {
        return this.mapResultValue(location, queryHelper, cellSet, rowId, extractParamDto.getFileBatchId());
    }
	
   private String[] mapResultValue(ILocation location, QueryHelper queryHelper,ICellSet cellSet, int rowId, String uniqueId){
	  
	   
	   int csvSize     =  nonVarCsvMapping.size();
	   String[] csvArr =  new String[csvSize-1];
	   
		  for (ExtractHeader node : nonVarCsvMapping) {
			  String src  = node.getType();// .valueOf("@type");
				 String name = node.getName();// .valueOf("@name");
				 String overWrite = node.getOverWriteIf();// .valueOf("@overWriteIf");
				 String containerName = queryHelper.retrieveValue("Container@Container", location).toString(); 
		
				 int arrPos =  node.getIndex();
			
				 if( src.equals("var") ){
					 if(name.equalsIgnoreCase("DbId") ){ 
						 csvArr[ arrPos ] = uniqueId;
					 }
				 } else if(src.equals("dim")){
					 if(overWrite!=null && overWrite.length() > 0){
						 String containers[] = overWrite.split(",");
						 for (int i = 0; i < containers.length; i++) {
							 if(containers[i].equalsIgnoreCase(containerName)){
								 csvArr[ arrPos ] =  (String)queryHelper.retrieveValue(name, location);	 
								 break;
							 }
						 }
						 containers = null;
					 } else {
						 csvArr[ arrPos ] =  (String)queryHelper.retrieveValue(name, location);	 
					 } 
				 } else if(src.equals("msr")){
					   Object value = getMeasureValue(containerName,name,rowId,cellSet);
					  csvArr[ arrPos ] = value == null ? "" : String.valueOf( getValue(value) ) ;
				}
		   }
	   return csvArr;
	   
   }	
   
   
   //must put in configuration
   private Object getMeasureValue(String containerName, String measureName,int rowId, ICellSet cellSet){
	   Object measureValue = null;
	   if (containerName.equalsIgnoreCase(ANZConstants.FXO_CONTAINER)) {
			if (measureName.equalsIgnoreCase(ANZConstants.M_RESULT_MEASURE)) {
				measureValue = cellSet.getCellValue(rowId,ANZConstants.FXDELGAMMA_RESULT);
			} else if (measureName.equalsIgnoreCase(ANZConstants.M_RESULTV_MEASURE)) {
				measureValue = cellSet.getCellValue(rowId,ANZConstants.FXDELGAMMAV_RESULTV);
			}
			
		} else if (containerName.equalsIgnoreCase("IR_GAMMA")) {
			
			if (measureName.equalsIgnoreCase(ANZConstants.M_RESULT_MEASURE)) {
				measureValue = cellSet.getCellValue(rowId,	ANZConstants.IRGAMMA_VECTOR_MRESULT);
			} else if (measureName.equalsIgnoreCase(ANZConstants.M_RESULTV_MEASURE)) {
				measureValue = cellSet.getCellValue(rowId, ANZConstants.IRGAMMA_VECTOR_MRESULTV );
			}
		} else if ( containerName.equalsIgnoreCase("GAMMA_BASIS")) {
			if (measureName.equalsIgnoreCase(ANZConstants.M_RESULT_MEASURE)) {
				measureValue = cellSet.getCellValue(rowId,	ANZConstants.GAMMA_BASIS_VECTOR_RESULT);
			} else if (measureName.equalsIgnoreCase(ANZConstants.M_RESULTV_MEASURE)) {
				measureValue = cellSet.getCellValue(rowId, ANZConstants.GAMMA_BASIS_VECTOR_RESULTV);
			}
		} else if ( containerName.equalsIgnoreCase( ANZConstants.VAR_CONTAINER )) {
			if (measureName.equalsIgnoreCase(ANZConstants.M_RESULT_MEASURE)) {
				measureValue = cellSet.getCellValue(rowId,"HypoPL_scenario_AUD.SUM");
			} else if (measureName.equalsIgnoreCase(ANZConstants.M_RESULTV_MEASURE)) {
				measureValue = 0;
			}
		} else {
			measureValue = cellSet.getCellValue(rowId, measureName);
		}
	   return measureValue;
	   
	   
   }
   
   public Object getValue (Object value){
		if (value instanceof double[]){
			double[] vector=(double[])value;
			return doubleToString(vector);			
		}
		return value;
	}
	
	private String doubleToString(double[] values)		{
		
		StringBuilder sb = new StringBuilder();
		for(double o : values)
		{
			sb.append(o).append("|");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
  
   
   
   
	
	
	
	
	
	
}
