/*
 * (C) Quartet FS 2012
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.sandbox.context.impl;

import com.quartetfs.biz.pivot.context.impl.StringContextValueTranslator;
import com.quartetfs.fwk.QuartetPluginValue;
import com.quartetfs.pivot.sandbox.context.IReferenceCurrency;

/**
 * 
 * Context value translator for reference currencies.
 * As a plugin value it is automatically registered
 * with the other available context value translators.
 * 
 * @author Quartet FS
 *
 */
@QuartetPluginValue(interfaceName = "com.quartetfs.biz.pivot.context.IContextValueTranslator")
public class ReferenceCurrencyTranslator extends StringContextValueTranslator<IReferenceCurrency> {

	/** serialVersionUID */
	private static final long serialVersionUID = -1129088664293462391L;

	/** Translator key */
	public static final String KEY = "referenceCurrency";
	
	@Override
	public Class<IReferenceCurrency> getContextInterface() { return IReferenceCurrency.class; }

	@Override
	public String key() { return KEY; }

	@Override
	protected IReferenceCurrency createInstance(String content) { return new ReferenceCurrency(content); }

	@Override
	protected String getContent(IReferenceCurrency instance) { return instance.getCurrency(); }

}
