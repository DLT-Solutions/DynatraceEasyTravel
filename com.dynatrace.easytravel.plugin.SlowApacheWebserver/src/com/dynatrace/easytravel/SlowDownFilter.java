package com.dynatrace.easytravel;

import java.io.*;
import java.util.concurrent.TimeUnit;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class SlowDownFilter {

	private static Logger LOGGER = LoggerFactory.make();

	private static InputStream apacheInputStream;
	private static OutputStream apacheOutputStream;

	public SlowDownFilter() {
		apacheInputStream = new ApacheInputStream();
		apacheOutputStream = new ApacheOutputStream();
	}

	public static void main(String[] args) throws IOException {

		long waitTime = Integer.valueOf(args[0]);

		serverWaitTime(TimeUnit.SECONDS, waitTime );

		try {
			Transfer.performTransfer(apacheInputStream, apacheOutputStream);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		} finally {
			closeStreams();
		}

	}

	/**
	 * Set server wait time
	 *
	 * @param timeUnit
	 * @param waitTime
	 */
	private static void serverWaitTime(TimeUnit timeUnit, long waitTime) {
		try {
			timeUnit.sleep(waitTime);
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage());
		}
	}

	private static void closeStreams() {
		try {
			apacheInputStream.close();
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}

		try {
			apacheOutputStream.close();
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}

	}

}
