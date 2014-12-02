package com.quartetfs.pivot.anz.extraction;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class AnEmptyContainerMappingManagerShould {
	private ContainerMappingManager manager;
	@Before
	public void init(){
		manager=new ContainerMappingManager("ContainerMappingTestData", "ContainerMappingEmpty.csv");
	}
	@Test
	public void NotThrowExceptionWhenInit(){
		manager.init();
	}
	@Test
	public void ReturnEmptyMappings(){
		assertEquals(manager.getMappings().size(),0);
	}
	@Test
	public void ReturnEmptyMeasures(){
		assertEquals(manager.getAllContainerMeasures().size(), 0);
	}
	
	
}
