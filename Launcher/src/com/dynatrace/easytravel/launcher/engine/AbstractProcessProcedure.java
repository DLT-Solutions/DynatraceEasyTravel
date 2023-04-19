package com.dynatrace.easytravel.launcher.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Properties;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.Version;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.process.Process;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.DtVersionDetector;


/**
 * {@link Procedure} class that is operating on top of a separate {@link Process}.
 *
 * @author martin.wurzinger
 */
public abstract class AbstractProcessProcedure extends AbstractProcedure {
	private static final Logger LOGGER = LoggerFactory.make();

    protected final Process process;
    private String detailCache = null;

    public AbstractProcessProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
        super(mapping);
        this.process = createProcess(mapping);

		setEnvironmentVariables(mapping);
    }

    /**
     * Called within the constructor to actually create a process instance for this procedure.
     * Must not return null, may throw CorruptInstallationException.
     */
    protected abstract Process createProcess(ProcedureMapping mapping) throws CorruptInstallationException;

	private void setEnvironmentVariables(ProcedureMapping mapping){
		setTenantEnvVariables(mapping);
		setReleaseOverviewEnvVariables(mapping);
	}

	private void setTenantEnvVariables(ProcedureMapping mapping){
		String apmTenantUUID = mapping.getAPMTenantUUID();
		// pass APM NG Tenant UUID to the process via Environment Variables
		if(apmTenantUUID != null && process != null) {
			process.setEnvironmentVariable("DT_TENANT", apmTenantUUID);
			process.setEnvironmentVariable("RUXIT_TENANT", apmTenantUUID);
		}
	}
	
	private void setReleaseOverviewEnvVariables(ProcedureMapping mapping){
		Version version = Version.read();
		EasyTravelConfig config = EasyTravelConfig.read();
		if(process != null) {
			process.setEnvironmentVariable("DT_RELEASE_VERSION", getReleaseVersion(version) );
			process.setEnvironmentVariable("DT_RELEASE_BUILD_VERSION", getReleaseBuildVersion(version));
			process.setEnvironmentVariable("DT_RELEASE_PRODUCT", mapping.getId());
			process.setEnvironmentVariable("DT_RELEASE_STAGE", config.dtReleaseStage);
		}
	}
	
	private String getReleaseVersion(Version version) {
		return version.getMajor() + "." + version.getMinor() + "." + version.getRevision();
	}
	
	
	private String getReleaseBuildVersion(Version version) {
		return getReleaseVersion(version) + "." + version.getBuildnumber() + " (" + version.getOnlyDateString() + ")";
	}

	/**
	 * Return the DtAgentConfig associated with this procedure.
	 */

    protected abstract DtAgentConfig getAgentConfig();


    @Override
    public Feedback run() {
    	// clear cache to re-compute it if needed again
    	detailCache = null;
    	
        return process.start();
    }

    @Override
    public StopMode getStopMode() {
    	return StopMode.SEQUENTIAL;
    }

    @Override
    public boolean isStoppable() { // NOPMD
    	return getStopMode().isStoppable();
    }

    @Override
    public Feedback stop() {
    	LOGGER.debug("Stopping procedures. stop() called for process: " + process.getDetails());
    	
    	// clear cache to re-compute it if needed again
    	detailCache = null;

        return process.stop();
    }

    @Override
    public boolean isRunning() {
        return process.isRunning();
    }

	// Method used in constructors of subclasses. Should not be overridable.
	protected final void addApplicationArgument(String appArgument) {
		process.addApplicationArgument(appArgument);
	}

	// Method used in constructors of subclasses. Should not be overridable.
	protected final void clearApplicationArguments() {
		process.clearApplicationArguments();
	}

    @Override
    public void addStopListener(StopListener stopListener) {
        process.addStopListener(stopListener);
    }

    @Override
    public void removeStopListener(StopListener stopListener) {
        process.removeStopListener(stopListener);
    }

    @Override
    public void clearStopListeners() {
        process.clearStopListeners();
    }

	@Override
	public String getDetails() {
		// remember the answer as it usually does not change for a procedure
		if(detailCache == null) {
			detailCache = process.getDetails();
		}

		return detailCache;
	}

	@Override
	public boolean isInstrumentationSupported() {
    	// no instrumentation support for APM NG if auto-injection is used
		try {
			if(DtVersionDetector.isAPM()) {
				DtAgentConfig agentConfig = getAgentConfig();
				if(agentConfig == null) {
					return false;
				}

				if(agentConfig.getAgentPath(getTechnology()) == null) {
					return false;
				}
			}
		} catch (ConfigurationException e) {
			LOGGER.info("Could not get AgentPath: " + e);
		}

    	return super.isInstrumentationSupported();
	}

	@Override
    public boolean agentFound() {
        return agentFound(process.getDtAgentConfig());
    }

	public Process getProcess() {
		return process;
	}

	@Override
	public File getPropertyFile() {
		return process.getPropertyFile();
	}
}
