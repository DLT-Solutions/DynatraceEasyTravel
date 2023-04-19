package com.dynatrace.easytravel.integration.spring;

import java.rmi.RemoteException;
import java.util.Date;

import org.junit.Assert;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.frontend.data.DataProvider;
import com.dynatrace.easytravel.frontend.data.DataProviderInterface;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.misc.UserType;
import com.dynatrace.easytravel.spring.SpringUtils;
import com.dynatrace.easytravel.util.ExecuteInMultipleThreads;


public class BookingTestDriver extends ExecuteInMultipleThreads {
	private final static Logger log = LoggerFactory.make();

	public BookingTestDriver(int threadCount, int nr, long reportStart) {
		super(threadCount, nr, reportStart);
	}

	private final DataProviderInterface provider = new DataProvider();

	@Override
	protected boolean runSomeWork() throws Throwable {
		String userName = "hainer";
		String destination = "Mumbasa";
		String creditCard = "1234567890000";
		Date fromDate = null;
		Date toDate = null;
		JourneyDO[] journeys = provider.findJourneys(destination, fromDate, toDate);

		Assert.assertNotNull("Expecting some journeys", journeys);

		for (JourneyDO journey : journeys)
		{
    		boolean ok = provider.checkCreditCard(creditCard);
    		Assert.assertTrue("CreditCard should be ok: " + creditCard, ok);
    		provider.storeBooking(journey.getId(), userName, UserType.WEB, creditCard, null);
		}

		return true;
	}

	@Override
	protected void printExtraInfo() throws RemoteException {
		log.info(provider.getDatabaseStatistics());
	}

	public static void main(String[] args) throws InterruptedException {

		int threadCount = Integer.parseInt(args[0]);

		SpringUtils.initCustomerFrontendContext();

		log.info("Starting...");
		long now = System.currentTimeMillis();
		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threadCount; i++) {
			threads[i] = new Thread(new BookingTestDriver(threadCount, i, now));
			threads[i].start();
		}

		ExecuteInMultipleThreads.waitForThreads(5000, threads);

//		SpringUtils.disposeCustomerFrontendContext();
	}
}
