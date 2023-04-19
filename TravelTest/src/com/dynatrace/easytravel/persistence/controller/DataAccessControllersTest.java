package com.dynatrace.easytravel.persistence.controller;

import static org.junit.Assert.assertNull;

import org.junit.Test;


public class DataAccessControllersTest {

	@Test
	public void test() {
		DataAccessControllers cont = new DataAccessControllers(null);
		assertNull(cont.getBusinessController());
	}
}
