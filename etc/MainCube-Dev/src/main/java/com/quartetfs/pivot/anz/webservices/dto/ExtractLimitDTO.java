package com.quartetfs.pivot.anz.webservices.dto;

import java.util.Collections;
import java.util.List;

public class ExtractLimitDTO {
 
//	private List<String> header   =   Collections.emptyList();
	
	private List< ExtractedLimitDTO > values = Collections.emptyList();
    
	public ExtractLimitDTO(){}
	
    /*public ExtractLimitDTO(List<String> header, List<List<String>> values) {
		this.header = header;
		this.values = values;
	}
    */
	
	/*public List<String> getHeader() {
		return header;
	}
	public void setHeader(List<String> header) {
		this.header = header;
	}*/
	public List<ExtractedLimitDTO> getValues() {
		return values;
	}
	public void setValues(List<ExtractedLimitDTO> values) {
		this.values = values;
	}
  
	 

}
