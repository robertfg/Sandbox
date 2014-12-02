package com.quartetfs.pivot.anz.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RebuildCubeFromJmxTest {

	RebuildCubeFromJmx jmx;

	String jmxBeanName;
	String jmxMethodName;

	@Before
	public void setup() {
		String userName = "apuser";
		String password = "r3r4NZ";
		String hostAddr = "localhost";
		String port = "7009";

		jmxBeanName = "com.quartetfs:node0=ActivePivotManager,node1=MarketRiskSchema,node2=MarketRiskCube,node3=AggregateProvider";
		jmxMethodName = "Rebuild and compress the structure";
		jmx = new RebuildCubeFromJmx(userName, password, hostAddr, port);

	}

	@Test
	public void testJmxInvoke() {

		try {
			boolean ret = jmx.invoke(jmxBeanName, jmxMethodName);
			assertEquals(true, ret);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testJmxInvokeWrongBeanName() {
		jmxBeanName = "wrong bean name";
		jmx.invoke(jmxBeanName, jmxMethodName);
	}

	@Test(expected = IllegalStateException.class)
	public void testJmxInvokeWrongMethodName() {
		jmxMethodName = "wrong method name";
		boolean ret = jmx.invoke(jmxBeanName, jmxMethodName);
		assertEquals(true, ret);
	}

	@Test(expected = IllegalStateException.class)
	public void testWrongHostName() {
		jmx.setHostName("asdf");
		jmx.invoke(jmxBeanName, jmxMethodName);
	}

	@Test(expected = IllegalStateException.class)
	public void testWrongPort() {
		jmx.setPort("1222");
		jmx.invoke(jmxBeanName, jmxMethodName);
	}

	@Test(expected = IllegalStateException.class)
	public void testNoUserNameAndPassword() {
		jmx.setUserName("");
		jmx.setPassword("");
		jmx.invoke(jmxBeanName, jmxMethodName);
	}

}
