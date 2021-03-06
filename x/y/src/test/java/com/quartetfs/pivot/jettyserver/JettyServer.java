/*
 * (C) Quartet FS 2007-2009
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.jettyserver;


import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * <b>JettyServer</b>
 * 
 * Launches a server on port 8087<br/>
 * For client testing, point client to:<br/>
 * <code>http://localhost:8087/cube/xmla</code><br/>
 * 
 * 
 * @author Quartet Financial Systems
 *
 */
public class JettyServer {  

	/** Root of the web application files, defined relatively to the project root */
	public static final String WEBAPP = "src/main/webapp";
	
	/** Jetty server default port (9090) */
	public static final int DEFAULT_PORT = 8087;

	/**
	 * Configure and launch the standalone server.
	 * @param args only one optional argument is supported: the server port
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {    

		int port = DEFAULT_PORT;   
		if(args != null && args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}  
		
        WebAppContext root = new WebAppContext();
        root.setResourceBase(WEBAPP);
        root.setContextPath("/cube");
        root.setParentLoaderPriority(true);
        
		FilterHolder gzipFilter = new FilterHolder(org.eclipse.jetty.servlets.GzipFilter.class);
		gzipFilter.setInitParameter("mimeTypes", "text/xml,application/x-java-serialized-object");
		root.addFilter(gzipFilter, "/*", EnumSet.of(DispatcherType.REQUEST)); 
        
		// Create server and configure it
		Server server = new Server(port);
		server.setHandler(root);

		// Launch the server
		server.start();		
		server.join();
	}

}
