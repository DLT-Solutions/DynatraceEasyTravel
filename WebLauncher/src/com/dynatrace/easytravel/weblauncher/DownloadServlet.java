package com.dynatrace.easytravel.weblauncher;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;


/**
 * This servlet simply copies the content of a file to the response output stream.
 *
 * Mandatory Parameters:
 * - filename  name of the file to download
 *
 * TODO: Currently, this provides all files accessible to the Servlet for download,
 * with full system pathnames.
 * There should be a download directory where those files are.
 *
 * This is currently used for:
 * - download of easyTravel System Profile from WebLauncher.
 *
 * @author philipp.grasboeck
 */
public class DownloadServlet extends BaseServlet {

	private static final long serialVersionUID = -6617110823539599739L;

	private static final Logger log = LoggerFactory.make();

	@Override
	protected String doService(HttpServletRequest request, HttpServletResponse response) throws ParamException, IOException {
		String fileName = getMandatoryParameter(request, "filename");
		log.info(String.format("Sending file download: %s", fileName));
		sendFileDownload(response, new File(fileName));
		return null;
	}
}
