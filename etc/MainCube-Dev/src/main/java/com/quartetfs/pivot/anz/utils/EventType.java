package com.quartetfs.pivot.anz.utils;

public enum EventType {
	Parse{
		@Override
		String prefixSource(String str) {
			return this.name()+separator+str;
		}
	},Commit {
		@Override
		String prefixSource(String str) {
			return this.name()+separator+str;
		}
	}, Delete {
		@Override
		String prefixSource(String str) {
			return this.name()+separator+str;
		}
	};
	static final String separator=":";
	abstract String prefixSource(String str);
}
