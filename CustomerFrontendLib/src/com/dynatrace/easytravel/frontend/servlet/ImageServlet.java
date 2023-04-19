package com.dynatrace.easytravel.frontend.servlet;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.servlets.DefaultServlet;

import com.dynatrace.easytravel.couchdb.CouchDBCommons;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;

public class ImageServlet extends DefaultServlet {

	private static final Logger log = LoggerFactory.make();

	private static final GenericPluginList plugins = new GenericPluginList(
			PluginConstants.FRONTEND_IMAGEDB);
	
	private static List<String> imageList = null;
	

	/**
	 * Servlet for serving images from CouchDB or from local disk, depending on
	 * plugin state. For serving from disk, we use built-in Tomcat functionality
	 * by forwarding to the default servlet.
	 * 
	 */

	private static final long serialVersionUID = 4844900152215549008L;

	@Override
	public void init() throws ServletException {
		// Any initialization goes here...
		
		// Get the list of files we expect to serve from the image DB
		synchronized (ImageServlet.class) {
			if (imageList == null) {
				imageList = CouchDBCommons.getImageList();
			}
		}

		super.init(); // we do need this! - else we get a Null pointer exception
						// in super.doGet();
	}

	private byte[] getImageFromPlugin(String requestedFile) {
		return getResponse(plugins.execute(PluginConstants.FRONTEND_IMAGEDB,
				requestedFile));
	}

	private byte[] getResponse(Iterable<Object> execute) {
		if (execute == null) {
			return null;
		}

		for (Iterator<Object> iterator = execute.iterator(); iterator.hasNext();) {
			Object temp = iterator.next();
			if ((temp) instanceof byte[]) {
				return (byte[]) temp;
			}
		}

		return null;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// Get the requested file path.
		// (I originally expected that we would extract it from getPathInfo(),
		// however, not in this case.)
		String requestedFile = request.getRequestURI();

		// Check if the file is actually supplied.
		if (requestedFile == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
			log.info("Image Servlet: Error: requested a null file>");
			super.doGet(request, response);
			return;
		}
		log.debug("Requested image file is <" + requestedFile + ">");

		if (imageList.contains(requestedFile)) {

			//
			// We attempt to get the image from the image database (plugin).
			// If the plugin is not activated, we will get a null and will
			// handle it locally.
			//

			byte[] imageBytes = getImageFromPlugin(requestedFile);

			if (imageBytes != null) {

				//
				// Have successfully extracted the image from the database, so
				// use it to generate a request
				//

				// Set content type of response
				// For now we support only a few types: only those that we
				// choose to store in Couch DB.
				String myContentType = null;
				if (requestedFile.endsWith(".png")) {
					myContentType = "image/png";
				} else if (requestedFile.endsWith(".jpg")) {
					myContentType = "image/jpg";
				} else {
					log.info("ERROR: unsupported content type <"
							+ myContentType + ">");
					return;
				}
				response.setContentType(myContentType);

				// Note: for a general file (not an image), we might also want
				// to set content disposition, but here probably no need.
				// response.setHeader("Content-Disposition",
				// "attachment; filename=\"" + file.getName() + "\"");

				// Now we know the length and can finish setting the header...
				response.setHeader("Content-Length",
						String.valueOf(imageBytes.length));
				response.setHeader("Server", "CouchDB");

				// and display the image
				response.getOutputStream().write(imageBytes);

				return;
			}
		}

		//
		// The file is NOT in the list or we have failed to get it from the DB
		// meaning that probably the plugin is not or, or there is a problem
		// with the connection to the database. In either case we use the
		// default servlet to get the image from disk.
		//

		log.debug("Will hand over the image processing to the default servlet.");
		super.doGet(request, response);
		return;

	}
}
