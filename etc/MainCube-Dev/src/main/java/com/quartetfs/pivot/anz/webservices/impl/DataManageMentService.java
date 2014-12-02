package com.quartetfs.pivot.anz.webservices.impl;

import java.util.Map;
import java.util.Properties;

import javax.jws.WebService;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.impl.DateParser;
import com.quartetfs.pivot.anz.service.impl.CubeCleaner;
import com.quartetfs.pivot.anz.webservices.IDataManagement;


@WebService(name="IDataManagement",
		targetNamespace="http://webservices.quartetfs.com/activepivot",
		endpointInterface = "com.quartetfs.pivot.anz.webservices.IDataManagement",
		serviceName = "DataManageMentService")
public class DataManageMentService implements IDataManagement{
	
	 public DataManageMentService() {
	  this.parser =(DateParser) Registry.getPlugin(IParser.class).valueOf("date[yyyyMMdd]");
     
	 }

	 private CubeCleaner cubeCleaner;
	 private DateParser parser;
	 
	 
	@Override
	public int delete(DataManagementParamsDTO dataManagementParamsDTO) {
		Properties props = new Properties();
		
		IDate cobDate = Registry.create(IDate.class, parser.parse(dataManagementParamsDTO.getsDate()).getTime());
		
		 Map<String,Object> conditions = dataManagementParamsDTO.getConditions();
		 
		  for( Map.Entry<String, Object> datas : conditions.entrySet()) {
			  props.put(datas.getKey(), (String) datas.getValue());
		  }
		
		  try{
		  // cubeCleaner.removeByCondition(cobDate, dataManagementParamsDTO.getFileName(), props);
		  }catch(Exception e){
			  
		  }
		
		return 0;
		
		
		
	}


	public void setCubeCleaner(CubeCleaner cubeCleaner) {
		this.cubeCleaner = cubeCleaner;
	}

	public CubeCleaner getCubeCleaner() {
		return cubeCleaner;
	}

	
}

