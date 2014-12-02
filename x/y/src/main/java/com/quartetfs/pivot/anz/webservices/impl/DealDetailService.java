package com.quartetfs.pivot.anz.webservices.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebService;

import com.quartetfs.biz.pivot.IActivePivotSchema;
import com.quartetfs.biz.pivot.IProjection;
import com.quartetfs.biz.pivot.webservices.impl.AManagerService;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.IPair;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.filtering.ICondition;
import com.quartetfs.fwk.filtering.ILogicalCondition;
import com.quartetfs.fwk.filtering.impl.AndCondition;
import com.quartetfs.fwk.filtering.impl.EqualCondition;
import com.quartetfs.fwk.filtering.impl.OrCondition;
import com.quartetfs.fwk.filtering.impl.SingleAccessor;
import com.quartetfs.fwk.filtering.impl.SubCondition;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.impl.DateParser;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.webservices.IDealDetailService;
import com.quartetfs.tech.indexer.IProcedure;
import com.quartetfs.tech.indexer.IReader;

@WebService(name = "IDealDetailService", targetNamespace = "http://webservices.quartetfs.com/activepivot", 
		endpointInterface = "com.quartetfs.pivot.anz.webservices.IDealDetailService", serviceName = "DealDetailService")
public class DealDetailService extends AManagerService implements IDealDetailService {

	private String[] fieldsToRetrieve;
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	private DateParser parser ;
	public DealDetailService()
	{
		parser =(DateParser) Registry.getPlugin(IParser.class).valueOf("date[ddMMyyyy]");
	}
	
	@Override
	public DealDetailResultDTO getDealDetail(final DealDetailQueryDTO dealDetailQuery) {
		final DealDetailResultDTO results = new DealDetailResultDTO();
	
		//validation of DealDetailQueryDTO
		if(!validate(dealDetailQuery))
		{
			return results;
		}
		
		//schema
		IActivePivotSchema schema = checkAndRetrieveSchema(ANZConstants.SCHEMA_NAME);
		
		//parse and get the cob date
		IDate cobDate = null;
		
		 String strDate[] = dealDetailQuery.getCobDate();
		 final ICondition cobConditions[] = new ICondition[strDate.length];
		 for (int i = 0; i < strDate.length; i++) {
			try{
				cobDate = Registry.create(IDate.class, parser.parse(strDate[i]).getTime());
				cobConditions[i] = new SubCondition(new SingleAccessor(ANZConstants.COBDATE_IDX_COL_NAME), new EqualCondition(cobDate));
			}catch(Exception e){
				LOGGER.log(Level.SEVERE, MessagesANZ.DATE_NOT_VALID, dealDetailQuery.getCobDate());
				return results;	
			}
		 }
						 	
		final ILogicalCondition orCondition = new OrCondition(cobConditions);						 
		
		final ICondition conditions[] = new ICondition[2];
		conditions[0] = new SubCondition(new SingleAccessor(ANZConstants.DEAL_NUM_FIELD), new EqualCondition(dealDetailQuery.getDealNumber()));
		conditions[1] = orCondition;
		 
		final ILogicalCondition andCondition = new AndCondition(conditions);
		
		 
		schema.getIndexer().execute(
				Collections.<IPair<ICondition, IProcedure<IProjection>>> singleton(new com.quartetfs.fwk.impl.Pair<ICondition, IProcedure<IProjection>>(
						andCondition, new IProcedure<IProjection>() {

							private static final long serialVersionUID = 8392065783873046105L;

							public boolean supportsParallelExecution() {
								return false;
							}

							public boolean execute(IReader<IProjection> reader) {
								reader.setTuplePattern( fieldsToRetrieve );
							
								List<DealFiguresDTO> deals = new ArrayList<DealFiguresDTO>();
								int recCount = 0;
								
								while (reader.hasNext()) {
									reader.next();
									if(recCount< dealDetailQuery.getMaxResults() ){
									   Object[] temp = reader.readTuple();
										DealFiguresDTO dealsFigureDto = new DealFiguresDTO();
										
										dealsFigureDto.setMresult((Double) temp[2]);
										dealsFigureDto.setMresultv((Double) temp[3]); 
										dealsFigureDto.setOneDayVar((double[]) temp[4]);
										dealsFigureDto.setTenDayVar((double[]) temp[5]);
										dealsFigureDto.setCobDate(((IDate)temp[1]).toString());										
									    deals.add(dealsFigureDto);									    
								   }	
									recCount ++;
								}
							    results.setDealNumber(dealDetailQuery.getDealNumber() );
								results.setNoOfReturnRecords(deals.size());								
								results.setDeals(deals);
								return true;
							}
							@Override
							public void complete() {
							}

						}))

		);
		return results;
	}

	private boolean validate(DealDetailQueryDTO dealDetailQuery) 
	{
		if (dealDetailQuery == null){
			LOGGER.log(Level.SEVERE, MessagesANZ.DEAL_DETAIL_QUERY_NULL_DTO);
			return false;	
		}
		
		if (dealDetailQuery.getDealNumber() == 0){
			LOGGER.log(Level.SEVERE, MessagesANZ.DEAL_DETAIL_QUERY_DEAL_NUMBER_ZERO);
			return false;	
		}
		
		if (dealDetailQuery.getCobDate() == null){
			LOGGER.log(Level.SEVERE, MessagesANZ.DEAL_DETAIL_QUERY_NULL_COBDATE);
			return false;	
		}
		
		if (dealDetailQuery.getMaxResults() == 0){
			LOGGER.log(Level.SEVERE, MessagesANZ.DEAL_DETAIL_QUERY_MAX_RESULT_ZERO);
			return false;	
		}
		return true;
	}

	public void setFieldsToRetrieve(String[] fieldsToRetrieve) {
		this.fieldsToRetrieve = fieldsToRetrieve;
	} 
}