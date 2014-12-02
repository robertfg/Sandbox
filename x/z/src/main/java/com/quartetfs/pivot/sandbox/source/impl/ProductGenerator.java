/*
 * (C) Quartet FS 2007-2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.source.impl;

import java.util.Random;

import jsr166e.ThreadLocalRandom;

import com.quartetfs.pivot.sandbox.impl.SandboxUtil;
import com.quartetfs.pivot.sandbox.model.impl.Product;

/**
 * This class generates random {@link Product products}.
 *
 * @author Quartet FS
 *
 */
public class ProductGenerator {

	//currencies
	private final static String [] underlierCurrencies ={"EUR", "USD", "GBP", "JPY", "CHF", "ZAR"};

	//underlier descriptions
	private final static String [] underlierDescriptions ={"EquityIndex", "SingleStock"};

	//underlier codes for EquityIndex
	private final static String [] underlierCodesEquityIndex = {"AEX", "CECEE", "ESTX50", "FTEPRA", "GDAXI", "GSUK", "IBEX", "MIB30", "RDXEUR", "SD3E", "STXX50"};	

	//underlier codes for SingleStock
	private final static String [] underlierCodesSingleStock = {
		".EPEU",   ".GDAXIP",  "1mEUEB.RBS", "AAH.AS",   "AGFP.PA",  "ALBK.I",    "BASF.DE",  "BKIR.I",  "BNPP.PA", "COR.AS", 
		"CRDI.MI", "DTEGn.DE", "ELE.MC",     "ENI.MI",   "EONG.DE",  "ENI.MI",    "EONG.DE",  "FTE.PA",  "HICP", 
		"INWS.I",  "IPM.I",    "ISPA.AS",    "KESBV.HE", "LOIM.PA",  "LYOE.PA",   "MANG.DE",  "MMTP.PA", "RWEG_p.DE", 
		"SOGN.PA", "SRG.MI",   "SZUG.DE",    "TKAG.DE",  "TUIGn.DE", "VOWG_p.DE", "WBSV.VI"};

	//product types
	private final static String [] productTypes={"LISTED","OTC"};

	//product names
	private final static String [] OTC_ProductNames= { "EQUITY SWAP", "BARRIER OPTION"};
	private final static String [] LISTED_ProductNames= { "OPTION", "FUTURE"};
	
	/**
	 * Randomly generate a product with the given product id.
	 * 
	 * @param productId
	 * @return product
	 */
	public Product generate(int productId) {
		return generate(productId, ThreadLocalRandom.current());
	}
	

	/**
	 * Randomly generate a product with the given product id.
	 * 
	 * @param productId
	 * @param random random generator to use (this allows to provide a
	 * seeded random for repeatable scenarios).
	 * @return product
	 */
	public Product generate(int productId, Random random) {
		Product product = new Product();
		
		//id
		product.setId(productId);
		//underlierCurrency
		product.setUnderlierCurrency(underlierCurrencies[random.nextInt(underlierCurrencies.length)]);
		//underlierDescription
		product.setUnderlierType(underlierDescriptions[random.nextInt(underlierDescriptions.length)]);
		//underlierCode
		if (product.getUnderlierType().equals("EquityIndex")) {
			product.setUnderlierCode(underlierCodesEquityIndex[random.nextInt(underlierCodesEquityIndex.length)]);
		} else {
			product.setUnderlierCode(underlierCodesSingleStock[random.nextInt(underlierCodesSingleStock.length)]);
		}
		//productType
		product.setProductType(productTypes[random.nextInt(productTypes.length)]);
		//productName
		if("LISTED".equals(product.getProductType())) {
			product.setProductName(LISTED_ProductNames[random.nextInt(LISTED_ProductNames.length)]);
		} else {
			product.setProductName(OTC_ProductNames[random.nextInt(OTC_ProductNames.length)]);
		}

		//underlierValue
		product.setUnderlierValue(SandboxUtil.round(random.nextDouble() * 10000,2));
		//bumpedDown
		product.setBumpedMtmDown(SandboxUtil.round(random.nextDouble(),2));
		//bumpedUp
		product.setBumpedMtmUp(SandboxUtil.round(random.nextDouble(),2));
		//baseMTM
		product.setProductBaseMtm(SandboxUtil.round(SandboxUtil.nextDouble(500, 1000, random),2));
		return product;
	}


	/**
	 * Main function is only used for debugging and testing the random generation.
	 * @param args
	 */
	public static void main(String[] args){
		int count = 10; //number of generated objects
		ProductGenerator generator = new ProductGenerator();

		for (int i = 0; i < count; i++) {
			System.out.println(generator.generate(i));
		}
	}

}