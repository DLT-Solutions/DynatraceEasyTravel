package com.dynatrace.easytravel.launcher.procedures;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.AbstractProcedure;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopListener;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.persistence.DatabaseFactory;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.persistence.Database;


/**
 * Create the database content in a <em>blocking</em> way.
 *
 * @author martin.wurzinger
 */
public class DatabaseContentCreationProcedure extends AbstractProcedure {

    private static final Logger LOGGER = Logger.getLogger(DatabaseContentCreationProcedure.class.getName());
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public DatabaseContentCreationProcedure(ProcedureMapping mapping) {
        super(mapping);
    }

    @Override
    public Feedback run() {
        isRunning.set(true);

        try {
			String persistenceMode = getMapping().getSettingValue(Constants.Misc.SETTING_PERSISTENCE_MODE);
			Database database = DatabaseFactory.createDatabase(persistenceMode);
			EasyTravelConfig cfg = EasyTravelConfig.read();

			boolean randomContent = true;
	        // disable random content if disabled by setting
	        String value = getMapping().getSettingValue("randomContent");
	        if(Boolean.FALSE.toString().equalsIgnoreCase(value)) {
	        	LOGGER.info("Not creating random content in database because it is disabled via setting 'randomContent=false'");
	        	randomContent = false;
	        }

	        try {
				database.createContents(cfg, randomContent);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Setting up the database content failed.", e);
				return Feedback.Failure;
			} finally {
				database.closeConnection();
	        }
	    }finally {
            isRunning.set(false);
        }

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
    public boolean isStoppable() {
        return true;
    }

    @Override
    public Feedback stop() {
    	LOGGER.warning("Stopping procedures. Stopping DatabaseContentCreator");
    	// wait until procedure is done before allowing DBMS to be stopped
    	while(isRunning()) {
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, "Interrupted while waiting for procedure", e);
			}
    	}

    	LOGGER.warning("Stopping procedures. DatabaseContentCreator stopped");
    	return Feedback.Success;
    }

    @Override
    public boolean isOperatingCheckSupported() {
        return false;
    }

    @Override
    public boolean isOperating() {
        throw new UnsupportedOperationException("Operating check not supported.");
    }

    @Override
    public boolean isSynchronous() {
        return true;
    }

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
