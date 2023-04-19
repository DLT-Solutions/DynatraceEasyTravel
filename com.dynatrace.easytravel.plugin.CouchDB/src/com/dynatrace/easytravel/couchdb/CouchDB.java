package com.dynatrace.easytravel.couchdb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.lightcouch.CouchDbClient;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractPagePlugin;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;

public class CouchDB extends AbstractPagePlugin {

	private static final Logger log = LoggerFactory.make();
	private static CouchDbClient dbClient = null;

	private synchronized byte[] getImageFromCloudDb(String requestedFile) {

		byte[] imageBytes = null;

		// Connect to the database if not already connected.
		if (dbClient == null) {

			// do not create the db if it does not exist
			dbClient = CouchDBCommons.initCouchDbClient(false);
		}

		if (dbClient == null) {
			log.info("Error accessing CouchDB.");
			return null;
		}

		// ====================================
		// Get the attachment from CouchDB knowing that the name of the document
		// is the same as the name of the attachment.
		// ====================================

		String fileName = FilenameUtils.getName(requestedFile);
		InputStream in = dbClient.find(fileName + "/" + fileName);
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

		if (in == null) {
			log.info("Error finding image in CouchDB.");
			return null;
		}

		try {
			IOUtils.copy(in, bytesOut);
			bytesOut.flush(); // This flush is very necessary, else we will hang
								// forever after displaying the first image.

			imageBytes = bytesOut.toByteArray();
		} catch (IOException e) {
			log.info("Error accessing image in CouchDB.");
			return null;
		} finally {
			try {
				bytesOut.close();
			} catch (IOException e) {
				log.warn("Error closing output stream.");
			} 
			try {
				// As the lightcouch manual says:
				// "Note: Input streams need to be closed properly; to release the connection."
				in.close();
			} catch (IOException f) {
				log.warn("Error closing input stream from CouchDB.");
			}
		}

		return imageBytes;
	}

	@Override
	public Object doExecute(String location, Object... context) {

		if (PluginConstants.FRONTEND_IMAGEDB.equals(location)) {
			String requestedFile = getFileName(context);
			if (requestedFile != null) {
				return getImageFromCloudDb(requestedFile);
			}
		}
		return null;
	}

	private String getFileName(Object[] context) {
		String requestedFile = null;
		if (isString(context)) {
			requestedFile = (String) context[0];
		}
		return requestedFile;
	}

	private boolean isString(Object[] context) {
		return context != null && context.length > 0
				&& context[0] instanceof String;
	}

}
