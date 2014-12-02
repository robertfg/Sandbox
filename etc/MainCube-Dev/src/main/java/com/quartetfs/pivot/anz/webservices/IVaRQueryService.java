package com.quartetfs.pivot.anz.webservices;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

import com.quartetfs.pivot.anz.webservices.impl.VarDataExtractDTO;
import com.quartetfs.pivot.anz.webservices.impl.VarDrillthroughDTO;
import com.quartetfs.pivot.anz.webservices.impl.VarQueryDTO;

@WebService(targetNamespace="http://webservices.quartetfs.com")
@SOAPBinding(style=Style.DOCUMENT, use=Use.LITERAL)
public interface IVaRQueryService {

	/**
	 * For a given cell, a var type (1 day or 10 days) and a confidence level we retrieve all the underlying pnl values for the underlying vectors that contributed to that cell.
		The displayed pnl is taken from the underlying vector and that pnl value corresponds to the scenario of the var of the selected cell.
		In addition to the pnl, we display the deal number for all the children.
		We display once the scenario date and the scenario name of the selected cell.
		@see VarQueryDTO, VarDrillthroughDTO and VarDealValueDTO.				
	 * @param varQueryDTO
	 * @return VarDrillthroughDTO
	 */
	@WebMethod(operationName="varDrillthrough")
	@WebResult(name="varDrillthroughDTO")
	public VarDrillthroughDTO varDrillthrough(@WebParam(name="varQueryDTO")VarQueryDTO varQueryDTO);

	/**
	 * For a given cell and a var type (1 day or 10 days) we retrieve all the underlying vectors that contributed to that cell. Those vectors are displayed by deal number, we retrieve also the scenario dates vector that is displayed once.
		Notice that you can extract subvector by specifying the from and to parameters in the VarQueryDTO.
		@see VarQueryDTO, VarDataExtractDTO, VarDealVectorDTO.

	 * @param varQueryDTO
	 * @return VarDataExtractDTO
	 */
	@WebMethod(operationName="varDataExtract")
	@WebResult(name="varDataExtractDTO")
	public VarDataExtractDTO varDataExtract(@WebParam(name="varQueryDTO")VarQueryDTO varQueryDTO);
}
