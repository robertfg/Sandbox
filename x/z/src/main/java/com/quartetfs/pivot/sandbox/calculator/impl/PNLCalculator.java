/*
 * (C) Quartet FS 2007-2009
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.calculator.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import jsr166e.ThreadLocalRandom;

import com.quartetfs.biz.pivot.classification.impl.ACalculator;
import com.quartetfs.biz.pivot.cube.hierarchy.IBucketer;
import com.quartetfs.biz.pivot.cube.hierarchy.axis.impl.DefaultTimeBucketer;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.pivot.sandbox.impl.MessagesSandbox;
import com.quartetfs.pivot.sandbox.impl.SandboxUtil;
import com.quartetfs.pivot.sandbox.model.impl.Trade;
import com.quartetfs.pivot.sandbox.service.impl.HistoricalDatesService;


/**
 * <b>PNLCalculator</b> 
 * 
 * implements for the interface ICalculator.
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName="com.quartetfs.biz.pivot.classification.ICalculator",	key=PNLCalculator.KEY)
public class PNLCalculator extends ACalculator {

	/** serial version uid for the class. */
	private static final long serialVersionUID = -2675580919874747937L;

	/** the logger **/
	private static Logger logger = Logger.getLogger(MessagesSandbox.LOGGER_NAME, MessagesSandbox.BUNDLE);

	/** attribute key for the class. */
	public static final String KEY = "PNL";

	/**
	 * desksPropsFile used to retrieve the appropriate desk for the trade
	 * and figure out object enrichment 
	 * notice that a desk attribute was added in PNLCalculatorResult
	 * then this attribute can be retrieved during the introspection
	 **/ 
	private final String desksPropsFile="desks.properties";
	private final Map<String, String> desksProps = new HashMap<String, String>();

	/** Maps of buckets to bucket dates */
	private NavigableMap<Long, Object> bucketMap;

	// constants used in calculation formulas
	private static final Double BUMP_SIZE_50 = 0.5;	
	private static final Double SHIFT_OPERAND = 1.1;
	private final static double [] factors = {.5, .2, .3, .6};//factors used for bumpedMtmDown and bumpedMtmUp perturbation

	/** this service is used in order to produce trades with an asOfDate in the past in order to simulate historical data */
	private HistoricalDatesService historicalDatesService = null;

	/** historical days number retrieved from historicalDatesService */
	private Integer nbHistoricalDays = null;

	/** today's date time, rounded to the day */
	private final static long dayInMillis = 1000L * 60 * 60 * 24;
	private final long today = (System.currentTimeMillis() / dayInMillis) * dayInMillis;

	/** Empty constructor */
	public PNLCalculator(Properties properties){
		super(properties);
		//load desksPropsFile
		try {
			Properties props = new Properties();
			InputStream in =  getClass().getClassLoader().getResourceAsStream(desksPropsFile);
			props.load(in);
			for(String propertyName : props.stringPropertyNames()) {
				desksProps.put(propertyName, props.getProperty(propertyName));
			}
			logger.log(Level.INFO, MessagesSandbox.INFO_FILE_LOADED, new Object[] {"PNLCalculator", desksPropsFile});
		} catch (Exception e) {
			logger.log(Level.SEVERE, MessagesSandbox.SEVERE_FILE_NOT_LOADED, new Object[] {"PNLCalculator", desksPropsFile});
		}

		// Create the time bucket map
		IBucketer<Long> dateBucketer = new DefaultTimeBucketer();
		this.bucketMap = dateBucketer.createBucketMap(today);
	}

	/**
	 * ICalculator interface defines only one important method:
	 * <pre><code>public List&lt;ICalculatorResult&gt; evaluate(Object Obj) throws Exception;</code></pre>
	 * <p>The returned ICalculatorResult can be properties expected for classification in the cube,
	 * or computed measures like price, or risk. 

	 * @param object the object flowing through
	 * @return a list of Object computed by this calculator
	 * @throws Exception
	 */
	@Override
	public List<Object> evaluate(Object object) throws Exception {
		//results that hold the result of evaluate method
		List<Object> results = new ArrayList<Object>();
		
		//only TradeDefinition object is processed
		if(!(object instanceof Trade)) {
			logger.severe("Trade must be an instance of TradeDefinition: " + object);
			return results; //empty list returned means the object is skipped
		}

		Trade trade = (Trade) object;
		
		//enrichment consists in adding missing fields from the original object 
		enrichTrade(trade, results);

		return results;
	}

	/**
	 * We resolve all missing fields not available in the object and add the generated results in the results list
	 * Notice that for one object we produce (nbHistoricalDays+1) results, one result per asOfDate then we're abel to simulate historical data
	 * @param trade
	 * @param results
	 * @throws BucketerException
	 */
	private void enrichTrade(Trade trade, List<Object> results) {

		// Bucket the date of the trade
		Entry<Long, Object> ceilingEntry = bucketMap.ceilingEntry(trade.getDate().getTime());
		if(ceilingEntry == null) {
			throw new RuntimeException("There is no bucket large enough to hold: " + trade.getDate());
		} else {
			trade.setDateBucket(ceilingEntry.getValue().toString());
		}

		//calculate the rate change based on underlierValue and its shifted value (hard coded SHIFT_OPERAND)
		double underlierValue = trade.getUnderlierValue();
		double underlierValueShifted = underlierValue * SHIFT_OPERAND;
		double rateChange = (underlierValueShifted - underlierValue)/underlierValue;
		
		//get the pvQtyMultiplier used in delta and pnl calculation
		int qtyMultiplier = trade.getProductQtyMultiplier();
		
		//getting the pv from the original object, notice that this is the only non calculated measure
		double pv = trade.getProductBaseMtm();
		
		//loop over the nbHistoricalDays, in this case for one trade we create (nbHistoricalDays + 1) facts 
		//one fact with asOfDate equals to today's date and  nbHistoricalDays facts with the past asOfDate dates
		for (int i = 0 ; i <=  nbHistoricalDays; i++) {//notice that today's date is created also
			//create the asOfDate date
			Calendar asOfDate = CALENDAR.get();
			asOfDate.setTimeInMillis(today);
			asOfDate.add(Calendar.DAY_OF_MONTH, -i);

			//get bumpedMtmUp and bumpedMtmDown : this refers to the pv after a +25% and -25% bumps
			//that's why BUMP_SIZE_50 is 50% = (+25%) - (-25%)
			final Random random = ThreadLocalRandom.current();
			double bumpedMtmUp = SandboxUtil.round(trade.getBumpedMtmUp() + trade.getBumpedMtmUp() * factors[random.nextInt(factors.length)], 2);
			double bumpedMtmDown = SandboxUtil.round(trade.getBumpedMtmDown() + trade.getBumpedMtmDown() * factors[random.nextInt(factors.length)], 2);

			//init all the other measures
			double delta=.0, pnl=.0,pnlDelta=.0, pnlVega=.0,theta=.0, gamma=.0, vega=.0, rho=.0;

			//calculating and adding new measures that depend on external MarketData (rateChange in our case)
			delta = qtyMultiplier * ((bumpedMtmUp - bumpedMtmDown) / BUMP_SIZE_50);
			pnlDelta =  rateChange * delta;

			/* 
			 * theta, gamma, vega, rho, pnlVega and pnl calculation
			 *  this formulas do not match the reality the aim here is to have different values for each measure
			 *  in a real project these values can be calculated in the calculator or retrieved form an external pricer
			 *   
			 * gamma = delta * random[-.1 , +.1]
			 * vega = delta * random[-1 , +1]
			 * theta = pv * 3 / 365
			 * rho = -pv * 1/150
			 */
			theta = pv * 3/365;
			rho = -pv * 1/150;
			gamma = delta * SandboxUtil.nextDouble(-0.1, 0.1, random);
			vega = delta * SandboxUtil.nextDouble(-1, 1, random);
			pnlVega = vega * 0.01;
			pnl = pnlVega + pnlDelta;

			//instantiate the result that will hold the enrichment data
			PNLCalculatorResult result = new PNLCalculatorResult();

			//desk is retrieved from desksProps 
			//it is not part of trade object then it should be added in PNLCalculatorResult
			//thus it is retrieved when the introspection is performed
			result.setDesk(desksProps.get(trade.getUnderlierType()));

			result.setAsOfDate(asOfDate.getTime());

			//same for all calculated measures
			result.setDelta(SandboxUtil.round(delta,2));
			result.setPnlDelta(SandboxUtil.round(pnlDelta,2));
			result.setPnlVega(SandboxUtil.round(pnlVega,2));
			result.setPnl(SandboxUtil.round(pnl,2));
			result.setGamma(SandboxUtil.round(gamma,2));
			result.setVega(SandboxUtil.round(vega,2));
			result.setTheta(SandboxUtil.round(theta,2));
			result.setRho(SandboxUtil.round(rho,2));

			//adding the result in the results List
			//notice that an evaluated object produces a List of results, in our case for one object we produce (nbHistoricalDays+1) results
			results.add(result);
		}
	}

	/**
	 * Being a Plugin, it returns the Type it is attached to.
	 */
	@Override
	public String getType() { return KEY; }

	/**
	 * this setter is used by Spring injection, 
	 * see the Spring wiring file in which we inject this service to the calculator 
	 * @param historicalDatesService
	 */
	public void setHistoricalDatesService(HistoricalDatesService historicalDatesService) {
		this.historicalDatesService = historicalDatesService;
		// nbHistoricalDays is initialized here
		this.nbHistoricalDays = this.historicalDatesService.getNbHistoricalDates();
	}
	
	public class PNLCalculatorResult {

		/**
		 * These attributes below are  added in order to enrich the object
		 * for instance desk is not defined in TradeDefinition
		 * the desk is added then in PNLCalculatorResult with its getter and setter
		 * variable name should match the level name defined in your level configuration
		 */
		private String desk;
		private Double delta;
		private Double pnlDelta;
		private Double pnlVega;
		private Double pnl;
		private Double gamma;
		private Double vega;
		private Double theta;
		private Double rho;
		private Date asOfDate;

		/** Empty constructor */
		public PNLCalculatorResult() { };


		public Double getDelta() { return delta; }

		public void setDelta(Double delta) { this.delta = delta; }

		public Double getGamma() { return gamma; }

		public void setGamma(Double gamma) { this.gamma = gamma; }

		public Double getPnl() { return pnl; }

		public void setPnl(Double pnl) { this.pnl = pnl; }

		public Double getPnlDelta() { return pnlDelta; }

		public void setPnlDelta(Double pnlDelta) { this.pnlDelta = pnlDelta; }

		public Double getPnlVega() { return pnlVega; }

		public void setPnlVega(Double pnlVega) { this.pnlVega = pnlVega; }

		public Double getRho() { return rho; }

		public void setRho(Double rho) { this.rho = rho; }

		public Double getTheta() { return theta; }

		public void setTheta(Double theta) { this.theta = theta; }

		public Double getVega() { return vega; }

		public void setVega(Double vega) { this.vega = vega; }

		public String getDesk() { return desk; }

		public void setDesk(String desk) { this.desk = desk; }

		public Date getAsOfDate() { return asOfDate; }

		public void setAsOfDate(Date asOfDate) { this.asOfDate = asOfDate; }
	
	}

	/** Reusable, thread safe calendar */
	static final ThreadLocal<Calendar> CALENDAR = new ThreadLocal<Calendar>() {
		@Override
		public Calendar initialValue() {
			return Calendar.getInstance();
		}
	};

}