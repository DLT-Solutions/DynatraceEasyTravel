package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

public class LoginEnhancer extends AbstractGenericPlugin {

    @Override
	public Object doExecute(String location, Object... context) {
		// creates a http 500 - Internal Server error while login + LoginException
		throw new LoginException("LoginException occurred when processing Login transaction");
	}
}
