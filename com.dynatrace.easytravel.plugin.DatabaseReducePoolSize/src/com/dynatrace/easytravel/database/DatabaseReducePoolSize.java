package com.dynatrace.easytravel.database;

import org.apache.tomcat.jdbc.pool.DataSource;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.spring.SpringUtils;


public class DatabaseReducePoolSize extends AbstractGenericPlugin {

    public static final int REDUCED_POOL_SIZE = 3;


    private int oldPoolSize = 0;
    private int oldMaxIdle = 0;
    private int oldMinIdle = 0;
    private int oldMinEvictableIdleTimeMillis = 0;
    private boolean pluginActive = false;

    @Override
    public Object doExecute(String location, Object... context) {
        if (PluginConstants.LIFECYCLE_PLUGIN_ENABLE.equals(location)) {
            reducePoolSize();
        } else if (PluginConstants.LIFECYCLE_PLUGIN_DISABLE.equals(location)) {
            restorePoolSize();
        }
        return null;
    }


    /**
     *
     *
     * @author peter.kaiser
     */
    private synchronized void reducePoolSize() {
        if (!pluginActive) {
            DataSource dataSource = SpringUtils.getBean("propDataSource", DataSource.class);
            oldPoolSize = dataSource.getMaxActive();
            oldMaxIdle = dataSource.getMaxIdle();
            oldMinIdle = dataSource.getMinIdle();
            oldMinEvictableIdleTimeMillis = dataSource.getMinEvictableIdleTimeMillis();
            dataSource.setMaxIdle(REDUCED_POOL_SIZE);
            dataSource.setMinIdle(1);
            dataSource.setMaxActive(REDUCED_POOL_SIZE);
            //checkIdle only releases connections if MinEvictableIdleTimeMillis > 0
            dataSource.setMinEvictableIdleTimeMillis(1);
            dataSource.checkIdle();
            //connections will be released immediately on return if maxidlesize is exceeded
            dataSource.setMinEvictableIdleTimeMillis(0);
            pluginActive = true;
        }
    }


    /**
     *
     *
     * @author peter.kaiser
     */
    private synchronized void restorePoolSize() {
        if (pluginActive) {
            DataSource dataSource = SpringUtils.getBean("propDataSource", DataSource.class);
            dataSource.setMaxActive(oldPoolSize);
            dataSource.setMaxIdle(oldMaxIdle);
            dataSource.setMinIdle(oldMinIdle);
            dataSource.setMinEvictableIdleTimeMillis(oldMinEvictableIdleTimeMillis);
            pluginActive = false;
        }
    }

}
