package com.quartetfs.pivot.anz.drillthrough;

import java.util.List;

import com.quartetfs.biz.pivot.dto.DrillthroughRowDTO;
import com.quartetfs.pivot.anz.drillthrough.impl.CapFloorSpreadTypeTranspose;
import com.quartetfs.pivot.anz.drillthrough.impl.ETOSpreadTypeTranspose;
import com.quartetfs.pivot.anz.drillthrough.impl.NASpreadTypeTranspose;
import com.quartetfs.pivot.anz.drillthrough.impl.SwaptionSpreadTypeTranspose;

public enum SpreadType implements SpreadTypeTranspose {
	NA
	{
		@Override
		public void transpose(List<DrillthroughRowDTO> spreadTypeRows,TransposeRequestInput transposeDTO) 
		{
			 new NASpreadTypeTranspose(transposeDTO).transpose(spreadTypeRows);
		}
	},
	ETO{
		@Override
		public void transpose(List<DrillthroughRowDTO> spreadTypeRows,TransposeRequestInput transposeDTO) 
		{
			 new ETOSpreadTypeTranspose(transposeDTO).transpose(spreadTypeRows);
		}
	},
	CapFloor
	{
		@Override
		public void transpose(List<DrillthroughRowDTO> spreadTypeRows,TransposeRequestInput transposeDTO) 
		{
			 new CapFloorSpreadTypeTranspose(transposeDTO).transpose(spreadTypeRows);
		}
	}	,
	Swaption
	{
		@Override
		public void transpose(List<DrillthroughRowDTO> spreadTypeRows,TransposeRequestInput transposeDTO) 
		{
			 new SwaptionSpreadTypeTranspose(transposeDTO).transpose(spreadTypeRows);
		}	
	};
	@Override
	public void transpose(List<DrillthroughRowDTO> spreadTypeRows, TransposeRequestInput transposeDTO) {
		
	}
	
}
