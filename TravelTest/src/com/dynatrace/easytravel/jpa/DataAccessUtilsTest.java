package com.dynatrace.easytravel.jpa;

import org.junit.Test;

import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;


public class DataAccessUtilsTest {
	 // helper method to get coverage of the unused constructor
	 @Test
	 public void testPrivateConstructor() throws Exception {
	 	PrivateConstructorCoverage.executePrivateConstructor(JpaAccessUtils.class);
	 }

	 // helper method to get coverage of the unused constructor
	 @Test
	 public void testPrivateConstructorQueryNames() throws Exception {
	 	PrivateConstructorCoverage.executePrivateConstructor(QueryNames.class);
	 }
}
