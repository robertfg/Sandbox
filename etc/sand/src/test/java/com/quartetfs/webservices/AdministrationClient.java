/*
 * (C) Quartet FS 2012
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.webservices;

import java.net.URL;
import java.util.List;

import com.quartetfs.webservices.ActivePivotDescription;
import com.quartetfs.webservices.AdministrationService;
import com.quartetfs.webservices.AxisDimensionDescription;
import com.quartetfs.webservices.AxisLevelDescription;
import com.quartetfs.webservices.IAdministrationService;
import com.quartetfs.webservices.MeasureDescription;
import com.quartetfs.webservices.PluginDefinition;
import com.quartetfs.webservices.State;

/**
 * 
 * An ActivePivot client that connects to ActivePivot Web Services
 * using java classes generated with the Apache CXF wsdl2java tool.
 * <p>
 * This client expects by default that an ActivePivot application
 * with anonymous access runs at<pre>http://localhost:9090/</pre>
 * 
 * @author Quartet FS
 *
 */
public class AdministrationClient {

	/** Default url */
	public static final String URL = "http://localhost:9090/webservices/Administration?wsdl";
	
	/** Client user name */
	public static final String USERNAME = "admin";
	
	/** Client password */
	public static final String PASSWORD = "admin";
	
	public void run() throws Exception {
		
		// Instantiate service
		System.out.println("Connecting to ActivePivot Administration Service at " + URL + "\n");
		IAdministrationService as = new AdministrationService(new URL(URL)).getAdministrationServicePort();
		
		System.out.println("ActivePivot Version: " + as.getActivePivotVersion());
		
		State managerState = as.retrieveManagerState();
		System.out.println("ActivePivot Manager State: " + managerState);
		
		List<String> schemaIds = as.retrieveSchemaIds();
		System.out.println("Available ActivePivot Schemas:" + managerState);
		for(String id : schemaIds) {
			System.out.println("\t" + id);
		}
		
		List<String> pivotIds = as.retrievePivotIds();
		System.out.println("Available ActivePivot Instances:" + managerState);
		for(String id : pivotIds) {
			System.out.println("\t" + id);
		}

		System.out.print("\nPausing ActivePivot Manager ... ");
		as.changeManagerState(State.PAUSED);
		System.out.println("ActivePivot Manager paused.");
		System.out.print("Resuming ActivePivot Manager ... ");
		as.changeManagerState(State.STARTED);
		System.out.println("ActivePivot Manager resumed.");
		
		for(String id : pivotIds) {
			System.out.println("\nActivePivot Description for " + id);
			ActivePivotDescription ad = as.retrieveActivePivotDescription(id);
			
			System.out.println("\tMeasures");
			for(MeasureDescription measure : ad.getMeasures().getMeasure()) {
				System.out.println("\t  * " + measure.getName() + " " + measure.getAggregationFunctions());
				
				List<PluginDefinition> postProcessors = measure.getPostProcessor();
				if(postProcessors != null && !postProcessors.isEmpty()) {
					for(PluginDefinition pp : postProcessors) {
						System.out.println("\t    * " + pp.getPluginKey());
					}
				}
			}
			
			for(AxisDimensionDescription dim : ad.getDimensions().getDimension()) {
				System.out.println("\tDimension " + dim.getName() + " (type=" + dim.getPluginKey() + ")");
				for(AxisLevelDescription lev : dim.getLevel()) {
					System.out.println("\t  * " + lev.getName());
				}
			}
		}
		
	}
	
	
	/**
	 * 
	 * Launch the client and perform a series of tests.
	 * 
	 * @param parameters
	 */
	public static void main(String[] parameters) throws Exception {
		HTTPAuthenticator.install();
		new AdministrationClient().run();
	}

}
