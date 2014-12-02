package com.quartetfs.pivot.anz.staticdata.impl;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Set;
import java.util.TreeSet;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.IParser;
import com.quartetfs.fwk.format.impl.DateParser;
import com.quartetfs.fwk.ordering.impl.ReverseOrderComparator;
import com.quartetfs.pivot.anz.staticdata.IStaticData;
import com.quartetfs.pivot.anz.staticdata.IStaticDataParser;
import com.quartetfs.pivot.anz.utils.ANZConstants;

public class DateStaticDataParser implements IStaticDataParser {
    private int vecLength;
    
    private DateParser parser;

    public DateStaticDataParser(int vecLength) {
		super();
		this.vecLength = vecLength;
		 parser  =(DateParser) Registry.getPlugin(IParser.class).valueOf("date["+ANZConstants.DATE_PATTERN +"]");
	}
    
		
	
	@Override
	public IStaticData parse(File file, IDate date,StaticDataType type) throws Exception {

		BufferedReader reader = null;
		IStaticData VaRDates=null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			String[] datesStr =null;  
			if ((line = reader.readLine()) != null) {
				datesStr = line.split(",");
				/*if (datesStr.length < vecLength) {
					throw new IllegalStateException(String.format(
							"less than %s dates provided in fileInfo %s",ANZConstants.VECTOR_LENGTH,
							file));
				}*/
			} else {
				throw new IllegalStateException(String.format(
						"first line is empty for fileInfo %s", file));
			}
			Set<IDate>historyDates=parseStringDates(datesStr);
			VaRDates=new VaRDates(date, type, historyDates);
			

		} catch(Exception e){
			throw e;
		}
		finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (final IOException e) {
				throw e;
			}
		}

		return VaRDates;
	}
	
	private Set<IDate> parseStringDates(String[] datesStr)throws ParseException {
		Set<IDate> result = new TreeSet<IDate>(new ReverseOrderComparator());

		for (String dt : datesStr) {
			result.add(Registry.create(IDate.class, parser.parse(dt).getTime()));
		}
		return result; 
	}
	
	
		
}

	
	
	
