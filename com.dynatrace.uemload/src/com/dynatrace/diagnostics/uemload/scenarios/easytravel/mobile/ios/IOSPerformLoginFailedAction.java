/**
 * @author: cwat-pharukst
 */
package com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.ios;

import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.MobileSession;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.misc.CommonUser;


/**
 *
 * @author cwat-pharukst
 */
public class IOSPerformLoginFailedAction extends IOSPerformLoginAction {

	/**
	 *
	 * @param session
	 * @author cwat-pharukst
	 */
	public IOSPerformLoginFailedAction(MobileSession session) {
		super(session);
	}

	@Override
	protected String extractPassword(CommonUser user) {
		return user.getPassword() + BaseConstants.UNDERSCORE;
	}


}
