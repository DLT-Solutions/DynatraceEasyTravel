package com.dynatrace.easytravel.jpa;

import com.dynatrace.easytravel.spring.Plugin;

/**
 * Defines a plugin interface for a plugin that wants to override
 * specific named queries.
 * The names of overridable queries are listed in QueryNames.
 *
 * @author philipp.grasboeck
 */
public interface QueryOverride extends Plugin {

	/**
	 * Return the name of the query this plugin wants to override.
	 *
	 * @return    one of the names defined in QueryNames
	 * @author philipp.grasboeck
	 */
	public String getQueryName();

	/**
	 * Return true if the implementation this plugin provides
	 * is a database native query or call.
	 *
	 * @return
	 * @author philipp.grasboeck
	 */
	public boolean isNative();

	/**
	 * Return the query text.
	 *
	 * @return  the query text - either native or EJBQL / HQL
	 * @author philipp.grasboeck
	 */
	public String getQueryText();
}
