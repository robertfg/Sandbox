/*
 * (C) Quartet FS 2012
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.webservices;

import java.net.URL;

import com.quartetfs.webservices.BulkedStreamEvents;
import com.quartetfs.webservices.CellDTO;
import com.quartetfs.webservices.CellEvent;
import com.quartetfs.webservices.DomainStreamEvent;
import com.quartetfs.webservices.IIdGenerator;
import com.quartetfs.webservices.ILongPollingService;
import com.quartetfs.webservices.IStreamingService;
import com.quartetfs.webservices.IdGenerator;
import com.quartetfs.webservices.InitialState;
import com.quartetfs.webservices.LongPollingService;
import com.quartetfs.webservices.MdxQuery;
import com.quartetfs.webservices.StreamEvent;
import com.quartetfs.webservices.StreamProperties;
import com.quartetfs.webservices.StreamingService;

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
public class StreamingClient {

	/** Default url */
	public static final String URL = "http://localhost:9090/webservices/";
	
	public void run() throws Exception {
		// Instantiate service
		System.out.println("Connecting to ActivePivot Streaming Services at " + URL);
		
		IIdGenerator ig = new IdGenerator(new URL(URL + "IdGenerator?wsdl")).getIdGeneratorPort();
		IStreamingService ss = new StreamingService(new URL(URL + "Streaming?wsdl")).getStreamingServicePort();
		ILongPollingService lps = new LongPollingService(new URL(URL + "LongPolling?wsdl")).getLongPollingServicePort();

		
		
		// Register a continuous MDX query
		final String domain = "myDomain";
		final String streamId = ig.generateStreamIds(1).get(0);
		StreamProperties props = new StreamProperties();
		props.setStreamId(streamId);
		props.setPublicationDomain(domain);
		props.setPushData(true);
		props.setInitialState(InitialState.STARTED);
		
		// MDX query to retrieve the total pnl.
		MdxQuery query = new MdxQuery();
		query.setMDX("SELECT {[Measures].[pnl.SUM]} ON ROWS FROM [EquityDerivativesCube]");
		ss.createStream(query, props);
		
		final String listenerId = ig.generateListenerIds(1).get(0);
		lps.addListener(domain, listenerId);
		
		// Long polling listener loop
		try {
		for(int i = 0; i < 5; i++) {
			System.out.print("Listening to real-time events of stream " + streamId + " ... ");
			BulkedStreamEvents events = lps.listen(listenerId);
			System.out.println(events.getDomainEvents().size() + " domain event(s) received:");
			
			for(DomainStreamEvent domainEvent : events.getDomainEvents()) {
				for(StreamEvent streamEvent : domainEvent.getEvents()) {
					String eventStreamId = streamEvent.getStreamId();
					if(streamId.equalsIgnoreCase(eventStreamId)) {
						if(streamEvent instanceof CellEvent) {
							CellEvent cellEvent = (CellEvent) streamEvent;
							if(cellEvent.getCells().isEmpty()) {
								System.out.println("Received empty cell event");
							} else {
								for(CellDTO cell : cellEvent.getCells()) {
									System.out.println("Cell update: before=" + cell.getPreviousValue() + ", after=" + cell.getFormattedValue());
								}
							}
						} else {
							System.out.println("Received stream event: " + streamEvent);
						}
					} else {
						System.out.println("Received stream event from another stream (" + eventStreamId + ")");
					}
				}
			}

			System.out.println();
		}
		
		} finally {
			// Remove listener to avoid wasting server side resources.
			lps.removeListener(domain, listenerId);
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
		new StreamingClient().run();
	}

}
