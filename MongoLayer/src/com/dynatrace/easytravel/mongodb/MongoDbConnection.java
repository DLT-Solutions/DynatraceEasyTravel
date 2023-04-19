/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MongoDbConnection.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.logging.LoggingSuppresser;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import ch.qos.logback.classic.Logger;

/**
 *
 * @author stefan.moschinski
 */
public class MongoDbConnection {
	private final MongoClient mongo;
	
	private static final Logger log = LoggerFactory.make();

	public static MongoDbConnection openConnection(String... addresses) {
		Validate.notEmpty(addresses, "The passed MongoDB addresses must not be empty");
		return new MongoDbConnection(addresses);
	}

	private MongoDbConnection(String... addresses) {
		String mongoUser = EasyTravelConfig.read().mongoDbUser;
		String mongoPassword = EasyTravelConfig.read().mongoDbPassword;
		String mongoAuthDB = EasyTravelConfig.read().mongoDbAuthDatabase;
		MongoClientOptions mongoClientOptions = new MongoClientOptions.Builder().socketKeepAlive(true)
		.socketTimeout((int) TimeUnit.SECONDS.toMillis(10)).build();
		
		List<ServerAddress> mongoAddresses = getAsMongoServerAddresses(addresses);
		if (StringUtils.isNotBlank(mongoUser) && StringUtils.isNotBlank(mongoPassword) && StringUtils.isNotBlank(mongoAuthDB)) {
			List<MongoCredential> mongoCredentials = new ArrayList<MongoCredential>(Arrays
					.asList(MongoCredential.createCredential(mongoUser, mongoAuthDB, mongoPassword.toCharArray())));

			mongo = new MongoClient(mongoAddresses, mongoCredentials, mongoClientOptions);

		} else {
			log.warn("No user, password or authDB provided for MongoDB");
			mongo = new MongoClient(mongoAddresses, mongoClientOptions);
		}

	}

	private List<ServerAddress> getAsMongoServerAddresses(String[] addressess) {
		return FluentIterable.from(Arrays.asList(addressess)).transform(new Function<String, ServerAddress>() {

			@Override
			public ServerAddress apply(String input) {
				return new ServerAddress(input);
			}

		}).toList();
	}

	public DB getDatabase(String dbName) {
		return mongo.getDB(dbName);
	}

	public void dropDatabase(String dbName) {
		getDatabase(dbName).dropDatabase();
	}

	public void closeConnection() {
		mongo.close();
	}

	/**
	 * Shutdown the mongodb instance(s) to which we are connected
	 *
	 * @author stefan.moschinski
	 */
	public void shutdownDatabase() {
		// filter ugly MongoDB exception log happening when shutdown MongoDB
		LoggingSuppresser suppresser = new LoggingSuppresser("com.mongodb");
		try {
			suppresser.addLogPatternToSuppress("emptying DBPortPool to").suppressLogging();
			DB database = getDatabase("admin");
			CommandResult result = database.command(new BasicDBObject("shutdown", 1));
			log.debug("Mongo shutdown result: " + result);
		} catch (com.mongodb.MongoException e) {
			// can happen
		} finally {
			suppresser.endSuppressLogging();
		}
	}
}
