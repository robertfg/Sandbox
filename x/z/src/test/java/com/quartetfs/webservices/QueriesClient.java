/*
 * (C) Quartet FS 2012
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.webservices;

import java.net.URL;
import java.util.List;

import com.quartetfs.webservices.CellSetDTO;
import com.quartetfs.webservices.IQueriesService;
import com.quartetfs.webservices.MdxQuery;
import com.quartetfs.webservices.QueriesService;

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
public class QueriesClient {

	/** Default url */
	public static final String URL = "http://localhost:9090/webservices/Queries?wsdl";
	
	public void run() throws Exception {

		// Instantiate service
		System.out.println("Connecting to ActivePivot Queries Service at " + URL);
		IQueriesService qs = new QueriesService(new URL(URL)).getQueriesServicePort();

		// Retrieve the default members of each dimensions
		retrieveDefaultMembers(qs, "EquityDerivativesCube");

		// Execute an MDX query
		executeMDX(qs, "SELECT NON EMPTY {[Bookings].Members} ON COLUMNS, {[Underlyings].Members} ON ROWS FROM [EquityDerivativesCube] WHERE ([Measures].[pnl.SUM])");
		
		// Return the existing flat context values properties
		getFlatContextValueProperties(qs);
		
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
		new QueriesClient().run();
	}

	protected static void retrieveDefaultMembers(IQueriesService qs, String pivotId) {
		System.out.println("Retrieving default members for each dimension in " + pivotId);
		List<String> dimensions = qs.retrieveDimensions(pivotId);
		List<String> defaultMembers = qs.retrieveDefaultMembers(pivotId);
		for(int d = 0; d < dimensions.size(); d++) {
			String dim = dimensions.get(d);
			String dm = defaultMembers.get(d);
			System.out.println("   " + dim + ": " + dm);
		}
	}
	
	protected static void executeMDX(IQueriesService qs, String mdx) {
		MdxQuery query = new MdxQuery();
		query.setMDX(mdx);
		CellSetDTO cellSet = qs.executeMDX(query);
		
		new CellSetPrinter(cellSet).print(System.out);
	}
	
	protected static void getFlatContextValueProperties(final IQueriesService qs) {
		final List<Entry> entries = qs.getFlatContextValueProperties().getEntry();
		for (final Entry entry: entries) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
	}


}
