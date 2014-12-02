package com.quartetfs.pivot.anz.webservices;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

import com.quartetfs.pivot.anz.webservices.impl.DealDetailQueryDTO;
import com.quartetfs.pivot.anz.webservices.impl.DealDetailResultDTO;

@WebService(targetNamespace="http://webservices.quartetfs.com")
@SOAPBinding(style=Style.DOCUMENT, use=Use.LITERAL)
public interface IDealDetailService {
	
	@WebMethod(operationName="getDealDetail")
	@WebResult(name="dealDetailResultDTO")
	public DealDetailResultDTO getDealDetail(@WebParam(name="dealDetailQuery") DealDetailQueryDTO dealDetailQuery);
	
	
}
