package com.quartetfs.pivot.anz.webservices.impl;

import java.util.logging.Logger;

import javax.jws.WebService;

import org.apache.commons.lang.Validate;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.impl.DateParser;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.service.export.ExportDataHelper;
import com.quartetfs.pivot.anz.webservices.IDataExportService;

@WebService(name = "IDataExportService", targetNamespace = "http://webservices.quartetfs.com/activepivot", 
endpointInterface = "com.quartetfs.pivot.anz.webservices.IDataExportService", serviceName = "DataExportService")
public class DataExportService implements IDataExportService {

	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	private ExportDataHelper exportDataHelper;
	private DateParser parser ;
	
	public DataExportService()
	{
		parser =(DateParser) Registry.getPlugin(IParser.class).valueOf("date[yyyyMMdd]");
	}
	
	@Override
	public String extract(DataExportDTO extractParamsDTO) {
		
		Validate.isTrue(extractParamsDTO.getCobDate()!=null, "COB date is null");
		Validate.isTrue(extractParamsDTO.getContainerName()!=null, "Container is null");		
		
		LOGGER.info(String.format("Export Data for %s",extractParamsDTO));		
		String requestToken = constructFileName(extractParamsDTO) + String.valueOf(System.currentTimeMillis());	
		
		exportDataHelper.enqueue(extractParamsDTO, Registry.create(IDate.class, parser.parse(extractParamsDTO.getCobDate()).getTime()), requestToken);
		return requestToken;
	}
	
	
	public void setExportDataHelper(ExportDataHelper exportDataHelper) {
		this.exportDataHelper = exportDataHelper;
	}	

	private String constructFileName(DataExportDTO extractParamsDTO){
		return "AP_" + extractParamsDTO.getContainerName() + "_" + extractParamsDTO.getCobDate() + "_" + extractParamsDTO.getJobId() + "_"; 
	}
	
}
