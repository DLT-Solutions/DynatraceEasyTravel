package com.dynatrace.easytravel.frontend.servlet;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.frontend.beans.AdsBean;
import com.dynatrace.easytravel.util.AdsEnablementCheck;



// import com.dynatrace.easytravel.frontend.beans.RecommendationBean;

/**
 * Simulates the computation of journey recommendations as a use case for
 * asynchronous servlet processing
 *
 */
@WebServlet(urlPatterns = { "/AdsForBlog" }, asyncSupported = true)
public class AdsForBlog extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final boolean RECOMMENDATION_BEAN_ENABLED = EasyTravelConfig
			.read().enableRecommendationBean;
	private static final int ASYNC_CONTEXT_TIMEOUT = EasyTravelConfig.read().asyncContextTimeout;
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		try {
			if (!AdsEnablementCheck.isADSEnabled()) {
				if (RECOMMENDATION_BEAN_ENABLED) {
					AdsBean rb = new AdsBean();
					AsyncContext ac = request.startAsync();
					ac.setTimeout(ASYNC_CONTEXT_TIMEOUT);
					rb.setAsyncContext(ac);
					ac.start(rb);
				}
			}
		} catch (Exception e) {
			throw new IOException("EasyTravel exception caught", e);
		}
	}
}
