package com.dynatrace.easytravel.launcher.plugin.restore;


import org.junit.Assert;
import org.junit.Test;

/**
 * Created by cwpl-rorzecho on 2014-09-16.
 */
public class RestorePointHolderTest {

    private static String[] bootPlugins = {"boot1", "boot2", "boot3", "boot4", "boot5", "scenario1", "scenario2"};
    private static String scenarioRestorePoint = "UEM,Standard";


    @Test
    public void restorePointHolderTest() {
        Assert.assertTrue(RestorePointHolder.getInstance() == null);

        RestorePointHolder.init(bootPlugins, scenarioRestorePoint);

        Assert.assertFalse(RestorePointHolder.getInstance() == null);

        Assert.assertTrue(RestorePointHolder.getInstance().getPluginRestorePoint().equals(bootPlugins));

        Assert.assertTrue(RestorePointHolder.getInstance().getScenarioRestorePoint().equals(scenarioRestorePoint));
    }

}
