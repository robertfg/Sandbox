package com.quartetfs.pivot.anz.staticdata;


import java.io.File;

import com.quartetfs.biz.types.IDate;
import com.quartetfs.pivot.anz.staticdata.impl.StaticDataType;

public interface IStaticDataParser {
	IStaticData parse(File file, IDate date,StaticDataType type)throws Exception;
}
