package com.quartetfs.pivot.anz.service.impl;

import static com.quartetfs.pivot.anz.utils.ANZConstants.RESULTV_COL;
import static com.quartetfs.pivot.anz.utils.ANZConstants.RESULT_COL;
import static com.quartetfs.pivot.anz.utils.ANZConstants.VAR_COL;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.xml.bind.ValidationException;

import javolution.text.TypeFormat;

import com.quartetfs.pivot.anz.model.impl.Deal;
import com.quartetfs.pivot.anz.service.IValueHolderService;
import com.quartetfs.pivot.anz.utils.ANZConstants;

public class ValueHolderService implements IValueHolderService {

	private final Pattern densePattern;
	private PSRDetail psrDetail;
	private static boolean reversePLVector=false;
	private static Map<String,Integer> varVectorLength= new HashMap<String, Integer>();
	
	public ValueHolderService(final String densePattern) {
		this.densePattern = Pattern.compile(densePattern);
	}
	
	public enum ValueType{
		
		VAR(){
			public void clean(Deal deal,String separator) throws ValidationException{
				clearCols(deal,RESULT_COL,RESULTV_COL);
				
				int vecLength = (varVectorLength.get( deal.getAttributes().get(ANZConstants.LABEL_CONTAINER))!=null)?
						varVectorLength.get(deal.getAttributes().get(ANZConstants.LABEL_CONTAINER)): ANZConstants.VECTOR_LENGTH; //if(deal.getAttributes().get("containerName") )
				
				parseVarVector(deal, separator,vecLength, VAR_COL);
			
			}
			
		
		},
		VARTYPE(){
			@Override
			public void clean(Deal deal, String separator) throws Exception {
				clearCols(deal,RESULT_COL);
				parseGenericDouble(deal,RESULTV_COL,VAR_COL);
				clearCols(deal,RESULTV_COL);
			}
			
		},
		NULL(){

			@Override
			public void clean(Deal deal, String separator) throws Exception {
				return;
			}
			
		};
	
		protected void clearCols(Deal deal,String...types){
			for(String type:types){
				deal.getAttributes().put(type, null);
			}
			
		}
		
		protected void parseDouble(Deal deal,String...types){
			for (String type:types){
				parseGenericDouble(deal,type,type);
			}
		}
		
		/**
		 * Generic method read value from source column as String convert it into Double and write back to target column
		 * @param deal
		 * @param source
		 * @param target
		 */
		void parseGenericDouble(Deal deal, String source, String target){
			Double d=(Double) deal.getAttributes().get(source);
			//double d=TypeFormat.parseDouble(strVal);
			deal.getAttributes().put(target, d);
		}
		
		protected void parseVarVector(Deal deal,String separator, int varLength ,String...types)throws ValidationException{
			String varStr=(String)deal.getAttributes().get(VAR_COL);
			if (varStr!=null && varStr.length()>0){
				double result[]=fromStringToVector(varStr,separator,varLength);
				deal.getAttributes().put(VAR_COL,result);
			}
		}
		protected double[] fromStringToVector(String varStr, String separator,int size) {
			StringTokenizer strToken = new StringTokenizer(varStr, separator);
			try{
				return reversePLVector?fillArrayInReverse(strToken, size):fillArrayInOrder(strToken,size);
			}catch (NoSuchElementException e) {
				//throw new IllegalStateException("Expected Length "+ size + " issue with vector--> "+ varStr);
				return null;
			}
		}
		
		
		/**
		 * Fill the array in same order as in String 0th element comes from first token
		 * @param tokenizer
		 * @param size
		 * @return
		 */
		double[] fillArrayInOrder(StringTokenizer tokenizer, int size){
			double[] varVector = new double[size];
			int count = 0;
			while (count < size) {
				String token = tokenizer.nextToken();
				if(token.equals("N/A")){token="0.0";}
				varVector[count++] = TypeFormat.parseDouble(token);
				
			}
			return varVector;
		}
		
		
		/**
		 * fills the array in reverse order as compare to string 0th element comes from last token
		 * @param tokenizer
		 * @param size
		 * @return
		 */
		double[] fillArrayInReverse(StringTokenizer tokenizer, int size) {
			double[] varVector = new double[size];
			int count = size - 1;
			while (count >= 0) {
				String token = tokenizer.nextToken();
				varVector[count--] = TypeFormat.parseDouble(token);
			}
			return varVector;
		}
		
		
		public abstract void clean(Deal deal,String separator) throws Exception;
	}
	
	
	@Override
	public ValueType getValueHolder(String psrName) {
		if (densePattern.matcher(psrName).matches()){
			return ValueType.VAR;
		}else if (psrDetail!=null && psrDetail.retrieveVarPSRs().contains(psrName)){
			return ValueType.VARTYPE;
		}else{
			return ValueType.NULL;
		}
	}
	
	
	public void setVarPSR(PSRDetail varPSR) {
		this.psrDetail = varPSR;
	}
	
	public void setReversePLVector(boolean reversePLVector) {
		ValueHolderService.reversePLVector = reversePLVector;
	}


	public void setVarVectorLength(Map<String,Integer> varVectorLength) {
		this.varVectorLength = varVectorLength;
	}


	public Map<String,Integer> getVarVectorLength() {
		return varVectorLength;
	}
}
