package com.quartetfs.pivot.anz.webservices;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

import com.quartetfs.biz.pivot.dto.DrillthroughResultDTO;
import com.quartetfs.biz.pivot.query.IDrillthroughQuery;

@WebService(targetNamespace="http://webservices.quartetfs.com")
@SOAPBinding(style=Style.DOCUMENT, use=Use.LITERAL)
public interface ITransposeDrillThroughServiceSR {
	/**
	 * Execute a Transpose "DRILLTHROUGH" query.
	 *
	 * @param query the "DRILLTHROUGH" query.
	 * @return The list of drillthrough rows.
	 */
	@WebMethod(operationName="executeDrillthrough")
	public @WebResult(name="drillthroughResult") DrillthroughResultDTO execute(@WebParam(name="query") IDrillthroughQuery query);
	



}
