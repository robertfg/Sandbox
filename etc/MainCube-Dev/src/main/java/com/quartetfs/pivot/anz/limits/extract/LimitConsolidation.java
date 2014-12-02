package com.quartetfs.pivot.anz.limits.extract;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.pivot.anz.limits.bo.LimitDetail;
import com.quartetfs.pivot.anz.limits.service.LimitUtil;

public class LimitConsolidation {
	
	public LimitConsolidation(ConcurrentMap<String,Double> limitValuesMap,LimitUtil limitUtil){
		this.limitValuesMap = limitValuesMap; 
		this.limitUtil = limitUtil;
	}
	
	private ConcurrentMap<String,Double> limitValuesMap;
	private LimitUtil limitUtil;	
	
	
	private ConcurrentMap<String,Double> posRoomPosLimitValuesMap =  new ConcurrentHashMap<String, Double>(); 
	private ConcurrentMap<String,Double> negRoomPosLimitValuesMap =  new ConcurrentHashMap<String, Double>(); 
	private ConcurrentMap<String,Double> aggrRoomPosLimitValuesMap =  new ConcurrentHashMap<String, Double>(); 

	private ConcurrentMap<String,Double> bondLimitValuesMap =  new ConcurrentHashMap<String, Double>(); 
	private ConcurrentMap<String,Double> swapLimitValuesMap =  new ConcurrentHashMap<String, Double>(); 
	private ConcurrentMap<String,Double> oisLimitValuesMap =  new ConcurrentHashMap<String, Double>(); 
	private ConcurrentMap<String,Double> govtLimitValuesMap =  new ConcurrentHashMap<String, Double>(); 
	private ConcurrentMap<String,Double> semiGovtLimitValuesMap =  new ConcurrentHashMap<String, Double>(); 
	
	private ConcurrentMap<String,Double> negMinLimitValuesMap =  new ConcurrentHashMap<String, Double>(); 
	private ConcurrentMap<String,Double> posMinLimitValuesMap =  new ConcurrentHashMap<String, Double>(); 
	
	private ConcurrentMap<String,Double> minLimitValuesMap =  new ConcurrentHashMap<String, Double>(); 

	
	
	
	public ConcurrentMap<String,Double> consolidate(List<LimitDetail> limits , Map<String,Map<String,Double>> apLimitExposureExtract){
		
		if(apLimitExposureExtract == null || apLimitExposureExtract.size() == 0){
			performRoomPositionCalculation();
			performBondSwapSpreadRiskCalculation();
			performOISSwapSpreadRiskCalculation();
			performGovtSemiGovtSpreadRiskCalculation();
			performMinPositionCalculation();
			return limitValuesMap;
		}
		
		
		Map<String,LimitDetail> limitDetails =  new LinkedHashMap<String,LimitDetail>();	
		for(LimitDetail limit : limits)		{
			limitDetails.put(limit.getId()+"@@"+limitUtil.computeLocation(limit.getLocationValues().iterator().next()),limit);					
		}
	
	 	
		
		for(final Map.Entry<String,LimitDetail> entry : limitDetails.entrySet()){
			String apLimitExposureExtractKey ="";
		try{	
			String limitId = entry.getValue().getId().split("_")[0].trim();
			double weight = Double.parseDouble(entry.getValue().getWeight());
			
			
			
			ILocation newLoc = limitUtil.rebuildLocation(new Location( entry.getKey().split("@@")[1].trim() ) );
			
		   apLimitExposureExtractKey = limitUtil.generateKeyFromLocation(newLoc, limitUtil.getDefaultMember() );
			Map<String,Double> apLimitExposure = apLimitExposureExtract.get(apLimitExposureExtractKey);
			
			if(apLimitExposure==null)continue;
			
			double exp = 0.00;
		   
			
			if( apLimitExposure.get(entry.getValue().getMeasureName()) != null){
				exp = apLimitExposure.get(entry.getValue().getMeasureName())*weight;	
			} else {
				try{
				System.out.println("********:" + apLimitExposureExtractKey +  ":" + entry.getValue().toString() + "-----------------------------------------");
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			
			if(limitValuesMap.get(limitId).isNaN()){ 
				if(entry.getValue().getCalculationType().trim().contains("Spread")){
					//doNothing;
				}else{
					limitValuesMap.replace(limitId, 0.0);
				}
			} 
			
			if(entry.getValue().getCalculationType().trim().contains("Room Position")){
			
				if(entry.getValue().getCalculationType().trim().equals("Room Position")){
					if(exp<0){					
						Double negativeTotal = negRoomPosLimitValuesMap.get(limitId)==null?(exp*-1):negRoomPosLimitValuesMap.get(limitId) + (exp*-1);							
						negRoomPosLimitValuesMap.put(limitId,negativeTotal);
					}else{		
						Double positiveTotal =  posRoomPosLimitValuesMap.get(limitId)==null?exp:posRoomPosLimitValuesMap.get(limitId) + exp;
						posRoomPosLimitValuesMap.put(limitId,positiveTotal);
					}		
				}else{
					String key = limitId+"."+entry.getValue().getCalculationType().trim().split("\\.")[1];							
					Double expTotal = aggrRoomPosLimitValuesMap.get(key)==null?(exp):aggrRoomPosLimitValuesMap.get(key) + (exp);							
					aggrRoomPosLimitValuesMap.put(key, expTotal);				
				}
				
			}else if(entry.getValue().getCalculationType().trim().contains("Spread")){
				if(entry.getValue().getCalculationType().trim().equals("Spread.BOND")){
					Double expTotal =  bondLimitValuesMap.get(limitId)==null?exp:bondLimitValuesMap.get(limitId) + exp;
					bondLimitValuesMap.put(limitId,expTotal);
				}else if(entry.getValue().getCalculationType().trim().equals("Spread.SWAP")){
					Double expTotal =  swapLimitValuesMap.get(limitId)==null?exp:swapLimitValuesMap.get(limitId) + exp;
					swapLimitValuesMap.put(limitId,expTotal);
				}else if(entry.getValue().getCalculationType().trim().equals("Spread.OIS")){
					Double expTotal =  oisLimitValuesMap.get(limitId)==null?exp:oisLimitValuesMap.get(limitId) + exp;
					oisLimitValuesMap.put(limitId,expTotal);
				}else if(entry.getValue().getCalculationType().trim().equals("Spread.GOVT")){
					Double expTotal =  govtLimitValuesMap.get(limitId)==null?exp:govtLimitValuesMap.get(limitId) + exp;
					govtLimitValuesMap.put(limitId,expTotal);
				}else if(entry.getValue().getCalculationType().trim().equals("Spread.SEMI_GOVT")){
					Double expTotal =  semiGovtLimitValuesMap.get(limitId)==null?exp:semiGovtLimitValuesMap.get(limitId) + exp;
					semiGovtLimitValuesMap.put(limitId,expTotal);
				}
			}else if(entry.getValue().getCalculationType().trim().startsWith("Min")){
				String key = limitId+"."+entry.getValue().getCalculationType().trim().split("\\.")[1];							
				Double expTotal = minLimitValuesMap.get(key)==null?(exp):minLimitValuesMap.get(key) + (exp);							
				minLimitValuesMap.put(key, expTotal);		
			}else {
				Double value = limitValuesMap.get(limitId) + exp;
				limitValuesMap.replace(limitId,value);
			}
		}catch(Exception e){
			System.out.println("********:" + apLimitExposureExtractKey + "***************************"); 
			e.printStackTrace();
			
		}
			
		}
		
		
		performRoomPositionCalculation();
		performBondSwapSpreadRiskCalculation();
		performOISSwapSpreadRiskCalculation();
		performGovtSemiGovtSpreadRiskCalculation();
		performMinPositionCalculation();
	
		return limitValuesMap;
	}
	
	
	
	
	/**
	 * Perform a search by using a longer leg as a key to traverse down.
	 */
	private void performRoomPositionCalculation(){
		
		Iterator<String> iter0 = aggrRoomPosLimitValuesMap.keySet().iterator();
		while (iter0.hasNext()){
			String limitId = iter0.next();
			String key = limitId.split("\\.")[0];	
			Double exp = aggrRoomPosLimitValuesMap.get(limitId)==null?0.0:aggrRoomPosLimitValuesMap.get(limitId);
			if(exp<0){
				Double nExp = negRoomPosLimitValuesMap.get(key)==null?0.0:negRoomPosLimitValuesMap.get(key)+exp*-1;
				negRoomPosLimitValuesMap.put(key, nExp);				
			}else{
				Double pExp = posRoomPosLimitValuesMap.get(key)==null?0.0:posRoomPosLimitValuesMap.get(key)+exp;
				posRoomPosLimitValuesMap.put(key, pExp);	
			}		
		}
		
		if (negRoomPosLimitValuesMap.size() > posRoomPosLimitValuesMap.size()){		
			Iterator<String> iter1 = negRoomPosLimitValuesMap.keySet().iterator();
			while (iter1.hasNext()){
				String limitId = iter1.next();
				Double negExp = negRoomPosLimitValuesMap.get(limitId)==null?0.0:negRoomPosLimitValuesMap.get(limitId);
				Double posExp = posRoomPosLimitValuesMap.get(limitId)==null?0.0:posRoomPosLimitValuesMap.get(limitId);
				limitValuesMap.put(limitId, Math.max(negExp, posExp));
			}
		}else{
			Iterator<String> iter2 = posRoomPosLimitValuesMap.keySet().iterator();
			while (iter2.hasNext()){
				String limitId = iter2.next();
				Double posExp = posRoomPosLimitValuesMap.get(limitId)==null?0.0:posRoomPosLimitValuesMap.get(limitId);
				Double negExp = negRoomPosLimitValuesMap.get(limitId)==null?0.0:negRoomPosLimitValuesMap.get(limitId);;
				limitValuesMap.put(limitId, Math.max(negExp, posExp));
			}
		}
	}
	
	/**
	 * Perform a Bond/Swap spread risk logic to stamp limit exposure.
	 */
	private void performBondSwapSpreadRiskCalculation(){
		
		if (bondLimitValuesMap.size() > swapLimitValuesMap.size()){		
			Iterator<String> iter1 = bondLimitValuesMap.keySet().iterator();
			while (iter1.hasNext()){
				String limitId = iter1.next();
				Double swapExp = swapLimitValuesMap.get(limitId)==null?Double.NaN:swapLimitValuesMap.get(limitId);
				Double bondExp = bondLimitValuesMap.get(limitId)==null?Double.NaN:bondLimitValuesMap.get(limitId);
				if(swapExp.isNaN()||bondExp.isNaN()){
					limitValuesMap.put(limitId, Double.NaN);
				}else if((swapExp*bondExp)>=0 ){
					limitValuesMap.put(limitId, 0.0);
				}else {
					Double finalValue = Math.min(Math.abs(bondExp), Math.abs(swapExp));
					if(bondExp>0){
						limitValuesMap.put(limitId, finalValue*-1);
					}else{
						limitValuesMap.put(limitId, finalValue);
					}
				}
			}
		}else{
			Iterator<String> iter2 = swapLimitValuesMap.keySet().iterator();
			while (iter2.hasNext()){
				String limitId = iter2.next();
				Double bondExp = bondLimitValuesMap.get(limitId)==null?Double.NaN:bondLimitValuesMap.get(limitId);
				Double swapExp = swapLimitValuesMap.get(limitId)==null?Double.NaN:swapLimitValuesMap.get(limitId);;
				if(swapExp.isNaN()||bondExp.isNaN()){
					limitValuesMap.put(limitId, Double.NaN);
				}else if((swapExp*bondExp)>=0 ){
					limitValuesMap.put(limitId, 0.0);
				}else {
					Double finalValue = Math.min(Math.abs(bondExp), Math.abs(swapExp));
					if(bondExp>0){
						limitValuesMap.put(limitId, finalValue*-1);
					}else{
						limitValuesMap.put(limitId, finalValue);
					}
				}
			}
		}
	}
	
	/**
	 * Perform a Bond/Swap spread risk logic to stamp limit exposure.
	 */
	
	private void performOISSwapSpreadRiskCalculation(){
		
		if (oisLimitValuesMap.size() > swapLimitValuesMap.size()){		
			Iterator<String> iter1 = oisLimitValuesMap.keySet().iterator();
			while (iter1.hasNext()){
				String limitId = iter1.next();
				Double swapExp = swapLimitValuesMap.get(limitId)==null?Double.NaN:swapLimitValuesMap.get(limitId);
				Double oisExp = oisLimitValuesMap.get(limitId)==null?Double.NaN:oisLimitValuesMap.get(limitId);
				if(swapExp.isNaN()||oisExp.isNaN()){
					limitValuesMap.put(limitId, Double.NaN);
				}else if((swapExp*oisExp)>=0 ){
					limitValuesMap.put(limitId, 0.0);
				}else {
					Double finalValue = Math.min(Math.abs(oisExp), Math.abs(swapExp));
					if(oisExp>0){
						limitValuesMap.put(limitId, finalValue*-1);
					}else{
						limitValuesMap.put(limitId, finalValue);
					}
				}
			}
		}else{
			Iterator<String> iter2 = swapLimitValuesMap.keySet().iterator();
			while (iter2.hasNext()){
				String limitId = iter2.next();
				Double oisExp = oisLimitValuesMap.get(limitId)==null?Double.NaN:oisLimitValuesMap.get(limitId);
				Double swapExp = swapLimitValuesMap.get(limitId)==null?Double.NaN:swapLimitValuesMap.get(limitId);;
				if(swapExp.isNaN()||oisExp.isNaN()){
					limitValuesMap.put(limitId, Double.NaN);
				}else if((swapExp*oisExp)>=0 ){
					limitValuesMap.put(limitId, 0.0);
				}else {
					Double finalValue = Math.min(Math.abs(oisExp), Math.abs(swapExp));
					if(oisExp>0){
						limitValuesMap.put(limitId, finalValue*-1);
					}else{
						limitValuesMap.put(limitId, finalValue);
					}
				}
			}
		}
	}
	
	
	/**
	 * Perform a Bond/Swap spread risk logic to stamp limit exposure.
	 */
	private void performGovtSemiGovtSpreadRiskCalculation(){
		
		if (govtLimitValuesMap.size() > semiGovtLimitValuesMap.size()){		
			Iterator<String> iter1 = govtLimitValuesMap.keySet().iterator();
			while (iter1.hasNext()){
				String limitId = iter1.next();
				Double semiGovtExp = semiGovtLimitValuesMap.get(limitId)==null?Double.NaN:semiGovtLimitValuesMap.get(limitId);
				Double govtExp = govtLimitValuesMap.get(limitId)==null?Double.NaN:govtLimitValuesMap.get(limitId);
				if(semiGovtExp.isNaN()||govtExp.isNaN()){
					limitValuesMap.put(limitId, Double.NaN);
				}else if((semiGovtExp*govtExp)>=0 ){
					limitValuesMap.put(limitId, 0.0);
				}else {
					Double finalValue = Math.min(Math.abs(govtExp), Math.abs(semiGovtExp));
					if(govtExp>0){
						limitValuesMap.put(limitId, finalValue*-1);
					}else{
						limitValuesMap.put(limitId, finalValue);
					}
				}
			}
		}else{
			Iterator<String> iter2 = semiGovtLimitValuesMap.keySet().iterator();
			while (iter2.hasNext()){
				String limitId = iter2.next();
				Double govtExp = govtLimitValuesMap.get(limitId)==null?Double.NaN:govtLimitValuesMap.get(limitId);
				Double semiGovtExp = semiGovtLimitValuesMap.get(limitId)==null?Double.NaN:semiGovtLimitValuesMap.get(limitId);
				if(govtExp.isNaN()||semiGovtExp.isNaN()){
					limitValuesMap.put(limitId, Double.NaN);
				}else if((semiGovtExp*govtExp)>=0 ){
					limitValuesMap.put(limitId, 0.0);
				}else {
					Double finalValue = Math.min(Math.abs(govtExp), Math.abs(semiGovtExp));
					if(govtExp>0){
						limitValuesMap.put(limitId, finalValue*-1);
					}else{
						limitValuesMap.put(limitId, finalValue);
					}
				}
			}
		}
	}
	
	
	/**
	 * Perform a Min position calculation to stamp limit exposure.
	 */
	private void performMinPositionCalculation(){	
	
		Iterator<String> iter0 = minLimitValuesMap.keySet().iterator();
		while (iter0.hasNext()){
			String limitId = iter0.next();
			String key = limitId.split("\\.")[0];	
			String signBpKey = limitId.split("\\.")[1];
			Double exp = minLimitValuesMap.get(limitId)==null?Double.POSITIVE_INFINITY:minLimitValuesMap.get(limitId);
			if(signBpKey.contains("+")){
				posMinLimitValuesMap.put(key, exp);	
			}else if (signBpKey.contains("-")){
				negMinLimitValuesMap.put(key, exp);	
			}
		}	
		
		if (negMinLimitValuesMap.size() > posMinLimitValuesMap.size()){		
			Iterator<String> iter1 = negMinLimitValuesMap.keySet().iterator();
			while (iter1.hasNext()){
				String limitId = iter1.next();
				Double negExp = negMinLimitValuesMap.get(limitId)==null?Double.POSITIVE_INFINITY:negMinLimitValuesMap.get(limitId);
				Double posExp = posMinLimitValuesMap.get(limitId)==null?Double.POSITIVE_INFINITY:posMinLimitValuesMap.get(limitId);
				if(negExp==Double.POSITIVE_INFINITY && posExp==Double.POSITIVE_INFINITY){
					return;
				}
				limitValuesMap.put(limitId, Math.min(negExp, posExp));
			}
		}else{
			Iterator<String> iter2 = posMinLimitValuesMap.keySet().iterator();
			while (iter2.hasNext()){
				String limitId = iter2.next();
				Double posExp = posMinLimitValuesMap.get(limitId)==null?Double.POSITIVE_INFINITY:posMinLimitValuesMap.get(limitId);
				Double negExp = negMinLimitValuesMap.get(limitId)==null?Double.POSITIVE_INFINITY:negMinLimitValuesMap.get(limitId);;
				if(negExp==Double.POSITIVE_INFINITY && posExp==Double.POSITIVE_INFINITY){
					return;
				}
				limitValuesMap.put(limitId, Math.min(negExp, posExp));
			}
		}
	
	}
	

}
