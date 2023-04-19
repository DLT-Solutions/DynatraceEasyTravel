package com.dynatrace.easytravel.frontend.servlet;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.frontend.beans.SpecialOffersBean;

/**
 * Creates special offers asynchronously for the "Special Offers" site .
 *
 * @author cwat-bfellner
 */
@WebServlet(urlPatterns = { "/CreateSpecialOffers" }, asyncSupported = true)
public class CreateSpecialOffers extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final int ASYNC_CONTEXT_TIMEOUT = EasyTravelConfig.read().asyncContextTimeout;
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		SpecialOffersBean rb = new SpecialOffersBean();
		AsyncContext ac = request.startAsync();
		ac.setTimeout(ASYNC_CONTEXT_TIMEOUT);
		rb.setAsyncContext(ac);
		ac.start(rb);
	}
}
