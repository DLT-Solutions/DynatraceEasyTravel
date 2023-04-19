package com.dynatrace.easytravel.launcher.misc;

import java.util.logging.Logger;

import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeListener;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.launcher.engine.Batch;
import com.dynatrace.easytravel.launcher.engine.CouchDBProcedure;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.fancy.custom.ProcedureComponent;
import com.dynatrace.easytravel.net.UrlUtils;

/**
 * Listens to whether specific procedure control plugins are ON or OFF.
 *
 * cwpl-wjarosz
 */
public class ProcedureControlPluginListener implements PluginChangeListener {
	private static Logger LOGGER = Logger
			.getLogger(ProcedureControlPluginListener.class.getName());

	private boolean pluginLastTransitionedToON = false;
	private boolean pluginLastTransitionedToOFF = false;

	/**
	 * Get CouchDB procedure from running batch
	 *
	 * @return CouchDB StatefulProcedure
	 */
	protected StatefulProcedure getCouchDBProcedure() {
		
		Batch runningBatch = LaunchEngine.getRunningBatch();
		if(runningBatch != null) {
			for (StatefulProcedure proc : runningBatch.getProcedures()) {
				String procedureId = proc.getMapping().getId();
				if (Constants.Procedures.COUCHDB_ID.equals(procedureId)) {
					return proc;
				}
			}
		}

		return null;
	}

	@Override
	public void pluginsChanged() {

		//
		// At the moment we only operate it for CouchDB
		//
		
		// a plugin has changed state - it can be CrashCouchDB or some other plugin

		StatefulProcedure myCouchDBProcedure = getCouchDBProcedure();

		// If the plugin is enabled
		if (PluginChangeMonitor.isPluginEnabled("CrashCouchDB")) {

			// ProcedureControlPlugin is Enabled, thus CouchDB should STOP.

			//
			// Note re. previous state handling:
			//
			// Below we check for the last transition flag: if it indicates that
			// there was no change in this plugin, then there will be no action
			// for us, as we have already done what was to be done and this
			// callback is simply because some other plugin has changed.
			// Thus, if the procedure is not in the state we need it to be
			// (active or inactive), that means that someone else has turned
			// it on or off, presumably manually, and we do no interfere.
			//
			
			if (!pluginLastTransitionedToON) {
				// this is a new state of the plugin, so perform the action on CouchDB
				
				// Plugin in NEW state, so stopping CouchDB... ");
				if (myCouchDBProcedure != null
						&& myCouchDBProcedure.isRunning()) {
					// we now do not want to stop it, but crash it
					myCouchDBProcedure.setState(State.STOPPING);
					UrlUtils.checkRead(CouchDBProcedure.getCrashURL()).isOK();
				}
				pluginLastTransitionedToON = true;
				pluginLastTransitionedToOFF = false;
				
			} // else
				// Plugin in OLD state, so NO ACTION...
				// (we have already transitioned to ON earlier,
				// so whatever we needed to do with CouchDB, has been done.)

		} else { // plugin is off

			// ProcedureControlPlugin is Disabled, thus CouchDB should be ON.
			if (!pluginLastTransitionedToOFF) {
				// this is a new state of the plugin, so perform the action on CouchDB
				
				// Plugin in NEW state, so starting CouchDB...
				if (myCouchDBProcedure != null
						&& !myCouchDBProcedure.isRunning()) {
					
					// A simple myCouchDBProcedure.run() will not suffice here. We need to also do waitUntilOperating() to show correct status in the launcher.
					if (myCouchDBProcedure.run().isOk()) {
						ProcedureComponent.waitUntilOperating(myCouchDBProcedure);
					} else {
						LOGGER.warning("Error starting CouchDB procedure.");
					}
				}
				pluginLastTransitionedToOFF = true;
				pluginLastTransitionedToON = false;
				
			} // else
				// Plugin in OLD state, so NO ACTION... "
				// (we have already transitioned to OFF earlier,
				// so whatever we needed to do with CouchDB, has been done.)
		}
	}
}
