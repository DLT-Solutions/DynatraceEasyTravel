package com.dynatrace.easytravel.weblauncher.security;

import static com.dynatrace.easytravel.constants.BaseConstants.Security.JSP_LOGIN_PAGE;
import static com.dynatrace.easytravel.constants.BaseConstants.Security.SERVICE_LOGIN_SERVLET;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ServiceLogin for redurecting to login.jsp page
 * @author cwpl-rorzecho
 */
@WebServlet(name = SERVICE_LOGIN_SERVLET, urlPatterns = {"/" + SERVICE_LOGIN_SERVLET})
public class ServiceLogin extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6183554189539797859L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher(JSP_LOGIN_PAGE).forward(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
