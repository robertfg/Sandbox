package com.anz.util;

import java.util.Map;

public class LookUps {
	Map<String,Map<String,?>> lookUps;
	private String lookUpId;

	public String getLookUpId() {
		return lookUpId;
	}

	public void setLookUpId(String lookUpId) {
		this.lookUpId = lookUpId;
	}

	public Map<String, Map<String, ?>> getLookUps() {
		return lookUps;
	}

	public void setLookUps(Map<String, Map<String, ?>> lookUps) {
		this.lookUps = lookUps;
	}
}
