package com.quartetfs.pivot.anz.utils;

import static junit.framework.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PropertiesSolverTest {

	@Test
	public void testSolve() {
		final Properties props1 = new Properties();
		props1.setProperty("test", "value of ${test2}");
		props1.setProperty("test2", "value2");
		props1.setProperty("value22", "toto");

		final Properties props2 = new Properties();
		props2.setProperty("prop",
				" a non trivial ${test} : ${test2} : ${${test2}2}");

		assertEquals(" a non trivial value of value2 : value2 : toto",
				PropertiesSolver.solve(props1, props2).getProperty("prop"));
	}

}
