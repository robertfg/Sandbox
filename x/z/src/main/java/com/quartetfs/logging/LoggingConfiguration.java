/*
 * (C) Quartet FS 2013
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.logging;

import static java.util.logging.Level.*;
import static java.util.logging.Logger.getLogger;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * java.util.logging configuration class with the following features:
 * <ul>
 * <li>Define independently the log levels of the main ActivePivot components
 * <li>Define both a console handler and a file handler
 * <li>Apply the QFS formatter to enrich log records with the current thread
 * and the current user.
 * </ul>
 * <p>
 * To load this class at the JVM startup time, add the following VM option:
 * <br>-Djava.util.logging.config.class=com.quartetfs.logging.LoggingConfiguration</br>
 * 
 * <p>
 * This configuration class is designed to be used programmatically, when
 * deploying in the Jetty server. When deploying in Apache Tomcat, use
 * the logging.properties based configuration.
 * 
 * @author Quartet FS
 *
 */
public class LoggingConfiguration {

	public LoggingConfiguration() throws IOException {
		
		// Console Handler
		Handler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		consoleHandler.setFormatter(new QFSFormatter());

		// File Handler
		Handler fileHandler = new FileHandler("logs/activepivot-sandbox.log");
		fileHandler.setLevel(Level.ALL);
		fileHandler.setFormatter(new QFSFormatter());
		
		Logger root = Logger.getLogger("");
		root.addHandler(consoleHandler);
		root.addHandler(fileHandler);


		// DEFINE LOG LEVELS
		
		getLogger("quartetfs.composer").setLevel(INFO);
		getLogger("quartetfs.composer.serialization").setLevel(INFO);

		getLogger("quartetfs.tech.indexer").setLevel(INFO);
		getLogger("quartetfs.tech.indexer.transactions").setLevel(INFO);
		getLogger("quartetfs.tech.indexer.queries").setLevel(INFO);
		getLogger("quartetfs.tech.streaming").setLevel(INFO);
		getLogger("quartetfs.activepivot.level").setLevel(INFO);
		getLogger("quartetfs.activepivot.queries").setLevel(FINE);
		getLogger("quartetfs.activepivot.transactions").setLevel(INFO);
		getLogger("quartetfs.activepivot.distribution").setLevel(INFO);
		getLogger("quartetfs.activepivot.xmla").setLevel(INFO);
		getLogger("quartetfs.activepivot.xmla.performance").setLevel(INFO);
		getLogger("quartetfs.activepivot.mdx").setLevel(INFO);
		getLogger("quartetfs.activepivot.pivolap").setLevel(INFO);
		getLogger("quartetfs.activepivot.server").setLevel(INFO);
		getLogger("quartetfs.messaging.csv").setLevel(INFO);

		getLogger("quartetfs.activepivot.sandbox").setLevel(INFO);

		getLogger("org.apache.cxf").setLevel(INFO);

	}
	
}