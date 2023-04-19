package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

/**
 * Small plugin that causes the login to generally become very slow
 * because we wait 10s for some "data" to arrive.
 *
 * @author dominik.stadler
 */
public class UserLoginUtils extends AbstractGenericPlugin {

    private UserLoginEnhancer enhancer = new UserLoginEnhancer();

	@Override
	public Object doExecute(String location, Object... context) {
		// slow us down if we are trying to log in
		enhancer.waitForData();
		return null;
	}
}
