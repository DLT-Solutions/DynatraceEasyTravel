package com.dynatrace.easytravel.launcher.fancy;

/**
 * Declares that a class is able to filter its elements 
 * 
 * @author christoph.neumueller
 */
public interface Filterable {

	/**
	 * Called when a new filter string should be applied.
	 * 
	 * @author christoph.neumueller
	 * @param filterTaskParams the filter parameters
	 */
	void applyFilter(FilterTaskParams filterTaskParams);

	/**
	 * Called when the filter should be cleared.
	 * 
	 * @author christoph.neumueller
	 */
	void clearFilter();
}
