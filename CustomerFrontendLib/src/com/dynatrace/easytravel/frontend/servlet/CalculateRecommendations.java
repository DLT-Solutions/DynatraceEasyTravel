package com.dynatrace.easytravel.frontend.servlet;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.frontend.beans.RecommendationBean;

/**
 * Simulates the computation of journey recommendations as a use case for asynchronous servlet processing
 * 
 * @author cwat-ceiching
 */
@WebServlet(urlPatterns = { "/CalculateRecommendations" }, asyncSupported = true)
public class CalculateRecommendations extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	private boolean enableRecomendations;
	private int timeout;
	
	@Override
	public void init() throws ServletException {
		super.init();
		EasyTravelConfig config = EasyTravelConfig.read();
		timeout =  config.asyncContextTimeout;
		enableRecomendations = config.enableRecommendationBean;
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (enableRecomendations) {
			RecommendationBean rb = new RecommendationBean();
			AsyncContext ac = request.startAsync();
			ac.setTimeout(timeout);
			rb.setAsyncContext(ac);
			ac.start(rb);
		}
	}
}
