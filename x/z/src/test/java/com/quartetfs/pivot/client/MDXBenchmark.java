/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.quartetfs.biz.pivot.dto.CellSetDTO;
import com.quartetfs.biz.pivot.query.IMDXQuery;
import com.quartetfs.biz.pivot.query.impl.MDXQuery;
import com.quartetfs.biz.pivot.server.http.impl.HTTPServiceFactory;
import com.quartetfs.biz.pivot.webservices.IQueriesService;

/**
 *
 * Simple MDX benchmark.
 *
 * <p>
 * The benchmark uses Spring HTTP Invoker mechanism to
 * connect to the ActivePivot Sandbox queries service,
 * and to serialize the results.
 *
 * @author Quartet Financial Systems
 *
 */
public class MDXBenchmark {

	/** Base url for Spring HTTP Remoting Services */
	static final String BASE_URL = "http://localhost:9090/remoting/";

	/** User name */
	static final String USER = "admin";

	/** User password */
	static final String PASSWORD = "admin";



	/** Number of query iterations (per client) */
	protected int iterations;

	/** Number of concurrent clients */
	protected int clientCount;

	/** List of MDX queries */
	protected List<String> queries;

	/** Execute queries in the list sequentially or randomly? */
	protected final boolean random;

	public MDXBenchmark(List<String> queries, int iterations, int clientCount) {
		this(queries, iterations, clientCount, true);
	}

	public MDXBenchmark(List<String> queries, int iterations, int clientCount, boolean random) {
		this.iterations = iterations;
		this.clientCount = clientCount;
		this.queries = queries;
		this.random = random;
	}

	public void run() {
		List<MDXClient> clients = new ArrayList<MDXClient>(clientCount);
		for(int i = 0; i < clientCount; i++) {
			clients.add(new MDXClient(queries, iterations));
		}
		for(int i = 0; i < clientCount; i++) {
			clients.get(i).start();
		}
		for(int i = 0; i < clientCount; i++) {
			try {
				clients.get(i).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}



	/** Counts the instantiated MDX clients */
	static final AtomicInteger COUNTER = new AtomicInteger(0);

	/**
	 * Single MDX Client, designed to run
	 * in its own thread, over its own
	 * HTTP connection.
	 */
	public class MDXClient extends Thread {

		/** Queries */
		final List<String> queries;

		/** Number of query iterations */
		final int iterations;

		/** Query error count */
		int errorCount = 0;

		/** Recorded executions */
		final List<QueryExecution> executions;

		/** Random generator */
		final Random rand;

		public MDXClient(List<String> queries, int iterations) {
			super("mdx-client-" + COUNTER.getAndIncrement());
			this.queries = queries;
			this.iterations = iterations;
			this.executions = new ArrayList<QueryExecution>(iterations);
			this.rand = new Random();
		}

		@Override
		public void run() {
			// Setup Spring HTTP Invoker for the queries service
			final HTTPServiceFactory<IQueriesService> factory = new HTTPServiceFactory<IQueriesService>();
			factory.setAddress(BASE_URL + "QueriesService");
			factory.setServiceClass(IQueriesService.class);
			factory.setUsername(USER);
			factory.setPassword(PASSWORD);
			IQueriesService queriesService = factory.create();

			int queryCount = queries.size();

			for(int i = 0; i < iterations; i++) {
				try {
					int queryIndex = selectQuery(queryCount, i);
					String mdx = queries.get(queryIndex);
					IMDXQuery query = new MDXQuery(mdx);
					long before = System.currentTimeMillis();
					CellSetDTO cellSet = queriesService.execute(query);
					long elapsed = System.currentTimeMillis() - before;

					QueryExecution execution = new QueryExecution(getName(), i, queryIndex, cellSet.getCells().size(), elapsed);
					executions.add(execution);
					System.out.println(execution);

				} catch(Exception e) {
					errorCount++;
					e.printStackTrace();
				}
			}
		}

		int selectQuery(final int queryCount, final int iteration) {
			if(random) {
				return rand.nextInt(queryCount);
			} else {
				return iteration % queryCount;
			}
		}

		/** Query execution entry */
		class QueryExecution {

			final String name;

			final int iteration;

			final int queryIndex;

			final int cellCount;

			final long elapsed;

			public QueryExecution(String name, int iteration, int queryIndex, int cellCount, long elapsed) {
				this.name = name;
				this.iteration = iteration;
				this.queryIndex = queryIndex;
				this.cellCount = cellCount;
				this.elapsed = elapsed;
			}

			String getQuery() {
				return queries.get(queryIndex);
			}

			@Override
			public String toString() {
				return name + ", iteration-" + iteration + ", query-" + queryIndex + ", result size = " + cellCount + ", elapsed = " + elapsed + "ms";
			}
		}


	}


	/**
	 * Manual launcher.
	 * A collections of queries is populated, and we launch
	 * an instance of the MDX benchmark that will concurrently execute those
	 * queries from several clients in parallel. Queries in the list can
	 * be executed sequentially, or randomly.
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		int iterations = 100;
		int clientCount = 10;

		List<String> queries = new ArrayList<String>();
		queries.add("SELECT NON EMPTY {[Bookings].Members} ON ROWS, {[HistoricalDates].[AsOfDate].Members} ON COLUMNS FROM [EquityDerivativesCube] WHERE ([Measures].[pnl.SUM])");
		queries.add("SELECT NON EMPTY {[Bookings].Members} ON ROWS FROM [EquityDerivativesCube] WHERE ([Measures].[pnl.SUM])");
		queries.add("SELECT NON EMPTY {[Bookings].Members} ON ROWS, {[Underlyings].Members} ON COLUMNS FROM [EquityDerivativesCube] WHERE ([Measures].[pnl.SUM])");
		queries.add("SELECT NON EMPTY CROSSJOIN([Bookings].[Desk].Members, [TimeBucket].Members) ON ROWS FROM [EquityDerivativesCube] WHERE ([Measures].[pnl.SUM])");
		MDXBenchmark benchmark = new MDXBenchmark(queries, iterations, clientCount, false);

		long before = System.currentTimeMillis();
		benchmark.run();
		long elapsed = System.currentTimeMillis() - before;

		System.out.println(clientCount + " concurrent clients each executed " + iterations + " queries, in " + elapsed/1000 + " seconds.");
	}

}
