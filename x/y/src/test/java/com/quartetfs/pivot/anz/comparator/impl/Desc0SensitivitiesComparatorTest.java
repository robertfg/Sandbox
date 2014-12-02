package com.quartetfs.pivot.anz.comparator.impl;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
public class Desc0SensitivitiesComparatorTest {
	Desc0SensitivitiesComparator comparator=new Desc0SensitivitiesComparator();
	String intValues[]=new String[]{"10","100","-10","60", "0", "15", "-90"};
	String resultIntValues[]=new String[]{"-90","-10","0","10","15","60","100"};
	String doubleValues[]=new String[]{"1000.632","2.5", "10.5", "0.89", "-3.2", "-1.0", "0.0"};
	String resultDoubleValues[]=new String[]{"-3.2","-1.0","0.0","0.89","2.5","10.5","1000.632"};
	String mixValues[]= new String[]{"USD","-60","AUD","10","5","0"};
	String resultMixValues[]= new String[]{"-60","0","5","10","AUD","USD"};
	String [] customValues=new String[]{"20M","60M","1Y","1W","2Y"}; 
	String [] customResultValues=new String[]{"60M", "20M", "1W", "1Y", "2Y"};

	String CustomMixValues[]= new String[]{"AAX.AX","N/A","N/A","USD","-60","AUD","20M","10","5","0"};
	String resultCustomMixValues[]= new String[]{"20M","-60","0","5","10","AUD","USD","AAX.AX","N/A","N/A"};

	
	
	@Before
	public void setup ()
	{
		comparator.setFirstObjects(Arrays.asList(customResultValues));
	}
	
	
	
	@Test
	public void sortNumbers()
	{
		Arrays.sort(intValues,comparator);
		assertArrayEquals(resultIntValues, intValues);
		Arrays.sort(doubleValues,comparator);
		assertArrayEquals(resultDoubleValues, doubleValues);
	}
	
	@Test
	public void sortCustom()
	{
		Arrays.sort(customValues,comparator);
		assertArrayEquals(customResultValues,customValues);
	}
	@Test
	public void sortMixCustom()
	{
		Arrays.sort(mixValues,comparator);
		assertArrayEquals(resultMixValues, mixValues);
		
		Arrays.sort(CustomMixValues,comparator);
		assertArrayEquals(resultCustomMixValues, CustomMixValues);

		
	}
	
}
