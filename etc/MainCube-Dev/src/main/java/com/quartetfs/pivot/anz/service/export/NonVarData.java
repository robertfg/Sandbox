package com.quartetfs.pivot.anz.service.export;

import java.util.List;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;

public class NonVarData implements ILocationProcedure{


	
	@Override
	public boolean execute(ILocation location, int rowId) {
	/*	StringBuilder temp = rowBuilder.buildRow(location,queryHelper, cellSet, rowId,  extractParamsDTO );// complete with the
		// pnl measure
		if (temp == null){
			return false;
		} else if(temp.length()==0){
			return true;
		}
			varData.add(  temp.toString().split(this.columnDelimeter) );*/
			return true;
	}


	
}
