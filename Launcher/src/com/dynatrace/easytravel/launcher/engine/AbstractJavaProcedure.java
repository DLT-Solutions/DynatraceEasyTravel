package com.dynatrace.easytravel.launcher.engine;

import java.io.File;
import java.io.FileNotFoundException;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.TomcatResourceReservation;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.BusinessBackend.Persistence;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.process.JavaProcess;
import com.dynatrace.easytravel.launcher.process.Process;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.TextUtils;
import com.dynatrace.easytravel.launcher.scenarios.Setting;

public abstract class AbstractJavaProcedure extends AbstractProcessProcedure {

	protected AbstractJavaProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
        super(mapping);
    }

	protected abstract String getModuleJar();

	protected abstract String getWorkingDir();

	protected abstract String[] getJavaOpts();

    @Override
    public Technology getTechnology() {
        return Technology.JAVA;
    }

    @Override
    protected Process createProcess(ProcedureMapping mapping) throws CorruptInstallationException {
    	return createJavaProcess(getModuleJar(), getAgentConfig(), getWorkingDir(), getJavaOpts());
    }

    private static JavaProcess createJavaProcess(String moduleJar, DtAgentConfig dtAgentConfig, String workingDir, String[] javaopts) throws CorruptInstallationException {
        File moduleJarFile = new File(Directories.getInstallDir(), moduleJar);

        try {
            JavaProcess javaProcess = new JavaProcess(moduleJarFile, dtAgentConfig);
            javaProcess.setJavaArguments(javaopts);
            javaProcess.setWorkingSubDir(workingDir);

            return javaProcess;
        } catch (FileNotFoundException fnfe) {
            throw new CorruptInstallationException(TextUtils.merge("The following file could not be found: {0}", moduleJarFile.getAbsolutePath()), fnfe);
        }
    }

	@Override
	public boolean agentFound() {
        // also report as found if we have a custom "agentpath" setting in the javaopts
		// this is used for Training Mode, where we set javaOpts directly

		// this only should be done for Procedures that support agents
        if(isInstrumentationSupported()) {
        	String[] javaOpts = getJavaOpts();
        	if(javaOpts != null) {
        		for(String javaOpt : javaOpts) {
        			if(javaOpt.startsWith("-agentpath:")) {
        				return true;
        			}
        		}
        	}
		}

        // look at normal agent config if not found in javaOpts
		return super.agentFound();
	}


	protected void addPersistenceModeSetting(ProcedureMapping mapping) {
		String persistenceMode = mapping.getSettingValue(Constants.Misc.SETTING_PERSISTENCE_MODE);

		if (persistenceShouldBeSetToDefault(persistenceMode)) {
			persistenceMode = Persistence.JPA;
		}

		process.addApplicationArgumentPair(BaseConstants.MINUS + BaseConstants.CmdArguments.PERSISTENCE_MODE, persistenceMode);
	}

    protected void addInstalltionModeSetting(ProcedureMapping mapping){
        String installationMode = mapping.getSettingValue(Constants.Misc.SETTING_INSTALLATION_MODE);

        if (installationShouldBeSetToDefault(installationMode)) {
            installationMode = DtVersionDetector.getInstallationType().name();
        }

        process.addApplicationArgumentPair(BaseConstants.MINUS + BaseConstants.CmdArguments.INSTALLATION_MODE, installationMode);

    }

	protected void addReservationSettings(TomcatResourceReservation reservation) {
		// also apply the route-prefix if there is any
		reservation.setRoutePrefix(getMapping().getSettingValue("route_prefix"));

		addApplicationArgument(BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.PORT);
		addApplicationArgument(Integer.toString(reservation.getPort()));
		addApplicationArgument(BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.SHUTDOWN_PORT);
		addApplicationArgument(Integer.toString(reservation.getShutdownPort()));
		addApplicationArgument(BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.AJP_PORT);
		addApplicationArgument(Integer.toString(reservation.getAjpPort()));
		addApplicationArgument(BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.CONTEXT_ROOT);
		addApplicationArgument(reservation.getContextRoot());
		addApplicationArgument(BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.WEBAPP_BASE);
		addApplicationArgument(reservation.getWebappBase());
		if (reservation.getRoutePrefix() != null) {
			addApplicationArgument(BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.ROUTEPREFIX);
			addApplicationArgument(reservation.getRoutePrefix());
		}
	}

	private boolean persistenceShouldBeSetToDefault(String persistenceMode) {
		return persistenceMode == null;
	}

    private boolean installationShouldBeSetToDefault(String installatioMode) {
        return installatioMode == null;
    }
    
	protected void adjustAgentConfig(String procID, String settingName, DtAgentConfig dtAgentConfig) {
		
		// Customize config settings for the procedure.
		// The CONFIG object above contains settings defined in the scenario on
		// per-procedure basis, but they are extracted much earlier,
		// and combined for all procedures, to custom settings for the scenario.
		// Therefore per-procedure settings are lost
		// (see ScenarioConfiguration and LaunchEngine.run()). Fortunately, the
		// per-procedure settings are still present in the mapping.
		
		ProcedureMapping mapping = getMapping();
		
		if (mapping != null) {
			if (mapping.getId().equals(procID)) {
				if (mapping.hasCustomSettings()) {

					for (Setting setting : mapping.getCustomSettings()) {

						String name = setting.getName();
						String value = setting.getValue();

						if (name.equals(settingName)) {

							// At this point value as got from the mapping for the procedure
							// will be just one long, comma-separated string of "var=value,var=value,..."
							// (this is unlike in the easyTravelConfig object, where env args
							// are stored as an array of Strings, each as "var=value").
							String[] dtEnvArgs = value.split(",");
							dtAgentConfig.setEnvironmentArgs(dtEnvArgs);
							break;
						}
					}

				}
			}
		}
	}

}
