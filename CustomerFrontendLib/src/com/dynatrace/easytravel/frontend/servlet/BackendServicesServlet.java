package com.dynatrace.easytravel.frontend.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.frontend.lib.RequestProxy;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.google.common.base.Strings;

import ch.qos.logback.classic.Logger;

/**
 * Servlet for forwarding SOAP and REST service requests to the backend.
 * Primarily used by mobile easyTravel application on iOS or Android devices.

 *
 * @author clemens.fuchs
 *
 */
public class BackendServicesServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.make();

	private static void fine(String msg) {
		if (LOG.isDebugEnabled()) LOG.debug(msg);
		//System.out.println("=====> " + msg);
	}

	private String backendHostPort;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		EasyTravelConfig easyConfig = EasyTravelConfig.read();
		if (!Strings.isNullOrEmpty(easyConfig.webServiceBaseDir)) {
			backendHostPort = easyConfig.webServiceBaseDir.replace("/services/", "");
		} else {
			LOG.warn("webServiceBaseDir not configured. Cannot forward requests to backend.");
		}
		
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		forward(request, response);
	}
	
	private void forward(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (backendHostPort != null) {
			String backendUrl = backendHostPort + request.getServletPath() + request.getPathInfo();
			fine("forwarding request " + request.getRequestURL() + " to " + backendUrl);
			
			RequestProxy.instance().forward(request, response, backendUrl);
		}
	}
}
