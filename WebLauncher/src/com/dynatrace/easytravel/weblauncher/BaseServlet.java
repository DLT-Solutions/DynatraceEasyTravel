package com.dynatrace.easytravel.weblauncher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.dynatrace.easytravel.util.WebUtils;

/**
 * Base class for Servlets running along with the WebLauncher.
 *
 * @author philipp.grasboeck
 */
public abstract class BaseServlet extends GenericServlet {

	private static final long serialVersionUID = -4903816564839444442L;

	@Override
	public final void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		String answer;
		try {
			answer = doService((HttpServletRequest) request, (HttpServletResponse) response);
		} catch (ParamException e) {
			answer = e.getMessage();
//			((HttpServletResponse) response).setStatus(HttpServletResponse.SC_NOT_FOUND); // send a 404
		}
		if (answer != null) {
			IOUtils.write(answer, response.getOutputStream());
		}
	}

	protected abstract String doService(HttpServletRequest request, HttpServletResponse response) throws ParamException, IOException;

	protected String[] getMandatoryPathParams(HttpServletRequest request) throws ParamException {
		String path = request.getPathInfo();
		if (path == null || path.equals("/")) {
			throw new ParamException("Path needed");
		}
		return path.substring(1).split("/");
	}

	private static boolean isEmpty(String string) {
		return string == null || string.trim().isEmpty();
	}

	protected String getMandatoryParameter(ServletRequest request, String name) throws ParamException {
		String value = request.getParameter(name);
		if (isEmpty(value)) {
			throw new ParamException("Missing parameter: %s", name);
		}
		return value;
	}

	protected static void sendFileDownload(HttpServletResponse response, File file) throws IOException {
		if (!file.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found: " + file);
			return;
		}
		response.setHeader("Content-Type", WebUtils.getContentType(file.getName()));
		response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());

		try(InputStream in = new FileInputStream(file);) {
			IOUtils.copy(in, response.getOutputStream());
		}
	}

	@SuppressWarnings("serial")
	protected static class ParamException extends Exception {
		protected ParamException(String message, Object... args) {
			super(String.format(message, args));
		}
	}
}
