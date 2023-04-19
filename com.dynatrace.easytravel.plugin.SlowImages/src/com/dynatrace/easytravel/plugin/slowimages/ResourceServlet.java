package com.dynatrace.easytravel.plugin.slowimages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.frontend.servlet.BaseServlet;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.GenericPlugin;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;

/**
 * 
 * this servlet includes the plugin SlowImagesPlugin for any requested resources
 * 
 * the requested resource is being retrieved and written onto the response.
 * 
 * @see SlowImagesPlugin
 * @author cwat-shauser
 *
 */
public class ResourceServlet extends BaseServlet {


	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.make();

	private static final GenericPluginList plugins = new GenericPluginList(PluginConstants.FRONTEND_RESOURCES);
	
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String uri = req.getRequestURI();
		
		for(GenericPlugin plugin : plugins){
			plugin.execute(PluginConstants.FRONTEND_RESOURCES, uri, Integer.valueOf(1000));
		}
		

		// writing requested file onto servlet response stream
		ServletContext sc = getServletContext();
		String filename = sc.getRealPath(uri);

		// Get the MIME type of the image
		
		String mimeType = this.getMimeType(sc, filename);
		if (mimeType == null) {
			logger.warn("Could not get MIME type of " + filename);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		} 
		
		// Set content type
		resp.setContentType(mimeType);

		// Set content size
		File file = new File(filename);
		resp.setContentLength((int) file.length());

		// Open the file and output streams
		try (FileInputStream in = new FileInputStream(file); OutputStream out = resp.getOutputStream();) {

			// Copy the contents of the file to the output stream
			byte[] buf = new byte[1024];
			int count = 0;
			while ((count = in.read(buf)) >= 0) {
				out.write(buf, 0, count);
			}
		}
	}
	
	private String getMimeType(ServletContext context, String filename){
		String mimeType = context.getMimeType(filename);
		if(mimeType != null) return mimeType;
		if(filename.toLowerCase().endsWith(".ico")){
			return "image/icon";
		}
		return null;
		
	}
	
	
}
