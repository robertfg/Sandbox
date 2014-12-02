/*package com.quartetfs.pivot.anz.postprocessing.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VarHelperTest {

//	private double [] vector500=new double[500]; 
//	private double [] vector100=new double[100];
//	protected double[] confidenceLevels = new double[]{0, 0.01, 0.025, 0.975, 0.99, 1};
//	protected int[] expected500 = new int[]{500, 496, 488,13, 5, 1};
//	protected int[] expected100 = new int[]{100, 100, 98,3, 1, 1};
//	
	@Before
	public void setup()
	{
		for (int i=0;i<500;i++)
		{  
			vector500[i]=++i;   
		}
		
		for (int i=0;i<100;i++)
		{
			vector100[i]=++i;
		}
		
	}
	
	
	@Test
	public void vector500()
	{
		int [] result=new int[6];
		int i=0;
		for (double d: confidenceLevels)
		{
			result[i++]=VaRHelper.getIndexFromVectorLength(vector500.length,d)+1;
		}
		
		
		
		Assert.assertArrayEquals(expected500, result);
	}
	
	
	@Test
	public void vector100()
	{
		int [] result=new int[6];
		int i=0;
		for (double d: confidenceLevels)
		{
			result[i++]=VaRHelper.getIndexFromVectorLength(vector100.length,d)+1;
		}
		
		Assert.assertArrayEquals(expected100, result);
	}
	

	@Test
	public void test()
	{
		int chunkOrder=2;
		System.out.println( 123&((1 << chunkOrder) - 1));
	}
	
}
*/