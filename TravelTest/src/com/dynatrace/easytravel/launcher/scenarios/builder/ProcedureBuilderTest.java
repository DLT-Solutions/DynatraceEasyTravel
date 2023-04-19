package com.dynatrace.easytravel.launcher.scenarios.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.config.ScenarioConfiguration;

public class ProcedureBuilderTest {

	@Test
	public void test() {
		//assertNotNull(ProcedureBuilder.paymentBackend().set((SettingBuilder)null));
		assertNotNull(ProcedureBuilder.paymentBackend().set(new SettingBuilder[] {}));
		assertNotNull(ProcedureBuilder.paymentBackend().setIf(true));
		assertNotNull(ProcedureBuilder.paymentBackend().setIf(true, new SettingBuilder[] {}));

		// call ScenarioConfiguration to cover many of the methods
		ScenarioConfiguration conf = new ScenarioConfiguration();
		conf.createDefaultScenarios();

		assertNotNull(ProcedureBuilder.paymentBackend().set());

		SettingBuilder type = SettingBuilder.antProperty("someprop").enable().type("sometype").value("234sd");
		type.setName("somename");
		assertEquals("sometype", type.getType());
		assertNotNull(ProcedureBuilder.paymentBackend().set(type));

		SettingBuilder enabled = SettingBuilder.procedure().enable();
		assertNotNull(enabled);

		ProcedureBuilder hostAgentControl = ProcedureBuilder.hostAgentControl();
		assertNotNull(hostAgentControl);
		assertEquals(InstallationType.Classic, hostAgentControl.create().getCompatibility());
		assertNotNull(ProcedureBuilder.webserverAgentControl());
		assertNotNull(ProcedureBuilder.mdbms());
		assertNotNull(ProcedureBuilder.mdcontentCreator());
		assertNotNull(ProcedureBuilder.apacheHttpdPhp());
	}
}
