package com.quartetfs.pivot.anz.cube.hierarchy.axis.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.quartetfs.biz.pivot.cube.hierarchy.axis.impl.AAnalysisDimension;
import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.ordering.IComparator;
import com.quartetfs.pivot.anz.comparator.impl.NumberPercentSignComaparator;
import com.quartetfs.pivot.anz.service.impl.VectorLabelService;
import com.quartetfs.pivot.anz.utils.ANZConstants;

/**
 * On this analysis dimension we expose the  vector detail, It uses container name to lookup labels for a given container.
 * 
 */
@QuartetExtendedPluginValue(interfaceName = "com.quartetfs.biz.pivot.cube.hierarchy.IDimension", key = VectorLabelAnalysisDimension.PLUGIN_KEY)
public class VectorLabelAnalysisDimension extends AAnalysisDimension {
	private static final long serialVersionUID = 6152069512062337135L;
	public static final String PLUGIN_KEY = "ANZ_VECTOR_ANALYSIS_DIM";  
	private  String containerName;
	private VectorLabelService vectorLabelService;
	  
	
	/**
	 * @param name
	 * @param ordinal
	 * @param properties
	 */ 
	public VectorLabelAnalysisDimension(final String name, final int ordinal,
			final Properties properties, final Set<String> measureGroups) {
		super(name, ordinal, properties, measureGroups);
		containerName=(String)properties.get(ANZConstants.LABEL_CONTAINER);
	}

	@Override
	public int getLevelsCount() {
		return 1;
	}

	@Override
	public String getType() {
		return PLUGIN_KEY;
	}

	@Override
	public boolean getNeedRebuild() {
		return true;
	}
 
	
	@Override
	public boolean getUseInstropection(int levelOrdinal) {
		return false;
	}
	
	@Override
	public Object getDefaultDiscriminator(final int levelOrdinal) {
		return  ANZConstants.DEFAULT_DISCRIMINATOR;
	}
	
	@Override
	public IComparator<Object> getLevelComparator(int levelOrdinal) {
		return new NumberPercentSignComaparator();
	}
	

	@Override
	public Collection<Object[]> buildDiscriminatorPaths() {
		final List<Object[]> paths = new ArrayList<Object[]>();
		List<String> values = null;
		if("IR_GAMMA".equals(containerName)){
			values = vectorLabelService.get(null, "GAMMA_BASIS");
			if(values==null || values.size() == 0){
				values = vectorLabelService.get(null, containerName);
			}
		} else {
			values = vectorLabelService.get(null, containerName);
		}
		for (String label : values) {
			paths.add(new String[]{ label });
		}
		return paths;      
	}

	public void setVectorLabelService(VectorLabelService vectorLabelService) {
		this.vectorLabelService = vectorLabelService;
	}
	
}
