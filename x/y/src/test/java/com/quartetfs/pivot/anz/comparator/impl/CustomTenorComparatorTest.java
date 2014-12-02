package com.quartetfs.pivot.anz.comparator.impl;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;


public class CustomTenorComparatorTest {
	private CustomTenorComparator<String> comprator = new CustomTenorComparator<String>();
	private Set<String> values = new TreeSet<String>(comprator); 
	
	/*@Test
	public void testWithTenorValues()
	{
		values.addAll(Arrays.asList("6m","9m","1m","5y","2y"));
		Assert.assertEquals("[1m, 6m, 9m, 2y, 5y]", values.toString());
	}
	
	@Test
	public void testWithTenorValuesAndFirstObjectAndLastObject()
	{
		comprator.setFirstObjects(Arrays.asList("1m","t/n","third"));
		comprator.setLastObjects(Arrays.asList("SecondLast","Last"));
		
		values.addAll(Arrays.asList("t/n", "SecondLast","6m","9m","1m","Last","5y","2y","third"));
		Assert.assertEquals("[1m, t/n, third, 6m, 9m, 2y, 5y, SecondLast, Last]", values.toString());
		
	}
	
	@Test
	public void testWithTenorValuesWithInvalidTenor()
	{				
		values.addAll(Arrays.asList("20y & 30y","6m","9m","1m","5y","2y"));
		Assert.assertEquals("[1m, 6m, 9m, 2y, 5y, 20y & 30y]", values.toString());
		
	}
	*/
	
	@Test
	public void testWithTenorValuesWithMonth()
	{
//		values.addAll(Arrays.asList("6m","9m","1m","5y","2y","JAN-1","DEC-1"));
	//	Assert.assertEquals("[1m, 6m, 9m, 2y, 5y, JAN-1, DEC-1]", values.toString());
		values.addAll(Arrays.asList("1m", "6m", "9m","2y", "5y", "FEB-11", "FEB-15", "JAN-13","DEC-14"));
		Assert.assertEquals("[1m, 6m, 9m, 2y, 5y, FEB-11, JAN-13, DEC-14, FEB-15]", values.toString());
   
	//	values.addAll(Arrays.asList( "JAN-01","DEC-01"));
	//	Assert.assertEquals("[JAN-01, DEC-01]", values.toString());
	}
	
}
