package com.dynatrace.easytravel.jms;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;


/**
 *
 * @author tomasz.wieremjewicz
 */
public class JmsHandlerTest {

	private static int PORT = 5446;

	@Test
	public void consumerNumberTest() throws Exception {
		JmsHandler handler = new JmsHandler();
		assertNotNull(handler);

		handler.execute(PORT, null);
		assertNotNull(handler.getJmsHotDealProvider());

		int number = handler.getRequestQueueConsumers();

		org.junit.Assert.assertEquals("The number of consumers is wrong. Should be 1, and is: " + number, 1, number);

		handler.close();
	}
}
