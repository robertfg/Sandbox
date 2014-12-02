/*
 * (C) Quartet FS 2011
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.model;

import com.quartetfs.pivot.anz.model.impl.Deal;


/**
 * Unified VR parsing entry 
 * @author Quartet Financial Systems
 */
public interface IVRParsingEntry {

	Deal getDeal();
	
	String getContainerName();
	
	String getKey();
}
