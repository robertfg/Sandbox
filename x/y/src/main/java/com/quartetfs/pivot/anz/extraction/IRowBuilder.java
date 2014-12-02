package com.quartetfs.pivot.anz.extraction;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.pivot.anz.utils.QueryHelper;
import com.quartetfs.pivot.anz.webservices.impl.ExtractParamsDTO;

public interface IRowBuilder {
//	public StringBuilder buildRow(ILocation location, QueryHelper queryHelper,ICellSet cellSet,int rowId, boolean addScenarioDate, String uniqueId, ExtractParamsDTO extractParamDto);

//	public StringBuilder buildRow(ILocation location, QueryHelper queryHelper,ICellSet cellSet,int rowId, ExtractParamsDTO extractParamDto);

	public String[] buildRow(ILocation location, QueryHelper queryHelper,ICellSet cellSet,int rowId, ExtractParamsDTO extractParamDto);

}
