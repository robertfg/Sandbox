/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.cube.hierarchy.axis.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.quartetfs.biz.pivot.cube.hierarchy.axis.impl.AAnalysisDimension;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.pivot.anz.utils.ANZConstants;

/**
 * Analysis dimension for confidence levels
 * 
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.cube.hierarchy.IDimension", key = VarConfidenceLevelDimension.PLUGIN_KEY)
public class VarConfidenceLevelDimension extends AAnalysisDimension {

	private static final long serialVersionUID = -1414475045552749591L;
	public static final String PLUGIN_KEY = "VAR_CONFIDENCE_LEVEL_DIM";
	
	public VarConfidenceLevelDimension(final String name, final int ordinal,
			final Properties properties, final Set<String> measureGroups) {
		super(name, ordinal, properties, measureGroups);
	}


	@Override
	public String getType() {
		return PLUGIN_KEY;
	}

	@Override
	public boolean getUseInstropection(final int levelOrdinal) {
		return false;
	}

	@Override
	public boolean getNeedRebuild() {
		return false;
	}

	@Override
	public int getLevelsCount() {
		return 1;
	}

	@Override
	public Object getDefaultDiscriminator(final int levelOrdinal) {
		return  ANZConstants.DEFAULT_DISCRIMINATOR;
	}

	@Override
	public Collection<Object[]> buildDiscriminatorPaths() {
		final List<Object[]> paths = new ArrayList<Object[]>();
		for(Double confidenceLevel : ANZConstants.CONFIDENCE_LEVELS){
			paths.add(new Object[]{confidenceLevel.doubleValue()});
		}
		return paths;
	}
}
