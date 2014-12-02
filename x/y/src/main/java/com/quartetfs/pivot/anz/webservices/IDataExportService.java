package com.quartetfs.pivot.anz.webservices;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

import com.quartetfs.pivot.anz.webservices.impl.DataExportDTO;

@WebService(targetNamespace="http://webservices.quartetfs.com")
@SOAPBinding(style=Style.DOCUMENT, use=Use.LITERAL)
public interface IDataExportService {

	@WebMethod(operationName="extract")
	public String extract(@WebParam(name="extractParamsDTO")DataExportDTO extractParamsDTO);
}
