package com.quartetfs.pivot.anz.limits.excess;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.query.QueryException;
import com.quartetfs.pivot.anz.limits.bo.LimitDetail;
import com.quartetfs.pivot.anz.limits.bo.LimitMasterData.LimitDataHolder;

public interface LimitExcessExtract {
	public Map<String,Double> extract(IDate limitDate) throws QueryException;
	public Map<String,Double> extract(IDate limitDate,List<LimitDetail> limitDetails) throws QueryException;
	
}
