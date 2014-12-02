package com.quartetfs.pivot.anz.drillthrough;

import java.util.List;

import com.quartetfs.biz.pivot.dto.DrillthroughRowDTO;

public interface SpreadTypeTranspose {
	public void transpose(List<DrillthroughRowDTO> spreadTypeRows,TransposeRequestInput transposeDTO);
}
