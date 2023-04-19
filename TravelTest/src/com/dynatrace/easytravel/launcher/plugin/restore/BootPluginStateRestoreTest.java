package com.dynatrace.easytravel.launcher.plugin.restore;


import com.dynatrace.easytravel.spring.PluginInfoList;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

/**
 * Created by cwpl-rorzecho on 2014-09-16.
 */
public class BootPluginStateRestoreTest {
    private static String[] bootPlugins = {"boot1", "boot2", "boot3", "boot4", "boot5", "scenario1", "scenario2"};
    private static String scenarioRestorePoint = "UEM,Standard";

    private static BootPluginStateRestore bootPluginStateRestore;


    static {
        RestorePointHolder.init(bootPlugins, scenarioRestorePoint);
        bootPluginStateRestore = new BootPluginStateRestore();
    }

    @Test
    public void noBootDeselectedPattern() {
        String[] enabledPlugins = {"boot1", "boot2", "boot3", "boot4", "boot5", "scenario1", "scenario2"};

        PluginInfoList pluginInfoList = new PluginInfoList(enabledPlugins);

        bootPluginStateRestore.setPluginInfoList(pluginInfoList);

        bootPluginStateRestore.findAndStore(AbstractPluginStateRestore.PluginType.DISABLED_BOOT_PLUGINS);

        Collection<String> disabledBootPlugins = bootPluginStateRestore.getDisabledBootPlugins();

        Assert.assertEquals(0, disabledBootPlugins.size());

    }

    @Test
    public void userOneDeselectedBootPlugin() {
        String[] enabledPlugins = {"boot1", "boot3", "boot4", "boot5", "scenario1", "scenario2"};

        PluginInfoList pluginInfoList = new PluginInfoList(enabledPlugins);

        bootPluginStateRestore.setPluginInfoList(pluginInfoList);

        bootPluginStateRestore.findAndStore(AbstractPluginStateRestore.PluginType.DISABLED_BOOT_PLUGINS);

        Collection<String> disabledBootPlugins = bootPluginStateRestore.getDisabledBootPlugins();

        Assert.assertEquals(1, disabledBootPlugins.size());

        Assert.assertEquals("boot2",bootPluginStateRestore.getDisabledBootPlugins().toArray()[0]);
    }

    @Test
    public void userTwoDeselectedBootPlugins() {
        String[] enabledPlugins = {"boot1", "boot4", "boot5", "scenario1", "scenario2", "user1", "user2"};

        PluginInfoList pluginInfoList = new PluginInfoList(enabledPlugins);

        bootPluginStateRestore.setPluginInfoList(pluginInfoList);

        bootPluginStateRestore.findAndStore(AbstractPluginStateRestore.PluginType.DISABLED_BOOT_PLUGINS);

        Collection<String> disabledBootPlugins = bootPluginStateRestore.getDisabledBootPlugins();

        Assert.assertEquals(2, disabledBootPlugins.size());

        Assert.assertEquals("boot2",bootPluginStateRestore.getDisabledBootPlugins().toArray()[0]);
        Assert.assertEquals("boot3",bootPluginStateRestore.getDisabledBootPlugins().toArray()[1]);
    }

    @Test
    public void userEnablesOneBootPlugin() {
        String[] enabledPlugins = {"boot1", "boot3", "boot4", "boot5", "scenario1", "scenario2", "user2"};

        PluginInfoList pluginInfoList = new PluginInfoList(enabledPlugins);

        bootPluginStateRestore.setPluginInfoList(pluginInfoList);

        bootPluginStateRestore.findAndStore(AbstractPluginStateRestore.PluginType.DISABLED_BOOT_PLUGINS);

        Collection<String> userPlugins = bootPluginStateRestore.getDisabledBootPlugins();

        Assert.assertEquals(1, userPlugins.size());

        Assert.assertEquals("boot2", bootPluginStateRestore.getDisabledBootPlugins().toArray()[0]);
    }
}
