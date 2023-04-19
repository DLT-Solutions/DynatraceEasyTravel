/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: PluginDependency.java
 * @date: 07.08.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.spring;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.BusinessBackend.Persistence;
import com.dynatrace.easytravel.constants.BaseConstants.SystemProperties;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * This class allows to manage the plugins according their dependencies. For instance, if you are using Cassandra as data store
 * the plugins using Hibernate will fail. Thus, you can previously check whether Hibernate/JPA is available using
 * {@link PluginDependency#JPA#isAvailable()} and avoid such failures.
 * 
 * @author stefan.moschinski
 */
public enum PluginDependency {
	JPA(Persistence.JPA) {

		@Override
		public boolean isAvailable() {
			return isPersistenceModeAvailable(Persistence.JPA);
		}


	},
	CASSANDRA(Persistence.CASSANDRA) {

		@Override
		public boolean isAvailable() {
			return isPersistenceModeAvailable(Persistence.CASSANDRA);
		}
	},
	NONE(BaseConstants.EMPTY_STRING) {

		@Override
		public boolean isAvailable() {
			return true;
		}
	};

	private static final Logger log = Logger.getLogger(PluginDependency.class.getName());

	private String synonym;

	PluginDependency(String synonym) {
		this.synonym = synonym;
	}

	/**
	 * 
	 * @return <code>true</code> if the dependency is available, <code>false</code> if not
	 *         For {@link PluginDependency#NONE} the method always returns <code>true</code>.
	 * @author stefan.moschinski
	 */
	public abstract boolean isAvailable();

	/**
	 * 
	 * @param name plugin dependency name as {@link String}
	 * @return A {@link PluginDependency} that represents the given name or {@link PluginDependency#NONE} if no dependency is
	 *         found.
	 *         That is, the method never returns <code>null</code>.
	 * @author stefan.moschinski
	 */
	public static PluginDependency forName(String name) {
		for (PluginDependency dependency : PluginDependency.values()) {
			if (dependency.synonym.equals(name))
				return dependency;
		}

		log.info(TextUtils.merge("No plugin dependency found for ''{0}''", String.valueOf(name)));
		return NONE;
	}

	/**
	 * 
	 * @param names plugin dependencies that should be converted in their respective {@link PluginDependency} form
	 * @return {@link Iterable} that contains the {@link PluginDependency} representations
	 *         according to the passed plugin dependency names
	 * @author stefan.moschinski
	 */
	public static Iterable<PluginDependency> forNames(String... names) {
		if (ArrayUtils.isEmpty(names)) {
			Collections.emptySet();
		}

		Set<PluginDependency> dependencies = EnumSet.noneOf(PluginDependency.class);
		for (String name : names) {
			PluginDependency dependency = forName(name);
			if (dependency != NONE)
				dependencies.add(dependency);
		}
		return Collections.unmodifiableCollection(dependencies);
	}

	@Override
	public String toString() {
		return synonym;
	}

	private static boolean isPersistenceModeAvailable(String persistenceIdentifier) {
		boolean persistenceAvailable = persistenceIdentifier.equals(getPersistenceModeSystemProp());
		if (log.isLoggable(Level.FINE)) {
			log.fine(TextUtils.merge(
					"Persistence dependency ''{0}'' is {1}available, because persistence mode is set to ''{2}''",
					persistenceIdentifier,
					persistenceAvailable ? BaseConstants.EMPTY_STRING : "not ",
					String.valueOf(getPersistenceModeSystemProp())));
		}

		return persistenceAvailable;
	}

	private static String getPersistenceModeSystemProp() {
		return System.getProperty(SystemProperties.PERSISTENCE_MODE);
	}


}
