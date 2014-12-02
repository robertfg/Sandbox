/*
 * (C) Quartet FS 2013
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.drillthrough.impl;

import java.util.Properties;

import com.quartetfs.biz.pivot.context.drillthrough.impl.APostProcessedProperty;
import com.quartetfs.fwk.QuartetException;
import com.quartetfs.fwk.QuartetExtendedPluginValue;

@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.context.drillthrough.IPostProcessedProperty", key = DrillthroughColumnPostProcessor.PLUGIN_KEY)
public class DrillthroughColumnPostProcessor extends APostProcessedProperty {

	private static final long serialVersionUID = 1L;

	public static final String PLUGIN_KEY = "DrillthroughColumn";

	protected final String c1;
	protected final String c2;

	public DrillthroughColumnPostProcessor(Properties properties) {
		super(properties);
		c1 = (String) properties.get("c1");
		c2 = (String) properties.get("c2");
	}

	@Override
	public Object getValue(Object target) throws QuartetException {
		Double v1 = (Double) attributeAccessors.get(c1).getValue(target);
		Double v2 = (Double) attributeAccessors.get(c2).getValue(target);

		// Return the sum
		if (v1 == null)
			return v2;
		else if (v2 == null)
			return v1;
		else
			return v1 + v2;
	}

	@Override
	public String getType() {
		return PLUGIN_KEY;
	}

}
