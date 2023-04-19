package com.dynatrace.diagnostics.uemload;

import java.util.List;

import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.collect.Lists;

/**
 * @author Rafal Psciuk
 * 
 * {@link IterableSet} implementation.
 * Contains list of elements and quantities of each element. 
 * Following calls to the {@link #getNext()} method should return each element as many times as it's quantity
 * 
 * For example:
 * add("a",2), add("b",1), add("c",3)
 * 
 * Will result in:
 * getNext() -> "a"
 * getNext() -> "a"
 * getNext() -> "b"
 * getNext() -> "c"
 * getNext() -> "c"
 * getNext() -> "c"
 * 
 * After all elements are returned, everything starts from the beginning  
 *  
 * @param <T> type of items stored in the set
 */
public class SimpleIterableSet<T> implements IterableSet<T> {
	//list containing all items
	private List<T> items = Lists.newArrayList();
	//list contains initial quantities of each item
	private List<Integer> quantities = Lists.newArrayList();;
	//current number of items in each slot
	private List<Integer> counters = Lists.newArrayList();;
	
	/**
	 * Add new element with given quantity to the set. This method doesn't check if there is such element in the set, just creates a new slot
	 * @param element
	 * @param quantity
	 */
	public synchronized  void add(T element, int quantity){
		items.add(element);
		quantities.add(quantity);
		counters.add(quantity);
	}
	
	/* (non-Javadoc)
	 * @see com.dynatrace.diagnostics.uemload.IterableSet#getNext()
	 * return next element
	 */
	@Override
	public synchronized T getNext() {
		int position = getNextAvailablePosition();
				
		if(position >= 0){
			return getElement(position);
		}
		
		//no available elements found, reset
		reset();
		
		position = getNextAvailablePosition();
		if(position <0) {
			return null;
		} else {
			return getElement(position);
		}
	}
	
	/**
	 * @return first available slot that is not empty; -1 is returned if there is no such slot
	 */
	protected int getNextAvailablePosition() {
		for(int i=0; i<getSize(); i++){
			if(getCounter(i) >0) {
				return i;
			}
		}
		return -1;
	}
 

	/**
	 * @return number of slots in the Set
	 */
	protected synchronized int getSize() {
		return items.size();
	}
	
	/**
	 * @param position - slot number
	 * @return number of available elements in given slot
	 */
	protected synchronized int getCounter(int position){
		return counters.get(position);
	}
			
	/**
	 * The method returns element for given slot. If counter for given slot is 0 null is returned
	 * @param position - slot
	 * @return element at given slot. null is returned if counter for given slot is 0. 
	 */
	private T getElement(int position) {
		int cnt = counters.get(position);
		if(cnt <= 0){ 		//no elements available in given slot
			return null;
		} else { //elements are available in this slot
			//decrease counter first
			cnt--;
			counters.set(position, cnt);
			//return element at given position
			return items.get(position);
		}
	}
	
	/**
	 * Reset counters; Rewrite values from quantities to counters 
	 */
	private synchronized void reset() {
		for(int i=0; i<quantities.size(); i++){
			int quantity = quantities.get(i);
			counters.set(i, quantity);
		}
	}
	
	/**
	 * Set new quantities for elements. All counters are reset to new values 
	 * {@link IllegalArgumentException} is thrown if newQuatities length != quantities
	 * @param newQuantities - new quantities
	 */
	public synchronized void updateQuantities(int[] newQuantities){
		//check if we have quantities for all elements
		if(quantities.size() != newQuantities.length) {
			throw new IllegalArgumentException(TextUtils.merge("Invalid number of quantities given. Should be: {0}, but was {1}", quantities.size(), newQuantities.length));
		}
		
		//update values
		for(int i=0; i<quantities.size(); i++){
			quantities.set(i, newQuantities[i]);
			counters.set(i, newQuantities[i]);
		}
	}

}
