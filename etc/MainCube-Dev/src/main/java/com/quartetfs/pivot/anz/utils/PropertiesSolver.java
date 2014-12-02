/*
 * (C) Quartet FS 2010
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.quartetfs.pivot.anz.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This component allows solving property values using other properties. For
 * example, it lets the possibility to define source properties e.g.
 * <code>{ main.property = value }</code> and solve target properties using
 * those source properties e.g.
 * <code>{ target.property = ${main.property} }</code>. The pattern for
 * identifying property redirections is by default
 * <code>${nameOfTheSourceProperty}</code> but can be changed.
 * 
 * @author Quartet Financial Systems
 */
public final class PropertiesSolver {
	// The pattern
	private Pattern pattern = Pattern.compile("(\\$\\{([^\\$\\{\\}]*)\\})");
	// The source properties
	private List<Properties> sourceProperties;

	/**
	 * No-arg constructor
	 */
	public PropertiesSolver() {
		this(null, (Properties[]) null);
	}

	/**
	 * Constructor
	 * 
	 * @param pattern
	 *            The pattern to use to match and extract the property
	 *            redirections - Default is (characters not escaped)
	 *            <code>(${([^${}]*)})</code>
	 */
	public PropertiesSolver(final String pattern) {
		this(pattern, (Properties[]) null);
	}

	/**
	 * Constructor
	 * 
	 * @param pattern
	 *            The pattern to use to match and extract the property
	 *            redirections - Default is (characters not escaped)
	 *            <code>(${([^${}]*)})</code>
	 * @param sourceProperties
	 *            Var-arg array of source properties - in order of precedence
	 */
	public PropertiesSolver(final String pattern,
			final Properties... sourceProperties) {
		setPattern(pattern);
		setSourceProperties(sourceProperties);
	}

	/**
	 * Set the source properties
	 * 
	 * @param sourceProperties
	 *            Var-arg array of source properties - in order of precedence
	 */
	public void setSourceProperties(final Properties... sourceProperties) {
		if (sourceProperties != null && sourceProperties.length > 0) {
			this.sourceProperties = Arrays.asList(sourceProperties);
		}
	}

	/**
	 * Set the pattern
	 * 
	 * @param pattern
	 *            The pattern to use to match and extract the property
	 *            redirections - Default is (characters not escaped)
	 *            <code>(${([^${}]*)})</code>
	 */
	public void setPattern(final String pattern) {
		if (pattern != null) {
			this.pattern = Pattern.compile(pattern);
		}
	}

	/**
	 * Solve redirections of properties
	 * 
	 * @param toSolve
	 *            The properties to solve
	 * @return The solved properties
	 */
	public Properties solveProperties(final Properties toSolve) {
		final Properties props = new Properties(toSolve);
		final StringBuilder buffer = new StringBuilder();
		for (final String propToSolve : toSolve.stringPropertyNames()) {
			String propertyValue = toSolve.getProperty(propToSolve);
			boolean oneUnsolved = false;
			while (!oneUnsolved) {
				final Matcher matcher = pattern.matcher(propertyValue);
				int from = 0;
				while (matcher.find() && matcher.groupCount() > 1) {
					final String indirection = matcher.group(2);
					final String solved = lookup(indirection);
					if (solved == null) {
						oneUnsolved = true;
					} else {
						buffer.append(propertyValue.substring(from,
								matcher.start(1)));
						buffer.append(solved);
						from = matcher.end();
					}
				}
				if (buffer.length() == 0) {
					break;
				}
				buffer.append(propertyValue.substring(from));
				propertyValue = buffer.toString();
				props.setProperty(propToSolve, propertyValue);

				buffer.setLength(0);
			}
		}
		return props;
	}

	/**
	 * Local lookup in the source properties
	 * 
	 * @param property
	 *            The property to lookup
	 * @return The found value (null if not found)
	 */
	protected String lookup(final String property) {
		String ret = null;
		if (sourceProperties != null) {
			for (final Properties props : sourceProperties) {
				final String lresult = props.getProperty(property);
				if (lresult != null) {
					ret = lresult;
				}
			}
		}
		return ret;
	}

	/**
	 * Convenience method for one shot solving of properties
	 * 
	 * @param pattern
	 *            The pattern
	 * @param source
	 *            The source properties
	 * @param toSolve
	 *            The properties to solve
	 * @return The solved properties
	 */
	public static Properties solve(final String pattern,
			final Properties source, final Properties toSolve) {
		final PropertiesSolver solver = new PropertiesSolver();
		solver.setPattern(pattern);
		solver.setSourceProperties(source);
		return solver.solveProperties(toSolve);
	}

	/**
	 * Convenience method for one shot solving of properties
	 * 
	 * @param source
	 *            The source properties
	 * @param toSolve
	 *            The propeties to solve
	 * @return the properties
	 */
	public static Properties solve(final Properties source,
			final Properties toSolve) {
		return solve(null, source, toSolve);
	}

}
