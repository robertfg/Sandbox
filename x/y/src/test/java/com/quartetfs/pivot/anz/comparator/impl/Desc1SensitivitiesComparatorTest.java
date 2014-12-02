package com.quartetfs.pivot.anz.comparator.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class Desc1SensitivitiesComparatorTest {

	@Test
	public void testCompare() {
		final String[] notSortedArray = new String[] { "-0.25%", "-0.50%",
				"-0.75%", "-1%", "-1.00%", "-1.25%", "-1.50%", "-1.75%",
				"-10%", "-2%", "-2.00%", "-2.25%", "-2.50%", "-2.75%",
				"-3.00%", "10 Put", "-3.25%", "Net 1m-2y", "-3.50%", "-3.75%",
				"25 Put", "-4.00%", "-4.25%", "-4.50%", "-4.75%", "ATM", "-5%",
				"-5.00%", "Total", "0.25%", "25 Call", "1d", "0.50%", "0.75%",
				"1%", "1.00%", "1.25%", "1.50%", "1.75%", "10%", "10y", "1D",
				"1m", "1w", "10 Call", "1y", "2%", "2.00%", "Net 1m-1y",
				"2.25%", "Net 3y-10y", "2.50%", "2.75%", "2D", "2m", "2y",
				"3.00%", "3.25%", "3.50%", "3.75%", "3D", "3m", "3y",
				"Net 2y-10y", "4.00%", "4.25%", "4.50%", "4.75%", "4D", "4y",
				"5%", "5.00%", "5D", "5y", "6m", "7y", "All", "N/A" };

		final String[] sortedArray = new String[] { "N/A", "All", "Total",
				"1d", "1w", "1m", "2m", "3m", "6m", "1y", "Net 1m-1y", "2y",
				"Net 1m-2y", "3y", "4y", "5y", "7y", "10y", "Net 2y-10y",
				"Net 3y-10y", "1D", "2D", "3D", "4D", "5D", "ATM", "10 Call",
				"25 Call", "10 Put", "25 Put", "-10%", "-5%", "-2%", "-1%",
				"1%", "2%", "5%", "10%", "-5.00%", "-4.75%", "-4.50%",
				"-4.25%", "-4.00%", "-3.75%", "-3.50%", "-3.25%", "-3.00%",
				"-2.75%", "-2.50%", "-2.25%", "-2.00%", "-1.75%", "-1.50%",
				"-1.25%", "-1.00%", "-0.75%", "-0.50%", "-0.25%", "0.25%",
				"0.50%", "0.75%", "1.00%", "1.25%", "1.50%", "1.75%", "2.00%",
				"2.25%", "2.50%", "2.75%", "3.00%", "3.25%", "3.50%", "3.75%",
				"4.00%", "4.25%", "4.50%", "4.75%", "5.00%" };

		final Desc1SensitivitiesComparator comparator = new Desc1SensitivitiesComparator();
		comparator.setFirstObjects(Arrays.asList("N/A", "All", "Total", "1d",
				"1w", "1m", "2m", "3m", "6m", "1y", "Net 1m-1y", "2y",
				"Net 1m-2y", "3y", "4y", "5y", "7y", "10y", "Net 2y-10y",
				"Net 3y-10y", "1D", "2D", "3D", "4D", "5D", "ATM", "10 Call",
				"25 Call", "10 Put", "25 Put"));
		Arrays.sort(notSortedArray, comparator);
		//assertEquals(sortedArray.length, notSortedArray.length);
		//assertTrue(Arrays.equals(sortedArray, notSortedArray));
	}
}
