package com.dynatrace.easytravel.launcher.plugin;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.easymock.EasyMock;
import org.easymock.internal.matchers.Any;
import org.eclipse.rap.rwt.testfixture.TestContext;
import org.eclipse.swt.widgets.Display;
import org.junit.Rule;
import org.junit.Test;

import com.dynatrace.easytravel.launcher.engine.Procedure;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.engine.StopListener;
import com.dynatrace.easytravel.launcher.fancy.MenuPage;
import com.dynatrace.easytravel.launcher.fancy.PageProvider;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.spring.PluginInfoList;
import com.dynatrace.easytravel.utils.TestHelpers;

import ch.qos.logback.classic.Level;


public class PluginStateListenerTest {
	@Rule
	public TestContext context = new TestContext();

	@Test
	public void testNotifyProcedureStateChanged() {
		PluginStateListener listener = new PluginStateListener();
		Procedure procedure = EasyMock.createStrictMock(Procedure.class);

		procedure.addStopListener(anyStopListener());
		EasyMock.expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.ANT_ID));
		procedure.addStopListener(anyStopListener());
		EasyMock.expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID));
		procedure.addStopListener(anyStopListener());
		EasyMock.expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID));
		procedure.addStopListener(anyStopListener());
		EasyMock.expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID));
		procedure.addStopListener(anyStopListener());
		EasyMock.expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID));
		procedure.addStopListener(anyStopListener());
		EasyMock.expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID));

		EasyMock.replay(procedure);

		listener.notifyProcedureStateChanged(new StatefulProcedure(procedure), State.STOPPED, State.STARTING);
		listener.notifyProcedureStateChanged(new StatefulProcedure(procedure), State.STOPPED, State.STARTING);
		listener.notifyProcedureStateChanged(new StatefulProcedure(procedure), State.STARTING, State.OPERATING);
		listener.notifyProcedureStateChanged(new StatefulProcedure(procedure), State.OPERATING, State.STOPPING);
		listener.notifyProcedureStateChanged(new StatefulProcedure(procedure), State.STOPPING, State.STOPPED);
		listener.notifyProcedureStateChanged(new StatefulProcedure(procedure), State.STARTING, State.TIMEOUT);

		EasyMock.verify(procedure);
	}

	private StopListener anyStopListener() {
		EasyMock.reportMatcher(Any.ANY);
		return null;
	}

	@Test
	public void testRefreshPluginMenuPage() {
		PluginStateListener listener = new PluginStateListener();

		listener.refreshPluginMenuPage();
	}

	@Test
	public void testRefreshPluginMenuPageWithHeaderListener() {
		PluginStateListener listener = new PluginStateListener();

		final AtomicBoolean called = new AtomicBoolean(false);
		listener.setPluginEnabledListener(new PluginEnabledListener() {

			@Override
			public void notifyEnabledPlugins(PluginInfoList enabledPlugins) {
				called.set(true);
			}
		});

		listener.refreshPluginMenuPage();

		assertTrue("Listener should have been called", called.get());
	}

	@Test
	public void testSetMenu() {
		TestHelpers.assumeCanUseDisplay();
		Display display = new Display();
		try {
			assertNotNull(Display.getCurrent());

			PluginStateListener listener = new PluginStateListener();
			assertNotNull(listener);

			// cannot run this without SWT GUI: listener.setMenu(new MenuComponent(null));
		} finally {
			display.dispose();
		}
	}

	@Test
	public void testCreateNoPluginsPage() {
		assertNotNull(PluginStateListener.createNoPluginsPage());
	}

	@Test
	public void testWithDifferentLogLevel() {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				testNotifyProcedureStateChanged();
				testCreateNoPluginsPage();
				testRefreshPluginMenuPage();
			}
		}, PluginStateListener.class.getName(), Level.DEBUG);
	}

	private int id = -1;
	private MenuPage page = null;

	private void setPage(int id, MenuPage page) {
		this.id = id;
		this.page = page;
	}

	@Test
	public void testNotifyProcedureStateChangedIOException() {
		PluginStateListener listener = new PluginStateListener();
		Procedure procedure = EasyMock.createStrictMock(Procedure.class);

		procedure.addStopListener(anyStopListener());
		expect(procedure.getMapping()).andReturn(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID));

		PageProvider menu = new PageProvider() {

			@Override
			public void replacePage(int id, MenuPage page) {
				setPage(id, page);
			}

			@Override
			public int getPageCount() {
				return 4;
			}
		};
		listener.setMenu(menu);

		replay(procedure);

		listener.notifyProcedureStateChanged(new StatefulProcedure(procedure), State.STARTING, State.OPERATING);

		assertEquals("Should have page 3 set now", 3, id);
		TestHelpers.assertContainsMsg("Should have a page with useful error message now",
				page.getTitle(), "Problem Patterns");
		// TODO: cannot check that we have the right error message item...

		verify(procedure);
	}

	// TODO: add tests which start up a dummy business backend so we successfully query the plugin state...
}
