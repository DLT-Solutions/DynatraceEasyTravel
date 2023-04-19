package com.dynatrace.easytravel;

import java.io.IOException;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.ipc.NativeApplication;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractPlugin;

public class DummyNativeApplication extends AbstractPlugin implements NativeApplication {

    private static Logger log = LoggerFactory.make();

	@Override
	public void setChannel(String channel) {
	}

	@Override
	public String sendAndReceive(String creditCard) throws IOException {
		boolean valid = creditCard.matches("\\d{10,}"); // 10 digits
		String result = valid ? VALID : INCORRECT;
		if (log.isDebugEnabled()) log.debug("Validate credit card: " + creditCard + " result: " + result);
		return result;
	}
}
