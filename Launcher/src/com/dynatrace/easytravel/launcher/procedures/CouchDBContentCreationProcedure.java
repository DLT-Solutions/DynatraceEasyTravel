package com.dynatrace.easytravel.launcher.procedures;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.lightcouch.CouchDbClient;

import com.dynatrace.easytravel.couchdb.CouchDBCommons;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.AbstractProcedure;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopListener;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;

/**
 * Create the database content for CouchDB
 *
 * @author wojtek.jarosz
 */
public class CouchDBContentCreationProcedure extends AbstractProcedure {
	
	private static final Logger LOGGER = Logger
			.getLogger(CouchDBContentCreationProcedure.class.getName());
	private final AtomicBoolean isRunning = new AtomicBoolean(false);

	public CouchDBContentCreationProcedure(ProcedureMapping mapping) {
		super(mapping);
	}

	@Override
	public Feedback run() {
		isRunning.set(true);
		Feedback myFeedback = createDBContent();
		isRunning.set(false);
		return myFeedback;
	}


	//
	// public static for unit test purposes
	// - else it could have all been a part of run()
	//
	public static Feedback createDBContent() {
		
		CouchDbClient dbClient = null;
		InputStream ISFromDB = null;
		InputStream ISFromResource = null;
		try {
			
			LOGGER.log(Level.FINE, "Creating CouchDB content...");

			// ===========================================
			// See if there is already an instance of the database.
			// ===========================================

			dbClient = CouchDBCommons.initCouchDbClient(false);
			if (dbClient != null) {
				LOGGER.log(Level.INFO, "CouchDB base already exists - exiting content creator.");

				// It is tidy and perhaps important to do shutdown the client
				// here. Depending on how lightcouch actually works inside,
				// if we do not shutdown the client, then the next time we
				// try to connect to the same db we might get the same client,
				// which will think that the database still exists - even if,
				// for example, the database has been removed in the meantime.
				// Regardless, it is best to always close down fully, and then
				// start afresh.
				dbClient.shutdown();
				return Feedback.Neutral;
			}

			// ===========================================
			// There is no database, so create one.
			// ===========================================			
			LOGGER.log(Level.INFO, "Will create a new CouchDB database.");

			dbClient = CouchDBCommons.initCouchDbClient(true);
			if (dbClient == null) {
				LOGGER.log(Level.SEVERE, "Error creating CouchDB client.");
				return Feedback.Failure;
			}

			ArrayList<String> myList = CouchDBCommons.getImageList();
			for (String myFile : myList) {
				
				String fileName = FilenameUtils.getName(myFile);
				LOGGER.log(Level.FINE, "Inserting <" + fileName
						+ "> to CouchDB");

				// ===========================================
				// Set content type - we will need that when putting the
				// attachment in the database.
				// ===========================================

				String myContentType;
				if (fileName.endsWith(".png")) {
					myContentType = "image/png";
				} else if (fileName.endsWith(".jpg")) {
					myContentType = "image/jpg";
				} else {
					LOGGER.log(Level.SEVERE,
							"Unsupported content type for file <" + fileName
									+ ">");
					return Feedback.Failure;
				}

				// ===========================================
				// Get the image from the resources in the jar.
				// ===========================================

				ISFromResource = CouchDBCommons.class.getClassLoader()
						.getResourceAsStream(fileName);
				if (ISFromResource == null) {
					LOGGER.log(Level.SEVERE, "Received null stream from resource, for file <" + fileName + ">");					
					return Feedback.Failure;
				}

				// Saving the bytes in an intermediate array introduces as small
				// overhead, but it has the advantage
				// of being able to validate the image later on. The volume of
				// images is not large, so this is acceptable.
				byte[] bytesFromResource = IOUtils.toByteArray(ISFromResource);
				
				// Important to close() here for this file, as the close() in finally
				// will only close the last used stream.
				ISFromResource.close(); ISFromResource = null;
				if (LOGGER.isLoggable(Level.FINEST)) {
					LOGGER.log(Level.FINEST, "Received <"
							+ bytesFromResource.length
							+ "> bytes from resource.");
				}

				// Create a new stream to pass it to CouchDB
				ByteArrayInputStream bytesIn = new ByteArrayInputStream(
						bytesFromResource);

				// ===========================================
				// Save the doc as attachments to a new document, with the name
				// of the doc matching the name of the attachment
				// ===========================================

				dbClient.saveAttachment(bytesIn, fileName,
					myContentType, fileName, null);

				// Re-get to validate.
					
				// Note that while we could use response.getId() to get the
				// document id, it is probably a better test to go back to
				// first principles and simply use the file name
				// as the document id, because we know that this is what the
				// doc
				// id should be and this is also the way the Image servlet
				// will be getting the attachment.
				//
				// ISFromDB = dbClient.find(response.getId() + "/" +
				// fileName);
			
				ISFromDB = dbClient.find(fileName + "/" + fileName);
					
				if (ISFromDB == null) {
					LOGGER.log(Level.SEVERE, "Failed to obtain file <"
							+ fileName + "> from CouchDB");
					return Feedback.Failure;
				}

				byte[] bytesFromDB = IOUtils.toByteArray(ISFromDB);
				// Important to close here, as the close() in finally
				// will only close for the last used stream.
				ISFromDB.close();
				ISFromDB = null;

				if (!Arrays.equals(bytesFromResource, bytesFromDB)) {

					LOGGER.log(Level.WARNING,
						"Validation failure for file <" + fileName
								+ ">");
					return Feedback.Failure;
				}
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error populating CouchDB.", e);
			return Feedback.Failure;

		} finally {
			if (dbClient != null) {
				dbClient.shutdown();
			}
			if (ISFromDB != null) {
				try {
					ISFromDB.close(); ISFromDB = null;
				} catch (IOException e) {
					LOGGER.log(Level.WARNING, "Error closing input stream from CouchDB.");
				}
			}
			if (ISFromDB != null) {
				try {
					ISFromResource.close(); ISFromResource = null;
				} catch (IOException f) {
					LOGGER.log(Level.WARNING, "Error closing input stream from resource.");
				}
			}
		}
		
		LOGGER.log(Level.FINEST, "Finished populating CouchDB");
		return Feedback.Neutral;
	}

	@Override
	public boolean isRunning() {
		return isRunning.get();
	}

	@Override
	public StopMode getStopMode() {
		return StopMode.PARALLEL;
	}

	@Override
	public Feedback stop() {
		LOGGER.warning("Stopping procedures. Stopping CouchDBContentCreator");
		// wait until procedure is done before allowing DBMS to be stopped
		while (isRunning()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING,
						"Interrupted while waiting for procedure", e);
			}
		}

		LOGGER.warning("Stopping procedures. CouchDBContentCreator stopped");
		return Feedback.Success;
	}

	@Override
	public boolean isOperatingCheckSupported() {
		return false;
	}

	@Override
	public boolean isOperating() {
		throw new UnsupportedOperationException(
				"Operating check not supported.");
	}

	//===========================================================================
	// According to the definition of a Procedure (see the Procedure interface definition),
	// this procedure is synchronous and not stoppable:
	// "if the procedure is synchronous and not stoppable then the procedure will
	// terminate after run() completes" - and this is the case here, as after the database
	// has been populated, the procedure is done.
	//
	// Note also that as a synchronous procedure, before it is executed, it will wait 
	// for the run() method of the previous procedure to complete, even if that
	// procedure is not synchronous.  This is important if e.g. the previous procedure
	// starts the database, as in the content creator we need to communicate with
	// a started database.
	
	@Override
	public boolean isSynchronous() {
		return true;
	}

	@Override
	public boolean isStoppable() {
		return false;
	}
	//===========================================================================

	@Override
	public void addStopListener(StopListener stopListener) {
		// stop notifications not supported
	}

	@Override
	public void removeStopListener(StopListener stopListener) {
		// stop notifications not supported
	}

	@Override
	public void clearStopListeners() {
		// stop notifications not supported
	}

	@Override
	public String getDetails() {
		return "Running: " + isRunning.get();
	}

	@Override
	public String getLogfile() {
		return null;
	}

	@Override
	public boolean hasLogfile() {
		return false;
	}

	@Override
	public Technology getTechnology() {
		return null;
	}

	@Override
	public boolean agentFound() {
		return false;
	}
}
