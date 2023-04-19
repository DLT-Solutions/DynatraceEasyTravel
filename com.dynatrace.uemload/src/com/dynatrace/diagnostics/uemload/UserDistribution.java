package com.dynatrace.diagnostics.uemload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User distribution for UEMLoad.
 * Stored static to decrease memory usage.
 * 
 * @author Michal.Bakula
 *
 */

public class UserDistribution {
	
	private UserDistribution() {
		throw new IllegalAccessError("Utility class");
	}
	
	private static final Map<String, RandomSet<ExtendedCommonUser>> USERS_DISTRIBUTION = createUsersDistribution();
	private static final RandomSet<ExtendedCommonUser> DEMO_USERS_DISTRIBUTION = createDemoUsersDistribution();
	
	public static Map<String, RandomSet<ExtendedCommonUser>> getUsers(){
		return USERS_DISTRIBUTION;
	}
	
	public static RandomSet<ExtendedCommonUser> getDemoUsers(){
		return DEMO_USERS_DISTRIBUTION;
	}
	
	private static Map<String, RandomSet<ExtendedCommonUser>> createUsersDistribution(){
		Map<String, RandomSet<ExtendedCommonUser>> tmpUsers = new HashMap<>();

		List<ExtendedCommonUser> demousers = new ArrayList<>();
		demousers.addAll(Arrays.asList(
				ExtendedDemoUser.MARIA_USER, ExtendedDemoUser.MONICA_USER, ExtendedDemoUser.DEMOUSER, ExtendedDemoUser.DEMOUSER2,
				ExtendedDemoUser.MONTHLY_USER_1, ExtendedDemoUser.MONTHLY_USER_2, ExtendedDemoUser.MONTHLY_USER_3,
				ExtendedDemoUser.MONTHLY_USER_4, ExtendedDemoUser.MONTHLY_USER_5, ExtendedDemoUser.MONTHLY_USER_6,
				ExtendedDemoUser.MONTHLY_USER_7, ExtendedDemoUser.MONTHLY_USER_8, ExtendedDemoUser.MONTHLY_USER_9,
				ExtendedDemoUser.MONTHLY_USER_10, ExtendedDemoUser.MONTHLY_USER_11,
				ExtendedDemoUser.WEEKLY_USER_1, ExtendedDemoUser.WEEKLY_USER_2, ExtendedDemoUser.WEEKLY_USER_3,
				ExtendedDemoUser.WEEKLY_USER_4, ExtendedDemoUser.WEEKLY_USER_5, ExtendedDemoUser.WEEKLY_USER_6,
				ExtendedDemoUser.WEEKLY_USER_7, ExtendedDemoUser.WEEKLY_USER_8, ExtendedDemoUser.WEEKLY_USER_9,
				ExtendedDemoUser.WEEKLY_USER_10, ExtendedDemoUser.WEEKLY_USER_11
				));
		for(ExtendedCommonUser user : demousers){
			String country = user.getLocation().getCountry();
			RandomSet<ExtendedCommonUser> rs = tmpUsers.get(country);
			if(rs == null){
				rs = new RandomSet<>();
				rs.add(user, user.getWeight());
			} else {
				rs.add(user, user.getWeight());
			}
			tmpUsers.put(country, rs);
		}

		for(ExtendedCommonUser user : ExtendedCommonUser.getExtendedUsers()) {
			String country = user.getLocation().getCountry();
			RandomSet<ExtendedCommonUser> rs = tmpUsers.get(country);
			if(rs == null){
				rs = new RandomSet<>();
				rs.add(user, user.getWeight());
			} else {
				rs.add(user, user.getWeight());
			}
			tmpUsers.put(country, rs);
		}

		return tmpUsers;
	}
	
	private static RandomSet<ExtendedCommonUser> createDemoUsersDistribution() {
		RandomSet<ExtendedCommonUser> tmpUsers = new RandomSet<>();
		
		tmpUsers.add(ExtendedDemoUser.MARIA_USER, ExtendedDemoUser.MARIA_USER.getWeight());
		tmpUsers.add(ExtendedDemoUser.MONICA_USER, ExtendedDemoUser.MONICA_USER.getWeight());
		tmpUsers.add(ExtendedDemoUser.DEMOUSER2, ExtendedDemoUser.DEMOUSER2.getWeight());
		tmpUsers.add(ExtendedDemoUser.DEMOUSER, ExtendedDemoUser.DEMOUSER.getWeight());
		
		tmpUsers.add(ExtendedDemoUser.WEEKLY_USER_1, ExtendedDemoUser.WEEKLY_USER_1.getWeight());
		tmpUsers.add(ExtendedDemoUser.WEEKLY_USER_2, ExtendedDemoUser.WEEKLY_USER_2.getWeight());
		tmpUsers.add(ExtendedDemoUser.WEEKLY_USER_3, ExtendedDemoUser.WEEKLY_USER_3.getWeight());
		tmpUsers.add(ExtendedDemoUser.WEEKLY_USER_4, ExtendedDemoUser.WEEKLY_USER_4.getWeight());
		tmpUsers.add(ExtendedDemoUser.WEEKLY_USER_5, ExtendedDemoUser.WEEKLY_USER_5.getWeight());
		tmpUsers.add(ExtendedDemoUser.WEEKLY_USER_6, ExtendedDemoUser.WEEKLY_USER_6.getWeight());
		tmpUsers.add(ExtendedDemoUser.WEEKLY_USER_7, ExtendedDemoUser.WEEKLY_USER_7.getWeight());
		tmpUsers.add(ExtendedDemoUser.WEEKLY_USER_8, ExtendedDemoUser.WEEKLY_USER_8.getWeight());
		tmpUsers.add(ExtendedDemoUser.WEEKLY_USER_9, ExtendedDemoUser.WEEKLY_USER_9.getWeight());
		tmpUsers.add(ExtendedDemoUser.WEEKLY_USER_10, ExtendedDemoUser.WEEKLY_USER_10.getWeight());
		tmpUsers.add(ExtendedDemoUser.WEEKLY_USER_11, ExtendedDemoUser.WEEKLY_USER_11.getWeight());
		
		tmpUsers.add(ExtendedDemoUser.MONTHLY_USER_1, ExtendedDemoUser.MONTHLY_USER_1.getWeight());
		tmpUsers.add(ExtendedDemoUser.MONTHLY_USER_2, ExtendedDemoUser.MONTHLY_USER_2.getWeight());
		tmpUsers.add(ExtendedDemoUser.MONTHLY_USER_3, ExtendedDemoUser.MONTHLY_USER_3.getWeight());
		tmpUsers.add(ExtendedDemoUser.MONTHLY_USER_4, ExtendedDemoUser.MONTHLY_USER_4.getWeight());
		tmpUsers.add(ExtendedDemoUser.MONTHLY_USER_5, ExtendedDemoUser.MONTHLY_USER_5.getWeight());
		tmpUsers.add(ExtendedDemoUser.MONTHLY_USER_6, ExtendedDemoUser.MONTHLY_USER_6.getWeight());
		tmpUsers.add(ExtendedDemoUser.MONTHLY_USER_7, ExtendedDemoUser.MONTHLY_USER_7.getWeight());
		tmpUsers.add(ExtendedDemoUser.MONTHLY_USER_8, ExtendedDemoUser.MONTHLY_USER_8.getWeight());
		tmpUsers.add(ExtendedDemoUser.MONTHLY_USER_9, ExtendedDemoUser.MONTHLY_USER_9.getWeight());
		tmpUsers.add(ExtendedDemoUser.MONTHLY_USER_10, ExtendedDemoUser.MONTHLY_USER_10.getWeight());
		tmpUsers.add(ExtendedDemoUser.MONTHLY_USER_11, ExtendedDemoUser.MONTHLY_USER_11.getWeight());

		return tmpUsers;
	}
}
