package com.dynatrace.easytravel.database;

import javax.persistence.Query;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.persistence.JpaDatabaseController;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;

public class DisableHibernateCache extends AbstractGenericPlugin {

	private static Logger log = LoggerFactory.make();
	private boolean evictCache = false;

	@Override
	public Object doExecute(String location, Object... context) {
		if (location.equals(PluginConstants.LIFECYCLE_PLUGIN_ENABLE)) {
			evictCache = true;
		} else if (location.equals(PluginConstants.DATAACESS_INTERCEPT_QUERY)) {
			if (evictCache) {
				evictCache(context);
				evictCache = false;
			}

			disableQueryCache((Query) context[0]);
		}
		return null;
	}

	private void evictCache(Object[] context) {
		for (Object object : context) {
			if (object instanceof JpaDatabaseController) {
				log.info("Evicting cache");
				((JpaDatabaseController) object).evictCache();
			}
		}
	}

	private void disableQueryCache(Query query) {
		query.setHint("org.hibernate.cacheable", false);
	}

}
