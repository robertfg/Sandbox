package com.quartetfs.pivot.anz.limits.excess;

public interface LimitExcessService 
{
	public String NORMAL="NormalLimitExcessExtract";
	//public String COMBINE="CombineLimitExcessExtract";
	
	public LimitExcessExtract getService (String serviceName);
}
