/*
 * (C) Quartet FS 2012
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.cfg;


import static com.quartetfs.fwk.types.impl.ExtendedPluginInjector.inject;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.classification.ICalculator;
import com.quartetfs.biz.pivot.cube.hierarchy.IBucketer;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.axis.impl.DefaultTimeBucketer;
import com.quartetfs.biz.pivot.monitoring.impl.JMXEnabler;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesContinuousHandler;
import com.quartetfs.biz.pivot.query.aggregates.IStream;
import com.quartetfs.biz.pivot.security.IContextValueManager;
import com.quartetfs.biz.pivot.security.impl.ContextValuePropagator;
import com.quartetfs.biz.pivot.security.impl.UserDetailsServiceWrapper;
import com.quartetfs.biz.pivot.spring.ActivePivotConfig;
import com.quartetfs.biz.pivot.spring.ActivePivotServicesConfig;
import com.quartetfs.biz.pivot.spring.RemotingConfig;
import com.quartetfs.biz.pivot.spring.WebServicesConfig;
import com.quartetfs.biz.pivot.transaction.impl.TransactionWrapper;
import com.quartetfs.fwk.AgentException;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.contributions.impl.ClasspathContributionProvider;
import com.quartetfs.fwk.monitoring.impl.HealthCheckAgent;
import com.quartetfs.pivot.sandbox.service.IDataService;
import com.quartetfs.pivot.sandbox.service.impl.DataService;
import com.quartetfs.pivot.sandbox.service.impl.ForexService;
import com.quartetfs.pivot.sandbox.service.impl.HistoricalDatesService;
import com.quartetfs.pivot.sandbox.source.impl.Feeder;
import com.quartetfs.pivot.sandbox.source.impl.KeyExtractor;
import com.quartetfs.pivot.sandbox.source.impl.TradeSource;
import com.quartetfs.tech.distribution.messenger.IDistributedMessenger;

/**
 *
 * Spring configuration of the Sandbox Application services.<br>
 * The parameters of the Sandbox Services can be quickly changed
 * by modifying the sandbox.properties file.
 *
 * @author Quartet FS
 *
 */
@PropertySource(value="classpath:sandbox.properties")
@Configuration
@Import(value={
		ActivePivotConfig.class,
		ActivePivotServicesConfig.class,
		WebServicesConfig.class,
		RemotingConfig.class,
		SecurityConfig.class
})
public class SandboxConfig {

	/** Before anything else we statically initialize the Quartet FS Registry. */
	static {
		Registry.setContributionProvider(new ClasspathContributionProvider("com.quartetfs"));
	}


	/** Spring environment, automatically wired */
    @Autowired
    protected Environment env;

    /** ActivePivot Manager, automatically wired */
    @Autowired
    protected IActivePivotManager activePivotManager;

	/** Context value manager, injected from current context */
	@Autowired
	protected IContextValueManager contextValueManager;

	/** Context value propagator, injected from current context */
	@Autowired
	protected ContextValuePropagator contextValuePropagator;

	/** User details service, injected from current context */
	@Autowired
	protected UserDetailsServiceWrapper userDetailsService;


	/** Transaction Wrapper */
	@Bean
	@DependsOn(value="startManager")
	public TransactionWrapper transactionWrapper() throws Exception {
		TransactionWrapper tw = new TransactionWrapper();
		tw.setActivePivotManager(activePivotManager);
		tw.setSchemaName("SandboxSchema");
		tw.setSynchronousModeEnabled(false);

		return tw;
	}

	/** Feeder of records into ActivePivot */
	@Bean
	public Feeder feeder() throws Exception {
		Feeder feeder = new Feeder();
		feeder.setKeyExtractor(keyExtractor());
		feeder.setTransactionWrapper(transactionWrapper());
		return feeder;
	}




	/**
	 * Trade Source that generates random trades.
	 * The returned bean is initialized but not started.
	 */
	@Bean(destroyMethod="stop")
	public TradeSource tradeSource() throws AgentException {
		TradeSource source = new TradeSource();

		Properties props = new Properties();

		// Number of trades to produce (per historical day)
		props.setProperty("tradeCount", env.getProperty("tradeSource.tradeCount"));

		// Number of products to generate
		if (env.getProperty("tradeSource.productCount") != null)
			props.setProperty("productCount", env.getProperty("tradIeSource.productCount"));

		// Size of a batch when submitting new trades
		props.setProperty("batchSize", env.getProperty("tradeSource.batchSize"));

		// Number of seconds before continuous update kick in
		props.setProperty("timerDelay", env.getProperty("tradeSource.timerDelay"));

		// Period between two continuous updates
		props.setProperty("timerPeriod", env.getProperty("tradeSource.timerPeriod"));

		// Number of new trade in an update
		props.setProperty("newTradesNb", env.getProperty("tradeSource.newTradesNb"));

		// Number of amended (existing) trade in an update
		props.setProperty("amendedTradesNb", env.getProperty("tradeSource.amendedTradesNb"));


		// Start the source
		source.init(props);

		return source;
	}

	/** Data service */
	@Bean
	public IDataService dataService() throws AgentException {
		return new DataService(tradeSource());
	}

	/** Key Extractor */
	@Bean
	public KeyExtractor keyExtractor() {
		return new KeyExtractor();
	}

	/** Historical Dates Service */
	@Bean
	public HistoricalDatesService historicalDatesService() {

		int nbUnbHistoricalDatespdates = env.getProperty("historicalDates.nbHistoricalDates", Integer.class);

		HistoricalDatesService service = new HistoricalDatesService();
		service.setNbHistoricalDates(nbUnbHistoricalDatespdates);

		// Return the (singleton) service
		return service;
	}

	/** The Forex Service */
	@Bean(destroyMethod="stop")
	public ForexService forexService() throws AgentException {

		long period = env.getProperty("forex.period", Long.class);
		int nbUpdates = env.getProperty("forex.nbUpdates", Integer.class);

		// Update two quotes every 2 seconds
		ForexService service = new ForexService(period, nbUpdates);

		// Start the service automatically
		service.start();

		// Return the (singleton) service
		return service;
	}


	/** Create a time bucketer that buckets dates into time buckets */
	@Bean
	public IBucketer<Long> timeBucketer() {
		DefaultTimeBucketer bucketer = new DefaultTimeBucketer();
		return bucketer;
	}

	/**
	 *
	 * Initialize and start the ActivePivot Manager,
	 * after performing all the injections into
	 * the ActivePivot plug-ins.
	 *
	 * @return void
	 * @throws Exception
	 */
	@Bean
	public Void startManager() throws Exception {

		// Inject dependencies before the ActivePivot
		// components are initialized.
		injectDependencies();

		// Initialize the ActivePivot Manager and start it.
		activePivotManager.init(null);
		activePivotManager.start();

		return null;
	}

	/**
	 *
	 * Start the application once the ActivePivot Manager is started.
	 * This actually plus the trade source as an ActivePivot feed
	 * and then starts the trade source.
	 *
	 * @return void
	 * @throws Exception
	 */
	@Bean
	@DependsOn(value="startManager")
	public Void startApplication() throws Exception {

		// Listen to the source, start the source
		tradeSource().addSourceListener(feeder());
		tradeSource().start();

		return null;
	}

	/** Enable JMX Monitoring for ActivePivot Components */
	@Bean
	public JMXEnabler JMXEnabler() throws Exception {
		return new JMXEnabler(activePivotManager);
	}

	@Bean
	public HttpInvokerServiceExporter springDataService() throws Exception {
		HttpInvokerServiceExporter remoting = new HttpInvokerServiceExporter();
		remoting.setService(dataService());
		remoting.setServiceInterface(IDataService.class);

		return remoting;
	}

	/** Health Check Agent bean */
	@Bean(initMethod="start", destroyMethod="interrupt")
	public HealthCheckAgent healthCheckAgent() {
		HealthCheckAgent agent = new HealthCheckAgent(60);  // One trace per minute
		return agent;
	}

	/**
	 * Inject dependencies and services into Quartet FS extended plug-ins.
	 */
	protected void injectDependencies() throws Exception {

		// Inject the distributed messenger with security services
		inject(IDistributedMessenger.class, "JGROUPS_MESSENGER", "contextValueManager",    contextValueManager);
		inject(IDistributedMessenger.class, "JGROUPS_MESSENGER", "contextValuePropagator", contextValuePropagator);
		inject(IDistributedMessenger.class, "JGROUPS_MESSENGER", "userDetailsService",     userDetailsService);

		// Inject the time bucketer into the post processor and analysis dimension
		inject(IPostProcessor.class, "TIME_BUCKETER", "bucketer", timeBucketer());
		inject(IDimension.class,     "TIME_BUCKET",   "bucketer", timeBucketer());

		// Inject the Forex Service in the extended plugins that will use it
		inject(IStream.class,                      "CONTINUOUS_FOREX_STREAM",  "forexService",  forexService());
		inject(IPostProcessor.class,               "CONTINUOUS_FOREX",         "forexService",  forexService());
		inject(IAggregatesContinuousHandler.class, "CONTINUOUS_FOREX_HANDLER", "currencyLevel", "UnderlierCurrency");

		inject(IAggregatesContinuousHandler.class, "UPDATE_ANALYSIS_DIMENSION", "analysisDimensionName", "TimeBucketDynamic");


		// Inject the Historical Dates Service in the ActivePivot calculator
		inject(ICalculator.class, "PNL", "historicalDatesService", historicalDatesService());
	}


}