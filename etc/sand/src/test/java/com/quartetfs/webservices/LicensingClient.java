/*
 * (C) Quartet FS 2012
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.webservices;

import java.net.URL;

import com.quartetfs.webservices.ILicensingService;
import com.quartetfs.webservices.LicensingService;

/**
 * 
 * An ActivePivot client that connects to ActivePivot Web Services
 * using java classes generated with the Apache CXF wsdl2java tool.
 * <p>
 * This client expects by default that an ActivePivot application
 * with anonymous access runs at<pre>http://localhost:9090</pre>
 * 
 * @author Quartet FS
 *
 */
public class LicensingClient {

	/** Default url */
	public static final String URL = "http://localhost:9090/webservices/Licensing?wsdl";
	
	public void run() throws Exception {
		// Instantiate service
		System.out.println("Connecting to ActivePivot Licensing Service at " + URL);
		ILicensingService ls = new LicensingService(new URL(URL)).getLicensingServicePort();
		
		System.out.println("License Id: " + ls.getLicenseId());
		System.out.println("License status: " + ls.getLicenseStatus());
		System.out.println("Hostname restriction: " + ls.getHostname());
		System.out.println("IP Address restriction: " + ls.getIPAddress());
		System.out.println("MAC Address restriction: " + ls.getMacAddress());
		System.out.println("Target environment: " + ls.getEnvironment());
		System.out.println("Maximum number of cores: " + ls.getCoreLimit());
		System.out.println("Start date: " + ls.getStartDate());
		System.out.println("End date: " + ls.getEndDate());
		
		System.out.println();
	}
	
	/**
	 * 
	 * Launch the client and perform a series of tests.
	 * 
	 * @param parameters
	 */
	public static void main(String[] parameters) throws Exception {
		HTTPAuthenticator.install();
		new LicensingClient().run();
	}

}
