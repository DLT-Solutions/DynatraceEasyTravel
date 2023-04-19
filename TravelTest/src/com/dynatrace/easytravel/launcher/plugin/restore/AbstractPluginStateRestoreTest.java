package com.dynatrace.easytravel.launcher.plugin.restore;


import com.dynatrace.easytravel.launcher.plugin.restore.AbstractPluginStateRestore;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * cwpl-rorzecho
 */
public class AbstractPluginStateRestoreTest {

    @Test
    public void findAndStoreTest() {
        final String[] plugins = {"boot1"};
        final Collection<String> foundPlugins = Arrays.asList(plugins);

        AbstractPluginStateRestore abstractPluginStateRestore = new AbstractPluginStateRestore() {
            @Override
            protected Collection<String> find() {
                return foundPlugins;
            }
        };

        abstractPluginStateRestore.findAndStore(AbstractPluginStateRestore.PluginType.DISABLED_BOOT_PLUGINS);

        Map<Long, String> pluginsTimestamp = abstractPluginStateRestore.getPluginsTimestamp(AbstractPluginStateRestore.PluginType.DISABLED_BOOT_PLUGINS);

        Assert.assertEquals(1, pluginsTimestamp.values().size());

        Assert.assertTrue(pluginsTimestamp.values().containsAll(foundPlugins));

        for (Map.Entry<Long, String> longStringEntry : pluginsTimestamp.entrySet()) {
            Assert.assertTrue(System.nanoTime() > longStringEntry.getKey());
            Assert.assertEquals("boot1", longStringEntry.getValue());
        }
    }

    @Test
    public void storeTest() {
        final String[] plugins = {"plugin1", "plugin2", "plugin3"};
        final Collection<String> foundPlugins = Arrays.asList(plugins);


        AbstractPluginStateRestore abstractPluginStateRestore = new AbstractPluginStateRestore() {
            @Override
            protected Collection<String> find() {
                return foundPlugins;
            }
        };

        abstractPluginStateRestore.findAndStore(AbstractPluginStateRestore.PluginType.ENABLED_USER_PLUGINS);

        Map<Long, String> pluginsTimestamp = abstractPluginStateRestore.getPluginsTimestamp(AbstractPluginStateRestore.PluginType.ENABLED_USER_PLUGINS);

        Assert.assertEquals(3, pluginsTimestamp.values().size());

        Assert.assertTrue(pluginsTimestamp.values().containsAll(foundPlugins));

        int i = 1;
        for (Map.Entry<Long, String> longStringEntry : pluginsTimestamp.entrySet()) {
            Assert.assertTrue(System.nanoTime() > longStringEntry.getKey());
            Assert.assertEquals("plugin" + String.valueOf(i), longStringEntry.getValue());
            i++;
        }

        String[] morePlugins = {"plugin4", "plugin5", "plugin6"};

        abstractPluginStateRestore.store(AbstractPluginStateRestore.PluginType.ENABLED_USER_PLUGINS, Arrays.asList(morePlugins));

        Map<Long, String> morePluginsTimestamp = abstractPluginStateRestore.getPluginsTimestamp(AbstractPluginStateRestore.PluginType.ENABLED_USER_PLUGINS);

        Assert.assertEquals(6, morePluginsTimestamp.values().size());

        Assert.assertTrue(morePluginsTimestamp.values().containsAll(Arrays.asList(morePlugins)));

        int j = 1;
        for (Map.Entry<Long, String> longStringEntry : morePluginsTimestamp.entrySet()) {
            Assert.assertTrue(System.nanoTime() > longStringEntry.getKey());
            Assert.assertEquals("plugin" + String.valueOf(j), longStringEntry.getValue());
            j++;
        }

        String[] newTypeOfPlugins = {"plugin7", "plugin8", "plugin9"};

        abstractPluginStateRestore.store(AbstractPluginStateRestore.PluginType.DISABLED_BOOT_PLUGINS, Arrays.asList(newTypeOfPlugins));

        Map<Long, String> newTypeOfPluginsTimestamp = abstractPluginStateRestore.getPluginsTimestamp(AbstractPluginStateRestore.PluginType.DISABLED_BOOT_PLUGINS);

        Assert.assertEquals(3, newTypeOfPluginsTimestamp.values().size());

        Assert.assertTrue(morePluginsTimestamp.values().containsAll(Arrays.asList(morePlugins)));

        int k = 1;
        for (Map.Entry<Long, String> longStringEntry : morePluginsTimestamp.entrySet()) {
            Assert.assertTrue(System.nanoTime() > longStringEntry.getKey());
            Assert.assertEquals("plugin" + String.valueOf(k), longStringEntry.getValue());
            k++;
        }

        String[] twoTheSamePlugins = {"plugin7", "plugin8", "plugin10"};

        abstractPluginStateRestore.store(AbstractPluginStateRestore.PluginType.DISABLED_BOOT_PLUGINS, Arrays.asList(twoTheSamePlugins));

        Map<Long, String> twoTheSamePluginsTimestamp = abstractPluginStateRestore.getPluginsTimestamp(AbstractPluginStateRestore.PluginType.DISABLED_BOOT_PLUGINS);

        Assert.assertEquals(4, twoTheSamePluginsTimestamp.values().size());

        Assert.assertFalse(morePluginsTimestamp.values().containsAll(Arrays.asList(twoTheSamePlugins)));

        int l = 7;
        for (Map.Entry<Long, String> longStringEntry : twoTheSamePluginsTimestamp.entrySet()) {
            Assert.assertTrue(System.nanoTime() > longStringEntry.getKey());
            Assert.assertEquals("plugin" + String.valueOf(l), longStringEntry.getValue());
            l++;
        }

    }

}
