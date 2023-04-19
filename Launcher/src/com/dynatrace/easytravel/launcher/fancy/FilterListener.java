package com.dynatrace.easytravel.launcher.fancy;

/**
 * 
 * @author christoph.neumueller
 */
public interface FilterListener {

	/**
	 * called before a filter is applied
	 **/
	void beforeApplyFilter();

	/**
	 * called after a filter is applied
	 **/
	void afterApplyFilter();
}
