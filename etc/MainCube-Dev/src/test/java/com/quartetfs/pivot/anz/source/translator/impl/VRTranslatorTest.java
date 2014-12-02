/*package com.quartetfs.pivot.anz.source.translator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.quartetfs.fwk.messaging.IFileInfo;
import com.quartetfs.fwk.messaging.ILineReader;
import com.quartetfs.fwk.messaging.impl.FileInfo;
import com.quartetfs.pivot.anz.model.impl.Deal;
import com.quartetfs.pivot.anz.model.impl.VRParsingEntry;
import com.quartetfs.pivot.anz.service.impl.PSRService;

public class VRTranslatorTest {
	
	private VRTranslator translator=null;
	private ILineReader reader1,reader2,record;
	
	@Before
	public void setup() throws Exception{
		Map<String, Integer> columnOrders=new HashMap<String, Integer>();
		Map<String, String> columnTypes=new HashMap<String, String>(); 
		
		columnOrders.put("date",8);
		columnOrders.put("result",9);
		columnOrders.put("resultv",10);
		columnOrders.put("psrName", 11);
		
		columnTypes.put("date", "date[dd-MM-yyyy]");
		columnTypes.put("result", "string");
		columnTypes.put("resultv", "string");
		columnTypes.put("psrName", "string");
		
		
		setUpTranslator(columnOrders, columnTypes);
		
		reader1=mockreader(); 
		when(reader1.readSequence(8)).thenReturn("01-08-2011");
		
		reader2=mockreader();
		when(reader2.readSequence(8)).thenReturn("");
	}

	private void setUpTranslator(Map<String, Integer> columnOrders,
			Map<String, String> columnTypes) throws Exception {
		translator=new VRTranslator();
		translator.setBaseCCYIndex(1);
		translator.setDealNumberIndex(2);
		translator.setFamilyIdx(3);
		translator.setGroupIdx(4);
		translator.setInstrumentIdx(5);
		translator.setMcurrIdx(6);
		translator.setPortfolioIndex(7);
		translator.setColumnOrders(columnOrders);
		translator.setColumnTypes(columnTypes);
		translator.setPsrNameIndex(1);
		
//		translator.setDatePattern("dd-MM-yyyy");
	}
	
	
	
	private ILineReader mockreader(){
		ILineReader reader=mock(ILineReader.class);
		when(reader.read(eq(1))).thenReturn("AUD");
		when(reader.parseLong(eq(2))).thenReturn(23452L);
		
		when(reader.read(eq(3))).thenReturn("EQT");
		when(reader.read(eq(4))).thenReturn("OPT");
		when(reader.read(eq(5))).thenReturn("ASX");
		when(reader.read(eq(6))).thenReturn("USD");
		when(reader.read(eq(7))).thenReturn("FXO APG U");
		
		
		when(reader.readSequence(10)).thenReturn("3121.3211");
		when(reader.readSequence(9)).thenReturn("3");
		when(reader.read(11)).thenReturn("TAPV0");
		
		return reader;
	}
	
	//@Test
	public void testSimpleTranslate() throws Exception{
		
		
		
		IFileInfo fileInfo = new FileInfo(new File("TAPV0_AM00#VR170811_UVR.gz"));
		when(reader1.getCurrentFile()).thenReturn(fileInfo);
		when(reader1.readSequence(11)).thenReturn("TAPV0");
		
		PSRService psrService = new PSRService();
		psrService.setCurrentFilePsrName("TAPV0");
		
		translator.setPsrService(psrService);
		translator.setPsrNameIndex(11);
		Object obj=translator.translate(reader1);
		
		
		verify(reader1, atLeastOnce()).readSequence(8);
	    verify(reader1, times(1)).parseLong(2);
		
		assertNotNull(obj);
		assertTrue(obj instanceof VRParsingEntry);
		VRParsingEntry entry=(VRParsingEntry) obj;
		Deal deal=entry.getDeal();
		assertNotNull(deal);
		assertEquals("AUD", deal.getBaseCCY());
		assertEquals(23452,deal.getDealNum());
		assertEquals("EQT",deal.getFamily());
		assertEquals( "OPT",deal.getGroup());
		assertEquals("ASX",deal.getInstrument());
		assertEquals("FXO APG U",deal.getPortfolio());
		assertEquals("USD",deal.getMcurr());
		assertEquals("8/1/11",deal.getDate().toString());
	}
	
	@Test(expected=NullPointerException.class)
	public void testIncorrectDate() throws Exception{
		 
		translator.translate(reader2);
	}
	
	
	
	public void varTranslatorSetup()  {

		Map<String, Integer> columnOrders = new HashMap<String, Integer>();
		Map<String, String> columnTypes = new HashMap<String, String>();
		
		columnOrders.put("psrName", 1);
		columnOrders.put("date", 2);

		columnTypes.put("date", "date[dd-MM-yyyy]");
		columnTypes.put("psrName", "string");
		try {
			setUpTranslator(columnOrders, columnTypes);
		} catch (Exception e) {
		
			e.printStackTrace();
		}

		record = mock(ILineReader.class);
	
		int containerNameIndex = 2;
		when(record.read(containerNameIndex)).thenReturn("A");

		int dealNumberIndex = 3;
		when(record.parseLong(dealNumberIndex)).thenReturn(Long.valueOf(3));

		int keyIdIndex = 4;
		when(record.read(keyIdIndex)).thenReturn("5");

		when(record.readSequence(1)).thenReturn("A");
		when(record.readSequence(2)).thenReturn("01-08-2011");

		IFileInfo fileInfo = new FileInfo(new File("TAPV0_AM00#VR170811_UVR.gz"));

		when(record.getCurrentFile()).thenReturn(fileInfo);

	}

	

	@Test
	public void testAllCorrectData() {
		varTranslatorSetup();
		PSRService psrService = new PSRService();
		psrService.setCurrentFilePsrName("TAPV0");
		translator.setPsrService(psrService);
		when(record.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content
																				// psr
																				// name

		Object translatedObject = translator.translate(record);
		assertNotNull(translatedObject);
	}

	@Test
	public void testNullContentPsrName() {
		varTranslatorSetup();
		PSRService psrService = new PSRService();
		psrService.setCurrentFilePsrName("TAPV0");
		translator.setPsrService(psrService);
		when(record.read(translator.getPsrNameIndex())).thenReturn(null); // content
																			// psr
																			// name
		Object translatedObject = translator.translate(record);
		Assert.assertNull(translatedObject);

	}

	@Test
	public void testNullPsrFileName() {
		varTranslatorSetup();
		PSRService psrService = new PSRService();
		psrService.setCurrentFilePsrName(null);
		translator.setPsrService(psrService);
		when(record.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content
																				// psr
																				// name

		Object translatedObject = translator.translate(record);
		Assert.assertNull(translatedObject);
	}

	@Test
	public void testAllMultipleCorrectData() {
		varTranslatorSetup();
		List<Object> records = new ArrayList<Object>();
		
		

		ILineReader rec1, rec2, rec3, rec4, rec5;

		rec1 = mock(ILineReader.class);
		when(rec1.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content psr name
		setUpReader(rec1,"TAPV0");													
																			

		rec2 = mock(ILineReader.class);
		when(rec2.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content psr name
		setUpReader(rec2,"TAPV0");													
																			

		rec3 = mock(ILineReader.class);
		when(rec3.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content psr name
		setUpReader(rec3,"TAPV0");											
																			

		rec4 = mock(ILineReader.class);
		when(rec4.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content psr name
		setUpReader(rec4,"TAPV0");											
																			

		rec5 = mock(ILineReader.class);
		when(rec5.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content psr name
		setUpReader(rec5,"TAPV0");											
		
		PSRService psrService = new PSRService();
		psrService.setCurrentFilePsrName("TAPV0");
		translator.setPsrService(psrService);
		translator.setPsrNameIndex(1);
		

		Object translatedRec1 = translator.translate(rec1);
		assertNotNull(translatedRec1);
		records.add(translatedRec1);

		Object translatedRec2 = translator.translate(rec2);
		assertNotNull(translatedRec2);
		records.add(translatedRec2);

		Object translatedRec3 = translator.translate(rec3);
		assertNotNull(translatedRec3);
		records.add(translatedRec3);

		Object translatedRec4 = translator.translate(rec4);
		assertNotNull(translatedRec4);
		records.add(translatedRec4);

		Object translatedRec5 = translator.translate(rec5);
		assertNotNull(translatedRec5);
		records.add(translatedRec5);

		Assert.assertEquals(5, records.size());
		
	}

	
	
	@Test
	public void testAllMultiplerecordOneWrongDataInsideTheFile() {
		varTranslatorSetup();
		List<Object> records = new ArrayList<Object>();
		
		ILineReader rec1, rec2, rec3, rec4, rec5;

		rec1 = mock(ILineReader.class);
		when(rec1.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content psr name
		setUpReader(rec1,"TAPV0");													
																			

		rec2 = mock(ILineReader.class);
		when(rec2.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content psr name
		setUpReader(rec2,"TAPV0");													
																			

		rec3 = mock(ILineReader.class);
		when(rec3.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content psr name
		setUpReader(rec3,"TAPV0");											
																			

		rec4 = mock(ILineReader.class);
		when(rec4.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content psr name
		setUpReader(rec4,"TAPV0");											
																			

		rec5 = mock(ILineReader.class);
		when(rec5.read(translator.getPsrNameIndex())).thenReturn("TAPG0"); // content psr name
		setUpReader(rec5,"TAPG0");											
		
		PSRService psrService = new PSRService();
		psrService.setCurrentFilePsrName("TAPV0");
		translator.setPsrService(psrService);
		translator.setPsrNameIndex(1);
		

		Object translatedRec1 = translator.translate(rec1);
		assertNotNull(translatedRec1);
		records.add(translatedRec1);

		Object translatedRec2 = translator.translate(rec2);
		assertNotNull(translatedRec2);
		records.add(translatedRec2);

		Object translatedRec3 = translator.translate(rec3);
		assertNotNull(translatedRec3);
		records.add(translatedRec3);

		Object translatedRec4 = translator.translate(rec4);
		assertNotNull(translatedRec4);
		records.add(translatedRec4);

		Object translatedRec5 = translator.translate(rec5);
		Assert.assertNull(translatedRec5);
		if(translatedRec5!=null){
			records.add(translatedRec5);	
		}
		Assert.assertEquals(4, records.size());
		
	}

	
	@Test
	public void testAllMultipleWrongDataInsideTheFile() {
		varTranslatorSetup();
		List<Object> records = new ArrayList<Object>();
		
		

		
		
		ILineReader rec1, rec2, rec3, rec4, rec5;

		rec1 = mock(ILineReader.class);
		when(rec1.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content psr name
		setUpReader(rec1,"TAPV0");	
		
						
		rec2 = mock(ILineReader.class);
		when(rec2.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content psr name
		setUpReader(rec2,"TAPV0");													
																			

		rec3 = mock(ILineReader.class);
		when(rec3.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content psr name
		setUpReader(rec3,"TAPV0");											
																			

		rec4 = mock(ILineReader.class);
		when(rec4.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content psr name
		setUpReader(rec4,"TAPV0");											
																			

		rec5 = mock(ILineReader.class);
		when(rec5.read(translator.getPsrNameIndex())).thenReturn("TAPV0"); // content psr name
		setUpReader(rec5,"TAPV0");											
																			
		PSRService psrService = new PSRService();
		psrService.setCurrentFilePsrName("TAPG0");
		translator.setPsrService(psrService);
		translator.setPsrNameIndex(1);

		
		Object translatedRec1 = translator.translate(rec1);
		Assert.assertNull(translatedRec1);
		
		if(translatedRec1!=null){
			records.add(translatedRec1);	
		}
		
		
	
		Object translatedRec2 = translator.translate(rec2);
		Assert.assertNull(translatedRec2);
		if(translatedRec2!=null){
			records.add(translatedRec2);	
		}

		Object translatedRec3 = translator.translate(rec3);
		Assert.assertNull(translatedRec3);
		if(translatedRec3!=null){
			records.add(translatedRec3);	
		}
		

		Object translatedRec4 = translator.translate(rec4);
		Assert.assertNull(translatedRec4);
		
		if(translatedRec4!=null){
			records.add(translatedRec4);	
		}

		Object translatedRec5 = translator.translate(rec5);
		Assert.assertNull(translatedRec5);
		if(translatedRec5!=null){
			records.add(translatedRec5);	
		}

		Assert.assertEquals(0, records.size());
		
	}

	
	
	
	private void setUpReader(ILineReader rec, String psrName) {
		varTranslatorSetup();
		int containerNameIndex = 2;
		when(rec.read(containerNameIndex)).thenReturn(psrName);

		int dealNumberIndex = 3;
		when(rec.parseLong(dealNumberIndex)).thenReturn(Long.valueOf(3));

		int keyIdIndex = 4;
		when(rec.read(keyIdIndex)).thenReturn("5");

		when(rec.readSequence(1)).thenReturn(psrName);
		when(rec.readSequence(2)).thenReturn("01-08-2011");

		IFileInfo fileInfo = new FileInfo(new File("TAPV0_AM00#VR170811_UVR.gz"));

		when(rec.getCurrentFile()).thenReturn(fileInfo);

		
	}
	  

}
*/