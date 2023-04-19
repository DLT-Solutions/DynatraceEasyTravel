package com.dynatrace.easytravel.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import com.dynatrace.easytravel.cassandra.CassandraDatabase;
import com.dynatrace.easytravel.constants.BaseConstants.BusinessBackend;
import com.dynatrace.easytravel.constants.BaseConstants.BusinessBackend.Persistence;
import com.dynatrace.easytravel.constants.BaseConstants.SystemProperties;
import com.dynatrace.easytravel.hbase.HbaseDatabase;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.model.GenericDataAccess;
import com.dynatrace.easytravel.mongodb.MongoDb;
import com.dynatrace.easytravel.persistence.Database;
import com.dynatrace.easytravel.persistence.SqlDatabase;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;

public class QueryLocations extends AbstractGenericPlugin {

	private static Logger log = LoggerFactory.make();

	private Random random = new Random(System.currentTimeMillis());

	// The plugin supports multiple modes where it behaves differently
	private enum Mode {
		heavy, light, random
	}

	// these two are set in the .ctx.xml...
	private Mode mode = Mode.light;
	//private String lightQuery = "";


	public void setMode(String mode) {
		this.mode = Mode.valueOf(mode);
	}

	/*
	 * Sets the query, it expects to retrieve the name as query-param.
	 */
	public void setLightQuery(String lightQuery) {
		//this.lightQuery = lightQuery;
	}


	@Override
	public Object doExecute(String location, Object... context) {
		// clean up when the plugin is closed
		if (location.equals(PluginConstants.LIFECYCLE_PLUGIN_DISABLE)) {
			if(sqlDatabase != null) {
				sqlDatabase.closeConnection();
				sqlDatabase = null;
			}
			return null;
		}

//		EasyTravelConfig config = EasyTravelConfig.read();
		String persistenceMode = System.getProperty(SystemProperties.PERSISTENCE_MODE);

		if (log.isDebugEnabled())
			log.debug("DatabaseAccessPlugin: Had extension point: " + location + ", context: " + Arrays.toString(context));

		List<String> retList = new ArrayList<String>();
		switch (mode) {
			case heavy: {
				Database database = createDatabase(persistenceMode);
				DataAccess access = new GenericDataAccess(database.createNewBusinessController());
				try {
					// call database directly from here, Architectural problem!
					if (BusinessBackend.Persistence.CASSANDRA.equals(persistenceMode)) {
						log.info("Requesting some data from Cassandra");

						Collection<Location> locations = access.getLocations(0, Integer.MAX_VALUE);
						for (Location l : locations) {
							retList.add(l.getName());
						}

						// to ensure that we see access to Cassandra, Locations seem to be cached differently...
						access.getUser("monica");
						access.getUser("george");

						access.findJourneys("Beijing", null, null, false);
					} else {
						Collection<Location> locations = access.getLocations(0, Integer.MAX_VALUE);
						for (Location l : locations) {
							retList.add(l.getName());
						}
					}
				} finally {
					access.close();
					database.closeConnection();
				}

				break;
			}
			case light: {
				Database database = createDatabase(persistenceMode);
				DataAccess access = new GenericDataAccess(database.createNewBusinessController());
				try {
					// context is Object[] {destination, fromDate, toDate}
					List<Location> locations = new ArrayList<Location>(access.getMatchingLocations((String) context[0]));
					for (Location l : locations) {
						retList.add(l.getName());
					}
				} finally {
					access.close();
					database.closeConnection();
				}

				break;
			}
			case random: {
				// ensure that we initialize only once
				synchronized (this) {
					if(emf == null) {
						log.info("Creating Enterprise Manager for database access");
						sqlDatabase = new SqlDatabaseExtension();
						emf = sqlDatabase.getBusinessEntityManagerFactory();
					}
				}

				// EntityManager is not thread-safe, therefore create it every time
				EntityManager em = emf.createEntityManager();		// NOSONAR - emf is thread-safe, we only want to synchronize creation of object
				try {
					em.getTransaction().begin();
					// first run the query a few times to get some random SQLs into the string cache, it will not result in any data usually
					for(int i = 0;i < 20;i++) {
						TypedQuery<Location> query = em.createQuery("select b from Location b where 0 = " + random.nextInt(), Location.class);

						// disable cache to not go OOM because of it
						query.setHint("org.hibernate.cacheable", false);
						query.getResultList();
					}

					// the do the query once to get the actual data
					TypedQuery<Location> query = em.createQuery("select b from Location b", Location.class);
					Collection<Location> locations = query.getResultList();
					for (Location l : locations) {
						retList.add(l.getName());
					}

					em.flush();
					em.getTransaction().commit();
				} finally {
					em.close();
				}

				break;
			}
		}

		return retList;
	}

	private SqlDatabaseExtension sqlDatabase;
	private EntityManagerFactory emf;

	private final class SqlDatabaseExtension extends SqlDatabase {
		@Override
		public EntityManagerFactory getBusinessEntityManagerFactory() {	// NOPMD - false positive, we make the method visible here, otherwise it is protected and thus not callable
			return super.getBusinessEntityManagerFactory();
		}
	}

	// create factory class if more need this functionality!
	private static Database createDatabase(String persistenceMode) {
		if (persistenceMode == null || BusinessBackend.Persistence.JPA.equals(persistenceMode)) {
			return new SqlDatabase();
		}
		if (Persistence.CASSANDRA.equals(persistenceMode)) {
			return new CassandraDatabase();
		}
		if (Persistence.MONGODB.equals(persistenceMode)) {
			return new MongoDb();
		}
		if (Persistence.HBASE.equals(persistenceMode)) {
			return new HbaseDatabase();
		}

		throw new IllegalArgumentException(String.format("The passed persistence mode '%s' is unknown", persistenceMode));
	}

}
