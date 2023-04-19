package com.dynatrace.easytravel.persistence.controller;

import org.junit.Test;


public class TransactionlessControllerTest {

	@Test
	public void test() {
		TransactionlessController cont = new TransactionlessController() {

			@Override
			public void dropContents() {

			}

			@Override
			public void close() {
			}
		};

		cont.commitTransaction();
		cont.flush();
		cont.flushAndClear();
		cont.rollbackTransaction();
		cont.startTransaction();
	}
}
