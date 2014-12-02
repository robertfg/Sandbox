package soupUiTestCases;

import file.ReadAndCreate;
import xml.XmlDomUtil;

public class GenerateTestCase {

	public final static String[] container = new String[] { 
		"ANZ_STRESS_CM_ALG", "ANZ_STRESS_CREDIT", "ANZ_STRESS_EQCORR",
		"ANZ_STRESS_EQ_ALG", "ANZ_STRESS_FX_ALG", "ANZ_STRESS_INF",
		"ANZ_STRESS_IR_ALG", "ANZ_STRESS_IR_VOL", "APRA_STRESS_CM",
		"APRA_STRESS_EQ", "APRA_STRESS_FX_ALG", "APRA_STRESS_INF",
		"APRA_STRESS_IR", "APRA_STRESS_IR_VOL", "APRA_STRESS_PM",
		"CM_PL_LADDER", "COM_ADAPTED_DELTA", "COM_DELTA", "COM_GAMMA",
		"COM_VEGA", "DELTA_BASIS", "DV01", "EQ_CORRELATIONS",
		"EQ_DELTAGAMMA", "EQ_DIVIDENDS", "EQ_MONEYNESSVEGA",
		"EQ_SPOTVOL", "EQ_VEGA", "EXTRAORD_ALG", "FXO_DELGAMMA",
		"FXO_VEGA", "GAMMA_BASIS", "INFLATION_DELTA", "IR_CORR_SENS",
		"IR_CORR_STRESS", "IR_GAMMA", "IR_SMILE_SENS", "IR_VANNA",
		"IR_VEGA", "IR_VOLGA", "NPV", "PAR_CR01", "PAR_DV01",
		"RR_SENS", "SCCL", "THETA", "VAR_STRESS", "VaR and P&amp;L"
	};
	
	public final static String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<con:testSuite name=\"LimitServiceSoapBinding TestSuite\" xmlns:con=\"http://eviware.com/soapui/config\">"
			+ "	<con:settings/> " 
			+ "<con:runType>SEQUENTIAL</con:runType>"
			+ "<con:testCase failOnError=\"true\" failTestCaseOnErrors=\"true\" keepSession=\"false\" maxResults=\"0\" name=\"extractExcess TestCase\" searchProperties=\"true\">"
			+ "<con:settings/>\n";
	
	public final static String FOOTER = "<con:properties/></con:testCase><con:properties/></con:testSuite>";
	
	public final static String wsUrl = "http://10.52.16.1:8080/cube/webservices/LimitService";
	
	
	public static void main(String[] args){
		
		GenerateTestCase.generateTestCase("c:\\devs\\testcases\\limitextract.xml", "c:\\devs\\testcases\\limitExtractTestCases.xml");
		
	}
	
	public static String generateTestCase(String testCaseTemplateLocation, String testCaseDestinationLocation){
		ReadAndCreate rc = new ReadAndCreate();
		String template = rc.read(testCaseTemplateLocation); //

		String testCases = HEADER;

		for (int i = 0; i < container.length; i++) {
			String temp = template;
			testCases += temp.replace("$container", container[i]).replace("$url",wsUrl)+ "\n";
		}
		
		testCases+=FOOTER;
		XmlDomUtil xmlPretty = new XmlDomUtil();
		rc.write(testCaseDestinationLocation, xmlPretty.format(testCases));
		return xmlPretty.format(testCases);
		
	}
	
}
