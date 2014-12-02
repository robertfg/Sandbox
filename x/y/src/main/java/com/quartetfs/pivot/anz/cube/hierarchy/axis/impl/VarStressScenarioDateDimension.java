package com.quartetfs.pivot.anz.cube.hierarchy.axis.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.quartetfs.biz.pivot.cube.hierarchy.axis.impl.AAnalysisDimension;
import com.quartetfs.biz.types.IDate;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.pivot.anz.service.IDateService;
import com.quartetfs.pivot.anz.utils.ANZConstants;

/**
 * Analysis dimension for VaR Scenario dates
 * 
 * @author Quartet Financial Systems
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.cube.hierarchy.IDimension", key = VarStressScenarioDateDimension.PLUGIN_KEY)
public class VarStressScenarioDateDimension extends AAnalysisDimension{

	private IDateService dateService;
	private static final String TYPE = "VARSTRESSDATES";
	
	public VarStressScenarioDateDimension(final String name, final int ordinal,
			final Properties properties, final Set<String> measureGroups) {
		super(name, ordinal, properties, measureGroups);
	}
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -6302474856526120653L;
	public static final String PLUGIN_KEY = "VAR_STRESS_SCENARIO_DATE";
	@Override
	public String getType() {
		return PLUGIN_KEY;
	}
	@Override
	public Object getDefaultDiscriminator(final int levelOrdinal) {
		return  ANZConstants.DEFAULT_DISCRIMINATOR;
	}
	
	@Override
	public boolean getNeedRebuild() {
		return true;
	}
	
	@Override
	public Collection<Object[]> buildDiscriminatorPaths() {
		List<Object[]> paths=new ArrayList<Object[]>();
		Object[] temp=new Object[1];
		for (IDate date:dateService.getHistoryDates( TYPE )){
			Object[] value=temp.clone();
			value[0]=date;
			paths.add(value);
		}
		return paths;
	}
	
	public void setDateService(IDateService dateService) {
		this.dateService = dateService;
	}
	
	@Override
	public int getLevelsCount() {
		return 1;
	}

}
