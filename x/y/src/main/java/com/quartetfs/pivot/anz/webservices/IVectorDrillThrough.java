package com.quartetfs.pivot.anz.webservices;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

import com.quartetfs.pivot.anz.webservices.impl.VectorDrillthroughDTO;
import com.quartetfs.pivot.anz.webservices.impl.VectorQueryDTO;

@WebService(targetNamespace="http://webservices.quartetfs.com")
@SOAPBinding(style=Style.DOCUMENT, use=Use.LITERAL)
public interface IVectorDrillThrough {
	@WebMethod(operationName="vectorDrillthrough")
	@WebResult(name="vetcorDrillthroughDTO")
	public VectorDrillthroughDTO vectorDrillthrough(@WebParam(name="vectorQueryDTO")VectorQueryDTO varQueryDTO);

}
