package com.dynatrace.easytravel.frontend;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;

public class ContinuouslyRequestPage {
	private static final Logger log = LoggerFactory.make();
	private static AtomicInteger count = new AtomicInteger();

	/**
	 *
	 * @param args
	 * @author dominik.stadler
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int portStart = 18080;
		int portEnd = 18099;

		if(args.length < 2) {
			System.err.println("Usage: <port-range-start> <port-range-end>\nUsing default values: " + portStart + " - " + portEnd);
		} else {
			portStart = Integer.parseInt(args[0]);
			portEnd = Integer.parseInt(args[1]);
		}
		while(true) {
			for(int i = portStart;i <= portEnd;i++) {
				retrieve(i, "/");
				retrieve(i, "/main.jsf");
				retrieve(i, "/about.jsf");
				retrieve(i, "/forgotPwd.jsf");
				//retrieve(i, "/searchDelay.jsf");
				retrieve(i, "/session.jsp");
			}
		}
	}

	private static void retrieve(int port, String url) throws IOException, InterruptedException {
		log.info("[" + count.incrementAndGet() + "] Retrieving " + url + " from port: " + port);
		UrlUtils.retrieveData("http://" + TestUtil.getCustomerFrontendHost() + ":" + port + url, null);
		Thread.sleep(3000);
	}
}
