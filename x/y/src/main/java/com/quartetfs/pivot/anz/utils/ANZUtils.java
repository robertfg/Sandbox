/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.utils;

import static com.quartetfs.pivot.anz.utils.ANZConstants.FILE_EXT_FAILURE;
import static com.quartetfs.pivot.anz.utils.ANZConstants.FILE_EXT_SUCCESS;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.time.FastDateFormat;
//import org.dom4j.Document;
//import org.dom4j.DocumentException;
//import org.dom4j.Element;
//import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.quartetfs.biz.pivot.IActivePivot;
import com.quartetfs.biz.pivot.context.subcube.impl.ASubCubeDimension;
import com.quartetfs.biz.pivot.context.subcube.impl.SubCubeProperties;
import com.quartetfs.biz.pivot.cube.hierarchy.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.axis.IAxisMember;
import com.quartetfs.biz.pivot.query.IDrillthroughQuery;
import com.quartetfs.pivot.anz.impl.MessagesANZ;

public final class ANZUtils { 

	public static Document schemaConfig;
	public static XPath xPath;
	
	
	
	private static final Logger LOGGER = Logger.getLogger(ANZUtils.class.getSimpleName());
	private static FastDateFormat df = FastDateFormat.getInstance("yyyyMMdd");
			
	public static String[] generatePortfolioHierarchy(String portfHierarchy){
		return generateHierarchy(portfHierarchy,ANZConstants.PORTFOLIOHIERARCHY_DEPTH);
	}

	public static String[] generateGeoHierarchy(String portfHierarchy){
		return generateHierarchy(portfHierarchy,ANZConstants.GEOHIERARCHY_DEPTH);
	}


	public static String[] generateHierarchy(String hierarchyStr, int maxLen){
		String [] result=new String[maxLen];

		if (hierarchyStr!=null && hierarchyStr.length()>1 && hierarchyStr.indexOf("|")>-1 ){
			StringTokenizer stringToken=new StringTokenizer(hierarchyStr, ANZConstants.PIPE_SEPARATOR);

			int count=0;
			try{
				while(count<maxLen){
					result[count++]=stringToken.nextToken();
				}
			}catch(NoSuchElementException ex){
				//LOGGER.log(Level.WARNING, formatMessage(MessagesANZ.ERR_PARSE_HIERARCHY, new Object[]{hierarchyStr,maxLen,count}));
			} 
		}
		return result;
	}

	public static double[] addTwoArrays(Object oldValuesObj, Object newValuesObj){
		
		if (oldValuesObj==null) return (double[])newValuesObj;
		if (newValuesObj==null) return (double[])oldValuesObj;
		double[] oldValues=(double[]) oldValuesObj;
		double[] newValues=(double[]) newValuesObj;
		
		for (int i = 0; i < newValues.length; i++) {
			oldValues[i] += newValues[i];
		}
		return oldValues;
	}
	public static String cleanString(String stringToClean){
		String stringCleaned = stringToClean;
		if (stringToClean==null || stringToClean.trim().length() == 0){
			stringCleaned=ANZConstants.UNAVAILABLE;
		}
		return stringCleaned;
	}

	public static String[] generateCurrPairHierarchy(String currPair) {
		return generateHierarchy(currPair,ANZConstants.CURRENCYPAIR_DEPTH);
	}

	public static String[] generateCurrHierarchy(String currHierarchy) {
		return generateHierarchy(currHierarchy,ANZConstants.CURRENCY_DEPTH);

	}

	public static String[] generateEQHierarchy(String eqHierarchy) {
		return generateHierarchy(eqHierarchy,ANZConstants.EQUITY_DEPTH);
	}

	public static String[] generateCurrGroupHierarchy(String currGrouping) {
		return generateHierarchy(currGrouping,ANZConstants.CURGROUPING_DEPTH);
	}

	public static String formatMessage(String template, Object... params){
		ResourceBundle messages = ResourceBundle.getBundle(MessagesANZ.BUNDLE); 
		MessageFormat formatter = new MessageFormat("");
		formatter.applyPattern(messages.getString(template));
		return formatter.format(params);
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> getSortedTermBucket(IDimension dimension ){
		List<String> ret = new ArrayList<String>();
		
		@SuppressWarnings("rawtypes")
		List<IAxisMember> members = (List<IAxisMember>) ((ASubCubeDimension) dimension).getBaseDimension().retrieveMembers(1);		
		for (IAxisMember member : members) {
			ret.add( member.getDiscriminator().toString().toLowerCase() );
		}		
		TenorComparator c = new TenorComparator();
		Collections.sort(ret,c );
		return   ret;
	}

	public static double[] generateFxVectorValues(String fxVectorValues) {
		return generateDoubleVector(fxVectorValues,40);
	}
	public static double[] generateDoubleVector(String hierarchyStr, int maxLen){
		double [] result=new double[maxLen];
		
		if (hierarchyStr!=null && hierarchyStr.length()>1 && hierarchyStr.indexOf("|")>-1 ){
			StringTokenizer stringToken=new StringTokenizer(hierarchyStr, ANZConstants.PIPE_SEPARATOR);
			
			int count=0;
			try{
				while(count<maxLen){
					result[count]= Double.valueOf(stringToken.nextToken());
   				count++;
				}
			}catch(NoSuchElementException ex){
				//LOGGER.log(Level.WARNING, formatMessage(MessagesANZ.ERR_PARSE_HIERARCHY, new Object[]{hierarchyStr,maxLen,count}));
			} 
		}
		return result;
	}
	
	public static Object[] getFactTemplate(int size, String containerName)
	{
		Object [] template= new Object[size];
		template[0]=containerName;
		template[1]=containerName;
		return  template;

	}
	
	public static IDimension findDimensionByName(IActivePivot pivot,final String name)
	{
		return (IDimension)CollectionUtils.find(pivot.getDimensions(),  new Predicate() {			
			@Override
			public boolean evaluate(Object object) {
				IDimension dim = (IDimension) object;
				return dim.getName().equals(name);
			}
		});
	} 	
	
	public static String extractPSRName(Pattern psrPattern,String fileName)
	{
		return fileName.substring(0,5);
		//Matcher m = psrPattern.matcher(fileName);
		//return m.matches() ?  m.group(1) : null;		
	}
	
	public static void updateFileName(File rootFolder , File fileinfo, boolean error, String statusDirectory)
	{
		 
		String strRootFolder = (rootFolder.getParent() == null) ? rootFolder.getPath() : rootFolder.getParent();
		String strFileInfo   = (fileinfo.getParent() == null)   ? fileinfo.getPath():fileinfo.getParent();
		
		String newDir = strFileInfo.substring(strFileInfo.indexOf( strRootFolder)  +  strRootFolder.length() );
		File parentPath = new File( statusDirectory + File.separator +   "status" + File.separator + newDir );
	 	     parentPath.mkdirs();
		
		String fileName=fileinfo.getName();
		File newFile =null;
		String suffix=error?FILE_EXT_FAILURE:FILE_EXT_SUCCESS;
		StringBuilder sb = new StringBuilder();
		sb.append(fileName).append(".").append(System.currentTimeMillis()).append(suffix);//avoid issue if we run twice
		newFile=new File(parentPath,sb.toString());
		try{
			if (!newFile.createNewFile()){
				LOGGER.log(Level.SEVERE,ANZUtils.formatMessage(MessagesANZ.ERR_UNABLE_TOCREATE,newFile.getAbsolutePath()));
			}
		}catch(Exception e){
			LOGGER.log(Level.SEVERE,ANZUtils.formatMessage(MessagesANZ.ERR_UNABLE_TOCREATE,newFile.getAbsolutePath()),e);
		}
	}
	
	 public static Document parse(InputStream fStream) throws ParserConfigurationException, SAXException, IOException  {
		 DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder =  builderFactory.newDocumentBuilder();
         return builder.parse(fStream);
    }
	 
	 public static String[] generateCommodityHierarchy(String commodityGrouping) {
			return generateHierarchy(commodityGrouping,ANZConstants.COMMODITY_DEPTH);
	 }
	 
	 public static void createStatusFile(String filePath,String ext)
		{
			String fileName = new StringBuilder().append(filePath).append(".").append(System.currentTimeMillis()).append(ext).toString();
			File statusFile = new File(fileName);
			
			try {
				if(!statusFile.createNewFile())
				{
					LOGGER.log(Level.SEVERE,ANZUtils.formatMessage(MessagesANZ.ERR_UNABLE_TOCREATE,statusFile.getAbsolutePath()));
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE,ANZUtils.formatMessage(MessagesANZ.ERR_UNABLE_TOCREATE,statusFile.getAbsolutePath()),e);
			}
	}
	 
	 public static String[] generateHierarchy(String hierarchyStr){
			List<String> data=new ArrayList<String>();
			if (hierarchyStr!=null && hierarchyStr.length()>1 && hierarchyStr.indexOf("|")>-1 ){
				StringTokenizer stringToken=new StringTokenizer(hierarchyStr, ANZConstants.PIPE_SEPARATOR);
				while(stringToken.hasMoreTokens()){
					data.add(stringToken.nextToken());
				}
			}
			String [] result=new String[data.size()];
			return data.toArray(result);
		}
	 
		public static String[] generateIssuerHierarchy(String issuerGrouping) {
			return generateHierarchy(issuerGrouping,ANZConstants.ISSUER_DEPTH);
		}
	 
		public static String[] generateLegalEntityHierarchy(String legalEntity) {
			return generateHierarchy(legalEntity,ANZConstants.LEGAL_ENTITY_DEPTH ); 
		}
		
		public static String[] generateDealCurrHierarchy(String dealCurrHierarchy) {
			return generateHierarchy(dealCurrHierarchy,ANZConstants.DEAL_CURRENCY_DEPTH);
		}
		
		public static String[] generateFinanceHierarchy(String financeHierarchy) {
			return generateHierarchy(financeHierarchy,ANZConstants.FINANCE_HIERARCHY_DEPTH);

		}
		
		public static String[] generateRevenueHierarchy(String revenueHierarchy) {
			return generateHierarchy(revenueHierarchy,ANZConstants.REVENUE_HIERARCHY_DEPTH);

		}
		
		
		public static String findSchemaPropertyByDimensionName(String dimensionName){
		   if(schemaConfig==null || xPath == null){
				try {
					schemaConfig = parse(ANZUtils.class.getClass().getResourceAsStream( "/DESC-INF/MarketRiskDimensions.xml") );
					xPath =  XPathFactory.newInstance().newXPath();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		   }
			
			
			String expression =  "/dimensions/dimension/level[@name='"+dimensionName+"']";
	            Node nOde = null;
				try {
					nOde = (Node)xPath.compile(expression).evaluate(schemaConfig,XPathConstants.NODE);
				} catch (XPathExpressionException e) {
					e.printStackTrace();
				}
			return nOde.getAttributes().getNamedItem("property").getNodeValue();
		}
		
		public static String getDrillThroughContainer(IDrillthroughQuery dtQuery){
			SubCubeProperties subProps = (SubCubeProperties)dtQuery.getContextValues().get(0);
	  		Set<List<?>> dimContainer = subProps.getAllGrantedMembers().get("Container");
	  		List<String> container = (List<String>)dimContainer.iterator().next();

	  		if(container!=null && container.size()>=1){
	  			return container.get(1);
	  		}
	  		return null;
		}
		
		public static Set<String> aliasHeaders(Set<String> attributeHeader,Properties properties){
			Set<String> aliasHeaders= new LinkedHashSet<String>();
			for (String header : attributeHeader) {
				aliasHeaders.add( (String) properties.get(header) );
			}
			return aliasHeaders;
		}
		
}
