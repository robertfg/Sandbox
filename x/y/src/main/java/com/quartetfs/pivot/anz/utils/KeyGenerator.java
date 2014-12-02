package com.quartetfs.pivot.anz.utils;

import java.util.concurrent.atomic.AtomicLong;

public class KeyGenerator {
	private AtomicLong counter=new AtomicLong(Integer.MAX_VALUE+1);

	public long getNextId() {
		return counter.incrementAndGet();
	}

	public static void main(String[] args){
		KeyGenerator k = new KeyGenerator();
		
		System.out.println(k.getNextId());
		System.out.println(k.getNextId());
		
	}
}
