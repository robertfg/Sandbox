package com.quartetfs.pivot.anz.limits.parsing;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.MDC;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.fwk.messaging.ICSVSource;
import com.quartetfs.fwk.messaging.ICSVSourceListener;
import com.quartetfs.fwk.messaging.IFileInfo;
import com.quartetfs.fwk.messaging.IParsingInfo;
import com.quartetfs.fwk.messaging.ISource;
import com.quartetfs.fwk.messaging.ITransaction;
import com.quartetfs.fwk.util.impl.MappedTuple;
import com.quartetfs.pivot.anz.limits.bo.LimitMasterData;
import com.quartetfs.pivot.anz.limits.bo.LimitMasterData.LimitDataHolder;
import com.quartetfs.pivot.anz.limits.service.LimitEventManager;
import com.quartetfs.pivot.anz.limits.service.LimitLocationResolver;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.ANZUtils;

public class LimitDataFeeder implements ICSVSourceListener
{
	private static final Logger LOGGER = Logger.getLogger(LimitDataFeeder.class.getSimpleName());
	private ConcurrentHashMap<String,List<MappedTuple>> limits = new ConcurrentHashMap<String,List<MappedTuple>>(); 
	private LimitLocationResolver resolver;
	private LimitMasterData limitMasterData;
	private LimitEventManager eventManager;  
	private FastDateFormat dateFormat;
	
	public LimitDataFeeder(LimitLocationResolver resolver,LimitMasterData limitMasterData,LimitEventManager eventManager) {
		super();
		this.resolver = resolver;
		this.limitMasterData=limitMasterData;
		this.eventManager=eventManager;
		this.dateFormat = FastDateFormat.getInstance("dd-MM-yyyy");
	}

	@Override
	public void receive(String subject, Object content, Properties properties,ITransaction trans) 
	{
		try
		{
			@SuppressWarnings("unchecked")
			List<MappedTuple> receivedObjects = (List<MappedTuple>) content;
			LOGGER.info(String.format("Chunk Data received from %s file , size %s",subject,receivedObjects.size()));	
			List<MappedTuple> currentObjectObjects = limits.putIfAbsent(subject,receivedObjects);
			if(currentObjectObjects!=null)
			{
				currentObjectObjects.addAll(receivedObjects);
			}	
		}
		catch(Exception e)
		{
			ANZUtils.createStatusFile(subject, ANZConstants.FILE_EXT_FAILURE);
			throw new QuartetRuntimeException(e);
		} 
		
	}

	@Override
	public void sourceStarted(ISource source) {		
	}

	@Override
	public void sourceStopped(ISource source) {				
	}

	@Override
	public void sourcePaused(ISource source) {				
	}

	@Override
	public void sourceResumed(ISource source){				
	}

	@Override
	public void fileParsingStarted(ICSVSource source, IFileInfo fileInfo) {
		MDC.put(ANZConstants.CONTEXT, fileInfo.getFileName());
		logEvents(fileInfo);
				
	}

	private void logEvents(IFileInfo fileInfo) 
	{
		LOGGER.info(String.format("Parsing of file %s started", fileInfo.getFileAbsolutePath()));		
		Date currentDate =  new Date(System.currentTimeMillis());		
		eventManager.addEvent(dateFormat.format(currentDate),  new Object[]{"PARSE_STARTED",currentDate,fileInfo.getFileAbsolutePath()});
	}

	@Override
	public void fileParsingCompleted(ICSVSource source, IFileInfo fileInfo,IParsingInfo parsingInfo) {
		try
		{
			MDC.put(ANZConstants.CONTEXT, fileInfo.getFileName());
			String name = fileInfo.getFileAbsolutePath();
				
			List<MappedTuple> limitData = limits.remove(name);
			Validate.isTrue(CollectionUtils.isNotEmpty(limitData),String.format("File %s has not data", name));
			LimitDataHolder limitDataHolder = resolver.resolveLimit(limitData);
			LOGGER.info(String.format("File %s contains %s limits", name,limitDataHolder.getLimitDetails().size()));
			IDate limitDate = limitDataHolder.getLimitDetails().get(0).getLimitDate();
			limitMasterData.add(limitDate, limitDataHolder);
			
			ANZUtils.createStatusFile(fileInfo.getFileAbsolutePath(), ANZConstants.FILE_EXT_SUCCESS);
			logEvents(fileInfo, limitDate);
			
		}
		catch(Exception e)
		{
			ANZUtils.createStatusFile(fileInfo.getFileAbsolutePath(), ANZConstants.FILE_EXT_FAILURE);
			LOGGER.log(Level.SEVERE,String.format("Parsing Error %s",fileInfo.getFileAbsolutePath()));
			throw new QuartetRuntimeException(e);
		}
		finally
		{
			MDC.remove(ANZConstants.CONTEXT);
		}
		
	}

	private void logEvents(IFileInfo fileInfo, IDate limitDate) {
		Date currentDate =  new Date(System.currentTimeMillis());		
		eventManager.addEvent(dateFormat.format(currentDate),  new Object[]{"PARSE_DONE",currentDate,fileInfo.getFileAbsolutePath()});
		eventManager.addEvent(dateFormat.format(currentDate),  new Object[]{"LIMIT_LOADED",currentDate,limitDate + "=" + fileInfo.getFileAbsolutePath()});
		LOGGER.info(String.format("Parsing of file %s finished",fileInfo.getFileAbsolutePath()));
	}

}
