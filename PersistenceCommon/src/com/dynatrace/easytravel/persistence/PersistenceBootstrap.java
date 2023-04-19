/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: PersistenceBootstrap.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;


import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;


/**
 * 
 * @author stefan.moschinski
 */
public class PersistenceBootstrap implements DataContextProvider {

	private final Database database;


	public PersistenceBootstrap(Database database) {
		this.database = Preconditions.checkNotNull(database.initialize(database.getBusinessController()));
	}

	@Override
	public Database getInitializedDatabase(long timeout, TimeUnit unit) {
		return database;
	}


	// I have thought about integrating multiple database however it seems somewhat hard to realize ===> Please leave the dead for
// now

//	private final Future<DataAccessControllers> persistFuture;

//	public PersistenceBootstrap(DataProviderCfg... cfgs) {
////		Multimap<Database, DataProviderCfg> persistenceProviderCfgs = mapCfgsToDatabase(cfgs);
////		this.persistFuture = initializeDatabasesAsync(persistenceProviderCfgs);
//		
//		controllers = Preconditions.checkNotNull(cfgs[0].getDatabase().initialize());
//	}


//	private Multimap<Database, DataProviderCfg> mapCfgsToDatabase(DataProviderCfg... cfgs) {
//		Multimap<Database, DataProviderCfg> map = HashMultimap.create(1, cfgs.length);
//
//		for (DataProviderCfg persistenceProviderConfig : cfgs) {
//			map.put(persistenceProviderConfig.getDatabase(), persistenceProviderConfig);
//		}
//		return map;
//	}
//
//
//


//	private Future<DataAccessControllers> initializeDatabasesAsync(final Multimap<Database, ? extends DataProviderCfg> dataProviderCfgs) {
//		ExecutorService executor = Executors.newCachedThreadPool();
//		final Collection<Future<DataAccessControllers>> initializingDatabases = Lists.newArrayListWithCapacity(dataProviderCfgs.size());
//
//		final EasyTravelConfig cfg = EasyTravelConfig.read();
//
//		for (final Database database : dataProviderCfgs.keySet()) {
//			log.info(format("Initializing datatabase '%s'", database));
//			Future<DataAccessControllers> result = executor.submit(new Callable<DataAccessControllers>() {
//
//				@Override
//				public DataAccessControllers call() throws Exception {
//					return database.initialize();
//				}
//
//			});
//			initializingDatabases.add(result);
//		}
//
//		return executor.submit(new Callable<DataAccessControllers>() {
//
//			@Override
//			public DataAccessControllers call() throws Exception {
//				return getBusinessContext(dataProviderCfgs, initializingDatabases);
//			}
//
//			protected DataAccessControllers getBusinessContext(final Multimap<Database, ? extends DataProviderCfg> dataProviderCfgs,
//					final Collection<Future<DataAccessControllers>> startingDatabases)
//					throws InterruptedException, ExecutionException
//			{
//				while (!startingDatabases.isEmpty())
//				{
//					Iterator<Future<DataAccessControllers>> iterator = startingDatabases.iterator();
//					while (iterator.hasNext())
//					{
//						Future<Database> databaseFuture = iterator.next();
//						if (databaseFuture.isDone())
//						{
//							Database database = databaseFuture.get();
//							Collection<? extends DataProviderCfg> configs = dataProviderCfgs.get(database);
//							businessDataCtx.setProvidersForDatabase(configs, database);
//							iterator.remove();
//						}
//					}
//
//					if (!startingDatabases.isEmpty())
//					{
//						log.info(format("Initialization of database(s) '%s' is still pending - sleeping another 500ms",
//								startingDatabases));
//						TimeUnit.MILLISECONDS.sleep(500);
//					}
//				}
//				return businessDataCtx;
//			}
//		});
//	}

}
