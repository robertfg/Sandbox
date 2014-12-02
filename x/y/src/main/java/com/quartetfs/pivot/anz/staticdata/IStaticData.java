package com.quartetfs.pivot.anz.staticdata;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.pivot.anz.staticdata.impl.StaticDataType;

/**
 * Represent Static Data used by AP. 
 * 
 */
public interface IStaticData {
	IDate date();
	StaticDataType type();
}
