package com.dynatrace.easytravel.launcher.fancy.custom;

import static org.junit.Assert.assertNotNull;

import org.eclipse.rap.rwt.testfixture.TestContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.DummyProcedure;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.utils.TestHelpers;


public class ProcedureComponentTest {
	@Rule
	public TestContext context = new TestContext();

	@Test
	public void testProcedureComponent() {
		TestHelpers.assumeCanUseDisplay();

		Display display = new Display();
		assertNotNull(display);

    	// skip test if we do not have a graphical Display
    	Assume.assumeNotNull(Display.getCurrent());

    	Launcher.addLauncherUI(display);

    	ProcedureComponent comp = new ProcedureComponent(new Shell(display),
    			new StatefulProcedure(new DummyProcedure(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID), Feedback.Success, null)));
		assertNotNull(comp);

		display.dispose();
	}
}
