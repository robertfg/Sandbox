package com.quartetfs.pivot.anz.source.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;

import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.model.IVRParsingEntry;
import com.quartetfs.pivot.anz.model.impl.Deal;
import com.quartetfs.pivot.anz.service.impl.PSRService;
import com.quartetfs.pivot.anz.service.impl.ValueHolderService.ValueType;
import com.quartetfs.pivot.anz.source.IRunParseData;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.ANZUtils;

public class PSRData implements IRunParseData
{
	
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
	private final String varSeparator=ANZConstants.PIPE_SEPARATOR;
	
	private String psrName;
	private String containerName;
	private String fileName;
	private ValueType valueType;
	private ConcurrentLinkedQueue<Deal> values=new ConcurrentLinkedQueue<Deal>();
	private PSRService psrService;
	private Set<String> mergedPSR = new HashSet<String>();
	
	public PSRData(String psrName, String containerName,ValueType valueType,String fileName,PSRService psrService) {
		super();
		this.psrName = psrName;
		this.containerName = containerName;
		this.valueType=valueType;		
		this.fileName = fileName;
		this.psrService= psrService;
		mergedPSR.add(psrName);
	}
	
	/**
	 * Generic Algorithm for each deal.
	 * 1. enrich deal for static data
	 * 2. use valuetype to clean/parse value (result,reultv or VAR)
	 * 3. add into collection
	 * 
	 */	
	
	
	
	private void validatePsrName(String dealPsrName)
	{	
		Validate.notNull(dealPsrName, String.format("PSR name not available on deal in file %s", fileName));
		//Validate.isTrue(psrName.equals(dealPsrName),String.format("Deal psr %s does not match with file psr %s" , dealPsrName,psrName));		
	}
	
	@Override
	public void contribute(List<IVRParsingEntry> list, int from, int toExcluded) throws Exception {
		
		for (int i = from; i < toExcluded; i++) 
		{
			IVRParsingEntry entry = list.get(i);
			Deal deal=null;
			try 
			{
				deal = entry.getDeal();
			    
			    if(psrName.equals("QDVAR")){
			    	psrName = "V1AL0";
			    }
			    LOGGER.info("ANZConstants.PSR_NAME:" + psrName );
			    
			    deal.getAttributes().put(ANZConstants.PSR_NAME, psrName);
				valueType.clean(deal, varSeparator);
				values.add(deal);
			} 
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, ANZUtils.formatMessage(MessagesANZ.DEAL_PARSING_ERR, fileName, deal.toString()), e);
				psrService.errorWithFileLoad(fileName,e);
				throw e;
			}
		}		
	}	
	
	@Override
	public String getContainerName() {
		return containerName;
	}
	
	public String getFileName() {
		return fileName;
	}

	@Override
	public String getPSRName() {
		return psrName;
	}


	@Override
	public int getValueCount() {
		return values.size();
	}

	@Override
	public Collection<Deal> deals() {
		return values;
	}

	@Override
	public Set<String> mergedPSR() {
		return mergedPSR;
	}
	
	
}
