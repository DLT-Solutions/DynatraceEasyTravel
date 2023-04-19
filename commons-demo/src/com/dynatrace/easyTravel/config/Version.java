package com.dynatrace.easytravel.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.time.FastDateFormat;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.ConfigurationProvider;

/*
 * philipp.grasboeck:
 * changed this class to use PropertyBeanBuilder that automatically
 * injects properties into Java objects using MVEL.
 */
public final class Version {

	private static final Logger LOGGER = Logger.getLogger(Version.class.getName());

	private static final String PROPERTIES_FILE = "easyTravel";
	
	private static final String NOT_AVAILABLE = "Not available";
	
	private static String BUILDDATE;

	private int major;
	
	private int minor;
	
	private int revision;
	
	private int buildnumber;
	
	private Date builddate;

	/* Currently not read:
	version.company=dynaTrace software GmbH
	version.productname=dynaTrace easyTravel Demo Application 
	version.copyright=Copyright (C) 2010-2011
	version.patchlevel=
	version.companyURL=http\://www.dynatrace.com
	database.name = Derby
	*/

	/**
	 * Read local launcher version
	 *
	 * @return Version
	 */
	public static Version read() {
		return ConfigurationProvider.createPropertyBean(Version.class, PROPERTIES_FILE, "version");
	}

	/**
	 * Cast string version to Version object
	 *
	 * @param version
	 * @return Version
	 */
	public static Version read(String version)  {
		Map<String, Integer> versionProperties = createVersionProperties(version);
		Version ver = null;

		try {
			ver = ConfigurationProvider.createPropertyBean(Version.class, versionProperties, "version");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Cannot create Version object from string:  " + version, e.getMessage());
		}
		return ver;
	}

	private static Map<String, Integer> createVersionProperties(String version) {
		String[] tokens = version.split("\\"+ BaseConstants.DOT);

		// Map<String, Integer> versionProperties = new HashMap<String, Integer>();
		Map<String, Integer> versionProperties = new HashMap<>(); // Java 7 should pass this OK
		versionProperties.put(BaseConstants.Version.VERSION_MAJOR, Integer.parseInt(tokens[0]));
		versionProperties.put(BaseConstants.Version.VERSION_MINOR, Integer.parseInt(tokens[1]));
		versionProperties.put(BaseConstants.Version.VERSION_REVISION, Integer.parseInt(tokens[2]));
		versionProperties.put(BaseConstants.Version.VERSION_BUILDNUMBER, Integer.parseInt(tokens[3]));
		return versionProperties;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getRevision() {
		return revision;
	}

	
	public String toMajor() {
        return String.valueOf(major);
    }
	
	public String toMinor() {
	    return toMajor() + BaseConstants.DOT + String.valueOf(minor);
	}
	
	public String toRevision() {
        return toMinor() + BaseConstants.DOT + revision;
    }
	
	@Override
	public String toString() {
		return major + BaseConstants.DOT + minor + BaseConstants.DOT + revision + BaseConstants.DOT + buildnumber;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public int getBuildnumber() {
		return buildnumber;
	}

	public void setBuildnumber(int buildnumber) {
		this.buildnumber = buildnumber;
	}

	public Date getBuilddate() {
		return builddate;
	}

	public void setBuilddate(Date builddate) {
		this.builddate = builddate;
	}
	
	public String getBuilddateString(){
		if(builddate == null)
			BUILDDATE = NOT_AVAILABLE;
		if(BUILDDATE == null){
			FastDateFormat dFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
			BUILDDATE = dFormat.format(builddate);
		}
		return BUILDDATE;
	}
	
	public String getOnlyDateString(){
		if(BUILDDATE == null)
			getBuilddateString();
		if(BUILDDATE.equals(NOT_AVAILABLE))
			return NOT_AVAILABLE;
		else
			return BUILDDATE.substring(0, 10);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Version)) {
			return false;
		}

		Version other = (Version) obj;

		return 	major == other.getMajor() &&
				minor == other.getMinor() &&
				revision == other.getRevision() &&
				buildnumber == other.buildnumber;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + major;
		result = 31 * result + minor;
		result = 31 * result + revision;
		result = 31 * result + buildnumber;
		return result;
	}
}
