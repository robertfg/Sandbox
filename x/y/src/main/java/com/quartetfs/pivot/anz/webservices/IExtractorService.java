/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.webservices;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;

import com.quartetfs.pivot.anz.webservices.impl.ExtractParamsDTO;
/**
 * Extraction of data into a csv file that will be imported into the Data Ware House
 * @author Quartet FS
 *
 */
@WebService(targetNamespace="http://webservices.quartetfs.com")
@SOAPBinding(style=Style.DOCUMENT, use=Use.LITERAL)
public interface IExtractorService {

	@WebMethod(operationName="extract")
	public void extract(@WebParam(name="extractParamsDTO")ExtractParamsDTO extractParamsDTO);
	
	@WebMethod(operationName="extractMultipleContainer")
	public void extract(@WebParam(name="extractParamsDTO")ExtractParamsDTO extractParamsDTO, @WebParam(name="containers")String[] containers );
	
	@WebMethod(operationName="extractAtHierarchyLevel")
	public void extractAtHierarchyLevel(@WebParam(name="extractParamsDTO")ExtractParamsDTO extractParamsDTO);
	
	@Oneway
	@WebMethod
	public void doSignOff(@WebParam(name="batchId") String batchId);
	
	@Oneway
	@WebMethod(operationName="signOffWithMultipleContainer")
	public void doSignOff(@WebParam(name="cobDate") String cobDate,@WebParam(name="previousCobDate") String previousCobDate,
			               @WebParam(name="containerNameList")  String[] containerList);
	
	@WebMethod(operationName="reloadConfig")
	public void reloadConfig();
	 
	@WebMethod(operationName="extractVAR")
	public void extractVAR(@WebParam(name="extractParamsDTO")ExtractParamsDTO extractParamsDTO);
	
	@WebMethod(operationName="extractNonVAR")
	public void extractNonVAR(@WebParam(name="extractParamsDTO")ExtractParamsDTO extractParamsDTO);
	
	@WebMethod(operationName="triggerExtractFromFeedMonitoring")
	public void triggerExtractFromFeedMonitoring(@WebParam(name="currentCobDateInYYYYMMDD") int currentCobDate, 
			@WebParam(name="previousCobDateInYYYYMMDD") int previousCobDate,
			@WebParam(name="jobId") int jobId, @WebParam(name="containerName")  String containerName   );

	
	@WebMethod(operationName="testCode")
	public void testCode(@WebParam(name="cobDate") int cobDate);
	
}
