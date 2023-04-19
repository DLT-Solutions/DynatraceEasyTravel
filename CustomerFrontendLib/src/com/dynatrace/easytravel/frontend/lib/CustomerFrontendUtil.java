package com.dynatrace.easytravel.frontend.lib;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.codahale.metrics.Timer.Context;
import com.dynatrace.easytravel.frontend.data.DataProviderInterface;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.frontend.data.LocationDO;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.metrics.Metrics;

import ch.qos.logback.classic.Logger;

public final class CustomerFrontendUtil {
	
	private static final Logger log = LoggerFactory.make();
	private static final String MAIL_SUFFIX = "@example.com";
	private static final String DUMMY_PASSWORD = "123456";
	private static final Random RANDOM = new Random();
	
	// random names borrowed from http://www.xtra-rant.com/gennames/
	private static final String[] FIRST_NAMES = {
		"Unice","Sapphira","Emmett","Alvena","Tracee","Darla","Kerena","Yorick","Annabella","Darwin","Noelene","Lenora","Driskoll","Winifred","Romayne","Nancy","Thorley","Aline","Panda","Denzel","Brande","Cooper","Janae","Magdalen","Cecil","Primula","Seamour","Rosanna","Eglantine","Meghan","Delbert","Genesis","Magnus","Posy","Sharalyn","Deshawn","Gavin","Clarity","Cornelia","Kamryn","Christmas","Latasha","Oswald","Storm","Davie","Darrel","Holly","Em","Chelsea","Oliver","Iolanthe","Cora","Chandler","Jera","Quanah","Linda","Noah","Bentley","Pru","Jodie","Drew","Blythe","Katherina","Peggie","Deana","Rosaleen","Dave","Isolda","Chrystal","Christabel","Mickey","Lauryn","Rowley","William","Missie","Laureen","Vaughan","Tabatha","Hailee","Lane","Glenna","Sorrel","Cairo","Calista","Abner","Cathleen","Kerenza","Rosanne","Arthur","Radclyffe","Ryanne","Geoffrey","Jannine","Homer","Martie","Karen","Yazmin","Chantelle","Trudie","Caden",
		"Jaylee","Trent","Tabitha","Rubye","Githa","Cordelia","Eireen","Kassidy","Zena","Rickie","Wilton","Glanville","Millard","Tianna","Debora","Brandie","Salome","Caileigh","Rodge","Gretta","Rowina","Prince","Dwayne","Darrel","Cleo","Gaye","Gae","Matthew","Angus","Lalla","Astra","Liana","Madge","Oneida","Savannah","Loreto","July","Gladwyn","Fina","Kenton","Unique","Hall","Ina","Jill","Josepha","Avelina","Winona","Elle","Tammi","Kayleah","Alannis","Jervis","Quinton","Vince","Rebeccah","Madoline","Dena","Drake","Sebastian","Ashley","Lalo","Bennie","Tanisha","Denzil","Chet","Katy","Bernardine","Kylee","Kit","Tom","Ottoline","Daria","Avis","Lea","Ralph","Zavanna","Martha","Jools","Dionne","Derby","Melanie","Deborah","Hartley","Gwendolen","Lourdes","Hunter","Manley","Tacey","Corrine","Rex","Trevelyan","Audrey","Ulyssa","Stacey","Melita","Jayme","Sloan","Griselda","Kathi","Roxie"
	};

	private static final String[] LAST_NAMES = {
		"Cram","Greenawalt","Blois","Fuhrer","Fillmore","Hurst","Haines","Clarke","Dean","Poley","Leech","Gilman","Sanforth","Turzanski","Gleper","Eliza","Rosenstiehl","Whitling","Vanleer","Drennan","Coldsmith","Woodward","Joghs","Adams","Agnes","Blaine","Loewentsein","Sutton","Butler","Pittman","Gearhart","Marjorie","Errett","Priebe","Mays","Saltser","Werry","Linton","Campbell","Briggs","Fair","Pritchard","Compton","Rockwell","Prechtl","Moon","Jones","Patterson","Goodman","Hallauer","Orbell","Stephenson","Leonard","Wilkins","Ledgerwood","Ryals","Potter","Boyer","Benford","Fylbrigg","Dull","Allshouse","Lauffer","Hatfield","Hatcher","Knopsnider","Langston","Garneys","Vinsant","Schmidt","Richards","King","Kiefer","Busk","Jenner","Hair","Reddish","Bynum","Herrold","Harshman","Quirin","Rhinehart","Pershing","Mosser","Lowry","Evans","Sommer","Bennett","Hincken","Pearsall","Weisgarber","Chappel","Cason","Treeby","Wilkerson","Armstrong","Park","Richter","Steele","Moffat",
		"Pennington","Briner","Hector","Sholl","Fisher","Christman","Pearsall","Gadow","Trout","Dunkle","Mcintosh","Scott","Dealtry","Hughes","Hasely","Warren","Alice","Cox","Kelley","Sandys","Archibald","Woollard","Mccullough","Little","Kunkle","Steiner","Diegel","Winton","Ramos","Gleper","Schaeffer","Dryfus","Erskine","Mcdonald","Fitzgerald","Bynum","Osteen","Adams","Cross","Cox","Sherlock","Duncan","Gibson","Barnes","Hurst","Flanders","Lucy","Sheets","Gilman","Orner","Mills","Baer","Stiffey","Baughman","Erschoff","Baskett","Eliza","Mosser","Mercer","Pickering","Joghs","Wyatt","Caesar","Sutorius","Prescott","Geyer","Branson","Faust","Appleby","Newman","Swarner","Herndon","White","Sanforth","Saltser","Ewing","Schreckengost","Mays","Hynes","Nash","Thomas","Mildred","Sanborn","Losey","Seidner","Casteel","Cowart","Finlay","Powers","Kimple","Orbell","Knapenberger","Northey","Wheeler","Eisenman","Hawker","Bashline","Blaine","Garland","Sachse"
	};
	
	private CustomerFrontendUtil() {
		throw new IllegalAccessError();
	}
	
	public static User getRandomUser() {
		User user = new User();
		user.setFirstName(FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)]);
		user.setLastName(LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)]);
		user.setEmail(user.getFirstName().toLowerCase() + "." + user.getLastName().toLowerCase() + MAIL_SUFFIX);
		user.setPassword(DUMMY_PASSWORD);
		user.setCreditCardNumber(String.valueOf(100000000000000L + Math.abs(RANDOM.nextLong())));
		user.setVerificationNumber(String.valueOf(1000 + RANDOM.nextInt(1000)));
		user.setExpirationMonth("December");
		user.setExpirationYear(Integer.toString(Calendar.getInstance().get(Calendar.YEAR) + 2)); // always set the expiration to two years in the future
		return user;
	}
	
	public static List<JourneyDO> getRecommendedJourneys(DataProviderInterface dataProvider) {
		List<JourneyDO> randomJourneys = getRandomJourneys(dataProvider);
		List<JourneyDO> journeys = new ArrayList<>();
		
		Random r = new Random();
		
		for(int i = 0; i < Math.min(2, randomJourneys.size()); i++) {
			int index = r.nextInt(randomJourneys.size());
			journeys.add(randomJourneys.get(index));
			randomJourneys.remove(index);
		}
		
		return journeys;
	}
	
    public static List<JourneyDO> getRandomJourneys(DataProviderInterface dataProvider) {

        List<JourneyDO> journeys = new ArrayList<>();
        Context findLocationsContext = null;
        Context findJourneysContext = null;
        
        try {
            findLocationsContext = Metrics.getTimerContext(CustomerFrontendUtil.class, "findLocations");
            LocationDO[] locations = dataProvider.findLocations("a ", Integer.MAX_VALUE);
            findLocationsContext.stop();
            
            if (locations.length <= 1) {
                //fix for cassandra scenario where query for 'a ' returns only one result
				try (Context findLocationsContext2 = Metrics.getTimerContext(CustomerFrontendUtil.class,
						"findLocations");) {
					locations = dataProvider.findLocations("be", Integer.MAX_VALUE);
					findLocationsContext.stop();
					if (locations.length <= 0) {
						return Collections.emptyList();
					}
				}
            }
            
            //find 64 random locations
            //because of APM-34714 if we are searching for journeys for all locations recommendations are not working (timeout) in large envs
            Random r = new Random();
            int i = -1;
            Set<LocationDO> result = new HashSet<>();
            while (result.size() < Math.min(5, locations.length)) { // reduced from 64 to 12 because of increased number of calls //reduce to from 12 to 5 because of APM-80606
                i = r.nextInt(locations.length);
                result.add(locations[i]);
            }

            for (LocationDO location : result) {
                findJourneysContext = Metrics.getTimerContext(CustomerFrontendUtil.class, "findJourneys");
                JourneyDO[] trips = dataProvider.findJourneys(location.getName(), null, null);
                findJourneysContext.stop();
                for (JourneyDO trip : trips) {
                    if (!journeys.contains(trip)) {
                        journeys.add(trip);
                    }
                }
            }

        } catch (RemoteException ex) {
            log.debug(ex.getMessage(), ex);
        } finally {
        	if (findJourneysContext != null)
        		findJourneysContext.close();
        	if (findLocationsContext != null)
        		findLocationsContext.close();
        }

        return journeys;
    }
	
}
