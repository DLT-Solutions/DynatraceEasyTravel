package com.dynatrace.diagnostics.uemload;

/**
 * @author \Rafal Psciuk\
 *
 * Interface used by collection that allows to iterate over its elements. Example implementations:
 * {@link RandomSet}
 * {@link SimpleIterableSet}
 * {@link RandomIterableSet}
 *
 * @param <T> type of items 
 */
public interface IterableSet<T> {
		
	/**
	 * @return next element from the collection. Returned element can be null.  
	 */
	public T getNext();	
}
