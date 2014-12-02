/*
 * (C) Quartet FS 2013
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.client;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.biz.pivot.query.impl.MDXQuery;
import com.quartetfs.biz.pivot.server.http.impl.HTTPServiceFactory;
import com.quartetfs.pivot.sandbox.impl.MessagesSandbox;
import com.quartetfs.pivot.sandbox.service.IDataService;
import com.quartetfs.tech.streaming.IBulkedStreamEvents;
import com.quartetfs.tech.streaming.IDomainStreamEvent;
import com.quartetfs.tech.streaming.IFailureEvent;
import com.quartetfs.tech.streaming.IIdGenerator;
import com.quartetfs.tech.streaming.ILongPollingService;
import com.quartetfs.tech.streaming.IStreamEvent;
import com.quartetfs.tech.streaming.IStreamProperties;
import com.quartetfs.tech.streaming.IStreamProperties.InitialState;
import com.quartetfs.tech.streaming.IStreamingService;
import com.quartetfs.tech.streaming.impl.StreamProperties;

/**
 * 
 * Minimalist real-time client that subscribes some MDX queries,
 * triggers data updates and listen to the changes of the subscribed queries.
 * <p>
 * Note that to associate an event with the data update that triggered it,
 * this client assumes it is the only source of updates and that as
 * ActivePivot published events FIFO, each received event acknowledges
 * the oldest unacknowledged update. This pattern does not work if
 * there is another concurrent source of data updates.
 * 
 * @author Quartet FS
 *
 */
public class RTClient {

	/** Logger */
	static private final Logger LOGGER = MessagesSandbox.getLogger(RTClient.class);
	
	/** Base url for Spring HTTP Remoting Services */
	static final String BASE_URL = "http://localhost:9090/remoting/";

	/** User name */
	static final String USER = "admin";

	/** User password */
	static final String PASSWORD = "admin";

	/** Number of discarded warming events */
	static final int WARMING_EVENTS = 10;


	/** Streaming domain (randomized) */
	protected final String DOMAIN = "SANDBOX-DOMAIN-" + ThreadLocalRandom.current().nextLong();
	
	/**
	 * MDX (continuous) queries.
	 * Those are optional, if you provide an empty list
	 * the client will still submit data updates to
	 * the server, but only do that.
	 */
	protected final List<String> queries;
	
	/** Period between updates, in milliseconds (-1 for continuous updates) */
	protected final long period;
	
	/** Number of new trades in a transaction */
	protected final int nbNewTrades;
	
	/** Number of amended trades in a transaction */
	protected final int nbAmendedTrades;
	
	public RTClient(List<String> queries) {
		this(queries, 1000L, 0, 10);
	}
	
	public RTClient(List<String> queries, long period, int nbNewTrades, int nbAmendedTrades) {
		this.queries = queries;
		this.period = period;
		this.nbNewTrades = nbNewTrades;
		this.nbAmendedTrades = nbAmendedTrades;
	}
	
	public void run() {

		// Setup Spring HTTP Invoker for the id generator service
		final HTTPServiceFactory<IIdGenerator> gen = new HTTPServiceFactory<IIdGenerator>();
		gen.setAddress(BASE_URL + "IdGenerator");
		gen.setServiceClass(IIdGenerator.class);
		gen.setUsername(USER);
		gen.setPassword(PASSWORD);
		IIdGenerator idGenerator = gen.create();
		
		// Setup Spring HTTP Invoker for the streaming service
		final HTTPServiceFactory<IStreamingService> factory = new HTTPServiceFactory<IStreamingService>();
		factory.setAddress(BASE_URL + "StreamingService");
		factory.setServiceClass(IStreamingService.class);
		factory.setUsername(USER);
		factory.setPassword(PASSWORD);
		IStreamingService streamingService = factory.create();
		
		// Setup Spring HTTP Invoker for the long polling service
		final HTTPServiceFactory<ILongPollingService> lp = new HTTPServiceFactory<ILongPollingService>();
		lp.setAddress(BASE_URL + "LongPollingService");
		lp.setServiceClass(ILongPollingService.class);
		lp.setUsername(USER);
		lp.setPassword(PASSWORD);
		ILongPollingService longPollingService = lp.create();
		
		// Register the client as a real-time listener
		String listenerId = idGenerator.generateListenerIds(1)[0];
		longPollingService.addListener(DOMAIN, listenerId);
		LOGGER.log(Level.INFO, "Client registered to domain " + DOMAIN + " with listener id: " + listenerId);

		// Subscribe continuous queries
		String[] streamIds = idGenerator.generateStreamIds(queries.size());
		final List<StreamStatistics> stats = new ArrayList<>(queries.size());
		final Map<String, Integer> streamIndex = new HashMap<>();
		for(int q = 0; q < queries.size(); q++) {
			IStreamProperties props = new StreamProperties(streamIds[q], DOMAIN, InitialState.STARTED, true);
			streamingService.createStream(new MDXQuery(queries.get(q)), props);
			stats.add(new StreamStatistics(q, streamIds[q]));
			streamIndex.put(streamIds[q], q);
			
			LOGGER.log(Level.INFO, "Created stream " + streamIds[q] + " for query " + queries.get(q));
		}

		// Start the data updater
		Updater updater = new Updater(stats);
		updater.start();
		
		
		// Event listening loop
		for(;;) {
			
			IBulkedStreamEvents events = longPollingService.listen(listenerId);
			if(events != null) {
				for(IDomainStreamEvent de : events.getDomainEvents()) {
					for(IStreamEvent evt : de.getEvents()) {
						String streamId = evt.getStreamId();
						StreamStatistics stat = stats.get(streamIndex.get(streamId));
						if(stat != null) {
							stat.processEvent(evt);
							System.out.println(stat);
						} else {
							System.out.println("Received unexpected event for stream " + streamId + ", disposing stream.");
							streamingService.disposeStream(streamId);
						}
					}
				}
			} else {
				System.out.println("Received no event during long polling wait period.");
			}
		}
	}

	
	/**
	 * 
	 * Updater thread that remotely sends data updates to the server.
	 * 
	 * @author Quartet FS
	 *
	 */
	public class Updater extends Thread {
		
		/** Data service */
		protected final IDataService dataService;
		
		/** Stream statistics */
		protected final List<StreamStatistics> stats;
		
		/** Constructor */
		public Updater(List<StreamStatistics> stats) {
			
			super("rt-data-updater");
			
			// Setup Spring HTTP Invoker for the data service
			final HTTPServiceFactory<IDataService> ds = new HTTPServiceFactory<IDataService>();
			ds.setAddress(BASE_URL + "DataService");
			ds.setServiceClass(IDataService.class);
			ds.setUsername(USER);
			ds.setPassword(PASSWORD);
			this.dataService = ds.create();
			
			this.stats = stats;
		}
		
		public void run() {
			
			while(!interrupted()) {

				long start = System.nanoTime();
				dataService.sendTradeUpdate(nbNewTrades, nbAmendedTrades);
				long elapsed = System.nanoTime() - start;
				System.out.printf("Update[%d new, %d updates] committed in %dms.\n",
						nbNewTrades, nbAmendedTrades, elapsed/1000000L);
				
				for(int q = 0; q < queries.size(); q++) {
					stats.get(q).recordStart(start);
				}
			
				// Wait for the specified period
				if(period > 0) {
					long wait = period - elapsed/1000000L;
					if(wait > 0) {
						try {
							Thread.sleep(wait);
						} catch (InterruptedException e) { }
					}
				}
				
			}
		}
		
	}
	
	
	
	
	/** Statistics of one stream */
	public static class StreamStatistics {

		/** Id of the stream */
		protected final String streamId;

		/** Query index */
		protected final int index;

		/** Queue of updates */
		protected final Queue<Long> updates;
		
		/** Duration of the last update */
		protected long last;
		
		/** Duration of the best update */
		protected long best;
		
		/** Duration of the worst update */
		protected long worst;

		/** Total update time */
		protected long totalTime;
		
		/** Total update time (squared) */
		protected long totalSquareTime;
		
		/** Number of updates */
		protected long updateCount;

		/** Number of failures */
		protected long failureCount;

		/** Warming iterations */
		protected int warming = WARMING_EVENTS;
		
		/** Formatter of numbers */
		protected final NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
		
		public StreamStatistics(int index, String streamId) {
			this.index = index;
			this.streamId = streamId;
			this.updates = new ConcurrentLinkedQueue<>();
			this.totalTime = 0;
			this.best = Long.MAX_VALUE;
			this.worst = 0;
		}

		/** Enqueue an update */
		public void recordStart(long start) {
			this.updates.add(start);
		}
		
		/** Process a stream event for this stream */
		public void processEvent(IStreamEvent event) {
			if(!streamId.equals(event.getStreamId())) {
				throw new IllegalArgumentException("Wrong stream: " + event.getStreamId());
			}

			if(event instanceof IFailureEvent) {
				failureCount++;
				System.out.println("Failure event received: " + ((IFailureEvent) event).getMessage());
			} else {
				long stamp = System.nanoTime();

				// Dequeue the update
				Long start = updates.poll();

				if(start == null) {
					// We have not committed an update with the epoch of this event
					// (it is likely an update sent from another source)
					System.err.println("Received " + event + ", but all the updates appear to be acknowledged already.\nMake sure there isn't another concurrent source of updates.");
				} else {

					if(warming == 0) {
						last = stamp - start;
						best = Math.min(best, (stamp - start));
						worst = Math.max(worst, (stamp - start));
						totalTime += (stamp - start);
						totalSquareTime += ((stamp - start) / 1000L) * ((stamp - start) / 1000L);
						updateCount++;
					} else {
						--warming;
					}
				}
			}

		}

		public String toString() {
			if(updateCount > 0) {
				long avgPeriod = (totalTime / 1000000L) / (updateCount);
				long variance = (totalSquareTime/updateCount) / 1000000L - (avgPeriod * avgPeriod);
				double sigma = Math.sqrt((double) variance);

				return String.format("query-%d %s updateCount=%d, last=%dms, avg=%dms, best=%dms, worst=%dms, std=%.2fms",
						index,
						streamId,
						updateCount,
						last/1000000L,
						avgPeriod,
						best/1000000L,
						worst/1000000L,
						sigma);

			} else {
				return String.format("query-%d %s, waiting for %d more events to warm up.",
						index,
						streamId,
						warming);
			}
		}

	}


	/** Manual launcher. First launch the Sandbox application, without RT updates. */
	public static void main(String[] params) {

		List<String> queries = new ArrayList<>();
		queries.add("SELECT NON EMPTY {[Bookings].Members} ON ROWS, {[HistoricalDates].[AsOfDate].Members} ON COLUMNS FROM [EquityDerivativesCube] WHERE ([Measures].[pnl.SUM])");
		queries.add("SELECT NON EMPTY {[Bookings].Members} ON ROWS FROM [EquityDerivativesCube] WHERE ([Measures].[pnl.SUM])");
		queries.add("SELECT NON EMPTY {[Bookings].Members} ON ROWS, {[Underlyings].Members} ON COLUMNS FROM [EquityDerivativesCube] WHERE ([Measures].[pnl.SUM])");
		queries.add("SELECT NON EMPTY CROSSJOIN([Bookings].[Desk].Members, [TimeBucket].Members) ON ROWS FROM [EquityDerivativesCube] WHERE ([Measures].[pnl.SUM])");

		RTClient client = new RTClient(queries, -1, 0, 1000);
		client.run();

	}

}