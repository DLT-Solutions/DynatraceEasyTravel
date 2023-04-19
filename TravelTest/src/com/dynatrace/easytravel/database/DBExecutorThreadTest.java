/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: DBExecutorThreadTest.java
 * @date: 15.09.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.database;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dynatrace.easytravel.model.GenericDataAccess;


/**
 * 
 * @author dominik.stadler
 */
public class DBExecutorThreadTest extends DatabaseBase {

	/**
	 * Test method for {@link com.dynatrace.easytravel.database.DBExecutorThread#runSomeWork()}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRunSomeWork() throws Exception {
		DBExecutorThread thread = null;
		try {
			thread = new DBExecutorThread(5, 1, System.currentTimeMillis(), new GenericDataAccess(
					database.getBusinessController()));

			assertTrue(thread.runSomeWork());

		} finally {
			if (thread != null)
				thread.close();
		}
	}

}
