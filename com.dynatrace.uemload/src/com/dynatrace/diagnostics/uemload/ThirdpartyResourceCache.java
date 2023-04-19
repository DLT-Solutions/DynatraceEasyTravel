package com.dynatrace.diagnostics.uemload;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.google.common.base.Strings;

/**
 * Stores the mimeType and response size of ThirdParty Resources. Synchronization is omitted intentionally
 * 
 * @author cwat-sschatka
 *
 */
public class ThirdpartyResourceCache {
	
	public static final int NO_THIRD_PARTY_RESOURCE = -1;
	
	private static Hashtable<String, CachedThirdPartyResouce> cache = new Hashtable<String, CachedThirdPartyResouce>();
	private static final Logger LOGGER = Logger.getLogger(ThirdpartyResourceCache.class.getName());
	private static String[] thirdPartyDomains = null;
	private static String[] thirdPartyUrls = null;
	private static final String THIRD_PARTY_RESOURCESIZES_PROPERTIES = "easyTravelThirdPartyResourcesizes.properties";
	protected static final int THIRD_PARTY_RESOURCESIZE_DEFAULT = 54015;	
	private static final int WEATHER_IMAGES_NUMBER = 3;
	private static final String WEATHER_IMAGES_REGEX = "//openweathermap.org/img/w/[0-9][0-9][dn]+.png";
	private static final String WEATHER_IMAGES_REPLACE_REGEX = "([0-9][0-9])[dn]+.png";
	private static final Pattern WEATHER_IMG_ID_PATTERN = Pattern.compile(WEATHER_IMAGES_REPLACE_REGEX);
	private static final String PROTOMATCH_PATTERN = "^https?:";
	private static final String PROTOMATCH_PATTERN_URL = "^https?:.*";
	private static final String PROTO_RELATIVE_PATTERN = "^//.*";
	
	static {
		try {
			EasyTravelConfig easyTravelConfig = EasyTravelConfig.read();
			thirdPartyDomains = easyTravelConfig.thirdPartyDomains;
			thirdPartyUrls = easyTravelConfig.thirdPartyUrls;
		} catch (Exception e) {
			LOGGER.warning("Could not read ThirdParty Domains/Urls from the config");
		}
		if (thirdPartyDomains == null) {
			thirdPartyDomains = new String[0];
		}
		if (thirdPartyUrls == null) {
			thirdPartyUrls = new String[0];
		}		
		initializeCache();
	}

	/**
	 * read the Third Party Resourcesizes Property File and initializes the cache
	 */
	private static void initializeCache() {
		FileInputStream inputStream = null;		
		try {
			File resourcesizeFile = new File(Directories.getResourcesDir().getAbsolutePath() + File.separator + THIRD_PARTY_RESOURCESIZES_PROPERTIES);
			inputStream = new FileInputStream(resourcesizeFile);
			Properties thirdPartyResourcesizes = new Properties();
			thirdPartyResourcesizes.load(inputStream);
			for(Entry<Object, Object> property: thirdPartyResourcesizes.entrySet()) {
				String resourceUrl = (String)property.getKey();
				
				if(!isThirdPartyDomain(resourceUrl)) {
					LOGGER.warning("Resourcesizes Propertyfile contains non third party resourceurl: '" + resourceUrl + "'. Entry will be skipped.");
					continue;
				}
				
				String value = (String)property.getValue();//value contains resource size and mime type (separator = '!')
				String resourceSizeString = StringUtils.substringBefore(value, "!");
				int resourceSize;
				try {
					resourceSize = Integer.parseInt(resourceSizeString);
				} catch(NumberFormatException e) {
					LOGGER.warning("Resourcesize of Resourceurl: '" + resourceUrl + "' is not a valid integer!");
					resourceSize = THIRD_PARTY_RESOURCESIZE_DEFAULT;
				}				
				
				String mimeType = StringUtils.substringAfter(value, "!");
				if(StringUtils.isBlank(mimeType)) {
					mimeType = URLUtil.guessMimeType(resourceUrl);
				}
				cache.put(resourceUrl, new CachedThirdPartyResouce(resourceSize, mimeType));
			}
		} catch (Exception e) {
			LOGGER.warning("Could not read ThirdParty Resourcesizes Propertyfile. " + ExceptionUtils.getMessage(e));
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
	
	/**
	 * @param resource
	 * @return the mimetype of the resource if it has been cached, null otherwise
	 */
	public static String getMimeType(String resource) {
		String nResource = normalizeURL(resource);
		CachedThirdPartyResouce cachedThirdPartyResouce = cache.get(nResource);
		if (cachedThirdPartyResouce == null) {
			if(isThirdPartyDomain(nResource)) {
				LOGGER.finer("No entry for Resource URL '" + nResource + "' found in the Third Party Resourcesizes Propertyfile!");
			}			
			return null;
		}
		return cachedThirdPartyResouce.getMimeType();
	}
	
	/**
	 * @param resource
	 * @return the download size of the resource if it has been cached, null otherwise
	 */
	public static int getResponseSize(String resource) {
		String nResource = normalizeURL(resource);
		CachedThirdPartyResouce cachedThirdPartyResouce = cache.get(nResource);
		if (cachedThirdPartyResouce == null) {
			if(isThirdPartyDomain(nResource)) {
				LOGGER.finer("Responsesize for the following Resource URL '" + nResource + "' not found in the Third Party Resourcesizes Propertyfile!");
				return THIRD_PARTY_RESOURCESIZE_DEFAULT;
			}
			return NO_THIRD_PARTY_RESOURCE;
		}

		return cachedThirdPartyResouce.getResponseSize();
	}
	
	public static boolean isThirdPartyDomain(String resource) {
		try {
			URL page = convertResourceToUrl(resource);
			String host = page.getHost();
			for (String thirdPartyDomain: thirdPartyDomains) {
				if (host.endsWith(thirdPartyDomain)) {
					return true;
				}
			}
			for (String thirdPartyUrl: thirdPartyUrls) {
				if (resource.equals(thirdPartyUrl)) {
					return true;
				}
			}
			return false;
		} catch (MalformedURLException e) {
			LOGGER.warning("MalformedURLException caught: " + e.getMessage());
			return false;
		}
	}
	
	private static URL convertResourceToUrl(String resource) throws MalformedURLException {
		String newResource = Strings.nullToEmpty(resource);
		if (newResource.matches(PROTO_RELATIVE_PATTERN)) {
			newResource = "http:" + resource;
		} else if (!newResource.matches(PROTOMATCH_PATTERN_URL)) {
			newResource = "http://" + resource;
		}

		return new URL(newResource);
	}
	
	/**
	 * Convert URL:
	 *  remove protocol
	 *  for images from openweathermap.org to 3 URLs defined in the thirdPartyResourcesizes.properties file. 
	 * 		This is done, because we don't want to specify 15 different URLs in property file for them.
	 * @param resource
	 * @return
	 */
	private static String normalizeURL(String resource) {
		String newResource = Strings.nullToEmpty(resource).replaceFirst(PROTOMATCH_PATTERN, ""); 
		if (isWeatherMapImage(newResource)) {
			return getWeatherImageURL(newResource);
		}
		
		return newResource;
	}
	
	private static boolean isWeatherMapImage(String resource) {
		return Strings.nullToEmpty(resource).matches(WEATHER_IMAGES_REGEX);
	}
	
	/**
	 * Get weather image url. Method will replace string containing image number + d/n into number%3. For example:
	 * http://openweathermap.org/img/w/09n.png -> http://openweathermap.org/img/w/0.png
	 * http://openweathermap.org/img/w/01n.png -> http://openweathermap.org/img/w/1.png
	 * http://openweathermap.org/img/w/02n.png -> http://openweathermap.org/img/w/2.png
	 * @param resource
	 * @return
	 */
	private static String getWeatherImageURL(String resource) {
		//find image id
		int id = 0;
		Matcher matcher = WEATHER_IMG_ID_PATTERN.matcher(resource);
		if (matcher.find()) {
			try {
				id = Integer.parseInt(matcher.group(1));				
			} catch (NumberFormatException e) {
				LOGGER.warning("Cannot get image number for " + resource);
			}
		}
		
		id = id % WEATHER_IMAGES_NUMBER;		
		return matcher.replaceAll(id + ".png");
	}	
}
