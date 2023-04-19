package com.dynatrace.easytravel.misc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.ResourceFileReader;


/**
 * CommonUser class is used in two contexts:
 * - read a file Users.txt with real users names and populate database content
 * - generate uemload for customer sessions with real user names
 *
 * @author cwpl-rorzecho
 * @author Michal.Bakula
 */
public class CommonUser {

	private static final Logger LOGGER = Logger.getLogger(CommonUser.class.getName());

	public String name;		// NOSONAR - public on purpose
	public String fullName;	// NOSONAR - public on purpose
	public LoyaltyStatus loyaltyStatus;	// NOSONAR - public on purpose
	public String password;			// NOSONAR - public on purpose
	public int weight;	// NOSONAR - public on purpose

	public CommonUser(String name, String fullName, LoyaltyStatus loyaltyStatus, String password, int weight) {
		this.name = name;
		this.fullName = fullName;
		this.loyaltyStatus = loyaltyStatus;
		this.password = password;
		this.weight = weight;
	}
	
	public CommonUser(String name, String fullName, LoyaltyStatus loyaltyStatus, String password) {
		this.name = name;
		this.fullName = fullName;
		this.loyaltyStatus = loyaltyStatus;
		this.password = password;
	}

	public CommonUser(String name, String password) {
		this.name = name;
		this.password = password;
	}

	/**
	 * Load real user names from file
	 *
	 * @author cwpl-rorzecho
	 *
	 * @return List<CommonUser>
	 */
	public static List<CommonUser> getUsers() {

		List<CommonUser> commonUsers = Collections.emptyList();
		InputStream inputStream = null;
		BufferedReader br = null;

		try {		
			inputStream = ResourceFileReader.getInputStream(ResourceFileReader.USERS);			
			br = new BufferedReader(new InputStreamReader(inputStream, BaseConstants.UTF8));

			try {
				commonUsers = new ArrayList<CommonUser>();
				String line;
				while ((line = br.readLine()) != null) {
					String[] tokens = line.split(",");
					String name = tokens[0];
					String fullName = tokens[1];
					LoyaltyStatus loyaltyStatus = LoyaltyStatus.get(tokens[2]);
					String password = tokens[3];
					int weight = Integer.parseInt(tokens[4]);

					CommonUser commonUser = new CommonUser(name, fullName, loyaltyStatus, password, weight);

					commonUsers.add(commonUser);
				}
			} finally {
				if(inputStream != null)
					inputStream.close();
				if(br != null)
					br.close();
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Cannot read file with real user names.", e.getMessage());
		}
		return commonUsers;
	}

	@Override
	public String toString() {
		return "CommonUser [name=" + name + ", fullName=" + fullName + ", loyaltyStatus=" + loyaltyStatus
				+ ", password=" + password + ", weight=" + weight + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getLoyaltyStatus() {
		return (loyaltyStatus != null) ? loyaltyStatus.toString() : null;
	}

	public void setLoyaltyStatus(LoyaltyStatus loyaltyStatus) {
		this.loyaltyStatus = loyaltyStatus;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public int getWeight(){
		return weight;
	}
	
	public void setWeight(int weight){
		this.weight = weight;
	}
}