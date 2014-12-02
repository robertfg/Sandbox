package com.quartetfs.pivot.anz.webservices;

import java.io.IOException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

import com.quartetfs.fwk.query.QueryException;
import com.quartetfs.pivot.anz.webservices.dto.ExtractExcessParam;
import com.quartetfs.pivot.anz.webservices.dto.ExtractLimitDTO;
import com.quartetfs.pivot.anz.webservices.dto.LimitMasterDataDTO;

@WebService(targetNamespace="http://webservices.quartetfs.com")
@SOAPBinding(style=Style.DOCUMENT, use=Use.LITERAL)
public interface ILimitService {

	@WebMethod(operationName="extractExcess")
	@WebResult(name="extractLimitDTO")
	public ExtractLimitDTO extractExcess(@WebParam(name="extractExcessParam")ExtractExcessParam param);

	@WebMethod(operationName="generateLimit")
	@WebResult(name="extractLimitDTO")
	public ExtractLimitDTO extractLimit(@WebParam(name="limitMasterDataDTO")LimitMasterDataDTO limit) throws QueryException, IOException;


	
	

}
