/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.source.translator.impl;


import static com.quartetfs.pivot.anz.utils.ANZUtils.cleanString;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.messaging.IColumnCalculator;
import com.quartetfs.fwk.messaging.ILineReader;
import com.quartetfs.fwk.messaging.impl.ColumnarTranslator;
import com.quartetfs.fwk.util.IMappedTuple;
import com.quartetfs.fwk.util.IMappedTupleFactory;
import com.quartetfs.fwk.util.impl.MappedTupleFactory;
import com.quartetfs.pivot.anz.datasource.factory.DataSourceServiceFactory;
import com.quartetfs.pivot.anz.datasource.factory.DataSourceServiceFactory.BeanName;
import com.quartetfs.pivot.anz.impl.MessagesANZ;
import com.quartetfs.pivot.anz.model.impl.Deal;
import com.quartetfs.pivot.anz.model.impl.VRParsingEntry;
import com.quartetfs.pivot.anz.service.IPSRService;
import com.quartetfs.pivot.anz.utils.ANZConstants;
import com.quartetfs.pivot.anz.utils.ANZUtils;

/**
 * Process a UVR source record (source gross data set) in order to translate it to an object that will be introspected by ActivePivot. 
 * 
 * @author Quartet Financial Systems
 */
public class VRTranslator extends ColumnarTranslator<Object> {
	private static final Logger LOGGER = Logger.getLogger(MessagesANZ.LOGGER_NAME, MessagesANZ.BUNDLE);

	
	/**
	 * Property index linking the name of the tuple properties and the position
	 * of the property in the tuple
	 */
	private int keyIdIndex;
	private int containerNameIndex;
	private int portfolioIndex;
	private int dealNumberIndex;
	private int baseCCYIndex;
	private int geoHierachyIndex;
	private int portfolioHierarchyIndex;
	private int currPairHierachyIndex;
	private int currHierachyIndex;
	private int equityHierachyIndex;
	private int currGroupHierachyIndex;
	private int psrNameIndex;
	private int commodityGroupHierarchyIndex;
	private int issuerHieIndex;
	private int legalEntityHieIndex;	
	private int dealCurrHierachyIndex;
	private int financeHierachyIndex;
	private int revenueHierachyIndex;
	

	

	@SuppressWarnings("unused")
	private int fxVectorValuesIndex;
 
	
	/**
	 * Property index linking the name of the tuple properties and the position
	 * of the property in the tuple
	 */
	private Map<String, Integer> propertyIndex;
	private int familyIdx;
	private int groupIdx;
	private int typeIdx;
	private int instrumentIdx;
	private int mcurrIdx;
	private IPSRService psrService;
	private IMappedTupleFactory mappedTupleFactory;

    private Map<String,Map<String,Integer>> vectorizationMapping = new  HashMap<String,Map<String,Integer>>();

	@Override
	public Object translate(final ILineReader reader) {
		try {
			
			//System.out.println("VrTranslator:translate"); 
			final Object[] tupleTranslated = super.translateTuple(reader);
			
			final IMappedTuple mappedTuple = mappedTupleFactory.create(tupleTranslated);

			// get the psr and the container name
			final String dealPsrName = reader.read(psrNameIndex);			
			final String containerName = reader.read(containerNameIndex);

			// get the deal number
			 final long dealNumber = reader.parseLong(dealNumberIndex);

			// parse the date
			final Date dateTmp  =(Date) mappedTuple.get(ANZConstants.DATE_COLUMN);
			IDate date  = Registry.create(IDate.class, dateTmp.getTime());
			date.applyTime(0, 0, 0, 0);
			
			//create the deal
			final Deal deal = createDeal(reader, dealNumber, date, mappedTuple);
			deal.setContainerName(containerName);
			deal.setPsrName(dealPsrName);
			LOGGER.info("dealPsrName:" + dealPsrName);
			if(dealPsrName.equals("QDVAR")){
				deal.setPsrName("V1AL0");
				deal.getAttributes().put("psrName", "V1AL0");
			}
			
			LOGGER.info("get dealPsrName:" + deal.getPsrName() + "," + deal.getPsrExtA());
			// return the VRParsingEntry
			final String keyId = reader.read(keyIdIndex);
			return new VRParsingEntry(keyId, deal, containerName);
		}catch(final Exception e){ 
			LOGGER.log(Level.SEVERE, Arrays.toString(reader.readAll()), e);
			psrService.errorWithFileLoad(reader.getCurrentFile().getFileName(),e);
		}
		return null;
	}

	public  Deal createDeal(final ILineReader reader, long dealNumber,IDate date, IMappedTuple mappedTuple) throws Exception {
		//get the baseCCY
		final String baseCCY = ANZUtils.cleanString(reader.read(baseCCYIndex));
		final String family=cleanString(reader.read(familyIdx));
		final String group=cleanString(reader.read(groupIdx));
		final String type=cleanString(reader.read(typeIdx));
		final String instrument=cleanString(reader.read(instrumentIdx));
		final String mcurr=cleanString(reader.read(mcurrIdx));
		// get portfolio and Geo hierarchy
		final String portfolio = reader.read(portfolioIndex);
		
		final String[] hierarchy = ANZUtils.generatePortfolioHierarchy(reader.read(portfolioHierarchyIndex));
		final String[] geoHierarchy=ANZUtils.generateGeoHierarchy(reader.read(geoHierachyIndex));
		
		final String[] currPairHierarchy=ANZUtils.generateCurrPairHierarchy(reader.read(currPairHierachyIndex));
		final String[] currHierarchy=ANZUtils.generateCurrHierarchy(reader.read(currHierachyIndex));
		final String[] equityHierarchy=ANZUtils.generateEQHierarchy(reader.read(equityHierachyIndex));
		final String[] currGrouping=ANZUtils.generateCurrGroupHierarchy(reader.read(currGroupHierachyIndex));
		final String[] commodityHierarchy=ANZUtils.generateCommodityHierarchy(reader.read(commodityGroupHierarchyIndex)  );
		final String[] issuerHierarchy=ANZUtils.generateIssuerHierarchy(reader.read(issuerHieIndex));  
		final String[] legalEntityHierarchy = ANZUtils.generateLegalEntityHierarchy( reader.read(legalEntityHieIndex) );
		final String[] dealCurrHierarchy=ANZUtils.generateDealCurrHierarchy(reader.read(dealCurrHierachyIndex));
		   
		final String[] financeHierarchy=ANZUtils.generateFinanceHierarchy(reader.read(financeHierachyIndex));
		final String[] revenueHierarchy=ANZUtils.generateRevenueHierarchy(reader.read(revenueHierachyIndex));
		
		
		
		Deal deal = new Deal(date, dealNumber,portfolio, baseCCY, hierarchy, geoHierarchy,mappedTuple);
		deal.setFamily(family);
		deal.setGroup(group);
		deal.setType(type);
		deal.setInstrument(instrument);
		deal.setMcurr(mcurr);
		deal.setCurrPairHierarchy(currPairHierarchy);
		deal.setCurrencyHierarchy(currHierarchy);
		deal.setEquityHierarchy(equityHierarchy);
		deal.setCurrencyGrouping(currGrouping);
		deal.setCommodityHierarchy(commodityHierarchy);
		deal.setIssuerHierarchy(issuerHierarchy);
		deal.setLegalEntityHierarchy(legalEntityHierarchy);
		deal.setDealCurrencyHierarchy(dealCurrHierarchy);
		
		deal.setFinanceHierarchy(financeHierarchy);
		deal.setRevenueHierarchy(revenueHierarchy);
		
		
		
		String psrExt = reader.getCurrentFile().getFileName().substring(0, reader.getCurrentFile().getFileName().indexOf("."));
		    
		LOGGER.info("psrExt:" + psrExt);
			if(psrExt.toUpperCase().startsWith("V1") || psrExt.toUpperCase().startsWith("QD")){
				deal.setPsrExtA("1D");
				deal.setPsrExtB("");
				deal.setPsrExtH("");
				LOGGER.info("set to 1D");
			} else if(psrExt.toUpperCase().startsWith("VX")) {
				deal.setPsrExtB("10D");
				deal.setPsrExtA("");
				deal.setPsrExtH("");
			} else if(psrExt.toUpperCase().startsWith("B1")) {
				deal.setPsrExtH("B1");
				deal.setPsrExtB("");
				deal.setPsrExtA("");
			} else{
				deal.setPsrExtA(psrExt);
				deal.setPsrExtB("");
				deal.setPsrExtH("");
			}		
			
		if ( deal.getAttributes().get("term")!=null)
		{
			//deal.getAttributes().put("term", deal.getAttributes().get("term").toString().toUpperCase());
			//put all term in lower case
			deal.getAttributes().put("term", deal.getAttributes().get("term").toString().toLowerCase());
			
		}
		
		if ( deal.getAttributes().get("scenarioTermUnderlying")!=null)
		{
			//deal.getAttributes().put("scenarioTermUnderlying", deal.getAttributes().get("scenarioTermUnderlying").toString().toUpperCase());
			deal.getAttributes().put("scenarioTermUnderlying", deal.getAttributes().get("scenarioTermUnderlying").toString().toLowerCase());
			
		}
		
		
		return deal;
	}

	@Override
	public Object[] translateTuple(ILineReader reader){
		return super.translateTuple(reader);
	}
	
	/**
	 * Override the default setColumnCalculators function to initialize the
	 * mappedTupleFactory attribute used to extract the data from the tuple.<br/>
	 * 
	 * @param calculators
	 */
	@Override
	public void setColumnCalculators(List<IColumnCalculator> calculators) {
		super.setColumnCalculators(calculators);
		String[] keysList = new String[columnIndex.size()];
		for (final Map.Entry<String, Integer> entry : columnIndex.entrySet()){
			keysList[entry.getValue().intValue()] = entry.getKey();
		}
		mappedTupleFactory =new MappedTupleFactory(keysList);
	}

	public void setKeyIdIndex(final int keyIdIndex) {
		this.keyIdIndex = keyIdIndex;
	}

	public void setContainerNameIndex(final int containerNameIndex) {
		this.containerNameIndex = containerNameIndex;
	}

	public void setPortfolioIndex(final int portfolioIndex) {
		this.portfolioIndex = portfolioIndex;
	}

	public void setDealNumberIndex(final int dealNumberIndex) {
		this.dealNumberIndex = dealNumberIndex;
	}

	
	public void setBaseCCYIndex(int baseCCYIndex) {
		this.baseCCYIndex = baseCCYIndex;
	}
	
	public void setFamilyIdx(int familyIdx) {
		this.familyIdx = familyIdx;
	}
	
	public void setTypeIdx(int typeIdx) {
		this.typeIdx = typeIdx;
	}
	
	public void setGroupIdx(int groupIdx) {
		this.groupIdx = groupIdx;
	}
	public void setInstrumentIdx(int instrumentIdx) {
		this.instrumentIdx = instrumentIdx;
	}
	
	public void setMcurrIdx(int mcurrIdx) {
		this.mcurrIdx = mcurrIdx;
	}
	
	public void setPortfolioHierarchyIndex(int portfolioHierarchyIndex) {
		this.portfolioHierarchyIndex = portfolioHierarchyIndex;
	}
	
	public void setGeoHierachyIndex(int geoHierachyIndex) {
		this.geoHierachyIndex = geoHierachyIndex;
	}
	public void setCurrGroupHierachyIndex(int currGroupHierachyIndex) {
		this.currGroupHierachyIndex = currGroupHierachyIndex;
	}
	
	public void setEquityHierachyIndex(int equityHierachyIndex) {
		this.equityHierachyIndex = equityHierachyIndex;
	}
	
	public void setCurrHierachyIndex(int currHierachyIndex) {
		this.currHierachyIndex = currHierachyIndex;
	}
	public void setCurrPairHierachyIndex(int currPairHierachyIndex) {
		this.currPairHierachyIndex = currPairHierachyIndex;
	}
	public void setPsrService(IPSRService psrService) {
		this.psrService = psrService;
	}
	

	public int getPsrNameIndex() {
		return psrNameIndex;
	}

	public void setPsrNameIndex(int psrNameIndex) {
		this.psrNameIndex = psrNameIndex;
	}


	public int getCommodityGroupHierarchyIndex() {
		return commodityGroupHierarchyIndex;
	}

	public void setCommodityGroupHierarchyIndex(int commodityGroupHierarchyIndex) {
		this.commodityGroupHierarchyIndex = commodityGroupHierarchyIndex;
	}

	public void setVectorizationMapping(Map<String,Map<String,Integer>> vectorizationMapping) {
		this.vectorizationMapping = vectorizationMapping;
	}

	public Map<String,Map<String,Integer>> getVectorizationMapping() {
		return vectorizationMapping;
	}
	
	public void setDataSourcefactory(DataSourceServiceFactory dataSourcefactory) 
	{		
		this.psrService = dataSourcefactory.getPSRService(BeanName.psrService.name());		
	}

	public void setIssuerHieIndex(int issuerHieIndex) {
		this.issuerHieIndex = issuerHieIndex;
	}

	public int getIssuerHieIndex() {
		return issuerHieIndex;
	}
	
	public int getLegalEntityHieIndex() {
		return legalEntityHieIndex;
	}

	public void setLegalEntityHieIndex(int legalEntityHieIndex) {
		this.legalEntityHieIndex = legalEntityHieIndex;
	}

	public void setDealCurrHierachyIndex(int dealCurrHierachyIndex) {
		this.dealCurrHierachyIndex = dealCurrHierachyIndex;
	}

	public int getDealCurrHierachyIndex() {
		return dealCurrHierachyIndex;
	}

	public int getFinanceHierachyIndex() {
		return financeHierachyIndex;
	}

	public void setFinanceHierachyIndex(int financeHierachyIndex) {
		this.financeHierachyIndex = financeHierachyIndex;
	}
	
	public int getRevenueHierachyIndex() {
		return revenueHierachyIndex;
	}

	public void setRevenueHierachyIndex(int revenueHierachyIndex) {
		this.revenueHierachyIndex = revenueHierachyIndex;
	}
}
