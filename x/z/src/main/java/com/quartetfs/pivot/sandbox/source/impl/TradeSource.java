/*
 * (C) Quartet FS 2007-2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.source.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.fwk.AgentException;
import com.quartetfs.fwk.messaging.ISourceListener;
import com.quartetfs.fwk.messaging.impl.ASource;
import com.quartetfs.pivot.sandbox.impl.MessagesSandbox;
import com.quartetfs.pivot.sandbox.model.impl.Trade;


/**
 * <b>TradeSource</b>
 * 
 * This source is initialized by the Spring injection
 * and provides then the different objects to the pivot 
 * objects are instance of TradeDefinition 
 * and are randomly generated thanks to RandomTradeGenerator
 * @author Quartet Financial Systems
 * @see TradeGenerator
 * @see Trade
 */
public class TradeSource extends ASource {

	/** serial version uid for the class. */
	private static final long serialVersionUID = -6490334069588626948L;

	/** the logger **/
	private static Logger logger = Logger.getLogger(MessagesSandbox.LOGGER_NAME, MessagesSandbox.BUNDLE);

	/** Property that when true signals a synchronous data update */
	public static final String SYNCHRONOUS_UPDATE_PROPERTY = "synchronousUpdate";
	
	/** Number of distinct products (randomly generated) */
	private Integer productCount = null;
	
	/** trade number of trades to generate */
	private Integer tradeCount = null;

	/** batchSize used to submit the generated trades */
	private Integer batchSize = null;

	/** seed may be used by RandomTradeGenerator in order to produce the same  set of data*/
	private Long seed = null;
	
	/** trade generator */
	private TradeGenerator randomTradeGenerator = null;

	/** last key used for trade generation */
	private final AtomicLong lastKey = new AtomicLong(0L);//this is used to make the key unique

	/** source subject */
	private final static String subject = "Trade Source";

	/** timer to manage amended and new trades */
	private final ScheduledExecutorService scheduler =  Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "TradeSource");
				t.setDaemon(true);
				return t;
			}
		});
	
	/** timerDelay in milliseconds before RealTimeTask to be executed */
	private long timerDelay;

	/** timerPeriod time in milliseconds between successive RealTimeTask executions */
	private long timerPeriod;

	/** random key selector */
	private Random random = new Random();

	/** number of new trades to periodically simulate */
	private int newTradesNb = 0;
	
	/** number of amended trades to periodically simulate */
	private int amendedTradesNb = 0;

	@Override
	protected void doInit(final State previousState, final Properties props) throws AgentException {
		super.doInit(previousState, props);
		try {
			this.productCount = Integer.parseInt(props.getProperty("productCount", "50"));
			this.tradeCount = Integer.parseInt(props.getProperty("tradeCount", "1000"));
			this.batchSize = Integer.parseInt(props.getProperty("batchSize", "100")) ;

			if (props.getProperty("seed") != null) {
				seed = Long.parseLong(props.getProperty("seed"));
			}

			//setup some parameters for the RealTimeTask 
			this.timerDelay = Long.parseLong(props.getProperty("timerDelay", "0")); // 0 delay by default
			this.timerPeriod = Long.parseLong(props.getProperty("timerPeriod", "5000")); // 5 seconds by default

			if (props.getProperty("newTradesNb") != null)
				newTradesNb = Integer.parseInt(props.getProperty("newTradesNb"));
			if (props.getProperty("amendedTradesNb") != null)
				amendedTradesNb = Integer.parseInt(props.getProperty("amendedTradesNb"));

			// Product repository
			ProductRepository productRepository = seed == null ?
					new ProductRepository(productCount) : new ProductRepository(productCount, new Random(seed));
			
			// Trade generator
			TradeGenerator randomTradeGenerator = seed == null ?
					new TradeGenerator(productRepository) : new TradeGenerator(productRepository, new Random(seed));
			this.randomTradeGenerator = randomTradeGenerator;

		} catch(Exception e) {
			throw new AgentException("Issue while setting up TradeSource props", e);
		}

		logger.log(Level.INFO, MessagesSandbox.INFO_SOURCE_INITIALIZED);
	}

	@Override
	public final void start() throws AgentException {
		super.start();
		for(ISourceListener listener : listeners) listener.sourceStarted(this);
	}
	
	/**
	 * This source starts and submit generated objects to its listeners
	 */
	@Override
	protected void doStart(final State previousState) throws AgentException {
		if (tradeCount != null && batchSize != null) {
			logger.log(Level.INFO, MessagesSandbox.INFO_NB_TRADES_GENERATED, tradeCount);
			long dur = System.currentTimeMillis();

			//generated the trades by batchSize
			int remainingTradesToGenerate = tradeCount;
			while (remainingTradesToGenerate > 0) {
				int tradesToGenerate = Math.min(remainingTradesToGenerate, batchSize);
				//generated the trades
				List<Trade> tradeList = randomTradeGenerator.generate(tradesToGenerate, lastKey);
				//submit them
				submitTrades(tradeList);

				remainingTradesToGenerate -= tradesToGenerate;
			}
			long spent = System.currentTimeMillis()-dur;
			logger.log(Level.INFO, MessagesSandbox.INFO_TRADE_SOURCE_REPORT, new Object[] {spent, tradeCount, batchSize});

			if (amendedTradesNb !=0 || newTradesNb != 0){
				logger.log(Level.INFO, "Starting real-time trade source (new trades and amended trades), " +
						"timerDelay=["+timerDelay+"] ms, timerPeriod=["+timerPeriod+"] ms, " +
						"amendedTradesNb=["+amendedTradesNb+"], newTradesNb=["+newTradesNb+"]");

				scheduler.scheduleAtFixedRate(new RealTimeTask(), timerDelay, timerPeriod, TimeUnit.MILLISECONDS);//schedule each timerPeriod
			}

		} else {
			logger.log(Level.SEVERE, MessagesSandbox.SEVERE_TRADES_NOT_GENERATED);
		}
	}

	/**
	 * Submit the trades to the listeners, the feeder in our case
	 * @param tradeList
	 */
	private void submitTrades(List<Trade> tradeList){
		for(ISourceListener listener : listeners) {
			listener.receive(subject, tradeList, null, null);
		}
	}

	/**
	 * This source can pause and will not receive anymore messages
	 * <p>
	 * <p>An exception will be thrown if the pause does not follow a start
	 * or if it has already been stopped or paused. 
	 */
	@Override
	public void pause() throws AgentException {
		super.pause();
		for(ISourceListener listener : listeners) listener.sourcePaused(this);
		logger.log(Level.INFO, MessagesSandbox.INFO_SOURCE_PAUSED);
	}

	/**
	 * This source can be resumed and will start receiving messages again
	 * <p>
	 * <p>An exception will be thrown if the resume does not follow a pause
	 * or if it has already been stopped. 
	 */
	@Override
	public void resume() throws AgentException {
		super.resume();
		for(ISourceListener listener : listeners) listener.sourceResumed(this);
		logger.log(Level.INFO, MessagesSandbox.INFO_SOURCE_RESUMED);
	}

	/**
	 * This source can be stopped and will not receive anymore messages. 
	 * Only a Start is necessary to put it back in action. 
	 * <p>
	 * <p>An exception will be thrown if the pause does not follow a start
	 * or if it has already been stopped or paused. 
	 */
	@Override
	public void stop() throws AgentException {
		super.stop();
		for(ISourceListener listener : listeners) listener.sourceStopped(this);
		logger.log(Level.INFO, MessagesSandbox.INFO_SOURCE_STOPPED);
	}

	@Override
	protected void doStop(State previousState) throws AgentException {
		this.scheduler.shutdownNow();
		try {
			this.scheduler.awaitTermination(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, MessagesSandbox.UNEXPECTED_EX, e);
		}
	}
	
	/**
	 * Being a Plugin, it returns the Type it is attached to.
	 */
	@Override
	public String getType() { return "TradeSource"; }

	
	
	// REAl TIME OPERATIONS
	
	/**
	 * 
	 * Make the source publish trade updates
	 * 
	 * @param nbNew number of new trades
	 * @param nbUpdates number of trade updates
	 * @param properties optional properties
	 */
	public void sendTradeUpdate(int nbNew, int nbUpdates, Properties properties) {
		List<Trade> tradesToSubmit=new ArrayList<Trade>();

		//===== amended trades
		//use a set for amended trades as we rely on the RandomTradeGenerator 
		//and we avoid to submit in the same batch duplicated objects 
		//as this is not allowed by the transactional engine
		Set<Trade> amendedTrades=new HashSet<Trade>();
		for (int i=0; i<nbUpdates;i++){
			int keyToUpdate = random.nextInt(lastKey.intValue());
			amendedTrades.addAll(randomTradeGenerator.generate(1, new AtomicLong(keyToUpdate)));
		}
		//append amended trades
		if (!amendedTrades.isEmpty()) tradesToSubmit.addAll(amendedTrades);

		//===== new trades
		List<Trade> newTrades = randomTradeGenerator.generate(nbNew, lastKey);

		//append new trades
		if (!newTrades.isEmpty()) tradesToSubmit.addAll(newTrades);

		logger.log(Level.INFO, newTrades.size() + " new trades inserted, " + amendedTrades.size() + " trades updated (total " + tradesToSubmit.size() + ")");
		//send produced trades to the feeder
		for(ISourceListener listener : listeners) {
			listener.receive(subject, tradesToSubmit, properties, null);
		}
	}
	
	/**
	 * 
	 * Real-time scheduled data updates.
	 * 
	 * @author Quartet FS
	 *
	 */
	class RealTimeTask implements Runnable {

		@Override
		public void run() {
			sendTradeUpdate(newTradesNb, amendedTradesNb, null);
		}

	}
}