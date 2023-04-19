package com.dynatrace.diagnostics.uemload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.diagnostics.uemload.mobile.MobileDeviceType;
import com.dynatrace.diagnostics.uemload.scenarios.HeadlessAngularScenario;
import com.dynatrace.diagnostics.uemload.utils.UserFileGenerator;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.misc.CommonUser;
import com.dynatrace.easytravel.misc.LoyaltyStatus;
import com.dynatrace.easytravel.util.ResourceFileReader;
import com.dynatrace.easytravel.util.SpecialUserData;
import com.dynatrace.easytravel.util.SpecialUserDataRow;

/**
 *
 * @author Michal.Bakula
 *
 */

public class ExtendedCommonUser extends CommonUser {

	private static final Logger LOGGER = Logger.getLogger(ExtendedCommonUser.class.getName());

	private static boolean regenAttemptFlag = false;

	private final Location location;
	private RandomSet<BrowserType> desktopBrowsers;
	private final MobileDeviceType mobileDevice;
	private final BrowserType mobileBrowser;
	private final Bandwidth bandwidth;
	private final BrowserWindowSize desktopBrowserWindowSize;
	private final BrowserWindowSize mobileBrowserWindowSize;
	private final Integer dnsSlowdown;
	private VisitorInfo visitorInfo;
	private final long mobileDeviceId;

	static class IPEndingZeroException extends Exception{
		private static final long serialVersionUID = 1L;
		public IPEndingZeroException( String message) {
			super(message);
		}
	}

	private ExtendedCommonUser(ExtendedCommonUserBuilder builder){
		super(builder.name, builder.fullName, builder.loyaltyStatus, builder.password, builder.weight);
		this.location = builder.location;
		this.desktopBrowsers = builder.desktopBrowsers;
		this.mobileDevice = builder.mobileDevice;
		this.mobileBrowser = builder.mobileBrowser;
		this.bandwidth = builder.bandwidth;
		this.desktopBrowserWindowSize = builder.desktopBrowserWindowSize;
		this.mobileBrowserWindowSize = builder.mobileBrowserWindowSize;
		this.dnsSlowdown = builder.dnsSlowdown;
		this.visitorInfo = builder.visitorInfo;
		this.mobileDeviceId = builder.mobileDeviceId;
	}

	public static List<ExtendedCommonUser> getExtendedUsers(){
		try {
			return getExtendedUsers(ResourceFileReader.getInputStream(ResourceFileReader.EXTENDEDUSERS));
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Cannot read extended file with real user names.", e);
		}
		
		return Collections.emptyList();
	}

	public static List<ExtendedCommonUser> getExtendedUsers(InputStream inputStream) {
		List<ExtendedCommonUser> commonUsers = Collections.emptyList();

		try {
			Map<String, String> coordinates = getIpCoordinates();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, BaseConstants.UTF8));

			try {
				commonUsers = new ArrayList<>();
				String line;
				while ((line = br.readLine()) != null) {
					String[] tokens = line.split(";");
					String name = tokens[0];
					String fullName = tokens[1];
					LoyaltyStatus loyaltyStatus = LoyaltyStatus.get(tokens[2]);
					String password = tokens[3];
					int weight = Integer.parseInt(tokens[4]);
					String country = tokens[5];
					String continent = tokens[6];
					Integer timezone = Integer.parseInt(tokens[7]);
					String ip = tokens[8];
					checkIpForDemoIpMaskingFeature(ip);
					Location location = new Location(continent, country, ip, coordinates.get(ip), timezone);
					RandomSet<BrowserType> desktopBrowsers = new RandomSet<>();
					String[] browsers = tokens[9].split(",");
					for (int i = 0; i < browsers.length; i++) {
						desktopBrowsers.add(BrowserType.getFieldByName(browsers[i]), i + 1);
					}
					MobileDeviceType mobileDevice = MobileDeviceType.getFieldByName(tokens[10]);
					BrowserType mobileBrowser = BrowserType.getFieldByName(tokens[11]);
					Bandwidth bandwidth = Bandwidth.valueOf(tokens[12]);
					BrowserWindowSize desktopBrowserWindowSize = BrowserWindowSize.valueOf(tokens[13]);
					BrowserWindowSize mobileBrowserWindowSize = BrowserWindowSize.valueOf(tokens[14]);
					int dnsSlowdown = Integer.parseInt(tokens[15]);

					VisitorInfo visitorInfo;
					String[] visitor = tokens[16].split(",");
					if(visitor.length > 1){
						visitorInfo = new VisitorInfo(visitor[1]);
					} else {
						visitorInfo = new VisitorInfo(Boolean.parseBoolean(visitor[0]));
					}

					long deviceId = Long.parseLong(tokens[17]);

					ExtendedCommonUser user = new ExtendedCommonUser.ExtendedCommonUserBuilder(name, fullName, loyaltyStatus, password, weight)
							.setLocation(location)
							.setDesktopBrowsers(desktopBrowsers)
							.setMobileDevice(mobileDevice)
							.setMobileBrowser(mobileBrowser)
							.setBandwidth(bandwidth)
							.setDesktopBrowserWindowSize(desktopBrowserWindowSize)
							.setMobileBrowserWindowSize(mobileBrowserWindowSize)
							.setDnsSlowdow(dnsSlowdown)
							.setVisitorInfo(visitorInfo)
							.setMobileDeviceId(deviceId)
							.build();

					commonUsers.add(user);
				}
			} finally {
				if(br != null)
					br.close();
				inputStream.close();
			}
		} catch (Exception e) {
			if (!regenAttemptFlag) {
				LOGGER.log(Level.SEVERE,
						String.format(
								"Cannot read %s file. File probably corrupted or outdated. Attepting to make a backup and generate a new one.",
								ResourceFileReader.EXTENDEDUSERS),
						e);
				regenerateFile();
				commonUsers = getExtendedUsers();
			} else {
				LOGGER.log(Level.SEVERE, String.format("Error occured even after regeneration of %s file.",
						ResourceFileReader.EXTENDEDUSERS), e);
			}
		}

		if (commonUsers.size() == 0 && !regenAttemptFlag) {
			LOGGER.log(Level.SEVERE,
					String.format( "File %s is empty. Attepting to make a backup and generate a new one.", ResourceFileReader.EXTENDEDUSERS));
			regenerateFile();
			commonUsers = getExtendedUsers();
		}

		return commonUsers;
	}

	private static void regenerateFile() {
		regenAttemptFlag = true;
		if (fileBuckup()) {
			UserFileGenerator generator = new UserFileGenerator();
			generator.generateUserFile();
		} else {
			LOGGER.log(Level.SEVERE,
					String.format("Could not make buckup of %s file.", ResourceFileReader.EXTENDEDUSERS));
		}
	}

	/**
	 * JLT-217860 - IPs ending .0 were removed from the geo.txt file, so that IP masking could be demonstrated
	 * ExtendedUsers is generated from geo.txt - so if we find such IP here then simply regenerate the txt file.
	 * @param ip
	 * @throws IPEndingZeroException
	 */
	private static void checkIpForDemoIpMaskingFeature(String ip) throws IPEndingZeroException {
		if (ip.endsWith(".0")) {
			if (regenAttemptFlag) {
				LOGGER.warning(
						"Even after ExtendedUsers file regeneration, IPs ending with \'.0\' still exists. You must have been using custom geo.txt file. Using ExtendingUsers file with your custom configuration.");
			} else {
				throw new IPEndingZeroException("IP [" + ip + "] ends with a zero [" + ResourceFileReader.EXTENDEDUSERS
						+ "] will be regenerated");
			}
		}
	}

	private static boolean fileBuckup(){
		LOGGER.log(Level.INFO, "Bucking up extended users file.");
		File oldFile = new File(Directories.getConfigDir(), ResourceFileReader.EXTENDEDUSERS);
		String buckupName = String.format("backup_%d_%s", System.currentTimeMillis(), ResourceFileReader.EXTENDEDUSERS);
		File newFile = new File(Directories.getConfigDir(), buckupName);

		return oldFile.renameTo(newFile);
	}

	private static Map<String, String> getIpCoordinates() throws IOException{
		Map<String, String> ipCoordinates = new HashMap<>();

		InputStream is = ResourceFileReader.getInputStream(ResourceFileReader.GEOCOORDINATES);
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		try {
			String line;
			while ((line = in.readLine()) != null) {
				String[] columns = line.split(";");
				String ip = columns[0];
				String coordinates = columns[1];
				ipCoordinates.put(ip, coordinates);
			}
		} finally {
			if(in != null)
				in.close();
			if(is != null)
				is.close();
		}
		return ipCoordinates;
	}

	public Location getLocation(){
		return location;
	}

	public BrowserType getRandomDesktopBrowser(){
		if(desktopBrowsers != null){
			return desktopBrowsers.getRandom();
		}
		return null;
	}

	public MobileDeviceType getMobileDevice(){
		return mobileDevice;
	}

	public BrowserType getMobileBrowser(){
		return mobileBrowser;
	}

	public Bandwidth getBandwidth(){
		return bandwidth;
	}

	public BrowserWindowSize getDesktopBrowserWindowSize(){
		return desktopBrowserWindowSize;
	}

	public BrowserWindowSize getMobileBrowserWindowSize(){
		return mobileBrowserWindowSize;
	}

	public Integer getDNSSlowdown(){
		return dnsSlowdown;
	}

	public VisitorInfo getVisitorInfo(){
		return visitorInfo;
	}

	public long getMobileDeviceId() {
		return mobileDeviceId;
	}
	
	public boolean isSpecialWeeklyUser() {
		return this.visitorInfo.getVisitorID().startsWith("1623930097");
	}
	
	public boolean isSpecialMonthlyUser() {
		return this.visitorInfo.getVisitorID().startsWith("1623930098");
	}

	/**
	 * Builder class for ExtendedCommonUser. Important!!! Java Builder Pattern
	 * used for better code transparency, but still all fields are required and
	 * must be set.
	 *
	 * @author Michal.Bakula
	 *
	 */
	public static class ExtendedCommonUserBuilder extends CommonUser{
		private Location location;
		private RandomSet<BrowserType> desktopBrowsers;
		private MobileDeviceType mobileDevice;
		private BrowserType mobileBrowser;
		private Bandwidth bandwidth;
		private BrowserWindowSize desktopBrowserWindowSize;
		private BrowserWindowSize mobileBrowserWindowSize;
		private Integer dnsSlowdown;
		private VisitorInfo visitorInfo;
		private long mobileDeviceId;

		public ExtendedCommonUserBuilder(String name, String fullName, LoyaltyStatus loyaltyStatus, String password, int weight){
			super(name, fullName, loyaltyStatus, password, weight);
		}

		public ExtendedCommonUserBuilder setLocation(Location location){
			this.location = location;
			return this;
		}

		public ExtendedCommonUserBuilder setDesktopBrowsers(RandomSet<BrowserType> desktopBrowsers){
			this.desktopBrowsers = desktopBrowsers;
			return this;
		}

		public ExtendedCommonUserBuilder setMobileDevice(MobileDeviceType mobileDevice){
			this.mobileDevice = mobileDevice;
			return this;
		}

		public ExtendedCommonUserBuilder setMobileBrowser(BrowserType mobileBrowser){
			this.mobileBrowser = mobileBrowser;
			return this;
		}

		public ExtendedCommonUserBuilder setBandwidth(Bandwidth bandwidth){
			this.bandwidth = bandwidth;
			return this;
		}

		public ExtendedCommonUserBuilder setDesktopBrowserWindowSize(BrowserWindowSize desktopBrowserWindowSize){
			this.desktopBrowserWindowSize = desktopBrowserWindowSize;
			return this;
		}

		public ExtendedCommonUserBuilder setMobileBrowserWindowSize(BrowserWindowSize mobileBrowserWindowSize){
			this.mobileBrowserWindowSize = mobileBrowserWindowSize;
			return this;
		}

		public ExtendedCommonUserBuilder setDnsSlowdow(Integer dnsSlowdown){
			this.dnsSlowdown = dnsSlowdown;
			return this;
		}

		public ExtendedCommonUserBuilder setVisitorInfo(VisitorInfo visitorInfo){
			this.visitorInfo = visitorInfo;
			return this;
		}

		public ExtendedCommonUserBuilder setMobileDeviceId(long mobileDeviceId) {
			this.mobileDeviceId = mobileDeviceId;
			return this;
		}

		public ExtendedCommonUser build(){
			if( visitorInfo == null ) {
				visitorInfo = new VisitorInfo(true);
			}
			return new ExtendedCommonUser(this);
		}
	}

	public boolean isUserGood(boolean canBeSpecialUser) {
		if (isSpecialMonthlyUser() == false && isSpecialWeeklyUser() == false) {
			return true;
		}
		
		if (canBeSpecialUser == true) {
			Calendar cal = Calendar.getInstance();
			
			if (isSpecialWeeklyUser() == true) {
				if (cal.get(Calendar.DAY_OF_WEEK) == SpecialUserData.getInstance().WEEKLY_USER_DAY) {
					return true;
				}
				else {
					return false;
				}
			}
			else {
				SpecialUserDataRow row = SpecialUserData.getInstance().getUserRow(this.getVisitorInfo().getVisitorID());
				if (row == null) {
					SpecialUserData.getInstance().usedUsers.add(new SpecialUserDataRow(
							this.getVisitorInfo().getVisitorID(), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)));
					return true;
				}
				else if ((cal.get(Calendar.MONTH) > row.monthOfVisit && cal.get(Calendar.YEAR) == row.yearOfVisit) || 
						(cal.get(Calendar.YEAR) > row.yearOfVisit)) {
					SpecialUserData.getInstance().usedUsers.remove(row);
					SpecialUserData.getInstance().usedUsers.add(new SpecialUserDataRow(
							this.getVisitorInfo().getVisitorID(), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)));
					return true;
				}
				else {
					return false;
				}
			}
		}
		
		return false;
	}
	
	protected boolean checkIfUserIsNotUsed(String visitorInfo) {
		SpecialUserDataRow row = SpecialUserData.getInstance().getUserRow(visitorInfo);
		return row == null || 
				(Calendar.getInstance().MONTH > row.monthOfVisit && Calendar.getInstance().YEAR == row.yearOfVisit) || 
				(Calendar.getInstance().YEAR > row.yearOfVisit);
	}
}
