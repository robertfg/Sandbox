/*
 * (C) Quartet FS 2010-2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.fwk.AgentException;
import com.quartetfs.pivot.sandbox.impl.MessagesSandbox;
import com.quartetfs.pivot.sandbox.impl.SandboxUtil;
import com.quartetfs.pivot.sandbox.service.IForexServiceListener;

/**
 * <b>ForexService</b>
 * 
 * This service is instantiated by the Spring injection and provides randomly
 * updated currencies quotations
 * 
 * @author Quartet Financial Systems
 */
public class ForexService {

	/** forex service listeners */
	protected List<IForexServiceListener> listeners = new CopyOnWriteArrayList<IForexServiceListener>();

	/** list of currencies pairs (e.g. "USD/EUR", "EUR/USD") */
	private static final List<String> currencyPairs = new ArrayList<String>();

	/** map of quotations (e.g. ["EUR/ZAR", 9.5637], ["ZAR/EUR", 0.1046]) */
	private ConcurrentMap<String, Double> currentQuotations = new ConcurrentHashMap<String, Double>();

	/** update period in ms */
	private Long interval;

	/** number of quotations updated every update period */
	private int updatedQuotationsNb;

	/** the logger */
	private static Logger logger = Logger.getLogger(MessagesSandbox.LOGGER_NAME, MessagesSandbox.BUNDLE);

	/** random generator */
	private final static Random rand = new Random();

	/** ForexThread - thread for real time currency quotation simulation */
	private ScheduledExecutorService forexScheduler;

	/** constructor */
	public ForexService(Long interval, Integer updatedQuotationsNb) {
		if (interval == null || interval < 0)
			this.interval = 1000l;
		else
			this.interval = interval;
		if (updatedQuotationsNb == null || updatedQuotationsNb < 0)
			this.updatedQuotationsNb = 2;
		else
			this.updatedQuotationsNb = updatedQuotationsNb;
	}

	/** quotations initialisation */
	private void initQuotations() {
		currentQuotations.put("USD/EUR", 0.7673);
		currencyPairs.add("USD/EUR");
		currentQuotations.put("GBP/EUR", 1.197);
		currencyPairs.add("GBP/EUR");
		currentQuotations.put("JPY/EUR", 0.0089);
		currencyPairs.add("JPY/EUR");
		currentQuotations.put("CHF/EUR", 0.7383);
		currencyPairs.add("CHF/EUR");
		currentQuotations.put("ZAR/EUR", 0.1046);
		currencyPairs.add("ZAR/EUR");
		currentQuotations.put("EUR/USD", 1.3034);
		currencyPairs.add("EUR/USD");
		currentQuotations.put("EUR/GBP", 0.8354);
		currencyPairs.add("EUR/GBP");
		currentQuotations.put("EUR/JPY", 112.5961);
		currencyPairs.add("EUR/JPY");
		currentQuotations.put("EUR/CHF", 1.3545);
		currencyPairs.add("EUR/CHF");
		currentQuotations.put("EUR/ZAR", 9.5637);
		currencyPairs.add("EUR/ZAR");
		currentQuotations.put("GBP/USD", 1.5601);
		currencyPairs.add("GBP/USD");
		currentQuotations.put("JPY/USD", 0.0116);
		currencyPairs.add("JPY/USD");
		currentQuotations.put("CHF/USD", 0.9622);
		currencyPairs.add("CHF/USD");
		currentQuotations.put("ZAR/USD", 0.1363);
		currencyPairs.add("ZAR/USD");
		currentQuotations.put("USD/GBP", 0.641);
		currencyPairs.add("USD/GBP");
		currentQuotations.put("USD/JPY", 86.395);
		currencyPairs.add("USD/JPY");
		currentQuotations.put("USD/CHF", 1.0393);
		currencyPairs.add("USD/CHF");
		currentQuotations.put("USD/ZAR", 7.3382);
		currencyPairs.add("USD/ZAR");
		currentQuotations.put("GBP/JPY", 134.7816);
		currencyPairs.add("GBP/JPY");
		currentQuotations.put("GBP/CHF", 1.6214);
		currencyPairs.add("GBP/CHF");
		currentQuotations.put("GBP/ZAR", 11.448);
		currencyPairs.add("GBP/ZAR");
		currentQuotations.put("JPY/GBP", 0.074);
		currencyPairs.add("JPY/GBP");
		currentQuotations.put("CHF/GBP", 0.6168);
		currencyPairs.add("CHF/GBP");
		currentQuotations.put("ZAR/GBP", 0.0874);
		currencyPairs.add("ZAR/GBP");
		currentQuotations.put("JPY/CHF", 0.012);
		currencyPairs.add("JPY/CHF");
		currentQuotations.put("JPY/ZAR", 0.0849);
		currencyPairs.add("JPY/ZAR");
		currentQuotations.put("CHF/JPY", 83.1281);
		currencyPairs.add("CHF/JPY");
		currentQuotations.put("ZAR/JPY", 11.7733);
		currencyPairs.add("ZAR/JPY");
		currentQuotations.put("CHF/ZAR", 7.0607);
		currencyPairs.add("CHF/ZAR");
		currentQuotations.put("ZAR/CHF", 0.1416);
		currencyPairs.add("ZAR/CHF");
	}

	/** start Forex service */
	public void start() throws AgentException {
		// init the quotations map
		initQuotations();

		if (forexScheduler == null) {
			// start the ForexThread
			forexScheduler =  Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
				
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r, "ForexThread");
					t.setDaemon(true);
					return t;
				}
			});
			
			forexScheduler.scheduleWithFixedDelay(new ForexTask(), 0, interval, TimeUnit.MILLISECONDS);
			
			logger.log(Level.INFO, MessagesSandbox.INFO_FOREX_SERVICE_STARTED);
		} else
			logger.log(Level.INFO, MessagesSandbox.INFO_FOREX_SERVICE_ALREADY_STARTED);
	}

	/**
	 * Forex Thread Generates random quotations updates every @interval ms
	 */
	public class ForexTask extends TimerTask {
		@Override
		public void run() {
			// generate updatedQuotationsNb random updated quotations
			Set<String> updatedCurrencies = generateRandomQuotations(updatedQuotationsNb);

			// notify listeners
			if (!updatedCurrencies.isEmpty()) {
				for (final IForexServiceListener listener : listeners) {
					// invoke onQuotationUpdated for the updated currencies
					listener.onQuotationUpdate(updatedCurrencies);
				}
			}
		}
	}

	/**
	 * Update the quotations and take into account the reverse currency pair
	 * 
	 * @param String
	 *            currenciesPair
	 * @param Double
	 *            quotation
	 */
	private void updateCurrentQuotations(String currencyPair, Double quotation) {
		StringBuilder reverseCurrencyPair = new StringBuilder();
		reverseCurrencyPair.append(currencyPair.split("/")[1]).append("/").append(currencyPair.split("/")[0]);
		currentQuotations.put(currencyPair, quotation);
		currentQuotations.put(reverseCurrencyPair.toString(), 1. / quotation);
		logger.log(Level.FINE, MessagesSandbox.INFO_QUOTATION_CHANGED, new Object[] { currencyPair, quotation });
		logger.log(Level.FINE, MessagesSandbox.INFO_QUOTATION_CHANGED, new Object[] { reverseCurrencyPair.toString(), 1. / quotation });
	}

	/**
	 * Get quotation of currency vs reference currency
	 * 
	 * @param String
	 *            referenceCurrency
	 * @param String
	 *            currency
	 */
	public Double retrieveQuotation(String referenceCurrency, String currency) {
		StringBuilder currencyPair = new StringBuilder();
		currencyPair.append(referenceCurrency).append("/").append(currency);
		return currentQuotations.get(currencyPair.toString());
	}

	/**
	 * Generates randomly a set of updated quotations
	 * 
	 * @param int count
	 */
	public Set<String> generateRandomQuotations(int count) {
		Set<String> updatedCurrencies = new HashSet<String>();
		for (int i = 0; i < count; i++) {
			// get a random sold currency/bought currency pair
			final String currencyPair = currencyPairs.get(rand.nextInt(currencyPairs.size()));
			// get its quotation
			final Double value = currentQuotations.get(currencyPair);
			// shift it randomly
			final Double shiftedValue = value + SandboxUtil.nextDouble(-0.01, 0.01, rand);
			// insert it in the quotation Map
			// as we play with random values we don't insert negative ones
			if (shiftedValue > .0) {
				// update quotations
				updateCurrentQuotations(currencyPair, shiftedValue);
				// add each currency of the pair to the updated currencies set
				updatedCurrencies.add(currencyPair.split("/")[0]);
				updatedCurrencies.add(currencyPair.split("/")[1]);
			}
		}
		return updatedCurrencies;
	}

	/**
	 * Adds a Forex service listener to the service.
	 * 
	 * @param listener
	 */
	public void addListener(IForexServiceListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a Forex service listener from the service.
	 * 
	 * @param listener
	 */
	public void removeListener(IForexServiceListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	/**
	 * Stop the Forex service
	 */
	public void stop() {
		forexScheduler.shutdown();
		try {
			forexScheduler.awaitTermination(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
