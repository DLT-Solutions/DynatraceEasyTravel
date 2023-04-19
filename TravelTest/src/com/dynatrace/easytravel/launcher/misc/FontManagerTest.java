package com.dynatrace.easytravel.launcher.misc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.eclipse.rap.rwt.testfixture.TestContext;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.dynatrace.easytravel.utils.TestHelpers;

import ch.qos.logback.classic.Level;


public class FontManagerTest {
	@Rule
	public TestContext context = new TestContext();

	private Display display;

	@Before
	public void setUp() {
		TestHelpers.assumeCanUseDisplay();
		display = new Display();
	}

	@After
	public void tearDown() {
		if (display != null) {
			display.dispose();
		}
	}

	@Test
	public void testFontManager() {
		assertNotNull(display);
		FontManager manager = new FontManager();
		assertNotNull(manager);
		manager.disposeFonts();
	}

	@Test
	public void testCreateFontIntIntString() {
		FontManager manager = new FontManager();
		assertNotNull(manager.createFont(1, 1, "some", display));
		manager.disposeFonts();
	}

	@Test
	public void testCreateFontIntInt() {
		FontManager manager = new FontManager();
		assertNotNull(manager.createFont(1, 1, display));
		manager.disposeFonts();
	}

	@Test
	public void testCreateFontInt() {
		FontManager manager = new FontManager();
		assertNotNull(manager.createFont(1, display));
		manager.disposeFonts();
	}

	@Test
	public void testNullDisplay() {
		display.dispose();
		display = null;

		try {
			new FontManager();
			fail("Should throw exception here!");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "Unable to retrieve current Display");
		}
	}

	@Test
	public void runInMultipleThreads() {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {
			@Override
			public void run() {
				testCreateFontIntInt();
			}
		}, FontManager.class.getName(), Level.DEBUG);
	}


}
