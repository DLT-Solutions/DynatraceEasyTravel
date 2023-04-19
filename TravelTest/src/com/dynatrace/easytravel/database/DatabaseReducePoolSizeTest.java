package com.dynatrace.easytravel.database;

import static org.junit.Assert.assertEquals;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.spring.SpringUtils;


public class DatabaseReducePoolSizeTest extends DatabaseWithContent {
    //private static final Logger log = LoggerFactory.make();

    @Before
    public void init() {
        System.setProperty("com.dynatrace.easytravel.propertiesfile", Thread.currentThread().getContextClassLoader().getResource(EasyTravelConfig.PROPERTIES_FILE + ".properties").toString());
		System.setProperty(BaseConstants.SystemProperties.PERSISTENCE_MODE, BaseConstants.BusinessBackend.Persistence.JPA);
        SpringUtils.initBusinessBackendContext();
    }


    @After
    public void dispose() {
        SpringUtils.disposeBusinessBackendContext();
    }

    @Test
    public void testDatabaseReducePoolSize() throws Exception {
        DatabaseReducePoolSize plugin = new DatabaseReducePoolSize();
        plugin.setExtensionPoint(new String[] { PluginConstants.LIFECYCLE_PLUGIN_ENABLE, PluginConstants.LIFECYCLE_PLUGIN_DISABLE });
        plugin.setEnabled(true);

        DataSource dataSource = SpringUtils.getBean("propDataSource", DataSource.class);
        int initialPoolSize =  dataSource.getMaxActive();
        checkPool(dataSource, initialPoolSize, false);
        plugin.execute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE);
        dataSource = SpringUtils.getBean("propDataSource", DataSource.class);
        assertEquals(DatabaseReducePoolSize.REDUCED_POOL_SIZE, dataSource.getMaxActive());
        checkPool(dataSource, initialPoolSize, true);
        plugin.execute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE);
        checkPool(dataSource, initialPoolSize, false);
        dataSource = SpringUtils.getBean("propDataSource", DataSource.class);
        assertEquals(initialPoolSize, dataSource.getMaxActive());
        Thread.sleep(2000);
    }


    private void checkPool(DataSource dataSource, int initialPoolSize, boolean reduced) throws InterruptedException {
        DatabaseAccessPoolContention contentionPlugin = new DatabaseAccessPoolContention();
        contentionPlugin.setExtensionPoint(new String[] { PluginConstants.BACKEND_LOCATION_SEARCH });
        contentionPlugin.setEnabled(true);
        for (int i = 0; i < initialPoolSize; i++) {
            contentionPlugin.execute(PluginConstants.BACKEND_LOCATION_SEARCH);
        }
        Thread.sleep(2000 / 2);
        if (reduced) {
            assertEquals(DatabaseReducePoolSize.REDUCED_POOL_SIZE, dataSource.getActive());
        } else {
            assertEquals(initialPoolSize, dataSource.getActive());
        }
        while (dataSource.getActive() > 0) {
            Thread.sleep(100);
        }
    }
}
