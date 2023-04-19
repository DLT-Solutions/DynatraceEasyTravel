package com.dynatrace.easytravel;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.frontend.beans.JourneyAccount;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.Plugin;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.spring.SpringTestBase;
import com.dynatrace.easytravel.spring.SpringUtils;

public class TravellersOptionBoxPluginTest  extends SpringTestBase {

	private static final Logger log = LoggerFactory.make();

	private static final String TRAVELLERS_OPTION_BOX = "TravellersOptionBox";
    private static final double AMOUNT = 100;

    @Test
    public void testJourneyAccountWithPluginOkWith4Travellers() throws InterruptedException {
        JourneyAccount journeyAccount = new JourneyAccount();
        JourneyDO journey = new JourneyDO(1, "journey_name", Calendar.getInstance(), Calendar.getInstance(), "departure", "destination", "tenant", AMOUNT, null);
        journeyAccount.setJourney(journey);
        journeyAccount.setTravellers(4);

		// add Plugin which enables the problem
        registerPlugin();

        // wait some time to ensure that the plugin is actually enabled
        //Thread.sleep(10500);

        log.info("Calculating cost for 4 travellers with plugin now");

        assertEquals(233, journeyAccount.getTotalCosts(), 0.1);
    }

    @Test
    public void testJourneyAccountWithPluginFailsWith6Travellers() throws InterruptedException {
        JourneyAccount journeyAccount = new JourneyAccount();
        JourneyDO journey = new JourneyDO(1, "journey_name", Calendar.getInstance(), Calendar.getInstance(), "departure", "destination", "tenant", AMOUNT, null);
        journeyAccount.setJourney(journey);
        journeyAccount.setTravellers(6);

        registerPlugin();

        // wait some time to ensure that the plugin is actually enabled
        //Thread.sleep(10500);

        log.info("Calculating cost for 6 travellers with plugin now");

        try {
        	journeyAccount.getTotalCosts();
        	fail("Should cause a failure now");
        } catch (RuntimeException e) {
        	assertTrue("Had: " + ExceptionUtils.getStackTrace(e),
        			e.getCause() instanceof ArrayIndexOutOfBoundsException);
        }
    }

	private static void registerPlugin() {
        log.info("Registering plugin");

		TravellersAccount travellersAccount = new TravellersAccount();
        travellersAccount.setName(TRAVELLERS_OPTION_BOX);
        travellersAccount.setGroupName("Http Errors");
        travellersAccount.setExtensionPoint(new String[] {PluginConstants.FRONTEND_TRAVELLERS_ACCOUNT});

        SpringUtils.getPluginHolder().addPlugin(travellersAccount);
        SpringUtils.getPluginStateProxy().setPluginEnabled(travellersAccount.getName(), true);
        SpringUtils.getPluginStateProxy().registerPlugins(new String[] {TRAVELLERS_OPTION_BOX});

        List<Plugin> plugins = SpringUtils.getPluginHolder().getPlugins();
        expectPlugins(plugins);

        String[] pluginNames = SpringUtils.getPluginStateProxy().getAllPluginNames();
		assertTrue("Had: " + Arrays.toString(pluginNames),
        		ArrayUtils.contains(pluginNames, TRAVELLERS_OPTION_BOX));

        pluginNames = SpringUtils.getPluginStateProxy().getAllPlugins();
		assertTrue("Had: " + Arrays.toString(pluginNames),
        		ArrayUtils.contains(pluginNames, TRAVELLERS_OPTION_BOX + ":Http Errors:Both"));

        pluginNames = SpringUtils.getPluginStateProxy().getEnabledPluginNames();
		assertTrue("Had: " + Arrays.toString(pluginNames),
        		ArrayUtils.contains(pluginNames, TRAVELLERS_OPTION_BOX));

        pluginNames = SpringUtils.getPluginStateProxy().getEnabledPlugins();
		assertTrue("Had: " + Arrays.toString(pluginNames),
        		ArrayUtils.contains(pluginNames, TRAVELLERS_OPTION_BOX + ":Http Errors:Both"));

		pluginNames = SpringUtils.getPluginStateProxy().getAllPluginNames();
		assertTrue("Had: " + Arrays.toString(pluginNames),
        		ArrayUtils.contains(pluginNames, TRAVELLERS_OPTION_BOX));
		pluginNames = SpringUtils.getPluginStateProxy().getAllPlugins();
		assertTrue("Had: " + Arrays.toString(pluginNames),
        		ArrayUtils.contains(pluginNames, TRAVELLERS_OPTION_BOX + ":Http Errors:Both"));
		pluginNames = SpringUtils.getPluginStateProxy().getEnabledPlugins();
		assertTrue("Had: " + Arrays.toString(pluginNames),
        		ArrayUtils.contains(pluginNames, TRAVELLERS_OPTION_BOX + ":Http Errors:Both"));
		pluginNames = SpringUtils.getPluginStateProxy().getEnabledPluginNames();
		assertTrue("Had: " + Arrays.toString(pluginNames),
        		ArrayUtils.contains(pluginNames, TRAVELLERS_OPTION_BOX));
	}

	private static void expectPlugins(List<Plugin> plugins) {
		boolean found = false;
        StringBuilder pluginText = new StringBuilder();
        for(Plugin plugin : plugins) {
        	if(plugin.getName().equals(TRAVELLERS_OPTION_BOX)) {
        		found = true;
        	}
        	pluginText.append(plugin.getName()).append(",");
        }
        assertTrue(pluginText.toString(), found);
	}
}
