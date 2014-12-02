/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.client;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.dto.CellDTO;
import com.quartetfs.biz.pivot.dto.CellSetDTO;
import com.quartetfs.biz.pivot.dto.DrillthroughResultDTO;
import com.quartetfs.biz.pivot.dto.DrillthroughRowDTO;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.impl.LocationUtil;
import com.quartetfs.biz.pivot.query.IMDXQuery;
import com.quartetfs.biz.pivot.query.impl.DrillthroughQuery;
import com.quartetfs.biz.pivot.query.impl.MDXQuery;
import com.quartetfs.biz.pivot.server.webservices.impl.WebServiceFactory;
import com.quartetfs.biz.pivot.webservices.IQueriesService;
import com.quartetfs.biz.pivot.webservices.impl.JAXBDataBindingFactory;
import com.quartetfs.pivot.sandbox.impl.MessagesSandbox;
import com.quartetfs.tech.streaming.IBulkedStreamEvents;
import com.quartetfs.tech.streaming.IDomainStreamEvent;
import com.quartetfs.tech.streaming.IIdGenerator;
import com.quartetfs.tech.streaming.ILongPollingService;
import com.quartetfs.tech.streaming.IStreamEvent;
import com.quartetfs.tech.streaming.IStreamProperties;
import com.quartetfs.tech.streaming.IStreamProperties.InitialState;
import com.quartetfs.tech.streaming.IStreamingService;
import com.quartetfs.tech.streaming.impl.StreamProperties;

/**
 *
 * A simple web service client to illustrate web service
 * consumption from a Java client.
 *
 * @author Quartet Financial Systems
 *
 */
public class WebServiceClient {

	/** the logger **/
	private static Logger logger = Logger.getLogger(MessagesSandbox.LOGGER_NAME, MessagesSandbox.BUNDLE);

	/** Base url for web services */
	static final String BASE_URL = "http://localhost:9090/webservices/";

	/** Publication domain */
	static final String PUBLICATION_DOMAIN = "test-domain";

	/** User name */
	static final String USER = "admin";

	/** User password */
	static final String PASSWORD = "admin";

	/**
	 * You can run this sample client once the Sandbox server has been started.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// Create a JAXB data binding
		JAXBDataBindingFactory bindingFactory = new JAXBDataBindingFactory();

		// Create a JAX-WS service factories for the target services.
		WebServiceFactory<IQueriesService> queriesServiceFactory = new WebServiceFactory<IQueriesService>();
		queriesServiceFactory.setAddress(BASE_URL + "Queries");
		queriesServiceFactory.setServiceClass(IQueriesService.class);
		initFactory(queriesServiceFactory, bindingFactory);

		WebServiceFactory<IStreamingService> streamingServiceFactory = new WebServiceFactory<IStreamingService>();
		streamingServiceFactory.setAddress(BASE_URL + "Streaming");
		streamingServiceFactory.setServiceClass(IStreamingService.class);
		initFactory(streamingServiceFactory, bindingFactory);

		WebServiceFactory<ILongPollingService> longPollingServiceFactory = new WebServiceFactory<ILongPollingService>();
		longPollingServiceFactory.setAddress(BASE_URL + "LongPolling");
		longPollingServiceFactory.setServiceClass(ILongPollingService.class);
		initFactory(longPollingServiceFactory, bindingFactory);

		WebServiceFactory<IIdGenerator> idGeneratorServiceFactory = new WebServiceFactory<IIdGenerator>();
		idGeneratorServiceFactory.setAddress(BASE_URL + "IdGenerator");
		idGeneratorServiceFactory.setServiceClass(IIdGenerator.class);
		initFactory(idGeneratorServiceFactory, bindingFactory);

		// Instantiate services using the factories
		IQueriesService queriesService = queriesServiceFactory.create();
		IStreamingService streamingService = streamingServiceFactory.create();
		ILongPollingService longPollingService = longPollingServiceFactory.create();
		IIdGenerator idGenerator = idGeneratorServiceFactory.create();


		// List dimensions
		logger.log(Level.INFO, "Discovering dimensions:");
		List<String> dimensions = queriesService.retrieveDimensions("EquityDerivativesCube");
		for(String dim : dimensions) {
			logger.log(Level.INFO, "Discovered dimension: " + dim);
		}

		// Execute an (ad-hoc) MDX query
		String mdx = "SELECT NON EMPTY {DrilldownLevel({[Bookings].[ALL].[AllMember]})} ON ROWS FROM [EquityDerivativesCube] WHERE ([Measures].[contributors.COUNT])";
		IMDXQuery mdxQuery = new MDXQuery(mdx);
		logger.log(Level.INFO, "Executing MDX query: " + mdx);
		CellSetDTO cellSet = queriesService.execute(mdxQuery);
		List<CellDTO> cells = cellSet.getCells();
		for(CellDTO cell : cells) {
			logger.log(Level.INFO, "Cell: " + cell);
		}

		// Execute an (ad-hoc) drillthrough query
		String locationString = "AllMember\\EUR|AllMember|AllMember\\DeskA|AllMember|AllMember|[*]|AllMember|ANY";
		ILocation location = new Location(LocationUtil.stringToArrayLocation(locationString));
		DrillthroughQuery drillthroughQuery = new DrillthroughQuery();
		drillthroughQuery.setLocations(Arrays.asList(location));
		drillthroughQuery.setPivotId("EquityDerivativesCube");
		logger.log(Level.INFO, "Executing Drillthrough query: " + location);
		DrillthroughResultDTO result = queriesService.execute(drillthroughQuery);
		List<DrillthroughRowDTO> rows = result.getRows();
		logger.log(Level.INFO, "Drillthrough query returned " + rows.size() + " rows.");
		for(DrillthroughRowDTO row : rows) {
			logger.log(Level.INFO, "Row: " + row);
		}

		// Initiate a (long polling based) communication channel
		String listenerId = idGenerator.generateListenerIds(1)[0];
		longPollingService.addListener(PUBLICATION_DOMAIN, listenerId);
		new Thread(new Listener(longPollingService, listenerId)).start();

		// Subscribe a continuous mdx query, events will be received through the communication channel.
		String[] ids = idGenerator.generateStreamIds(2);
		String mdxStreamId = ids[0];
		String drillthroughStreamId = ids[1];

		IStreamProperties mdxStreamProperties = new StreamProperties(mdxStreamId, PUBLICATION_DOMAIN, InitialState.STARTED, true);
		streamingService.createStream(mdxQuery, mdxStreamProperties);

		// Subscribe a continuous drillthrough query, events will be received through the communication channel.
		// Note that drillthrough events will be rare, because real-time updates are random will rarely
		// fall exactly in the drillthrough location (unless you configure a massive flow of trade updates).
		IStreamProperties drillthroughStreamProperties = new StreamProperties(drillthroughStreamId, PUBLICATION_DOMAIN, InitialState.STARTED, true);
		streamingService.createStream(drillthroughQuery, drillthroughStreamProperties);
	}


	protected static void initFactory(WebServiceFactory<?> factory, JAXBDataBindingFactory bindingFactory) throws Exception {
		factory.setDataBinding(bindingFactory.create());
		factory.setUsername(USER);
		factory.setPassword(PASSWORD);
		factory.setConnectionTimeout(5000);
		factory.setReceiveTimeout(30000);
		factory.setDisableChunking(true);
	}

	/**
	 *
	 * Listener agent, listens to 100 events and leaves.
	 *
	 * @author Quartet Financial Systems
	 *
	 */
	static class Listener implements Runnable {

		final ILongPollingService service;
		final String listenerId;

		Listener(ILongPollingService service, String listenerId) {
			this.service = service;
			this.listenerId = listenerId;
		}

		@Override
		public void run() {
			for(int iteration = 0; iteration < 100; iteration++) {
				IBulkedStreamEvents events = service.listen(listenerId);
				if(events != null) {
					logger.log(Level.INFO, "Received events:");
					for(IDomainStreamEvent domainEvent : events.getDomainEvents()) {
						for(IStreamEvent event : domainEvent.getEvents())
							logger.log(Level.INFO, event.toString());
					}
				} else
					logger.log(Level.INFO, "No events received.");
			}
		}
	}

}
