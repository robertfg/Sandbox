package com.quartetfs.pivot.anz.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PortfolioHierarchyServiceTest {

	@Test
	public void testResolveFullHierarchy() {
//		final PortfolioHierarchyService service = new PortfolioHierarchyService();
//		service.loadFile(this.getClass().getResource("/portfolio_20110301.txt")
//				.getFile());
//		assertTrue(ArrayUtils.isEquals(new String[] { "~EQHK VI HK M AU","~EQHK VI HK M AU",
//				"~EQHK VI HK M AU", "~EQHK VI HK M AU", "~EQHK VI HK M AU",
//				"~EQHK VI HK M AU", "EQHK VI HK M AU", "Equity Asia - IPG",
//				"Equity - Asia", "Equity" },
//				service.resolveFullHierarchy("EQHK VI HK M AU")));
//		final String[] empty = new String[9];
//		Arrays.fill(empty, ANZUtils.UNAVAILABLE);
//		assertTrue(ArrayUtils.isEquals(empty,
//				service.resolveFullHierarchy("aaa")));
//		assertTrue(ArrayUtils.isEquals(empty,
//				service.resolveFullHierarchy("Root")));
//		assertTrue(ArrayUtils.isEquals(empty,
//				service.resolveFullHierarchy(null)));
	}

	@Test
	public void testNormalize() {
//		final PortfolioHierarchyService service = new PortfolioHierarchyService();
//
//		final Map<String, String> child2Parent = new HashMap<String, String>();
//		child2Parent.put("Equity", "Root");
//		child2Parent.put("Combined Trading", "Root");
//		child2Parent.put("Traded", "Combined Trading");
//		child2Parent.put("Management Trading", "Combined Trading");
//		child2Parent.put("CM TR PROP 02", "Management Trading");
//		child2Parent.put("Structured Credit Discontinued Business",
//				"Combined Trading");
//		child2Parent.put("Structured Credit FX hedges",
//				"Structured Credit Discontinued Business");
//		child2Parent.put("FXO TR PROP 07", "Structured Credit FX hedges");
//		child2Parent.put("Sales", "Combined Trading");
//		child2Parent.put("FXO Sales", "Sales");
//		child2Parent.put("FXO Sales - Asia", "FXO Sales");
//		child2Parent.put("FXO SALE SG", "FXO Sales - Asia");
//
//		service.normalize(child2Parent);
//		final String[] hierarchyEquity2 = service
//				.resolveFullHierarchy("Equity");
//		assertEquals(5, hierarchyEquity2.length);
//		assertTrue(ArrayUtils.isEquals(hierarchyEquity2, new String[] {
//				"~Equity", "~Equity", "~Equity", "~Equity" ,"Equity"}));
//		final String[] hierarchyTraded2 = service
//				.resolveFullHierarchy("Traded");
//		assertEquals(5, hierarchyTraded2.length);
//		assertTrue(ArrayUtils.isEquals(hierarchyTraded2, new String[] {
//				"~Traded", "~Traded", "~Traded","Traded", "Combined Trading" }));
//		final String[] hierarchyManagementTrading2 = service
//				.resolveFullHierarchy("CM TR PROP 02");
//		assertEquals(5, hierarchyManagementTrading2.length);
//		assertTrue(ArrayUtils.isEquals(hierarchyManagementTrading2,
//				new String[] { "~CM TR PROP 02", "~CM TR PROP 02","CM TR PROP 02",
//						"Management Trading", "Combined Trading" }));
//		final String[] hierarchyDiscontinuedBusiness2 = service
//				.resolveFullHierarchy("FXO TR PROP 07");
//		assertEquals(5, hierarchyDiscontinuedBusiness2.length);
//		assertTrue(ArrayUtils.isEquals(hierarchyDiscontinuedBusiness2,
//				new String[] { "~FXO TR PROP 07","FXO TR PROP 07",
//						"Structured Credit FX hedges",
//						"Structured Credit Discontinued Business",
//						"Combined Trading" }));
//		final String[] hierarchySales2 = service
//				.resolveFullHierarchy("FXO SALE SG");
//		assertEquals(5, hierarchySales2.length);
//		assertTrue(ArrayUtils.isEquals(hierarchySales2, new String[] {
//				"FXO SALE SG","FXO Sales - Asia", "FXO Sales", "Sales", "Combined Trading" }));
	}
}
