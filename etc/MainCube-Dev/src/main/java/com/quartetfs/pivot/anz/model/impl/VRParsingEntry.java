/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.model.impl;

import com.quartetfs.pivot.anz.model.IVRParsingEntry;

public class VRParsingEntry implements IVRParsingEntry {
	private final String keyId;
	private final Deal deal;
	private final String containerName;

	public VRParsingEntry(final String keyId, final Deal deal,
			final String containerName) {
		this.keyId = keyId;
		this.deal = deal;
		this.containerName = containerName;
	}

	@Override
	public Deal getDeal() {
		return deal;
	}

	

	@Override
	public String getContainerName() {
		return containerName;
	}

	@Override
	public String getKey() {
		return keyId;
	}

}
