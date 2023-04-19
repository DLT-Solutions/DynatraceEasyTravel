package com.dynatrace.easytravel.launcher.remote;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.baseload.BaseLoadManager;
import com.dynatrace.easytravel.launcher.baseload.BaseLoadUtil;

/**
 * @author Rafal.Psciuk
 *
 */
public class LoadValueHandlerTest extends UiRestHandlerTestBase {

	@Test
	public void test() {
		int baseLoadDefault = EasyTravelConfig.read().baseLoadDefault;

		headerPanel.setLoad(15);
		headerPanel.setLoad(0);
		headerPanel.setLoad(baseLoadDefault);

		EasyMock.replay(headerPanel);

		LoadValueHandler handler = new LoadValueHandler();
		String res = handler.setLoad(15);
		assertEquals("Load value set to 15 visits/min", res);
		verifyLoadControllers(15);

		res = handler.setLoad(0);
		assertEquals("Load value set to 0 visits/min", res);
		verifyLoadControllers(0);

		res = handler.setLoad(baseLoadDefault);
		assertEquals("Load value set to " + baseLoadDefault + " visits/min", res);
		verifyLoadControllers(baseLoadDefault);

		EasyMock.verify(headerPanel);
	}

	private int getLoadNumberPerMinuteValue(int loadValue) {
		return BaseLoadUtil.getLoadNumberPerMinute(loadValue, LOAD_RATIO);
	}

	private void verifyLoadControllers(int load) {
		int loadPerMinute = getLoadNumberPerMinuteValue(load);
		assertEquals(loadPerMinute, BaseLoadManager.getInstance().getCustomerLoadController().getValue());
		assertEquals(loadPerMinute, BaseLoadManager.getInstance().getB2bLoadController().getValue());
		assertEquals(loadPerMinute, BaseLoadManager.getInstance().getMobileNativeLoadController().getValue());
		assertEquals(loadPerMinute, BaseLoadManager.getInstance().getMobileBrowserLoadController().getValue());
		assertEquals(load, Launcher.getBaseLoadValue());
	}
}
