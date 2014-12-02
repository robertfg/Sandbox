/*
 * (C) Quartet FS 2007-2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.source.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import com.quartetfs.pivot.sandbox.model.impl.Product;
import com.quartetfs.pivot.sandbox.model.impl.Trade;

/**
 * <b>RandomTradeGenerator</b>
 * 
 * This class generates randomly list of TradeDefinition objects 
 * @author Quartet Financial Systems
 *
 */
public class TradeGenerator {

	//statuses
	private final static String [] statuses ={ "SIMULATION", "MATCHED", "DONE" };

	//misc params
	private final static int  BOOK_MAX = 10;
	private final static int  PRODUCTQTY_MAX = 10000;
	private final static int  DATE_DEPTH_MAX = 10 * 365; //max generated days, up to 10 years
	private final static long DAY_IN_MS = 24L*60*60*1000;
	private final Date today = new Date();

	/** Repository of products */
	protected ProductRepository productRepository;
	
	/** Random generator (optional) */
	private Random random;

	/** Default constructor */
	public TradeGenerator(ProductRepository productRepository) { 
		this(productRepository, null);
	}

	/**
	 * Constructor with external random generator
	 * (allows fixed seed random generation)
	 *
	 * @param random
	 */
	public TradeGenerator(ProductRepository productRepository, Random random) {
		this.productRepository = productRepository;
		this.random = random;
	}

	/**
	 * Generates randomly a trade List, each trade is an instance of TradeDefinition.
	 * @param count number of trades to generate
	 * @param startKey key to start with and to increment for each generated object
	 * @return List<TradeDefinition> of TradeDefinition objects.
	 * @see Trade
	 *
	 * @return list of trades
	 */
	public List<Trade> generate(int count, AtomicLong startKey) {
		
		final Random random = this.random == null ? ThreadLocalRandom.current() : this.random;
		
		List<Trade> tradeList= new ArrayList<Trade>();
		
		for (int i=0;i<count;i++){
			
			// Pick one random product
			Product product = productRepository.getProduct(random.nextInt(productRepository.getProductCount()));
			
			Trade trade= new Trade();
			//key
			trade.setKey(startKey.getAndIncrement());
			//bookId
			trade.setBookId(random.nextInt(BOOK_MAX));
			//productId
			trade.setProductId(product.getId());
			//product quantity
			trade.setProductQtyMultiplier(random.nextInt(PRODUCTQTY_MAX));
			//underlierCurrency
			trade.setUnderlierCurrency(product.getUnderlierCurrency());
			//underlierDescription
			trade.setUnderlierType(product.getUnderlierType());
			//underlierCode
			trade.setUnderlierCode(product.getUnderlierCode());
			//productType
			trade.setProductType(product.getProductType());
			//productName
			trade.setProductName(product.getProductName());
			//underlierValue
			trade.setUnderlierValue(product.getUnderlierValue());
			//bumpedDown
			trade.setBumpedMtmDown(product.getBumpedMtmDown());
			//bumpedUp
			trade.setBumpedMtmUp(product.getBumpedMtmUp());
			//baseMTM
			trade.setProductBaseMtm(product.getBaseMtm());
			//status
			trade.setStatus(statuses[random.nextInt(statuses.length)]);

			//is simulated
			if ("SIMULATION".equals(trade.getStatus())) {
				trade.setIsSimulated("SIMULATION");
			} else {
				trade.setIsSimulated("LIVE");
			}
			//value date 
			trade.setDate(new Date(today.getTime() + random.nextInt(DATE_DEPTH_MAX)*DAY_IN_MS));

			//add the generated trade to the trade list
			tradeList.add(trade);
		}
		return tradeList;

	}


	/**
	 * Main function is only used for debugging and testing the random generation.
	 * @param args
	 */
	public static void main(String[] args){
		int count = 10;//number of generated objects
		TradeGenerator generator = new TradeGenerator(new ProductRepository(100));
		List<Trade> tradeList = generator.generate(count, new AtomicLong(0l));

		for (int i = 0; i < tradeList.size();i++)
			System.out.println(tradeList.get(i).toString());
	}

}