package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

/**
 * Tests {@link SimpleIterableSet}
 * @author Rafal Psciuk
 *
 */
public class SimpleIterableSetTest {

	/**
	 * Data to test key: object, value: number of occurrences of given object
	 */
	Map<String,Integer> testData = Maps.newLinkedHashMap();
	
	SimpleIterableSet<String> set;
	
	@Before
	public void setup() {
		testData.put("a", 3);
		testData.put("b", 5);
		testData.put("c", 2);
		
		//initialize set
		set = new SimpleIterableSet<String>();
		for(String key : testData.keySet()) {	
			Integer cnt = testData.get(key);
			set.add(key, cnt);						
		}
	}		
		
	/**
	 * Test getting elements from the set. Check if every element is returned correct number of times 
	 * 2 iterations: 
	 *  first: get all elemnts one time
	 *  second: get all elements four times. 
	 */
	@Test
	public void testSimpleIterableSet() {										
		testSet(set, testData, 1);
		testSet(set, testData, 4);		
	} 

	/**
	 * Update quantities in the set;
	 */
	@Test
	public void testUpdateQuantities(){
		//get elements from the set to change its initial state
		for(int i=0; i<5; i++) set.getNext();
		
		//update counters for values and do the test
		Map<String,Integer> testData = Maps.newLinkedHashMap();
		testData.put("a", 1);
		testData.put("b", 2);
		testData.put("c", 3);

		set.updateQuantities(new int[] {1,2,3});
		
		testSet(set,testData,1);
	}

	
	/**
	 * This method tries to get elements from the set and then checsk how many times given element occured. 
	 * @param set - {@link IterableSet} to test. 
	 * @param testData - data to test
	 * @param times - number of iterations  
	 */
	protected void testSet(IterableSet<String> set, Map<String, Integer> testData, int times) {
		int sum = 0;		
		for(Integer cnt : testData.values()) {
			sum += cnt;						
		}
		
		Map<String, Integer> counters = new HashMap<String, Integer>();
		for(int i=0; i<sum*times; i++){
			String s = set.getNext();
			Integer cnt = counters.get(s);
			if(cnt == null){
				counters.put(s, 1);
			} else {
				counters.put(s, cnt.intValue()+1);
			}
		}		

		for(String key : testData.keySet()) {
			Integer cnt = testData.get(key);
			assertEquals("Occurences of element " + key  + " do not match", cnt*times, counters.get(key).intValue());
		}
		
	}	
}
