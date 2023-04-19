/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MobileDoLoginAction.java
 * @date: 20.01.2012
 * @author: peter.lang
 */
package com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.android;


import static com.dynatrace.diagnostics.uemload.utils.UemLoadHttpUtils.createPair;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xmlbeans.XmlException;

import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.callback.ErrorHandlingHttpResonseCallbackAdapter;
import com.dynatrace.diagnostics.uemload.http.exception.PageNotAvailableException;
import com.dynatrace.diagnostics.uemload.mobile.MobileDevice;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.MobileEasyTravelAction;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.MobileSession;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;
import com.dynatrace.easytravel.business.webservice.AuthenticateResponseDocument;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Action;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Argument;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Response;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.Url;
import com.dynatrace.easytravel.misc.CommonUser;


/**
 *
 * @author peter.lang
 */
public class AndroidDoLoginAction extends MobileEasyTravelAction {

	private static final Logger LOGGER = Logger.getLogger(AndroidDoLoginAction.class.getName());


	/**
	 *
	 * @param session
	 * @author peter.lang
	 */
	public AndroidDoLoginAction(MobileSession session) {
		super(session);
	}

	@Override
	protected void runOnDevice(final MobileDevice device, final UEMLoadCallback continuation) throws Exception {
		device.startAction(getSession(), Action.MOBILE_LOGIN_ANDROID);
		device.startAction(getSession(), Action.MOBILE_LOGIN_SOAP);

		CommonUser user = getSession().getUser();

		String url = UemLoadUrlUtils.getUrl(getSession().getHost(), Url.MOBILE_LOGIN,
				createPair(Argument.USER_NAME, user.getName()),
				createPair(Argument.PASSWORD, extractPassword(user)));

		device.performWebRequest(getSession(), url, 0, new ErrorHandlingHttpResonseCallbackAdapter() {

			@Override
			public void readDone(HttpResponse response) throws IOException {

				if (response.getStatusCode() != 200) {
					authenticatRequestFinished(device, false, continuation);
					return;
				}

				String xml = response.getTextResponse();
				boolean authenticated = false;
				try {
					AuthenticateResponseDocument xmlResponseDoc = AuthenticateResponseDocument.Factory.parse(xml);
					authenticated = xmlResponseDoc.getAuthenticateResponse().getReturn();
				} catch (XmlException e) {
					LOGGER.log(Level.WARNING, "Unable to parse authentication response.", e);
				}
				authenticatRequestFinished(device, authenticated, continuation);
			}
			@Override
			public void handleRequestError(PageNotAvailableException exception) throws IOException {
				authenticatRequestFinished(device, false, continuation);
			}

		});

	}

	private void authenticatRequestFinished(MobileDevice device, boolean authenticated, UEMLoadCallback continuation) {
		device.simulateProcessingOnDevice(20, 200);
		if (authenticated) {
			try {
				//3rd party web requests
				device.performWebRequest(getSession(), "http://plusone.google.com", 0, null);
				device.performWebRequest(getSession(), "https://www.facebook.com/dynatrace", 0, null);
			} catch (Exception e) {
				device.reportException(getSession(), "3rd Party", e.getClass().getName(), e.getMessage(), Arrays.toString(e.getStackTrace()));
			}
		}
		device.leaveAction(getSession());
		getSession().setLoginSuccessful(authenticated);
		if (authenticated) {
			device.reportEvent(getSession(), Response.LOGIN_SUCCESSFUL);
		} else {
			device.reportEvent(getSession(), Response.LOGIN_FAILED);
		}
		device.simulateProcessingOnDevice(20, 200);
		device.leaveAllActions(getSession());
		try {
			cont(continuation);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Couldn't finish mobile login action.", e);
		}
	}

	/**
	 *
	 * @param user
	 * @return
	 * @author peter.lang
	 */
	protected String extractPassword(CommonUser user) {
		return user.getPassword();
	}

}
