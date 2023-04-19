package com.dynatrace.easytravel.launcher.procedures;

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.AngularFrontendReservation;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.TomcatResourceReservation;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.DynamicPortDtAgentConfig;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.WebProcedure;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.util.LocalUriProvider;

/**
 * Procedure which starts the REST service which we use for the angular app.
 * @author tibor.varga
 */
public class AngularFrontendProcedure extends CustomerFrontendProcedure implements WebProcedure {

	/**
	 * @param mapping
	 * @throws CorruptInstallationException if the JAR of the customer frontend could not be found
	 */
	public AngularFrontendProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
		super(mapping);
	}

	@Override
	protected DtAgentConfig getAgentConfig() {
		final EasyTravelConfig config = EasyTravelConfig.read();
		DtAgentConfig dtAgentConfig = new DynamicPortDtAgentConfig(this, config.angularFrontendSystemProfile,
				config.angularFrontendAgent, config.angularFrontendAgentOptions, config.angularFrontendEnvArgs);

		// Customize config settings for the procedure.
		adjustAgentConfig("customer frontend", "config.angularFrontendEnvArgs", dtAgentConfig);
		return dtAgentConfig;
	}

	@Override
	protected String getModuleJar() {
		return Constants.Modules.CUSTOMER_FRONTEND_REST;
	}

	@Override
	protected String getWorkingDir() {
		return BaseConstants.SubDirectories.ANGULAR;
	}
	
	@Override
	protected String[] getJavaOpts() {
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();
		return (String[]) ArrayUtils.addAll(CONFIG.javaopts, CONFIG.angularFrontendJavaopts);
	}
	
	@Override
	public String getURI() {
		final EasyTravelConfig config = EasyTravelConfig.read();
	    if (StringUtils.isNotEmpty(config.angularFrontendPublicUrl)) {
	        return config.angularFrontendPublicUrl;
	    }
		return LocalUriProvider.getLocalUri(getPort(), getContextRoot());
	}

	@Override
	public String getURIDNS() {
		final EasyTravelConfig config = EasyTravelConfig.read();
	    if (StringUtils.isNotEmpty(config.angularFrontendPublicUrl)) {
			return config.angularFrontendPublicUrl;
	    }

		return LocalUriProvider.getLocalUriDNS(getPort(), getContextRoot());
	}

    // from WebProcedure
    private static final String PROPERTY_ANGULAR_FRONTEND_PORT = "angularFrontendPort";

	@Override
	public String getPortPropertyName() {
		return PROPERTY_ANGULAR_FRONTEND_PORT;
	}

	@Override
	protected TomcatResourceReservation reserveResources() throws IOException {
		return AngularFrontendReservation.reserveResources();
	}
}
