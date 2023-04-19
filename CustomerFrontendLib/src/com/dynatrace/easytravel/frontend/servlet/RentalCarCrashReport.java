package com.dynatrace.easytravel.frontend.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Michal.Bakula
 *
 */
@WebServlet(urlPatterns = { "/RentalCarCrashReport" }, asyncSupported = true)
public class RentalCarCrashReport extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			out.println("Crash report saved.");
			response.sendError(HttpServletResponse.SC_OK);
		} finally {
			out.close();
		}
	}

}
