package com.dynatrace.easytravel.frontend.servlet;

import java.util.stream.Collectors;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.dynatrace.easytravel.frontend.data.UserDO;
import com.dynatrace.easytravel.frontend.login.LoginLogic;
import com.dynatrace.easytravel.frontend.login.UserContext;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Logger;

/**
 * 
 * @author Michal.Bakula
 *
 */
@WebServlet(urlPatterns = { "/RentalCarAuthentication" }, asyncSupported = true)
public class RentalCarAuthentication extends HttpServlet {

	private static final Logger log = LoggerFactory.make();
	private static final long serialVersionUID = 1L;
	private static final String SUCCESFUL_AUTHENTICATION = "IoT user authenticated correctly.";
	private static final String FAILED_AUTHENTICATION = "IoT user authentication failed.";
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			String reqBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			
			UserDO user = mapper.readValue(reqBody, UserDO.class);
			UserContext uc = new UserContext();
			LoginLogic.authenticate(user.getName(), user.getPassword(), uc);
			if (uc.isAuthenticated()) {
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().println(String.format("{\"message\": \"%s\"}", SUCCESFUL_AUTHENTICATION));
				log.info(SUCCESFUL_AUTHENTICATION);
			} else {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				response.getWriter().println(String.format("{\"message\": \"%s\"}", FAILED_AUTHENTICATION));
				log.info(FAILED_AUTHENTICATION);
			}
		} catch (Exception e) {
			log.error("Exception occured during user authentication.", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

}
