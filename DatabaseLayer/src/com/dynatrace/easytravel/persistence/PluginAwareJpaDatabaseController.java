/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: PluginAwareJpaDatabaseController.java
 * @date: 09.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.dynatrace.easytravel.jpa.QueryOverride;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.spring.PluginList;

import ch.qos.logback.classic.Logger;


/**
 * Note: This class is instantiated via Spring via the @Autowired annotation in {@link SpringAwareSqlDatabase}.
 *
 * @author stefan.moschinski
 */
@Repository
public class PluginAwareJpaDatabaseController extends JpaDatabaseController {

	private static final Logger log = LoggerFactory.make();

	private GenericPluginList plugins = new GenericPluginList(PluginConstants.DATAACCESS);
	private final PluginList<QueryOverride> queryOverridePlugins = new PluginList<QueryOverride>(QueryOverride.class);

	public PluginAwareJpaDatabaseController() {
		// constructor for spring
	}


	@PersistenceContext
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}


	/**
	 * @return the plugins
	 */
	public GenericPluginList getPlugins() {
		return plugins;
	}


	@Override
	protected void doIntercept(Query query) {
		super.doIntercept(query);
		plugins.execute(PluginConstants.DATAACESS_INTERCEPT_QUERY, query, /* pass */this /*
																						 * so plug can evict its cache if
																						 * necessary
																						 */);
	}

	@Override
	protected Query getActualQuery(String queryName, Class<?> clazz) {
		for (QueryOverride plugin : queryOverridePlugins) {
			if (plugin.getQueryName().equals(queryName)) {
				if (log.isDebugEnabled())
					log.debug("Overriding query " + queryName + " with: " + plugin.getQueryText());
				return plugin.isNative() ? createNativeQuery(plugin.getQueryText(), clazz) : createQuery(
						plugin.getQueryText(), clazz);
			}
		}
		return super.getActualQuery(queryName, clazz);
	}


}
