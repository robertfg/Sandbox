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
import com.quartetfs.pivot.anz.service.impl.PSRDetail;
import com.quartetfs.pivot.anz.utils.ANZConstants;

/**
 * Analysis dimension for VaR Scenario numbers
 * 
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.cube.hierarchy.IDimension", key = VarSixYearScenarioNumberDimension.PLUGIN_KEY)
public class VarSixYearScenarioNumberDimension extends AAnalysisDimension {
	private static final long serialVersionUID = -4420428241556492120L;

	public static final String PLUGIN_KEY = "VAR_SIX_YEAR_SCENARIO_DIM";



	public VarSixYearScenarioNumberDimension(final String name, final int ordinal,
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
		final Object[] array = new Object[1];
		Long index =1L;
	
		while(index<=ANZConstants.VAR_1540_VECTOR_LENGTH) {
				
				final Object[] tempArray = array.clone();
				tempArray[0] = index++;
				paths.add(tempArray);
		}
		return paths;
	}


	 

}
