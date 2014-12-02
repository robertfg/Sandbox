package com.quartetfs.pivot.anz.utils;



import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;


public class CubeEventTest {
	private CubeEventKeeper eKeeper =null;
	private Date today;
	private Date yesterday;
	private Calendar todayCal;
	private Calendar yesterdayCal;
	@Before
	public void setup() throws ParseException{
		eKeeper= new CubeEventKeeper();
		SimpleDateFormat sdf =new SimpleDateFormat("ddMMyyyy");
		todayCal=GregorianCalendar.getInstance();
		
		
		today=todayCal.getTime();
		today=sdf.parse(sdf.format(today));
		yesterdayCal=GregorianCalendar.getInstance();
		yesterdayCal.setTime(DateUtils.addDays(today, -1));
		
		yesterday=yesterdayCal.getTime();
		yesterday=sdf.parse(sdf.format(yesterday));
	}
	
	
	@Test
	public void fileParsingEvent() throws InterruptedException{
		String fileName = "File_A";
		String fileName2 = "File_B";
		eKeeper.fileParsingStarted(fileName);
		Thread.sleep(100);
		eKeeper.fileParsingCompleted(fileName);
		
		eKeeper.fileParsingStarted(fileName2);
		performCommonValidation(EventType.Parse);
	}
	
	
	@Test
	public void fileComittEvent() throws InterruptedException{
		String fileName = "File_A";
		String fileName2 = "File_B";
		eKeeper.fileComittStarted(fileName);
		Thread.sleep(100);
		eKeeper.fileComittCompleted(fileName);
		eKeeper.fileComittStarted(fileName2);
		performCommonValidation(EventType.Commit);
	}
	
	@Test
	public void deleteEvent() throws InterruptedException{
		
		String criteria = "COB=29022011";
		String criteria2 = "COB=28022011";
		eKeeper.dataDeleteStarted(criteria);
		Thread.sleep(100);
		eKeeper.dataDeleteCompleted(criteria);
		eKeeper.dataDeleteStarted(criteria2);
		performCommonValidation(EventType.Delete);
	}
	
	//@Test
	public void parseComittDelete() throws InterruptedException{
		eKeeper.fileParsingStarted("File_A");
		eKeeper.fileComittStarted("File_A");
		eKeeper.dataDeleteStarted("COB=29022011");
		Thread.sleep(100);
		eKeeper.fileComittCompleted("File_A");
		Thread.sleep(100);
		eKeeper.dataDeleteCompleted("COB=29022011");
		Thread.sleep(100);
		eKeeper.fileParsingCompleted("File_A");
		
		List<CubeEvent> cubeEvents =eKeeper.getEventsByDate(today);
		
		assertEquals(3, cubeEvents.size());
		CubeEvent parseEvent=cubeEvents.get(0);
		CubeEvent comittEvent=cubeEvents.get(1);
		CubeEvent deleteEvent=cubeEvents.get(2);
		
		assertEquals(parseEvent.getType(), EventType.Parse);
		
		assertEquals(comittEvent.getType(), EventType.Commit);
		assertTrue(parseEvent.getEndTime().compareTo(comittEvent.getEndTime())>0);
		
		assertTrue(comittEvent.getEndTime().compareTo(deleteEvent.getStartTime())>0);
		
		assertTrue(deleteEvent.getEndTime().compareTo(comittEvent.getEndTime())>0);
		
	}
	
	@Test(expected=IllegalStateException.class)
	public void sameEventTwiceWithoutComplete(){
		eKeeper.fileParsingStarted("File_A");
		eKeeper.fileParsingStarted("File_A");
		
	}

	@Test
	public void sameEventTwiceWithComplete(){
		eKeeper.fileParsingStarted("File_A");
		eKeeper.fileParsingCompleted("File_A");
		eKeeper.fileParsingStarted("File_A");
		List<CubeEvent> cubeEvents =eKeeper.getEventsByDate(today);
		assertEquals(2, cubeEvents.size());
	}
	
	
	@Test(expected=IllegalStateException.class)
	public void nullfileParsingStarted(){
		eKeeper.fileParsingStarted( null );
	}

	@Test(expected=IllegalStateException.class)
	public void nullfileParsingCompleted(){
		eKeeper.fileParsingCompleted(null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void nullfileComittStarted(){
		eKeeper.fileComittStarted(null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void nullfileComittCompleted(){
		eKeeper.fileComittCompleted( null );
	}
	
	@Test(expected=IllegalStateException.class)
	public void nulldataDeleteStarted(){
		eKeeper.dataDeleteStarted(null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void nulldataDeleteCompleted(){
		eKeeper.dataDeleteCompleted(null);
	}
	

	
	@Test
	public void twoDiffDateEvents(){
		
		eKeeper.setCalendar(yesterdayCal);
		eKeeper.fileParsingStarted("File_B");
		eKeeper.fileParsingCompleted("File_B");
		
		eKeeper.setCalendar(null);
		eKeeper.fileParsingStarted("File_A");
		eKeeper.fileParsingCompleted("File_A");
		
		
		List<CubeEvent> cubeEvents =eKeeper.getEventsByDate(today);
		List<CubeEvent> yestderdaycubeEvents =eKeeper.getEventsByDate(yesterday);
		CubeEvent todayEvent=cubeEvents.get(0);
		CubeEvent yesterdayEvent=yestderdaycubeEvents.get(0);
		
		assertEquals(1, cubeEvents.size());
		assertEquals(1, yestderdaycubeEvents.size());
		assertTrue(todayEvent.getStartTime().compareTo(yesterdayEvent.getEndTime())>0);
		
	
		
		
	}
	
		
	/**
	 *always check for two events first completed and second non completed
	 * @param type
	 */
	private void performCommonValidation(EventType type){
		List<CubeEvent> cubeEvents =eKeeper.getEventsByDate(today);
		List<CubeEvent> yestderdaycubeEvents =eKeeper.getEventsByDate(yesterday);
		assertNull(yestderdaycubeEvents);
		
		assertEquals(2, cubeEvents.size());
		CubeEvent event1=cubeEvents.get(0);
		CubeEvent event2=cubeEvents.get(1);
		
		assertEquals(event1.getType(), type);
		assertEquals(event2.getType(), type);
		
		assertNotNull(event2.getStartTime());
		assertNull(event2.getEndTime());
		
		assertNotNull(event1.getStartTime());
		assertNotNull(event1.getEndTime());
		
		assertTrue(event1.getEndTime().compareTo(event1.getStartTime())>0);
	}
	
	
}
