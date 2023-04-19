package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

public class HandleNamedPipeCommunication extends AbstractGenericPlugin {

	public class CommunicationException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public CommunicationException(String message) {
			super(message);
		}
    }

	@Override
	public Object doExecute(String location, Object... context) {
		// creates a http 500 - Internal Server error while credit-card check
		throw new CommunicationException("Communication plugin could not contact credit card verification application via named pipe.");
	}
}
