package com.dynatrace.easytravel.persistence.controller;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;


public class DelegateBasedDatabaseControllerTest {
	private DatabaseController delegate = createNiceMock(DatabaseController.class);

	@Test
	public void test() {
		replay(delegate);

		DelegateBasedDatabaseController cont = new DelegateBasedDatabaseController(delegate) {
		};

		cont.close();
		cont.commitTransaction();
		cont.dropContents();
		cont.flush();
		cont.flushAndClear();
		assertNotNull(cont.getDelegateController());
		cont.rollbackTransaction();
		cont.startTransaction();

		verify(delegate);
	}

}
