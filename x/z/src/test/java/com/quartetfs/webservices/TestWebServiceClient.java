/*
 * (C) Quartet FS 2012
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.webservices;

import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.quartetfs.pivot.jettyserver.JettyServer;

/**
 * 
 * Executes the various web service clients and checks
 * that no exceptions are thrown.
 * <p>
 * A standard ActivePivot Sandbox application is expected to be running at http://localhost:9090
 * 
 * @author Quartet FS
 *
 */
public class TestWebServiceClient {

	/** Jetty server */
	static Server SERVER;

	@BeforeClass
	public static void launchServer() throws Exception {
		SERVER = JettyServer.createServer(JettyServer.DEFAULT_PORT);
		SERVER.start();
	}
	
	/** Install java.net client side HTTP authentication */
	@BeforeClass
	public static void installAuthenticator() {
		HTTPAuthenticator.install();
	}

	@AfterClass
	public static void stopServer() throws Exception {
		SERVER.stop();
	}

	@Test
	public void testAdministrationClient() throws Exception {
		new AdministrationClient().run();
	}

	@Test
	public void testLicensingClient() throws Exception {
		new LicensingClient().run();
	}

	@Test
	public void testQueriesClient() throws Exception {
		new QueriesClient().run();
	}

	@Test
	public void testStreamingClient() throws Exception {
		new StreamingClient().run();
	}

}
