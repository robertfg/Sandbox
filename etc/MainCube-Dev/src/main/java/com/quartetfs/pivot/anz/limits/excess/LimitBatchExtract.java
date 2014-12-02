package com.quartetfs.pivot.anz.limits.excess;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.pivot.anz.limits.bo.LimitDetail;
import com.quartetfs.pivot.anz.limits.extract.LimitExposureExtract;
import com.quartetfs.pivot.anz.limits.service.LimitUtil;
import com.quartetfs.pivot.anz.webservices.dto.ExtractExcessParam;

public class LimitBatchExtract implements Callable<Map<String,Map<String,Double>>>{
	

	private List<LimitDetail> limitDetails;
	private LimitUtil limitUtil;
	private ExtractExcessParam excessParam; 
	 
	public LimitBatchExtract( List<LimitDetail> limitDetails, LimitUtil limitUtil,ExtractExcessParam excessParam ){
		this.limitDetails = limitDetails;
		this.limitUtil = limitUtil;
		this.excessParam = excessParam;
	}
	
	
	@Override
	public Map<String,Map<String,Double>> call() throws Exception {
		
		LimitExposureExtract limitExpExtract = new LimitExposureExtract( limitUtil,excessParam );
							return limitExpExtract.extract( limitDetails);
		
		
	}  

	
	
	
}
