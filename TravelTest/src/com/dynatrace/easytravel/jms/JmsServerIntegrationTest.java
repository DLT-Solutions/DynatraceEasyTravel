package com.dynatrace.easytravel.jms;

import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("Integration test")
public class JmsServerIntegrationTest {

	
	private static final int THREADS = 50;
	private static final long TIME = 30 * 1000;
	private static final long INTERVAL = 11000;
	private JmsConnector connector;
	
	@Test
	public void testOne() {
		connector = new JmsConnector ("localhost", 5446);
		
		//check if connection is working
		List<Integer> hotDealIds = connector.getHotDealIds();
		assertFalse("Cannot get hot deals", hotDealIds.isEmpty());				
	}
	
	@Test
	public void testMultiple() throws InterruptedException {
		connector = new JmsConnector ("localhost", 5446);

		for (int i=0; i<10000; i++) {
			//check if connection is working
			System.out.println(i);
			assertFalse("Cannot get hot deals", connector.getHotDealIds().isEmpty());
			Thread.sleep(INTERVAL);
		}
	}
	
	@Test
	public void test() throws InterruptedException{

		connector = new JmsConnector ("localhost", 5446);
		
		//check if connection is working
		List<Integer> hotDealIds = connector.getHotDealIds();
		assertFalse("Cannot get hot deals", hotDealIds.isEmpty());		

		
		ExecutorService executor = Executors.newFixedThreadPool(THREADS);
		
		long finshTime = System.currentTimeMillis() + TIME;
		int nr = 0;
		while (System.currentTimeMillis() < finshTime) {
			executor.execute(new HotDealRunnable(nr++));
			Thread.sleep (INTERVAL);
		}
		
		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.MINUTES);
	}
	
	class HotDealRunnable implements Runnable {
		
		private final int number;
		
		public HotDealRunnable(int nr) {
			this.number = nr;
		}
		
		@Override
		public void run() {			
			List<Integer> hotDealIds = connector.getHotDealIds();
			String s = (hotDealIds == null ? "null" : Integer.toString(hotDealIds.size()));
			System.out.println(number + " got deals: " + s);
		}
		
	}
}
