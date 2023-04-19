/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MobileDoLoginFailedAction.java
 * @date: 24.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.android;

import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.MobileSession;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.misc.CommonUser;


/**
 *
 * @author peter.lang
 */
public class AndroidDoLoginFailedAction extends AndroidDoLoginAction {

	/**
	 *
	 * @param session
	 * @author peter.lang
	 */
	public AndroidDoLoginFailedAction(MobileSession session) {
		super(session);
	}

	@Override
	protected String extractPassword(CommonUser user) {
		return user.getPassword() + BaseConstants.UNDERSCORE;
	}


}
