package com.quartetfs.pivot.anz.comparator.impl;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.Test;

public class AlphaNumReplaceComparatorTest {

	AlphaNumReplaceComaparator<String> comparator = new AlphaNumReplaceComaparator<String>();
	private Set<String> values = new TreeSet<String>(comparator); 
	@Test
	public void testWithTenorValuesAndFirstObjectAndLastObject()
	{
		comparator.setFirstObjects(Arrays.asList("1m","t/n","third"));
	    //comparator.setLastObjects(Arrays.asList("SecondLast","Last"));
		//values.addAll(Arrays.asList("t/n", "SecondLast","6m","9m","1m","Last","5y","2y","third"));
		values.addAll(Arrays.asList("t/n", "1m", "third"));
		Assert.assertEquals("[1m, t/n, third]", values.toString());
		
	}
	
	@Test
	public void testWithQ()
	{
		comparator.setReplaceMentStrings(Arrays.asList("Q"));
		
		values.addAll(Arrays.asList("Q213", "Q112", "Q113", "Q212"));
		Assert.assertEquals("[Q112, Q212, Q113, Q213]", values.toString());
		
	}
	
}
