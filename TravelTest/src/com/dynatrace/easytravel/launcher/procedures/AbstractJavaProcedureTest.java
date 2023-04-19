package com.dynatrace.easytravel.launcher.procedures;

import static com.dynatrace.easytravel.constants.BaseConstants.CustomerFrontendArguments.ROUTEPREFIX;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.After;
import org.junit.Test;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.CustomerFrontendReservation;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.config.TomcatResourceReservation;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.AbstractJavaProcedure;
import com.dynatrace.easytravel.launcher.engine.AbstractProcessProcedure;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.process.JavaProcess;
import com.dynatrace.easytravel.launcher.process.Process;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.TestHelpers;

public class AbstractJavaProcedureTest {

	/** tries to start a Java process, we test this in integration tests anyway
	@Test
	public void testCreate() throws IOException, CorruptInstallationException, ConfigurationException {
		final ProcedureMapping mapping = createNiceMock(ProcedureMapping.class);
		final DtAgentConfig dtAgentConfig = createNiceMock(DtAgentConfig.class);

		final File jarDummy = File.createTempFile("java", "jar");
		try {
			final AbstractProcessProcedure abstractJavaProcedure;
			final File agentDummy = File.createTempFile("dtagent", "dll");
			try {
				agentDummy.createNewFile();

				expect(dtAgentConfig.getAgentPath(anyObject(Technology.class))).andReturn(agentDummy.getAbsolutePath()).anyTimes();

				replay(mapping, dtAgentConfig);

				abstractJavaProcedure = new MockJavaProcedure(mapping) {

					@Override
					protected String getModuleJar() {
						return jarDummy.getAbsolutePath();
					}
				};

				assertTrue("Java agents are supported.", abstractJavaProcedure.isInstrumentationSupported());

				assertTrue("Java agent should be found", abstractJavaProcedure.agentFound());

			} finally {
				assertTrue(agentDummy.delete());
			}

			assertFalse("Java agent should not be found; it is not yet created.", abstractJavaProcedure.agentFound());
		} finally {
			assertTrue(jarDummy.delete());
		}

		verify(mapping, dtAgentConfig);
    }*/
	
	@After
	public void tearDown() {
		DtVersionDetector.enforceInstallationType(null);
	}

	@Test
	public void testAgentFound() throws IOException, CorruptInstallationException, ConfigurationException {
		final ProcedureMapping mapping = createNiceMock(ProcedureMapping.class);
		final File dummyFile = createNiceMock(File.class);
		final DtAgentConfig dtAgentConfig = createNiceMock(DtAgentConfig.class);

		expect(dummyFile.exists()).andReturn(true).anyTimes();

		final AbstractProcessProcedure abstractJavaProcedure;
		File agentDummy = File.createTempFile("dtagent", "dll");
		try {
			EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
			agentDummy.createNewFile();

			expect(dtAgentConfig.getAgentPath(anyObject(Technology.class))).andReturn(agentDummy.getAbsolutePath()).anyTimes();

			replay(mapping, dummyFile, dtAgentConfig);

			abstractJavaProcedure = new MockJavaProcedure(mapping) {
				@Override
				protected Process createProcess(ProcedureMapping mapping) {
					try {
						return new JavaProcess(dummyFile, dtAgentConfig);
					} catch (FileNotFoundException e) {
						throw new Error(e);
					}
				}
			};

			assertTrue("Java agents are supported.", abstractJavaProcedure.isInstrumentationSupported());

			assertTrue("Java agent should be found", abstractJavaProcedure.agentFound());

		} finally {
			assertTrue(agentDummy.delete());
			EasyTravelConfig.resetSingleton();
		}

		assertFalse("Java agent should not be found; it is not yet created.", abstractJavaProcedure.agentFound());

		verify(mapping, dummyFile, dtAgentConfig);
    }

	@Test
	public void testAgentFoundJavaOptsNoAgent() throws IOException, CorruptInstallationException, ConfigurationException {
		final ProcedureMapping mapping = createNiceMock(ProcedureMapping.class);
		final File dummyFile = createNiceMock(File.class);
		final DtAgentConfig dtAgentConfig = createNiceMock(DtAgentConfig.class);
		EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;

		try {
			expect(dummyFile.exists()).andReturn(true).anyTimes();


			replay(mapping, dummyFile, dtAgentConfig);

			final AbstractProcessProcedure abstractJavaProcedure = new MockJavaProcedure(mapping) {
				@Override
				protected Process createProcess(ProcedureMapping mapping) {
					try {
						return new JavaProcess(dummyFile, dtAgentConfig);
					} catch (FileNotFoundException e) {
						throw new Error(e);
					}
				}

				@Override
				protected String[] getJavaOpts() {
					return new String[] {"opt1", "opt2"};
				}
			};

			assertTrue("Java agents are supported.", abstractJavaProcedure.isInstrumentationSupported());

			assertFalse("Java agent should not be found", abstractJavaProcedure.agentFound());

			verify(mapping, dummyFile, dtAgentConfig);
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}


	@Test
	public void testAgentFoundJavaOpts() throws IOException, CorruptInstallationException, ConfigurationException {
		final ProcedureMapping mapping = createNiceMock(ProcedureMapping.class);
		final File dummyFile = createNiceMock(File.class);
		final DtAgentConfig dtAgentConfig = createNiceMock(DtAgentConfig.class);
		EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
		
		try {
			expect(dummyFile.exists()).andReturn(true).anyTimes();

			replay(mapping, dummyFile, dtAgentConfig);

			final AbstractProcessProcedure abstractJavaProcedure = new MockJavaProcedure(mapping) {
				@Override
				protected Process createProcess(ProcedureMapping mapping) {
					try {
						return new JavaProcess(dummyFile, dtAgentConfig);
					} catch (FileNotFoundException e) {
						throw new Error(e);
					}
				}

				@Override
				protected String[] getJavaOpts() {
					return new String[] {"-agentpath:blabla"};
				}
			};

			assertTrue("Java agents are supported.", abstractJavaProcedure.isInstrumentationSupported());

			assertTrue("Java agent should be found", abstractJavaProcedure.agentFound());

			verify(mapping, dummyFile, dtAgentConfig);
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testAgentNoInstrumentation() throws IOException, CorruptInstallationException, ConfigurationException {
		final ProcedureMapping mapping = createNiceMock(ProcedureMapping.class);
		final File dummyFile = createNiceMock(File.class);
		final DtAgentConfig dtAgentConfig = createNiceMock(DtAgentConfig.class);

		expect(dummyFile.exists()).andReturn(true).anyTimes();


		replay(mapping, dummyFile, dtAgentConfig);

		final AbstractProcessProcedure abstractJavaProcedure = new MockJavaProcedure(mapping) {
			@Override
			protected Process createProcess(ProcedureMapping mapping) {
				try {
					return new JavaProcess(dummyFile, dtAgentConfig);
				} catch (FileNotFoundException e) {
					throw new Error(e);
				}
			}

			@Override
			public boolean isInstrumentationSupported() {
				// test behaviour if this is false;
				return false;
			}

			@Override
			protected String[] getJavaOpts() {
				return new String[] {"-agentpath:blabla"};
			}
		};

		assertFalse("Java agents are not supported here.", abstractJavaProcedure.isInstrumentationSupported());

		assertFalse("Java agent should not be found because instrumentation is not supported for htis Procedure", abstractJavaProcedure.agentFound());

		verify(mapping, dummyFile, dtAgentConfig);
	}

	@Test
	public void testSetReservationSettings() throws Exception {
		final ProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID);

		final MockJavaProcedure procedure = new MockJavaProcedure(mapping) {
			@Override
			protected Process createProcess(ProcedureMapping mapping) {
				try {
					return new JavaProcess(new File("../Distribution/dist", Constants.Modules.CUSTOMER_FRONTEND));
				} catch (FileNotFoundException e) {
					throw new IllegalStateException(e);
				}
			}
		};

		procedure.addReservationSettings(CustomerFrontendReservation.reserveResources());

		String details = procedure.getProcess().getDetails();
		TestHelpers.assertContains(details,
				"customer.frontend.jar",
				BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.PORT,
				BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.SHUTDOWN_PORT,
				BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.AJP_PORT,
				BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.CONTEXT_ROOT,
				BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.WEBAPP_BASE);
		TestHelpers.assertNotContains(details, BaseConstants.MINUS + ROUTEPREFIX);
	}

	@Test
	public void testSetReservationSettingsWithRoutePrefix() throws Exception {
		final ProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID);
		mapping.addSetting(new DefaultProcedureSetting("route_prefix", "someprefix"));

		final MockJavaProcedure procedure = new MockJavaProcedure(mapping) {
			@Override
			protected Process createProcess(ProcedureMapping mapping) {
				try {
					return new JavaProcess(new File("../Distribution/dist", Constants.Modules.CUSTOMER_FRONTEND));
				} catch (FileNotFoundException e) {
					throw new IllegalStateException(e);
				}
			}
		};

		procedure.addReservationSettings(CustomerFrontendReservation.reserveResources());

		String details = procedure.getProcess().getDetails();
		TestHelpers.assertContains(details,
				"customer.frontend.jar",
				BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.PORT,
				BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.SHUTDOWN_PORT,
				BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.AJP_PORT,
				BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.CONTEXT_ROOT,
				BaseConstants.MINUS + BaseConstants.CustomerFrontendArguments.WEBAPP_BASE,
				BaseConstants.MINUS + ROUTEPREFIX + " someprefix");
	}

	private abstract class MockJavaProcedure extends AbstractJavaProcedure {

		protected MockJavaProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
			super(mapping);
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
		public boolean hasLogfile() {
			return false;
		}

		@Override
		public String getLogfile() {
			return null;
		}

		@Override
		protected String getModuleJar() {
			return null;
		}

		@Override
		protected DtAgentConfig getAgentConfig() {
			return null;
		}

		@Override
		protected String getWorkingDir() {
			return null;
		}

		@Override
		protected String[] getJavaOpts() {
			return null;
		}

		@Override
		public void addReservationSettings(TomcatResourceReservation reservation) {
			super.addReservationSettings(reservation);
		}
	}
}
