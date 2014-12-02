package com.quartetfs.pivot.anz.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

public class TestTenorComparator {
	private TenorComparator comparator=new TenorComparator();
	private Set<String> tenors;
	@Before
	public void setup(){
		tenors=new TreeSet<String>(comparator);
	}
	
	@Test
	public void simpleTenors(){
		String tenorsString[]={"10Y","3y","10d","1d","2m","3m","11m"};
		tenors.addAll(Arrays.asList(tenorsString));
		List<String> result=new ArrayList<String>(tenors);
		assertEquals(tenorsString.length, result.size());
		assertEquals("[1d, 10d, 2m, 3m, 11m, 3y, 10Y]", result.toString());
	}
	
	@Test
	public void simpleTenorsWithSpecialcase(){
		String tenorsString1[]={"nan1","nan2","t/n", "10Y","3y","10d","1d","2d","2m","3m","11m"};
		String tenorsString2[]={"10Y","3y","10d","1d","2d","2m","3m","11m","T/N","N/A"};
		String tenorsString3[]={"15y","1d","1m","1y","1y6m","25y","30y","8y","9m","N/A","T/N"};		
		
		tenors.addAll(Arrays.asList(tenorsString1));
		List<String> result1=new ArrayList<String>(tenors);
		tenors.clear();
		tenors.addAll(Arrays.asList(tenorsString2));
		List<String> result2=new ArrayList<String>(tenors);
		tenors.clear();
		tenors.addAll(Arrays.asList(tenorsString3));
		List<String> result3=new ArrayList<String>(tenors);
		
		assertEquals("[1d, 2d, 10d, 2m, 3m, 11m, 3y, 10Y, t/n, nan1, nan2]", result1.toString());
		assertEquals("[1d, 2d, 10d, 2m, 3m, 11m, 3y, 10Y, N/A, T/N]", result2.toString());
		assertEquals("[1d, 1m, 9m, 1y, 1y6m, 8y, 15y, 25y, 30y, N/A, T/N]", result3.toString());
		
	}
	
	
	@Test
	public void mixedTenor(){
		String tenorsString[]={"2y","2y2m","2y1m","1y1m", "n/a","something","10Y","3y","10d","t/n","2d","2m","44y3m","11m"};
		tenors.addAll(Arrays.asList(tenorsString));
		List<String> result=new ArrayList<String>(tenors);
		assertEquals("[2d, 10d, 2m, 11m, 1y1m, 2y, 2y1m, 2y2m, 3y, 10Y, 44y3m, n/a, t/n, something]", result.toString());
	}
	
	@Test
	public void weekTenors(){
		String tenorsString[]={"1d","1W","5d","3m"};
		tenors.addAll(Arrays.asList(tenorsString));
		List<String> result=new ArrayList<String>(tenors);
		assertEquals(tenorsString.length, result.size());
		assertEquals("[1d, 5d, 1W, 3m]", result.toString());
	}
	
	
	@Test
	public void yearTenors(){
		String tenorsString[]={"1y","2y","2y1m"};
		tenors.addAll(Arrays.asList(tenorsString));
		List<String> result=new ArrayList<String>(tenors);
		assertEquals(tenorsString.length, result.size());
		assertEquals("[1y, 2y, 2y1m]", result.toString());
	}
	
	@Test
	public void complexTenor(){
		String tenorsString[]={"1w","1d","8d"};
		tenors.addAll(Arrays.asList(tenorsString)); 
		List<String> result=new ArrayList<String>(tenors);
		assertEquals(tenorsString.length, result.size());
		assertEquals("[1d, 1w, 8d]", result.toString());
	}
	
	@Test
	public void complexMonthWordTenor(){
		String tenorsString[]={"JAN-03","JAN-01","JAN-02"};
		tenors.addAll(Arrays.asList(tenorsString)); 
		List<String> result=new ArrayList<String>(tenors);
		assertEquals(tenorsString.length, result.size());
		assertEquals("[JAN-01, JAN-02, JAN-03]", result.toString());
	}

	@Test
	public void complexDateWordTenor(){
		String tenorsString[]={"Net 3m","Net 1m","Net 5m","Net 30y+","Net 30y","Net 25y"};
		tenors.addAll(Arrays.asList(tenorsString)); 
		List<String> result=new ArrayList<String>(tenors);
		//System.out.println(result.toString());
		assertEquals(tenorsString.length, result.size());
	//	System.out.println(result.toString());
		assertEquals("[Net 1m, Net 3m, Net 5m, Net 25y, Net 30y, Net 30y+]", result.toString());
	}

}
