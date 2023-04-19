package com.dynatrace.easytravel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.jpa.business.Tenant;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.misc.CommonUser;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.model.GenericDataAccess;
import com.dynatrace.easytravel.model.LoyaltyStatus;
import com.dynatrace.easytravel.persistence.Database;
import com.dynatrace.easytravel.persistence.controller.BusinessDatabaseController;

import ch.qos.logback.classic.Logger;

/**
 * this class is not threadsafe create must only be called once
 *
 *
 * @author peter.kaiser
 */
public class CreateDatabaseContent {

	private static final String HAINER = "hainer";
	private static final String MONICA = "monica";
	private static final String GEORGE = "george";

	private static final Logger log = LoggerFactory.make();

	private static final int NO_JOURNEYS = 10000;

	private static final int NO_MONICA_LOGINS = 1000;
	private static final int NO_MONICA_BOOKINGS = 500;
	private static final int NO_GEORGE_LOGINS = 1000;
	private static final int NO_GEORGE_BOOKINGS = 500;

	private final Database database;
	private final boolean randomContent;
	private DataAccess access;

	private Thread randomJourneysThread;


	public CreateDatabaseContent(Database database, boolean randomContent) {
		this.database = database;
		this.randomContent = randomContent;
	}

	/**
	 * this class is not thread-safe and starts a new thread which will probably
	 * cause problems when this method gets invoked more than once.
	 *
	 * @throws IOException
	 *
	 */
	public void create() throws IOException {
		BusinessDatabaseController internalController = null;
		try {
			internalController = database.createNewBusinessController();
			this.access = new GenericDataAccess(internalController);
			createBusiness();
			int timeout = getEnv("journey.creation.timeout", 90);
			waitForAsyncJourneyCreationTask(timeout, TimeUnit.SECONDS);
		} catch (Throwable t) { // NOSONAR - on purpose here to close the
								// connection in case of errors
			// we need to make sure that the internalController is closed in
			// exception case
			if (internalController != null) {
				internalController.close();
			}
			throw new RuntimeException(t);
		}
		createSleepProcedure();
		createDelayFunction();
	}

	public int getEnv(String name, int def) {
		String sValue = System.getenv(name);
		if (sValue != null) {
			try {
				return Integer.parseInt(sValue);
			} catch (NumberFormatException nf) {
				log.info("Cannot parse env variable to int. Name: " + name + " value " + sValue, nf);
				return def;
			}
		}
		return def;
	}

	public void close() {
		if (access != null) {
			access.close();
			access = null;
		}
	}

	private static String utf8_general_ci(String s) {
		if (s == null) {
			return null;
		}
		return s.replace('Š', 'S').replace('š', 's').replace('Ð', 'D').replace('Ž', 'Z').replace('ž', 'z')
				.replace('À', 'A').replace('Á', 'A').replace('Â', 'A').replace('Ã', 'A').replace('Ä', 'A')
				.replace('Å', 'A').replace('Æ', 'A').replace('Ç', 'C').replace('È', 'E').replace('É', 'E')
				.replace('Ê', 'E').replace('Ë', 'E').replace('Ì', 'I').replace('Í', 'I').replace('Î', 'I')
				.replace('Ï', 'I').replace('Ñ', 'N').replace('Ò', 'O').replace('Ó', 'O').replace('Ô', 'O')
				.replace('Õ', 'O').replace('Ö', 'O').replace('Ø', 'O').replace('Ù', 'U').replace('Ú', 'U')
				.replace('Û', 'U').replace('Ü', 'U').replace('Ý', 'Y').replace('Þ', 'B').replace('ß', 's')
				.replace('à', 'a').replace('á', 'a').replace('â', 'a').replace('ã', 'a').replace('ä', 'a')
				.replace('å', 'a').replace('æ', 'a').replace('ç', 'c').replace('è', 'e').replace('é', 'e')
				.replace('ê', 'e').replace('ë', 'e').replace('ì', 'i').replace('í', 'i').replace('î', 'i')
				.replace('ï', 'i').replace('ð', 'o').replace('ñ', 'n').replace('ò', 'o').replace('ó', 'o')
				.replace('ô', 'o').replace('õ', 'o').replace('ö', 'o').replace('ø', 'o').replace('ù', 'u')
				.replace('ú', 'u').replace('û', 'u').replace('ý', 'y').replace('ý', 'y').replace('þ', 'b')
				.replace('ÿ', 'y').replace('ƒ', 'f');
	}


	@SuppressWarnings("deprecation")
	private void createBusiness() throws IOException {
		long start = System.currentTimeMillis();

		access.startTransaction();

		access.createTenant("Speed Travel Agency", "sta", "Online and offline travel booking.");
		access.createTenant("Personal Travel Inc.", "pti", "Customzied travel offerings.");
		access.createTenant("Thomas Chef", "tch", "Worldwide provider of travels.");
		access.createTenant("TravelNiche Ltd.", "tni", "Your provider for customized travel offerings.");

		log.info("Creating Locations");
		SortedMap<String, Location> locations = new TreeMap<String, Location>();

		for (Location location : access.allLocations()) {
			locations.put(location.getName().trim().toLowerCase(), location);
		}

		boolean mySqlDb = this.database.isMySqlDb();

		try {
			int count = 0;
			for (String cityName : readFile("data/Cities.txt")) {
				String cityKey = cityName.trim().toLowerCase();
				if (mySqlDb) {
					cityKey = utf8_general_ci(cityKey);
				}
				if (!locations.containsKey(cityKey)) {
					locations.put(cityKey, access.createLocation(cityName, false));
					count++;
					if (count % 1000 == 0) {
						log.info(count + " locations inserted in db");
						// try to make use of batch inserts if possible
						access.flushAndClear();
					}
				}
			}
		} catch (IOException ioe) {
			log.warn("could not read locations from file");
		}

		log.debug("Saving Locations");
		access.flush();

		log.info("Creating predefined journeys");

		// Entries used for promotions (found via "destination")
		access.createJourney("Paris - City of love", "Berlin", "Paris", "Personal Travel Inc.",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 11)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 25)), 730.99,
				readImage("img/Paris1.png"));
		access.createJourney("Paris - City of love", "Berlin", "Paris", "Personal Travel Inc.",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 21)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 35)), 720.99,
				readImage("img/Paris2.png"));
		access.createJourney("Mauritius - Island of dreams", "Berlin", "Mauritius", "Personal Travel Inc.",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 11)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 23)), 1730.99,
				readImage("img/Mauritius.png"));
		access.createJourney("Going to San Francisco...", "Berlin", "San Francisco", "Thomas Chef",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 13)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 22)), 723.98,
				readImage("img/GoldenGate.png"));
		access.createJourney("Walk on the Great Wall of China", "Berlin", "Beijing", "Personal Travel Inc.",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 8)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 37)), 730.99,
				readImage("img/GreatWall.png"));

		access.createJourney("Business Trip", "London", "New York", "Speed Travel Agency",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 10)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 17)), 299.99,
				readImage("img/NY_Dia_Small.png"));
		access.createJourney("Honeymoon Extravaganza", "Paris", "New York", "Speed Travel Agency",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 3)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 17)), 188,
				readImage("img/NY_Empire_State.png"));

		access.createJourney("Adventure Tour", "Berlin", "Mombasa", "Thomas Chef",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 4)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 26)), 799.23,
				readImage("img/Waterfall.png"));

		access.createJourney("Across New Zealand", "Wellington", "Auckland", "Personal Travel Inc.",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 5)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 28)), 1600.30,
				readImage("img/Christchurch.png"));

		access.createJourney("Walk in the Jungle", "Berlin", "Buenos Aires", "TravelNiche Ltd.",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 3)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 20)), 234.23,
				readImage("img/Waterfall.png"));

		access.createJourney("Weekend Trip into the Orient", "London", "Istanbul", "TravelNiche Ltd.",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 9)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 21)), 155.99,
				readImage("img/Bazar.png"));

		access.createJourney("Sport on the Beach", "London", "Hawaii", "Personal Travel Inc.",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 10)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 32)), 1300.99,
				readImage("img/Beachvolleyball.png"));

		access.createJourney("Party at the Pool", "New York", "Miami Beach", "Personal Travel Inc.",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 10)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 14)), 399.99,
				readImage("img/Pool.png"));

		access.createJourney("France - l'art de vivre", "Washington", "Provence", "Thomas Chef",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 10)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 27)), 780.99,
				readImage("img/PontDuGard.png"));

		access.createJourney("Visit the world famous Bazar", "Berlin", "Cairo", "Personal Travel Inc.",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 10)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 27)), 780.99,
				readImage("img/Bazar_Olives.png"));

		access.createJourney("Visit Turkey and enjoy the Beach", "Munich", "Ankara", "TravelNiche Ltd.",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 10)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 27)), 780.99,
				readImage("img/Bodrum.png"));

		access.createJourney("Lahore Beach", "Munich", "Lahore", "Personal Travel Inc.",
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 10)),
				new Date(System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY * 27)), 1780.99, null);

		log.debug("Saving Journeys");
		access.flush();

		log.info("Creating Schedules");
		access.createSchedule("Periodic report", 60 * 60);
		access.createSchedule("Cleanup", 3 * 60 * 60);
		access.createSchedule("Refresh Journeys", 10 * 60);

		log.debug("Saving Schedules");
		access.flush();

		log.info("Creating users");
		createUser(HAINER, "Hainer Hastings", HAINER, LoyaltyStatus.Gold);
		createUser(GEORGE, "George Laplass", GEORGE, LoyaltyStatus.None);
		createUser("maria", "Maria O'Donnel", "maria", LoyaltyStatus.None);
		createUser(MONICA, "Monica Tailor", MONICA, LoyaltyStatus.None);
		createUser("demouser", "Demo User", "demopass", LoyaltyStatus.None);
		createUser("demouser2", "Demo User2", "demouser2", LoyaltyStatus.None);
		createUser("silvia", "Silvia Silver", "silvia", LoyaltyStatus.Silver);
		createUser("geoffry", "Geoffry Gold", "geoffry", LoyaltyStatus.Gold);
		createUser("peter", "Peter Platinum", "peter", LoyaltyStatus.Platinum);
		createUser("synthetic1@dynatace.com", "Synthetic Browser", "syntheticB123", LoyaltyStatus.None);
		
		createUser("afrodyta", "Afrodyta Sachowich", "afrodyta", LoyaltyStatus.Platinum);
		createUser("agni", "Agni Weiho", "agni", LoyaltyStatus.Gold);
		createUser("qiao", "Qiao Kuchoo", "qiao", LoyaltyStatus.Silver);
		createUser("eliott", "Eliott Rashi", "eliott", LoyaltyStatus.None);
		createUser("rashiado", "Rashiado Okono", "rashiado", LoyaltyStatus.Platinum);
		createUser("anna", "Anna Jinji", "anna", LoyaltyStatus.Gold);
		createUser("annelia", "Annelia Wagelia", "annelia", LoyaltyStatus.Silver);
		createUser("oidi", "Oidi Mando", "oidi", LoyaltyStatus.None);
		createUser("armi", "Armi Cruzo", "armi", LoyaltyStatus.Platinum);
		createUser("arriva", "Arriva Kalejaja", "arriva", LoyaltyStatus.Gold);
		createUser("aso", "Aso Erdya", "aso", LoyaltyStatus.Silver);
		
		createUser("rofiq", "Rofiq Guagg", "rofiq", LoyaltyStatus.Platinum);
		createUser("baisa", "Baisa Giesow", "baisa", LoyaltyStatus.Gold);
		createUser("odill", "Odill Rui", "odill", LoyaltyStatus.Silver);
		createUser("roosevi", "Roosevi Keiwo", "roosevi", LoyaltyStatus.None);
		createUser("barbra", "Barbra Peox", "barbra", LoyaltyStatus.Platinum);
		createUser("gag", "Gag Jia", "gag", LoyaltyStatus.Gold);
		createUser("mir", "Mir Hayaka", "mir", LoyaltyStatus.Silver);
		createUser("biatao", "Biatao Shinji", "biatao", LoyaltyStatus.None);
		createUser("carlos", "Carlos Duto", "carlos", LoyaltyStatus.Platinum);
		createUser("komami", "Komami Coriano", "komami", LoyaltyStatus.Gold);
		createUser("ussu", "Ussu Hirodai", "ussu", LoyaltyStatus.Silver);

		/* Populate database with real user names content */
		for (CommonUser user : CommonUser.getUsers()) {
			createUser(user.getName(), user.getFullName(), user.getPassword(),
					LoyaltyStatus.valueOf(user.getLoyaltyStatus()));
		}

		log.debug("Saving Users");
		access.flush();

		log.info("Refreshing journeys");
		int refreshCnt = access.refreshJourneys();
		log.info("refreshed " + refreshCnt + " journeys");
		// TODO: Create existing Bookings
		access.flushAndClear();
		access.commitTransaction();

		if (randomContent) {
			final List<Location> locationList = new ArrayList<Location>(locations.values());
			final int journeysToCreate = NO_JOURNEYS - access.getJourneyCount();

			User monica = access.getUser(MONICA);
			int monicaLogins = access.getLoginCount(monica);
			int monicaBookings = access.getBookingCount(monica);
			final int monicaLoginsToCreate = NO_MONICA_LOGINS > monicaLogins ? NO_MONICA_LOGINS - monicaLogins : 0;
			final int monicaBookingsToCreate = NO_MONICA_BOOKINGS > monicaBookings ? NO_MONICA_BOOKINGS - monicaBookings
					: 0;

			User george = access.getUser(GEORGE);
			int georgeLogins = access.getLoginCount(george);
			int georgeBookings = access.getBookingCount(george);
			final int georgeLoginsToCreate = NO_GEORGE_LOGINS > georgeLogins ? NO_GEORGE_LOGINS - georgeLogins : 0;
			final int georgeBookingsToCreate = NO_GEORGE_BOOKINGS > georgeBookings ? NO_GEORGE_BOOKINGS - georgeBookings
					: 0;

			log.info("journeys to create asynchroneously: " + journeysToCreate);
			randomJourneysThread = new Thread("Long-Running Database Populate Thread") {

				@Override
				public void run() {
					DataAccess access = new GenericDataAccess(database.createNewBusinessController());
					try {
						createJourneys(access, locationList, journeysToCreate);
						createUserHistory(access, MONICA, monicaLoginsToCreate, monicaBookingsToCreate);
						createUserHistory(access, GEORGE, georgeLoginsToCreate, georgeBookingsToCreate);
					} finally {
						access.close();
					}
				}
			};
			randomJourneysThread.start();
			log.info("Business database basic-content was successfully created in "
					+ (System.currentTimeMillis() - start)
					+ "ms, some more random journeys are created in a separate thread.");
		} else {
			log.info("Business database basic-content was successfully created in "
					+ (System.currentTimeMillis() - start) + "ms, no random journeys are created.");
		}
	}

	public void createUser(String name, String fullName, String password, LoyaltyStatus status) {
		User user = access.getUser(name);

		if (user == null) {
			user = new User(name, fullName, password);
			user.setLoyaltyStatus(status.name());
			access.addUser(user);
		} else {
			log.debug("User '" + name + "' already exists, updating values.");
			user.setFullName(fullName);
			user.setPassword(password);
			user.setLoyaltyStatus(status.name());

			access.updateUser(user);
		}
	}

	private static byte[] readImage(String image) throws IOException {
		byte[] bytes = null;
		InputStream fileInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(image);
		if (fileInputStream == null) {
			throw new IOException("Could not find resource " + image);
		} else {
			try {
				bytes = IOUtils.toByteArray(fileInputStream);
			} finally {
				fileInputStream.close();
			}
		}
		return bytes;
	}

	/**
	 *
	 * @param date
	 * @return
	 * @author dominik.stadler
	 */
	@SuppressWarnings("deprecation")
	public static String dateToString(Date date) {
		return (date.getYear() + 1900) + "-" + (date.getMonth() < 9 ? "0" : "") + (date.getMonth() + 1) + "-"
				+ (date.getDate() < 10 ? "0" : "") + date.getDate();
	}

	/**
	 *
	 * @param date
	 * @return
	 * @author dominik.stadler
	 */
	@SuppressWarnings("deprecation")
	public static Date stringToDate(String date) {
		return new Date(Integer.parseInt(date.substring(0, 4)) - 1900, Integer.parseInt(date.substring(5, 7)) - 1,
				Integer.parseInt(date.substring(8, 10)));
	}


	/**
	 *
	 *
	 * @param path
	 * @return
	 * @throws IOException
	 * @author peter.kaiser
	 */
	private static List<String> readFile(String path) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is, BaseConstants.UTF8));
			try {
				List<String> ret = new ArrayList<String>();
				String line;
				while ((line = br.readLine()) != null) {
					ret.add(line);
				}
				return ret;
			} finally {
				br.close();
			}
		} finally {
			is.close();
		}
	}

	private static List<byte[]> readAllImages() throws IOException {
		List<byte[]> ret = new ArrayList<byte[]>();

		URL imagesUrl = Thread.currentThread().getContextClassLoader().getResource("img/auto");
		if ("file".equals(imagesUrl.getProtocol())) {
			File dir = new File(imagesUrl.getPath());
			if (dir.exists()) {
				String[] paths = dir.list();
				for (int i = 0; i < paths.length; i++) {
					// skip when running from svn
					if (paths[i].equals(".svn")) {
						continue;
					}

					InputStream is = null;
					try {
						is = new FileInputStream(new File(dir, paths[i]));
						ret.add(IOUtils.toByteArray(is));
					} catch (IOException ioe) {
						log.warn(ioe.getMessage());
					} finally {
						if (is != null) {
							is.close();
						}
					}
				}
			}
		} else if ("jar".equals(imagesUrl.getProtocol())) {
			String jarPath = imagesUrl.getPath();
			jarPath = URLDecoder.decode(jarPath.substring("file:".length(), jarPath.indexOf('!')), "UTF-8");
			log.info("reading images from jar " + jarPath);
			JarFile jar = new JarFile(jarPath);
			try {
				for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
					JarEntry entry = entries.nextElement();
					String name = entry.getName();
					if (name.startsWith("img/auto")) {
						byte[] tmp = readImage(name);
						if (tmp != null && tmp.length > 0) {
							ret.add(tmp);
						}
					}
				}
			} finally {
				jar.close();
			}
		}
		return ret;
	}

	private static void createJourneys(DataAccess access, List<Location> locations, int count) {
		if (count == 0) {
			return;
		}
		try {
			int imgThreshold = count / 2; // 50% of journeys will get images
			Random rand = new Random(count);
			List<byte[]> images;
			try {
				images = readAllImages();
			} catch (Exception e) {
				log.warn("cannot load journey images. creating journeys without images", e);
				images = new ArrayList<byte[]>();
			}
			int imgCnt = images.size();
			int locs = locations.size();
			long now = System.currentTimeMillis();
			Collection<Tenant> tenants = access.allTenants();
			access.startTransaction();
			for (int i = 0; i < count; i++) {
				Location dep = locations.get(rand.nextInt(locs));
				Location dest;
				do {
					dest = locations.get(rand.nextInt(locs));
				} while (dest.equals(dep));
				Date from = new Date(now + (rand.nextInt(180 * 24) + 1) * DateUtils.MILLIS_PER_HOUR);
				Date to = new Date(from.getTime() + (rand.nextInt(240) + 24) * DateUtils.MILLIS_PER_HOUR);
				int amount = 10000 + 10 * rand.nextInt(20000);
				byte[] img;
				if (i < imgThreshold && imgCnt > 0) {
					img = images.get(rand.nextInt(imgCnt));
				} else {
					img = null;
				}

				access.createJourney(dep.getName() + " - " + dest.getName(), dep, dest,
						tenants.toArray(new Tenant[tenants.size()])[rand.nextInt(tenants.size())], from, to,
						amount * .01, img);
				if ((i + 1) % 500 == 0) {
					log.info((i + 1) + " journeys inserted in db");
					access.flushAndClear();
					access.commitTransaction();
					access.startTransaction();
				}
			}
			log.info("done creating journeys");
			access.commitTransaction();
		} catch (Exception t) {
			log.error("error/exception executing JourneyCreator-Thread", t);
			throw new RuntimeException(t); // NOSONAR
		}
	}

	private void createSleepProcedure() {
		if (database.isDerbyDb()) {
			createDerbySleepProcedure();
		} else if (database.isOracleDb()) {
			createOracleSleepProcedure();
		} else if (database.isMssqlDb()){
			createMssqlSleepProcedure();
		} else if (database.isMySqlDb()){
			createMySqlSleepProcedure();
		}
	}

	private void createDelayFunction() {
		if (database.isDerbyDb()) {
			createDerbyNormalizeLocationFunction();
		} else if (database.isOracleDb()) {
			createOracleNormalizeLocationFunction();
		} else if (database.isMySqlDb()) {
			createMySqlNormalizeLocationFunction();
		}
	}

	//=======================================================
	// MsSQL
	//=======================================================

	/**
	 *
	 * @author kasper.kulikowski
	 */
	public static void createMssqlSleepProcedure() {
		final EasyTravelConfig config = EasyTravelConfig.read();
		try {

			final String dbDriver = config.databaseDriver;

			try {
			    Class.forName(dbDriver).newInstance(); // use java reflection to load the database driver
			} catch (Exception ex) {
			    log.error("Failed to load DB Driver for MS SQL.");
			}

			try (Connection conn = DriverManager.getConnection(config.databaseUrl, config.databaseUser,
					config.databasePassword);
					Statement stmt = conn.createStatement();
					Statement stmtBatch = conn.createStatement();) {

				try {
					stmt.executeUpdate("DROP PROCEDURE [dbo].[sp_verifyLocation]");
					log.info("Deleted procedure sp_verifyLocation.");
				} catch (Exception e) {
					log.info("Cannot delete procedure sp_verifyLocation - it may not exist");
				}
				stmt.executeUpdate("SET ANSI_NULLS ON");

				stmt.executeUpdate("SET QUOTED_IDENTIFIER ON");

				stmtBatch.addBatch("CREATE PROCEDURE [dbo].[sp_verifyLocation]" + "		@location int" + "	AS"
						+ "	BEGIN" + "		declare @t as datetime" + "		select @t=DATEADD(millisecond,@location,0)"
						+ "	    WAITFOR DELAY @t" + "	END");
				stmtBatch.executeBatch();
				log.info("Created procedure sp_verifyLocation.");
			}
		} catch (SQLException sqle) {
			log.warn("Exception creating sleep procedure", sqle);
		}
	}

	//=======================================================
	// Derby
	//=======================================================

	/**
	 *
	 * @author peter.kaiser
	 */
	private void createDerbySleepProcedure() {
		final EasyTravelConfig config = EasyTravelConfig.read();
		try {
			Connection conn = DriverManager.getConnection(config.databaseUrl, config.databaseUser,
					config.databasePassword);
			try {
				Statement stmt = conn.createStatement();
				try {
					ResultSet rst = stmt
							.executeQuery("SELECT count(*) FROM sys.sysaliases WHERE alias = 'VERIFY_LOCATION'");
					try {
						if (rst.next() && rst.getInt(1) == 0) {
							stmt.close();
							stmt = conn.createStatement();
							stmt.executeUpdate(
									"CREATE PROCEDURE verify_location(time BIGINT) LANGUAGE JAVA EXTERNAL NAME 'java.lang.Thread.sleep' PARAMETER STYLE JAVA NO SQL");
							log.info("derby sleep procedure created");
						}
					} finally {
						rst.close();
					}
				} finally {
					stmt.close();
				}
			} finally {
				conn.close();
			}
		} catch (SQLException sqle) {
			log.warn("Exception creating sleep procedure", sqle);
		}
	}

	private void createDerbyNormalizeLocationFunction() {
		final EasyTravelConfig config = EasyTravelConfig.read();

		try {
			Connection conn = DriverManager.getConnection(config.databaseUrl, config.databaseUser,
					config.databasePassword);
			try {
				Statement stmt = conn.createStatement();
				try {
					ResultSet rst = stmt
							.executeQuery("SELECT count(*) FROM sys.sysaliases WHERE alias = 'NORMALIZE_LOCATION'");
					try {
						if (rst.next() && rst.getInt(1) == 0) {
							stmt.close();
							stmt = conn.createStatement();
							stmt.executeUpdate(
									"CREATE FUNCTION normalize_location(name varchar (64), sleepTime integer ) "
											+ "returns varchar (64)" + "language java " + "parameter style java "
											+ "no sql "
											+ "external name 'com.dynatrace.easytravel.database.functions.DerbyFunctions.normalizeLocation'");
							log.info("derby normalize location function created");
						}
					} finally {
						rst.close();
					}
				} finally {
					stmt.close();
				}
			} finally {
				conn.close();
			}
		} catch (SQLException sqle) {
			log.warn("Exception creating normalize location function", sqle);
		}
	}

	//=======================================================
	// Oracle
	//=======================================================

	private void createOracleNormalizeLocationFunction() {
		final EasyTravelConfig config = EasyTravelConfig.read();

		try {

			Connection conn = DriverManager.getConnection(config.databaseUrl, config.databaseUser,
					config.databasePassword);
			try {
				Statement stmt = conn.createStatement();
				try {
					stmt.executeUpdate(
							"CREATE OR REPLACE FUNCTION normalize_location(name varchar2, sleep_time number) RETURN VARCHAR2 AS BEGIN VERIFY_LOCATION(sleep_time); return name; END;");
					log.info("oracle normalizeLocation function created");
				} finally {
					stmt.close();
				}

				// ========================================
				// Test if we have the right to execute the delay function.
				// It relies on DBMS_LOCK for which we may not have sufficient
				// permission. We want to know it now as opposed to failing
				// later.
				// ========================================

				Statement stmtTest = conn.createStatement();
				try {
					stmtTest.executeQuery(
							"SELECT NAME FROM LOCATION WHERE NAME = 'PARIS' AND NORMALIZE_LOCATION('Paris', 10) is not null");
					log.info("oracle normalizeLocation function tested OK");
				} finally {
					stmtTest.close();
				}

			} finally {
				conn.close();
			}

			// Catch just Exception: it is not useful to be too specific at this
			// point.
			// As it happens, here it would probably be
			// TransactionRequiredException,
			// but it may also be SQLException
		} catch (Exception e) {
			log.warn("Exception creating or testing normailzeLocation function.", e);
			throw new RuntimeException(e); // NOSONAR
		}
	}

	/**
	 * Create sleep procedure for Oracle
	 */
	private void createOracleSleepProcedure() {
		final EasyTravelConfig config = EasyTravelConfig.read();
		try {

			Connection conn = DriverManager.getConnection(config.databaseUrl, config.databaseUser,
					config.databasePassword);
			try {
				Statement stmt = conn.createStatement();
				try {
					stmt.executeUpdate(
							"CREATE OR REPLACE procedure verify_location(time in number) is begin dbms_lock.sleep(time/1000); end;");
					log.info("oracle sleep procedure created");

				} finally {
					stmt.close();
				}

				// ========================================
				// Test if we have the right to execute the delay function.
				// It relies on DBMS_LOCK for which we may not have sufficient
				// permission. We want to know it now as opposed to failing
				// later.
				// ========================================

				Statement stmtTest = conn.createStatement();
				try {
					stmtTest.execute("CALL VERIFY_LOCATION(10)");
					log.info("oracle verify_location procedure tested OK");
				} finally {
					stmtTest.close();
				}

			} finally {
				conn.close();
			}

			// Catch just Exception: it is not useful to be too specific at this
			// point.
			// As it happens, here it would probably be
			// TransactionRequiredException,
			// but it may also be SQLException
		} catch (Exception e) {
			log.warn("Exception creating or testing sleep procedure", e);
			throw new RuntimeException(e); // NOSONAR
		}
	}

	//=======================================================
	// MySQL
	//=======================================================

	private void createMySqlNormalizeLocationFunction() {
		final EasyTravelConfig config = EasyTravelConfig.read();

		try {

			Connection conn = DriverManager.getConnection(config.databaseUrl, config.databaseUser,
					config.databasePassword);
			try {
				Statement stmt = conn.createStatement();
				try {
					stmt.executeUpdate(
							"DROP FUNCTION IF EXISTS normalize_location;");
					stmt.executeUpdate(
							"CREATE FUNCTION normalize_location(name VARCHAR(100), sleep_time int) RETURNS VARCHAR(100) DETERMINISTIC BEGIN call verify_location(sleep_time); return name; END;");
					log.info("MySQL normalizeLocation function created");
				} finally {
					stmt.close();
				}

			} finally {
				conn.close();
			}

		} catch (Exception e) {
			log.warn("Exception creating or testing normailze_location function.", e);
			throw new RuntimeException(e); // NOSONAR
		}
	}


	private void createMySqlSleepProcedure() {
		final EasyTravelConfig config = EasyTravelConfig.read();
		try {

			Connection conn = DriverManager.getConnection(config.databaseUrl, config.databaseUser,
					config.databasePassword);
			try {
				Statement stmt = conn.createStatement();
				try {
					stmt.executeUpdate(
							"DROP PROCEDURE IF EXISTS verify_location;");
					stmt.executeUpdate(
							"CREATE PROCEDURE `verify_location`(in time int unsigned) BEGIN do SLEEP(time/1000); END;");
					log.info("MySQL sleep procedure created");
				} finally {
					stmt.close();
				}

			} finally {
				conn.close();
			}

		} catch (Exception e) {
			log.warn("Exception creating or testing verify_location procedure", e);
			throw new RuntimeException(e); // NOSONAR
		}
	}

	//=======================================================

	private static void createUserHistory(DataAccess access, String userName, int loginCount, int bookingCount) {
		if (loginCount == 0 && bookingCount == 0) {
			return;
		}
		access.startTransaction();
		final int seed = loginCount << 16 + bookingCount;
		Date now = new Date();
		User user = access.getUser(userName);
		Random random = new Random(seed);
		for (int i = 0; i < loginCount - bookingCount; i++) {
			// logins randomly distributed during the last two years
			access.createLoginHistory(user, DateUtils.addMinutes(now, -random.nextInt(60 * 24 * 730)));
		}
		if (bookingCount > 0) {
			Collection<Journey> journeys = access.getJourneys(bookingCount);
			int count = 0;
			for (Journey journey : journeys) {
				Date bkgDate = DateUtils.addDays(journey.getFromDate(), -7);
				bkgDate = DateUtils.addSeconds(bkgDate, count);
				access.createBooking(UUID.randomUUID().toString(), journey, user, bkgDate);
				access.createLoginHistory(user, bkgDate);
				count++;
			}
		}
		access.commitTransaction();
		log.info("created " + loginCount + " login history entries and " + bookingCount + " bookings for " + userName);
	}

	private void waitForAsyncJourneyCreationTask(int timeout, TimeUnit unit) throws InterruptedException {
		if (randomJourneysThread != null) {
			randomJourneysThread.join(unit.toMillis(timeout));
		}

	}

}
