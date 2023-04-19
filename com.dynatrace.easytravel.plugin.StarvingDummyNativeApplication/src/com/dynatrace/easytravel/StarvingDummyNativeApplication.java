package com.dynatrace.easytravel;

import java.io.IOException;

import com.dynatrace.easytravel.ipc.NativeApplication;
import com.dynatrace.easytravel.spring.AbstractPlugin;

/**
 * Simulates a single threaded, slow credit card check.
 */
public class StarvingDummyNativeApplication extends AbstractPlugin implements NativeApplication {

	private static final int CHECK_DELAY = 20000;

	private static final class GlobalLock {
	}

	private final Object lock = new GlobalLock();

	@Override
	public void setChannel(String channel) {
		// Ignore, since dummy
	}

	@Override
	public String sendAndReceive(String creditCard) throws IOException {
		String result;
		synchronized (lock) {
			// Sleeping while holding the lock starves other transactions
			Object waitObj = new Object();
			try {
				synchronized (waitObj) {
					waitObj.wait(CHECK_DELAY); // Object.wait() looks nicer than Thread.sleep()
				}
			} catch (InterruptedException e) {
			}

			boolean valid = creditCard.matches("\\d{10,}"); // 10 digits
			result = valid ? VALID : INCORRECT;
		}
		return result;
	}
}
