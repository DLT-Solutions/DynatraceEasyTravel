package com.dynatrace.easytravel.launcher.war;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.DatabaseContentCreationProcedure;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.util.MySqlVcapService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class JPADatabaseContentCreator implements Filter, com.sun.net.httpserver.HttpHandler, Runnable {
	
	private static final Logger LOGGER = Logger.getLogger(JPADatabaseContentCreator.class.getName());
	
	@SuppressWarnings("unused")
	private boolean isContentAlreadyThere = false;
	
	private com.sun.net.httpserver.HttpServer server = null;
	
	private String status = "busy";

	public static void main(String[] args) {
		MySqlVcapService.parseEnv("easyTravel-Business*");
		File fldResources = new File("resources");
		fldResources.mkdirs();
		ClassLoader cl = JPADatabaseContentCreator.class.getClassLoader();
		try (
			InputStream in = cl.getResourceAsStream("resources/easyTravelConfig.properties");
			FileOutputStream out = new FileOutputStream(new File("easyTravelConfig.properties"));
		) {
			copy(in, out);
		} catch (IOException ioe) {
			LOGGER.log(Level.SEVERE, "Excetpion when copying resources/easyTravelConfig.properties", ioe);
			ioe.printStackTrace(System.err); //NOSONAR
		}
		try (
			InputStream in = cl.getResourceAsStream("resources/easyTravel.properties");
			FileOutputStream out = new FileOutputStream(new File("easyTravel.properties"));
		) {
			copy(in, out);
		} catch (IOException ioe) {
			LOGGER.log(Level.SEVERE, "Excetpion when copying resources/easyTravel.properties", ioe);
			ioe.printStackTrace(System.err); //NOSONAR
		}
		
		new JPADatabaseContentCreator().execute();
	}
	
	public void execute() {
		Runtime.getRuntime().addShutdownHook(new Thread(this));
		try {
			synchronized (this) {
				server = HttpServer.create(new InetSocketAddress(8080), 0);
		        server.createContext("/", this);
		        server.setExecutor(null);
		        server.start();
			}
		} catch (IOException io) {
			LOGGER.log(Level.SEVERE, "Cannot start http server", io);
		}
		
		ProcedureMapping mapping = new DefaultProcedureMapping(
				Constants.Procedures.DATABASE_CONTENT_CREATOR_ID);
		mapping.addSetting(new DefaultProcedureSetting(Constants.Misc.SETTING_PERSISTENCE_MODE, "jpa"));
		
		DatabaseContentCreationProcedure contentCreator = new DatabaseContentCreationProcedure(mapping);
		Logger.getLogger(DatabaseContentCreationProcedure.class.getName()).setFilter(this);
		contentCreator.run();
		synchronized (this) {
			status = "done";
		}
	}
	
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[4096];
		int len = in.read(buffer);
		while (len > 0) {
			out.write(buffer, 0, len);
			len = in.read(buffer);
		}
		out.flush();
	}

	@Override
	public boolean isLoggable(LogRecord record) {
		if (record == null) {
			return false;
		}
		Throwable thrown = record.getThrown();
		if (thrown != null) {
			if (thrown instanceof PersistenceException) {
				Throwable cause = thrown.getCause();
				if (cause != null) {
					if (cause.getClass().getName().equals("org.hibernate.exception.ConstraintViolationException")) {
						isContentAlreadyThere = true;
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
        String response = "This is the response";
		synchronized (this) {
			response = status;
		}
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();		
	}

	@Override
	public void run() {
		synchronized (this) {
			if (this.server != null) {
				this.server.stop(0);
			}
		}
	}

}
