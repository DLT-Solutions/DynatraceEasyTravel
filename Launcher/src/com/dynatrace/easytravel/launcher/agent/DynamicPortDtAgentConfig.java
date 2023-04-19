package com.dynatrace.easytravel.launcher.agent;

import com.dynatrace.easytravel.launcher.engine.WebProcedure;
import com.dynatrace.easytravel.util.ConfigurationProvider;
import com.dynatrace.easytravel.util.ConfigurationProvider.DynamicProperty;

/**
 * Special DtAgentConfig that has a dynamic agentName dependent on a
 * dynamic port of a WebProcedure.
 *
 * @author philipp.grasboeck
 */
public class DynamicPortDtAgentConfig extends DtAgentConfig {

	// the #{port} property to replace
	private static final String PROPERTY_PORT = "port";

    private final DynamicProperty dynamicProperty;

	public DynamicPortDtAgentConfig(final WebProcedure webProcedure, String dtAgentName, String dtAgentPath, String[] dtFurtherArgs, String[] dtEnvArgs) {
		super(dtAgentName, dtAgentPath, dtFurtherArgs, dtEnvArgs);
		if (webProcedure == null) {
			throw new IllegalArgumentException("webProcedure must not be null");
		}
        this.dynamicProperty = new DynamicProperty() {
			@Override
			public String name() {
				return PROPERTY_PORT;
			}
			@Override
			public Object value() {
				return webProcedure.getPort();
			}
		};
	}

	@Override
	public String getAgentName() {
		return ConfigurationProvider.replaceDynamicProperties(super.getAgentName(), dynamicProperty);
	}
}
