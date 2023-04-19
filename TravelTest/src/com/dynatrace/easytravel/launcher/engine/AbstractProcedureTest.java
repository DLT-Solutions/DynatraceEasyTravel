package com.dynatrace.easytravel.launcher.engine;

import static org.junit.Assert.*;

import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.utils.TestHelpers;


public class AbstractProcedureTest {

	@Test
	public void testAbstractProcedure() {
		try {
			new MockProcedure(null);
			fail("Should catch exception on empty mapping");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "Mapping is required");
		}

		DefaultProcedureMapping mapping = new DefaultProcedureMapping("somemapping");
		AbstractProcedure proc = new MockProcedure(mapping);

		assertEquals(MessageConstants.UNKNOWN, proc.getName());

		//proc.notifyBatchStateChanged(null, State.STOPPED, State.OPERATING);
		proc.notifyProcedureStateChanged(null, State.STOPPED, State.OPERATING);

		DefaultProcedureMapping mapping2 = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);

		assertEquals(MessageConstants.UNKNOWN, ProcedureFactory.getNameOfProcedure(proc.getMapping()));
		proc.transfer(mapping2, State.STOPPED);
		assertEquals("Now the new mapping should be in place",
				MessageConstants.MODULE_BUSINESS_BACKEND, ProcedureFactory.getNameOfProcedure(proc.getMapping()));

		EasyTravelConfig.read().syncProcessTimeoutMs = 1;
		try {
			assertFalse(proc.waitUntilNotRunning());
			assertFalse(proc.waitUntilNotRunning(100, 100));

			assertFalse(proc.waitUntilRunning());
			assertFalse(proc.waitUntilRunning(100, 100));
		} finally {
			EasyTravelConfig.read().syncProcessTimeoutMs = 30;
		}

		assertNotNull(proc.getMapping());
		assertFalse(proc.isSynchronous());
		assertNull(proc.getURI());
		assertEquals(0, proc.getDependingProcedureIDs().size());
		assertNull(proc.getPropertyFile());
	}

	@Test
	public void testIsEnabled() {
		DefaultProcedureMapping  mapping = new DefaultProcedureMapping("somemapping");
		AbstractProcedure proc = new MockProcedure(mapping);

		assertTrue(proc.isEnabled());

		mapping.addSetting(new DefaultProcedureSetting(Constants.Misc.SETTING_ENABLED, Constants.Misc.SETTING_VALUE_OFF));
		proc = new MockProcedure(mapping);

		assertFalse("Now proc is not enabled any more because setting is set to OFF", proc.isEnabled());
	}

	@Test
	public void testTransfer() {
		DefaultProcedureMapping  mapping = new DefaultProcedureMapping("somemapping");
        DefaultProcedureSetting defaultProcedureSetting = new DefaultProcedureSetting(Constants.Misc.SETTING_ENABLED, Constants.Misc.SETTING_VALUE_OFF);
        mapping.addSetting(defaultProcedureSetting);
		AbstractProcedure proc1 = new MockProcedure(mapping);

		assertFalse(proc1.isTransferableTo(null));
		assertFalse(proc1.isTransferableTo(new DefaultProcedureMapping(null)));
		assertFalse(proc1.isTransferableTo(new DefaultProcedureMapping("someothermapping")));

		assertFalse("Still not transferable because of settings", proc1.isTransferableTo(new DefaultProcedureMapping("somemapping")));

		// add a setting which is not transferable
		AbstractProcedure proc2 = new MockProcedure(mapping);

		assertTrue("This should now be transferable", proc2.isTransferableTo(new DefaultProcedureMapping("somemapping").addSetting(defaultProcedureSetting)));

        DefaultProcedureSetting procedureConfig = new DefaultProcedureSetting(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, "RUXIT_CLUSTER_ID", "eT-BB");
        DefaultProcedureSetting procedureConfigChanged = new DefaultProcedureSetting(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, "RUXIT_CLUSTER_ID", "eT-BB-xxx");

        DefaultProcedureSetting scenarioConfig = new DefaultProcedureSetting(Scenario.TYPE_SCENARIO_CONFIG, "persistenceMode", "mongodb");
        DefaultProcedureSetting scenarioConfigChanged = new DefaultProcedureSetting(Scenario.TYPE_SCENARIO_CONFIG, "persistenceMode", "cassandra");

        // default procedure mappings, same settings, different mappingId
        DefaultProcedureMapping defaultMapping1 = new DefaultProcedureMapping("mapping1");
        defaultMapping1.addSetting(procedureConfig);
        defaultMapping1.addSetting(scenarioConfig);

        DefaultProcedureMapping defaultMapping2 = new DefaultProcedureMapping("mapping2");
        defaultMapping2.addSetting(procedureConfig);
        defaultMapping2.addSetting(scenarioConfig);

        // procedure config value changed
        DefaultProcedureMapping procedureConfigChangedMapping = new DefaultProcedureMapping("mapping1");
        procedureConfigChangedMapping.addSetting(procedureConfigChanged);
        procedureConfigChangedMapping.addSetting(scenarioConfig);

        // scenario config value changed
        DefaultProcedureMapping scenarioConfigChangedMapping = new DefaultProcedureMapping("mapping1");
        scenarioConfigChangedMapping.addSetting(procedureConfig);
        scenarioConfigChangedMapping.addSetting(scenarioConfigChanged);

        // procedure and scenario config value changed
        DefaultProcedureMapping procedureAndScenarioConfigChangedMapping = new DefaultProcedureMapping("mapping1");
        procedureAndScenarioConfigChangedMapping.addSetting(procedureConfigChanged);
        procedureAndScenarioConfigChangedMapping.addSetting(scenarioConfigChanged);

        assertFalse("Not transferable beceause of different mappingId", new MockProcedure(defaultMapping1).isTransferableTo(defaultMapping2));

        assertTrue(new MockProcedure(defaultMapping1).isTransferableTo(defaultMapping1));

        assertTrue(new MockProcedure(defaultMapping2).isTransferableTo(defaultMapping2));

        assertFalse(new MockProcedure(defaultMapping1).isTransferableTo(new DefaultProcedureMapping("mapping1").addSetting(procedureConfig)));

        assertFalse(new MockProcedure(defaultMapping1).isTransferableTo(procedureConfigChangedMapping));

        assertFalse(new MockProcedure(procedureConfigChangedMapping).isTransferableTo(defaultMapping1));

        assertFalse(new MockProcedure(defaultMapping1).isTransferableTo(scenarioConfigChangedMapping));

        assertFalse(new MockProcedure(scenarioConfigChangedMapping).isTransferableTo(defaultMapping1));

        assertFalse(new MockProcedure(defaultMapping1).isTransferableTo(procedureAndScenarioConfigChangedMapping));

        assertFalse(new MockProcedure(procedureAndScenarioConfigChangedMapping).isTransferableTo(defaultMapping1));

        assertFalse(new MockProcedure(procedureConfigChangedMapping).isTransferableTo(scenarioConfigChangedMapping));

        assertFalse(new MockProcedure(scenarioConfigChangedMapping).isTransferableTo(procedureConfigChangedMapping));

        assertTrue(new MockProcedure(procedureConfigChangedMapping).isTransferableTo(procedureConfigChangedMapping));

	}

	private class MockProcedure extends AbstractProcedure {

		private MockProcedure(ProcedureMapping mapping) throws IllegalArgumentException {
			super(mapping);
		}

		@Override
		public void removeStopListener(StopListener stopListener) {

		}

		@Override
		public void clearStopListeners() {

		}

		@Override
		public void addStopListener(StopListener stopListener) {

		}

		@Override
		public Feedback stop() {
			return null;
		}

		@Override
		public Feedback run() {
			return null;
		}

		@Override
		public boolean isStoppable() {
			return false;
		}

		@Override
		public StopMode getStopMode() {
			return StopMode.SEQUENTIAL;
		}

		@Override
		public boolean isRunning() {
			return false;
		}

		@Override
		public boolean isOperatingCheckSupported() {
			return false;
		}

		@Override
		public boolean isOperating() {
			return false;
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
		public String getDetails() {
			return null;
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
}
