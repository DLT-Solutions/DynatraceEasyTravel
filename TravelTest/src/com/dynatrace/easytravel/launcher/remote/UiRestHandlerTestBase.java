package com.dynatrace.easytravel.launcher.remote;

import org.easymock.EasyMock;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;

import com.dynatrace.easytravel.config.CustomerTrafficScenarioEnum;
import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.LauncherUI;
import com.dynatrace.easytravel.launcher.baseload.BaseLoadManager;
import com.dynatrace.easytravel.launcher.panels.HeaderPanel;

/**
 * @author Rafal.Psciuk
 *
 */
public class UiRestHandlerTestBase {
	static final int LOAD_RATIO = 1;
	protected HeaderPanel headerPanel;
	protected Display display;
	protected LauncherUI launcherUI;

	@Before
	public void setup() {
		//init load generators
		BaseLoadManager baseLoadMgr = BaseLoadManager.getInstance();
		baseLoadMgr.getCustomerBaseLoadInstance(CustomerTrafficScenarioEnum.EasyTravel, 10, LOAD_RATIO, false);
		baseLoadMgr.getB2BBaseLoadInstance(10, 1, false);
		baseLoadMgr.getMobileNativeBaseLoadInstance(10, 1, false);
		baseLoadMgr.getMobileBrowserBaseLoadInstance(10, 1, false);

		headerPanel = EasyMock.createStrictMock(HeaderPanel.class);
		BaseLoadManager.getInstance().setHeaderPanelInterface(headerPanel);

		launcherUI = new LauncherUI();
		display = Display.getCurrent();
		launcherUI.setDisplay(display);
		launcherUI.setHeaderPanel(headerPanel);
		Launcher.addLauncherUI(launcherUI);
	}

	@After
	public void teardown() {
		BaseLoadManager.getInstance().reset();
		headerPanel = null;
	}
}
