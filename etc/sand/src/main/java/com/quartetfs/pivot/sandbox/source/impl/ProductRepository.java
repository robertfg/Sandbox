/*
 * (C) Quartet FS 2007-2013
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.source.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import jsr166e.ThreadLocalRandom;

import com.quartetfs.pivot.sandbox.impl.MessagesSandbox;
import com.quartetfs.pivot.sandbox.model.impl.Product;


/**
 * 
 * Repository of products, shared by several services in the Sandbox application.
 * 
 * @author Quartet FS
 *
 */
public class ProductRepository {
	
	/** Logger **/
	protected static Logger LOGGER = MessagesSandbox.getLogger(ProductRepository.class);
	
	/** Cached products */
	public List<Product> products;
	
	/** Constructor */
	public ProductRepository(int productCount) {
		this(productCount, ThreadLocalRandom.current());
	}
	
	public ProductRepository(int productCount, Random random) {
		LOGGER.log(Level.INFO, "Generating " + productCount + " random products.");
		ProductGenerator productGenerator = new ProductGenerator();
		synchronized (this) {
			if(this.products == null) {
				List<Product> products = new ArrayList<>(productCount);
				for(int p = 0; p < productCount; p++) {
					products.add(productGenerator.generate(p, random));
				}
				this.products = products;
			}
		}
	}
	
	public int getProductCount() { return products.size(); }
	
	public Product getProduct(int productId) { return products.get(productId); }

}