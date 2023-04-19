package com.dynatrace.easytravel.util;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.business.client.VerificationServiceStub;
import com.dynatrace.easytravel.logging.LoggerFactory;


public class ServiceStubFactory {
	private static final Logger log = LoggerFactory.make();

	private static final String STUB_SUFFIX = "Stub";
	private static final String THIRD_PARTY_VERIFICATION_SERVICE_NAME = ServiceStubFactory.getServiceName(VerificationServiceStub.class);

	public static String getServiceName(Class<?> clazz)
	{
		String serviceName = clazz.getSimpleName();
		return serviceName.endsWith(STUB_SUFFIX) ? serviceName.substring(0, serviceName.length() - STUB_SUFFIX.length()) : serviceName;
	}

	// relies on Axis service stub contructor(String) that takes the service URI
	public static <T> T makeStub(Class<T> clazz, String serviceName) throws Exception {
		String path = getWebserviceUri(serviceName);
		if (log.isDebugEnabled()) log.debug("try get service with path: " + path);

		ConfigurationContext configurationContext =
				ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);

		{ // see http://amilachinthaka.blogspot.com/2010/01/improving-axis2-http-transport-client.html
			MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();

			HttpConnectionManagerParams params = new HttpConnectionManagerParams();
			params.setDefaultMaxConnectionsPerHost(20);
			params.setMaxTotalConnections(40);

			params.setConnectionTimeout(60000);
			params.setSoTimeout(60000);

			multiThreadedHttpConnectionManager.setParams(params);

			HttpClient httpClient = new HttpClient(multiThreadedHttpConnectionManager);
			configurationContext.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);

			//stub._getServiceClient().getServiceContext().getConfigurationContext().setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);
		}

		return clazz.getConstructor(ConfigurationContext.class, String.class).newInstance(configurationContext, path);
	}


	private static String getWebserviceUri(String serviceName) {
		if (THIRD_PARTY_VERIFICATION_SERVICE_NAME.equals(serviceName)) {
			return LocalUriProvider.getThirdPartyWebServiceUri(serviceName);
		}
		return LocalUriProvider.getBackendWebServiceUri(serviceName);
	}
}
