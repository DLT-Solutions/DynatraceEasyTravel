/**
 * @author: cwat-pharukst
 */
package com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.ios;


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
 * @author cwat-pharukst
 */
public class IOSPerformLoginAction extends MobileEasyTravelAction {

	private static final Logger LOGGER = Logger.getLogger(IOSPerformLoginAction.class.getName());


	/**
	 *
	 * @param session
	 * @author cwat-pharukst
	 */
	public IOSPerformLoginAction(MobileSession session) {
		super(session);
	}

	@Override
	protected void runOnDevice(final MobileDevice device, final UEMLoadCallback continuation) throws Exception {
		device.startAction(getSession(), Action.MOBILE_LOGIN_IOS);
		device.startAction(getSession(), Action.MOBILE_LOGIN_IOS_ANIMATE);

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
				device.reportNSError(getSession(), "3rd Party", e.getClass().getName(), e.getMessage(), Arrays.toString(e.getStackTrace()), null);
			}
		}
		device.leaveAction(getSession());
		getSession().setLoginSuccessful(authenticated);
		if (authenticated) {
			device.reportValue(getSession(), Response.LOGIN_SUCCESSFUL, getSession().getUser().getName());
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
	 * @author cwat-pharukst
	 */
	protected String extractPassword(CommonUser user) {
		return user.getPassword();
	}

}
