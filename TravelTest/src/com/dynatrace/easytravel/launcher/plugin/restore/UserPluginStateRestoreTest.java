package com.dynatrace.easytravel.launcher.plugin.restore;

import com.dynatrace.easytravel.spring.PluginInfoList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static com.dynatrace.easytravel.launcher.plugin.restore.AbstractPluginStateRestore.*;

/**
 * cwpl-rorzecho
 */
public class UserPluginStateRestoreTest {
    private static String[] bootPlugins = {"boot1", "boot2", "boot3", "boot4", "boot5", "scenario1", "scenario2"};
    private static String scenarioRestorePoint = "UEM,Standard";

    private static UserPluginStateRestore userPluginStateRestore;


    static {
        RestorePointHolder.init(bootPlugins, scenarioRestorePoint);
        userPluginStateRestore = new UserPluginStateRestore();
    }

    @Before
    public void setUp() {

    }

    @Test
    public void noUserSelectedPattern() {
        String[] enabledPlugins = {"boot1", "boot2", "boot3", "boot4", "boot5", "scenario1", "scenario2"};

        PluginInfoList pluginInfoList = new PluginInfoList(enabledPlugins);

        userPluginStateRestore.setPluginInfoList(pluginInfoList);

        userPluginStateRestore.findAndStore(PluginType.ENABLED_USER_PLUGINS);

        Collection<String> userPlugins = userPluginStateRestore.getUserPlugins();

        Assert.assertEquals(0, userPlugins.size());

    }

    @Test
    public void userEnablesOnePattern() {
        String[] enabledPlugins = {"boot1", "boot2", "boot3", "boot4", "boot5", "scenario1", "scenario2", "user1"};

        PluginInfoList pluginInfoList = new PluginInfoList(enabledPlugins);

        userPluginStateRestore.setPluginInfoList(pluginInfoList);

        userPluginStateRestore.findAndStore(PluginType.ENABLED_USER_PLUGINS);

        Collection<String> userPlugins = userPluginStateRestore.getUserPlugins();

        Assert.assertEquals(1, userPlugins.size());

        Assert.assertEquals("user1",userPluginStateRestore.getUserPlugins().toArray()[0]);
    }

    @Test
    public void userEnablesTwoPattern() {
        String[] enabledPlugins = {"boot1", "boot2", "boot3", "boot4", "boot5", "scenario1", "scenario2", "user1", "user2"};

        PluginInfoList pluginInfoList = new PluginInfoList(enabledPlugins);

        userPluginStateRestore.setPluginInfoList(pluginInfoList);

        userPluginStateRestore.findAndStore(PluginType.ENABLED_USER_PLUGINS);

        Collection<String> userPlugins = userPluginStateRestore.getUserPlugins();

        Assert.assertEquals(2, userPlugins.size());

        Assert.assertEquals("user1",userPluginStateRestore.getUserPlugins().toArray()[0]);
        Assert.assertEquals("user2",userPluginStateRestore.getUserPlugins().toArray()[1]);
    }

    @Test
    public void userDisablesOnePattern() {
        String[] enabledPlugins = {"boot1", "boot2", "boot3", "boot4", "boot5", "scenario1", "scenario2", "user2"};

        PluginInfoList pluginInfoList = new PluginInfoList(enabledPlugins);

        userPluginStateRestore.setPluginInfoList(pluginInfoList);

        userPluginStateRestore.findAndStore(PluginType.ENABLED_USER_PLUGINS);

        Collection<String> userPlugins = userPluginStateRestore.getUserPlugins();

        Assert.assertEquals(1, userPlugins.size());

        Assert.assertEquals("user2",userPluginStateRestore.getUserPlugins().toArray()[0]);

    }

}
