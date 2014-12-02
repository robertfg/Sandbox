package com.quartetfs.pivot.anz.extraction;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.types.impl.Date;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.service.export.ExtractObject.ExtractType;
import com.quartetfs.pivot.anz.utils.QueryHelper;
import com.quartetfs.pivot.anz.webservices.impl.ExtractParamsDTO;

public class VaRRowBuilder implements IRowBuilder {
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	private List<String> levels=Collections.emptyList();
	private List<String> measures=Collections.emptyList();
	
	
	public VaRRowBuilder(List<String> levels, List<String> measures/*,  List<String> mandatoryHierarchy*/) {
		this.levels = levels;
		this.measures = measures;
	}   
	
	@Override
	public String[] buildRow(ILocation location, QueryHelper queryHelper,ICellSet cellSet, int rowId, ExtractParamsDTO extractParamDto) {
		String currentLabel = null;
		String[] csvValue = null;
		
		if(extractParamDto.getExtractType().equals(ExtractType.HYPO)){
			csvValue = new String[7];
		}else {
			csvValue = new String[50];
		}
		
		
		try {
			csvValue[0] = extractParamDto.getFileBatchId();
			int ctr = 1;
			
			for (String level : levels) {
				 currentLabel = level;
				 if(level.equalsIgnoreCase("Container@Container")){
					 csvValue[ctr] = extractParamDto.getVarType();            					 
				 }else{
					 csvValue[ctr] =  getValue(queryHelper.retrieveValue( level, location));
				 } 
				 ctr++;
			}
			
			int levelCtr = ctr ;
			for (String measure : measures) {
				currentLabel = measure;
				Object value = cellSet.getCellValue(rowId, measure);
				csvValue[levelCtr] = value == null ? "" : getValue(value);
				levelCtr++;
			} 
			
		}catch (Exception e) {
			LOGGER.log(Level.SEVERE, MessagesANZ.EXTRACTION_FIELDS_ISSUE,location.toString());
			LOGGER.log(Level.SEVERE, currentLabel, e);
			return null;
		}
		return csvValue; 
		
	}
	
	public String getValue(Object value) {
		if (value instanceof double[]) {
			double[] vector = (double[]) value;
			return doubleToString(vector);
		}else if (value instanceof Double){
			return String.valueOf(value);
		}else if (value instanceof Date){
			return ((Date)value).toString();
		}else if (value instanceof String){
			return String.valueOf(value);
			
		} else {
			
		//	System.out.println(value);
		}
		return "";
	}
	
	private String doubleToString(double[] values) {

		StringBuilder sb = new StringBuilder();
		for (double o : values) {
			sb.append(o).append("|");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	

}
