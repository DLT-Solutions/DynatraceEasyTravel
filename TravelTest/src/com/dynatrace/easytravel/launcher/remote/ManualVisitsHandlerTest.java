package com.dynatrace.easytravel.launcher.remote;

import static org.junit.Assert.*;

import org.easymock.EasyMock;
import org.eclipse.rap.rwt.testfixture.TestContext;
import org.eclipse.swt.widgets.Composite;
import org.junit.Rule;
import org.junit.Test;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.baseload.BaseLoadManager;

/**
 * @author Rafal.Psciuk
 *
 */
public class ManualVisitsHandlerTest extends UiRestHandlerTestBase {

	@Test
	public void test() throws InterruptedException {
		//TODO: this test should be reworked - now it is strange
		ManualVisitsHandler handler = new ManualVisitsHandler();

		Composite com = EasyMock.createStrictMock(Composite.class);
		EasyMock.expect(com.isDisposed()).andReturn(false).anyTimes();
		EasyMock.expect(com.getDisplay()).andReturn(display).anyTimes();
		EasyMock.expect(com.isDisposed()).andReturn(false).anyTimes();

		headerPanel.setCreateNowVisible(true);
		EasyMock.expect(headerPanel.getMainComposite()).andReturn(com);

		headerPanel.setCreateNowVisible(false);
		headerPanel.setCreateNowVisible(false);
		EasyMock.replay(headerPanel, com);

		String res = handler.setManualVisitsCreation(true);
		assertEquals("Manual visits creation set to true", res);
		//TODO: make sure that elem at 0 is OK
		assertTrue(Launcher.getLauncherUIList().get(0).isCreateNowSelected());
		Thread.sleep(1000); //wait for thread in launcher to complete
		assertTrue(BaseLoadManager.getInstance().getCustomerLoadController().isSchedulingBlocked());
		assertTrue(BaseLoadManager.getInstance().getB2bLoadController().isSchedulingBlocked());
		assertTrue(BaseLoadManager.getInstance().getMobileNativeLoadController().isSchedulingBlocked());
		assertTrue(BaseLoadManager.getInstance().getMobileBrowserLoadController().isSchedulingBlocked());


		res = handler.setManualVisitsCreation(false);
		assertEquals("Manual visits creation set to false", res);
		//TODO: make sure that elem at 0 is OK
		assertFalse(Launcher.getLauncherUIList().get(0).isCreateNowSelected());
		Thread.sleep(1000); //wait for thread in launcher to complete
		assertFalse(BaseLoadManager.getInstance().getCustomerLoadController().isSchedulingBlocked());
		assertFalse(BaseLoadManager.getInstance().getB2bLoadController().isSchedulingBlocked());
		assertFalse(BaseLoadManager.getInstance().getMobileNativeLoadController().isSchedulingBlocked());
		assertFalse(BaseLoadManager.getInstance().getMobileBrowserLoadController().isSchedulingBlocked());

		EasyMock.verify(headerPanel, com);
	}
}
