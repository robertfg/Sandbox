package com.quartetfs.pivot.anz.comparator.impl;

import java.util.List;

import com.quartetfs.fwk.QuartetExtendedPluginValue;
import com.quartetfs.fwk.ordering.impl.CustomComparator;

@QuartetExtendedPluginValue(
        interfaceName="com.quartetfs.fwk.ordering.IComparator",
        key=ScenarioIRComparator.key
)
public class ScenarioIRComparator<T> extends CustomComparator<T>
{
	private static final long serialVersionUID = 1L;
	public static final String key = "ScenarioIR";		
	
	public void setFirstObjects(List<T> firstObjects)
	{
		super.setFirstObjects(firstObjects);
	}
}
