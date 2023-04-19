/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MobileEasyTravelAction.java
 * @date: 20.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile;

import com.dynatrace.diagnostics.uemload.MobileNativeAction;


/**
 *
 * @author peter.lang
 */
abstract public class MobileEasyTravelAction extends MobileNativeAction {

	private MobileSession session;
	/**
	 *
	 * @param session
	 * @author peter.lang
	 */
	public MobileEasyTravelAction(MobileSession session) {
		this.setSession(session);
	}
	/**
	 * @return the session
	 */
	public MobileSession getSession() {
		return session;
	}
	/**
	 * @param session the session to set
	 */
	public void setSession(MobileSession session) {
		this.session = session;
	}

}
