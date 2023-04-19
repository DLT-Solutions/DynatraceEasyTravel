package com.dynatrace.easytravel.launcher.misc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.eclipse.rap.rwt.testfixture.TestContext;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import com.dynatrace.easytravel.utils.TestHelpers;


public class ImageManagerTest {
	@Rule
	public TestContext context = new TestContext();

	@After
	public void tearDown() {
		// ensure that we clean up the Display that was implicitely created
		Display display = Display.getCurrent();
		if(display != null) {
			display.dispose();
		}
	}

	@Test
	public void testCreateImage() {
		ImageManager manager = new ImageManager();
		TestHelpers.assumeCanUseDisplay();
		Image createImage = manager.createImage(Constants.Images.FANCY_MENU_BUTTON_BG);
		assertNotNull("Image is read successfully", createImage);

		assertNotNull("Second time works again", manager.createImage(Constants.Images.FANCY_MENU_BUTTON_BG));
		assertSame("Image is cached and not re-read", createImage, manager.createImage(Constants.Images.FANCY_MENU_BUTTON_BG));

		manager.disposeImages();
	}

	@Test
	public void testCreateImageMissingResource() {
		ImageManager manager = new ImageManager();
		try {
			manager.createImage("notexisting");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "Cannot load image", "notexisting");
		}

		manager.disposeImages();
	}

	@Test
	public void testCreateImageInvalidResource() {
		ImageManager manager = new ImageManager();
		try {
			TestHelpers.assumeCanUseDisplay();
			manager.createImage("/META-INF/MANIFEST.MF");
		} catch (SWTException e) {
			TestHelpers.assertContains(e, "Unsupported or unrecognized format");
		}

		manager.disposeImages();
	}
}
