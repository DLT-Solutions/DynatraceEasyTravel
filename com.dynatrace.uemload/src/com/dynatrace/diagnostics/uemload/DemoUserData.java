package com.dynatrace.diagnostics.uemload;

import java.util.Map;

import com.dynatrace.diagnostics.uemload.mobile.MobileDeviceType;
import com.google.common.collect.Maps;

/**
 * Class contain traffic parameters for demo users
 * @author rafal.psciuk
 *
 */
public class DemoUserData {

	private final String name;
	private final String fullName;
	private final Location location;
	private final BrowserType browser;
	private final MobileDeviceType mobileDevice;
	private final BrowserWindowSize browserWindowSize;

	//users used in demo
	public static final String MONICA = "monica";
	public static final String MARIA = "maria";
	public static final String HAINER = "hainer";
	public static final String GEORGE = "george";

	//Demo users definitions
	public static final DemoUserData MONICA_USER = new DemoUserData.Builder()
	.withName(MONICA)
	.withFullName("Monica Tailor")
	.withLocation(new Location("Europe", "Germany", "194.245.56.136", 1))
	.withBrowserType(BrowserType.CHROME_56)
	.withBrowserWindowSize(BrowserWindowSize._1024x768)
	.withMobileDeviceType(MobileDeviceType.IPHONE_XR)
	.build();

	public static final DemoUserData MARIA_USER = new DemoUserData.Builder()
	.withName(MARIA)
	.withFullName("Maria O'Donnel")
	.withLocation(new Location("North America","United States","7.232.109.166", -7))
	.withBrowserType(BrowserType.EDGE_14)
	.withBrowserWindowSize(BrowserWindowSize._1280x768)
	.withMobileDeviceType(MobileDeviceType.SAMSUNG_GALAXY_S7)
	.build();

	public static final DemoUserData HAINER_USER = new DemoUserData.Builder()
			.withName(HAINER)
			.withFullName("Hainer Hastings")
			.withLocation(new Location("Europe", "Germany", "94.216.56.237", 1))
			.withBrowserType(BrowserType.CHROME_56)
			.withBrowserWindowSize(BrowserWindowSize._1024x768)
			.withMobileDeviceType(MobileDeviceType.IPHONE_XR)
			.build();

	public static final DemoUserData GEORGE_USER = new DemoUserData.Builder()
			.withName(GEORGE)
			.withFullName("George Laplass")
			.withLocation(new Location("Europe", "Germany", "194.245.56.136", 1))
			.withBrowserType(BrowserType.CHROME_56)
			.withBrowserWindowSize(BrowserWindowSize._1024x768)
			.withMobileDeviceType(MobileDeviceType.IPHONE_XR)
			.build();

	private static final Map<String, DemoUserData> demoUsers = Maps.newHashMap();
	static {
		demoUsers.put(MARIA, MARIA_USER);
		demoUsers.put(MONICA, MONICA_USER);
		demoUsers.put(HAINER, HAINER_USER);
		demoUsers.put(GEORGE, GEORGE_USER);
	}

	/**
	 * @param name
	 * @return {@link Location} for given demo user; null is returned if demo user was not found for given name
	 */
	public static Location getDemoUserLocation(String name) {
		DemoUserData user = demoUsers.get(name);

		if(user != null) {
			return user.getLocation();
		}
		return null;
	}

	/**
	 * @param name
	 * @return {@link BrowserType} for given demo user, null is returned if demo user was not found for given name
	 */
	public static BrowserType getDemoUserBrowserType(String name) {
		DemoUserData user = demoUsers.get(name);

		if(user != null) {
			return user.getBrowser();
		}
		return null;
	}

	/**
	 * @param name
	 * @return {@link BrowserWindowSize} for given demo user, null is returned if demo user was not found for given name
	 */
	public static BrowserWindowSize getDemoUserBrowserWindowSize(String name) {
		DemoUserData user = demoUsers.get(name);

		if(user != null) {
			return user.getBrowserWindowSize();
		}
		return null;
	}

	/**
	 * @param name
	 * @return {@link MobileDeviceType} for given demo user, null is returned if demo user was not found for given name
	 */
	public static MobileDeviceType getDemoUserMobileDeviceType(String name) {
		DemoUserData user = demoUsers.get(name);

		if(user != null) {
			return user.getMobileDevice();
		}
		return null;
	}

	/**
	 * @return user name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return user full name
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * @return user location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @return user browser
	 */
	public BrowserType getBrowser() {
		return browser;
	}

	/**
	 * @return user mobile device type
	 */
	public MobileDeviceType getMobileDevice() {
		return mobileDevice;
	}

	/**
	 * @return browser window size
	 */
	public BrowserWindowSize getBrowserWindowSize() {
		return browserWindowSize;
	}

	/**
	 * Default constructor, privatate, objects of this class can be create only by {@link Builder}
	 */
	private DemoUserData(Builder builder) {
		this.name = builder.name;
		this.fullName = builder.fullName;
		this.location = builder.location;
		this.browser = builder.browser;
		this.mobileDevice = builder.mobileDevice;
		this.browserWindowSize = builder.browserWindowSize;
	}

	/**
	 * Builder class for {@link DemoUserData}
	 * @author rafal.psciuk
	 *
	 */
	public static class Builder {
		private String name;
		private String fullName;
		private Location location;
		private BrowserType browser;
		private MobileDeviceType mobileDevice;
		private BrowserWindowSize browserWindowSize;

		public Builder withName(String name) {
			this.name = name;
			return this;
		}

		public Builder withFullName(String fullName) {
			this.fullName = fullName;
			return this;
		}

		public Builder withLocation(Location location) {
			this.location = location;
			return this;
		}

		public Builder withBrowserType(BrowserType browserType) {
			this.browser = browserType;
			return this;
		}

		public Builder withMobileDeviceType(MobileDeviceType mobileDevice) {
			this.mobileDevice = mobileDevice;
			return this;
		}

		public Builder withBrowserWindowSize(BrowserWindowSize browserWindowSize) {
			this.browserWindowSize = browserWindowSize;
			return this;
		}

		public DemoUserData build() {
			return new DemoUserData(this);
		}
	}
}
