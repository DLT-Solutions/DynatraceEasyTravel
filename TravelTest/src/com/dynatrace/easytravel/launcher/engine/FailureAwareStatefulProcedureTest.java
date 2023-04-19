/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: FailureAwareStatefulProcedureTest.java
 * @date: 03.10.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.launcher.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.easymock.EasyMock;
import org.easymock.internal.matchers.Any;
import org.junit.Test;

import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.process.Process;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;


/**
 *
 * @author dominik.stadler
 */
public class FailureAwareStatefulProcedureTest {
	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.engine.FailureAwareStatefulProcedure#FailureAwareStatefulProcedure(com.dynatrace.easytravel.launcher.engine.Procedure)}.
	 * @throws Exception
	 */
	@Test
	public void testFailureAwareStatefulProcedure() throws Exception {
		Procedure mock = EasyMock.createStrictMock(Procedure.class);

		mock.addStopListener(anyStopListener());
		MockJavaProcedure mockDelegate = new MockJavaProcedure(new DefaultProcedureMapping("test")) {
			@Override
			protected Process createProcess(ProcedureMapping mapping) throws CorruptInstallationException {
				return null;
			}
		};
		EasyMock.expectLastCall().andDelegateTo(mockDelegate);

		mock.notifyProcedureStateChanged(anySubject(), anyState(), anyState());
		EasyMock.expectLastCall().atLeastOnce();

		EasyMock.replay(mock);
		FailureAwareStatefulProcedure proc = new FailureAwareStatefulProcedure(mock);
		// the constructor of StatefulProcedure will call this anyway
		// proc.addDefaultStopListener();

		assertNotNull(mockDelegate.listener);

		mockDelegate.listener.notifyProcessFailed();
		assertEquals(State.ACCESS_DENIED, proc.getState());

		mockDelegate.listener.notifyProcessStopped();
		assertEquals(State.STOPPED, proc.getState());

		EasyMock.verify(mock);
	}

	private State anyState() {
	    EasyMock.reportMatcher(Any.ANY);

	    return null;
	}

	private StatefulProcedure anySubject() {
	    EasyMock.reportMatcher(Any.ANY);

	    return null;
	}

	private StopListener anyStopListener() {
	    EasyMock.reportMatcher(Any.ANY);

		return null;
	}

	private abstract class MockJavaProcedure extends AbstractJavaProcedure {
		private StopListener listener = null;

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
		public void addStopListener(StopListener stopListener) {
			this.listener = stopListener;
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
	}
}
