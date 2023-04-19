package com.dynatrace.easytravel;

import com.dynatrace.easytravel.model.DataAccess;

public interface MessageHandler {

	void execute(int port, DataAccess dataAccess);

	void close();

	void checkIfRunning();
}
