package com.quartetfs.pivot.anz.service.export;

import java.util.ArrayList;
import java.util.List;


import java.util.logging.Level;
import java.util.logging.Logger;



import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cube.provider.ILocationProcedure;
import com.quartetfs.pivot.anz.extraction.IRowBuilder;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.utils.QueryHelper;
import com.quartetfs.pivot.anz.webservices.impl.ExtractParamsDTO;

public class VarData implements ILocationProcedure{ 
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);
	
		private IRowBuilder rowBuilder;
		private QueryHelper queryHelper;
		
		private ICellSet cellSet;
		private ExtractParamsDTO extractParamsDTO; 
		
		public VarData( QueryHelper queryHelper,   ICellSet cellSet,
				 IRowBuilder rowBuilder, ExtractParamsDTO extractParamsDTO) {
			
			super();
			
			this.rowBuilder = rowBuilder;
			this.queryHelper = queryHelper;
			this.cellSet = cellSet;
			this.extractParamsDTO = extractParamsDTO;
		
		}
		private List<String[]> varData = new ArrayList<String[]>();;

		@Override
		public boolean execute(ILocation location, int rowId) {
			String[] record = rowBuilder.buildRow(location,queryHelper, cellSet, rowId,  extractParamsDTO );// complete with the
			
			// pnl measure
			if (record == null){
				return false;
			} else if(record.length == 0){
				return true;
			}
					if(record.length>0){
						  varData.add(record); 
					
				     }
						return true;
		}


		public void setVarData(List<String[]> varData) {
			this.varData = varData;
		}
		public List<String[]> getVarData() {
			return varData;
		}
	}