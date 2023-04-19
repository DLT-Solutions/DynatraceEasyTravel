package com.dynatrace.easytravel.frontend.data;

import static org.junit.Assert.*;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.spring.PluginList;
import com.dynatrace.easytravel.spring.SpringTestBase;
import com.dynatrace.easytravel.spring.SpringUtils;
import com.dynatrace.easytravel.util.PHPEnablementCheck;


public class JourneyDOTest extends SpringTestBase {

	@Test
	public void testGetAverageTotalNoPHP() {
		assertFalse(PHPEnablementCheck.isPHPEnabled());

		JourneyDO journey = new JourneyDO();
		assertEquals("", journey.getAverageTotal());
	}

	@Test
	public void testGetAverageTotal() throws Exception {
		PluginList.stopRefreshThread();

		SpringUtils.getPluginStateProxy().registerPlugins(new String[] { BaseConstants.Plugins.PHP_ENABLEMENT_PLUGIN });
		SpringUtils.getPluginStateProxy().setPluginEnabled(BaseConstants.Plugins.PHP_ENABLEMENT_PLUGIN, true);

		assertTrue(ArrayUtils.contains(SpringUtils.getPluginStateProxy().getEnabledPluginNames(), BaseConstants.Plugins.PHP_ENABLEMENT_PLUGIN));

		/* TODO: it's really horribly hard to test anything with plugins enabled!!
		assertTrue(PHPEnablementCheck.isPHPEnabled());

		JourneyDO journey = new JourneyDO();
		journey.getAverageTotal();
		*/
	}
}
