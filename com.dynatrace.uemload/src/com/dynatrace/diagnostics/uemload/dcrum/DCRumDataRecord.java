package com.dynatrace.diagnostics.uemload.dcrum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.dynatrace.diagnostics.uemload.URLUtil;
import com.dynatrace.diagnostics.uemload.http.base.ResponseHeaders;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.constants.BaseConstants.DtHeader;
import com.google.common.collect.Lists;


/**
 * Each instance of this class represents one DCRum data record
 * 
 * @author stefan.moschinski
 */
public class DCRumDataRecord {

	private static final int USER_DEF_PARAMS_NO = 7;
	public static String RECORD_TYPE_PUREPATH = "P";
	private static final Character[] EVENT_TYPES = { 'P', 'N', 'F', 'S', 'X', 'R', 'A' };
	private static final Character[] STATUS_TYPES = { 'o', 'a', 'b', 'd' };
	private static final Character[] SLOW_TYPES = { 'S', 'T', 'F' };
	private static final Short[] APP_TYPES = { -1, 0, 1, 2, 3 };

	private static final String ET_SOFTWARE_SERVICE = "easyTravel";

	private static Logger logger = Logger.getLogger(DCRumDataRecord.class.getName());

	private String recordType;
	private String ss;
	private String srvIP;
	private String cliName;
	private String cliIP;
	private String inCliIP;
	private String scheme;
	private String host;
	private String path;
	private String PPID;

	// use object types instead of primitives to easily identify null values
	private Long interval;
	private char slow; // 0 normal - 1 slow
	private short appType;


	private static long NOT_WARNED_YET = -1;
	private static AtomicLong lastWarning = new AtomicLong(NOT_WARNED_YET);
	private static final String HTTP_SCHEME = "http";


	private List<NameValuePair> userDefParams = new ArrayList<NameValuePair>(7);
	private Character eventType;
	private Character status;

	public DCRumDataRecord(String url, String ip, String user) {
		this.recordType = RECORD_TYPE_PUREPATH;
		this.interval = System.currentTimeMillis() - UemLoadUtils.randomInt((int) TimeUnit.SECONDS.toMillis(5));
		this.cliName = user;
		this.cliIP = ip;
		this.host = URLUtil.getHostFromURL(url);
		this.srvIP = URLUtil.convertHostToIP(this.host);
		this.path = URLUtil.getPathFromURL(url);
		this.scheme = HTTP_SCHEME;
		this.ss = ET_SOFTWARE_SERVICE;

		this.slow = getRandomElement(SLOW_TYPES);
		this.eventType = getRandomElement(EVENT_TYPES);
		this.status = getRandomElement(STATUS_TYPES);
		this.appType = getRandomElement(APP_TYPES);
		this.userDefParams = generateUserDefParams();

	}

	private <T> T getRandomElement(T[] elements) {
		return elements[UemLoadUtils.randomInt(elements.length)];
	}

	@TestOnly
	DCRumDataRecord(String recordType) {
		this.recordType = recordType;
	}

	/**
	 * Method extracts the PurePath information from the HTTP header response
	 * 
	 * @param responseHeaders
	 * @author stefan.moschinski
	 */
	public void filterPurePath(ResponseHeaders responseHeaders) {
		if (responseHeaders == null) {
			return;
		}
		Collection<String> dtHeader = responseHeaders.getValues(DtHeader.X_DYNATRACE);
		if (dtHeader.isEmpty()) {
			// only warn each minute
			if (lastWarning.get() == NOT_WARNED_YET ||
					System.currentTimeMillis() - lastWarning.get() > TimeUnit.MINUTES.toMillis(1)) {
				logger.info("Could not find X-dynaTrace HTTP header, possibly the dynaTrace Server/Collector is not running");
				lastWarning.set(System.currentTimeMillis());
			}
			return;
		}
		PPID = dtHeader.iterator().next();
	}

	protected String getPPID() {
		return PPID;
	}

	protected Long getBegT() {
		return interval;
	}


	protected String getSoftwareService() {
		return ss;
	}


	protected String getSrvIP() {
		return srvIP;
	}



	Long getInterval() {
		return interval;
	}


	Character getEventType() {
		return eventType;
	}


	Character getStatus() {
		return status;
	}


	protected Character getSlowType() {
		return slow;
	}


	protected String getCliName() {
		return cliName;
	}


	protected String getCliIP() {
		return cliIP;
	}


	protected String getInCliIP() {
		return inCliIP;
	}


	protected String getScheme() {
		return scheme;
	}


	protected String getHost() {
		return host;
	}


	protected String getPath() {
		return path;
	}


	protected String getRecordType() {
		return recordType;
	}

	protected short getAppType() {
		return appType;
	}

	protected List<NameValuePair> getUserDefParams() {
		return userDefParams;
	}

	private List<NameValuePair> generateUserDefParams() {
		// TODO@(stefan.moschinski): is there a better way to fill the list?
		// fill up the list with null entries
		List<NameValuePair> list = new ArrayList<NameValuePair>(userDefParams);
		for (int i = 0; i < USER_DEF_PARAMS_NO; i++) {
			list.add(new BasicNameValuePair("param" + (i + 1),
					RandomStringUtils.randomAlphanumeric(UemLoadUtils.randomInt(7))));
		}
		return list;
	}

	static void setRecordTypePurepath(String recordTypePurepath) {
		RECORD_TYPE_PUREPATH = recordTypePurepath;
	}

	static void setLogger(Logger logger) {
		DCRumDataRecord.logger = logger;
	}


	DCRumDataRecord setRecordType(String recordType) {
		this.recordType = recordType;
		return this;
	}


	DCRumDataRecord setBegT(Long begT) {
		this.interval = begT;
		return this;
	}


	DCRumDataRecord setSs(String ss) {
		this.ss = ss;
		return this;
	}


	DCRumDataRecord setSrvIP(String srvIP) {
		this.srvIP = srvIP;
		return this;
	}


	DCRumDataRecord setAppType(short appType) {
		this.appType = appType;
		return this;
	}


	DCRumDataRecord setSlow(char slow) {
		this.slow = slow;
		return this;
	}


	DCRumDataRecord setCliName(String cliName) {
		this.cliName = cliName;
		return this;
	}


	DCRumDataRecord setCliIP(String cliIP) {
		this.cliIP = cliIP;
		return this;
	}


	DCRumDataRecord setInCliIP(String inCliIP) {
		this.inCliIP = inCliIP;
		return this;
	}


	DCRumDataRecord setScheme(String scheme) {
		this.scheme = scheme;
		return this;
	}


	DCRumDataRecord setHost(String host) {
		this.host = host;
		return this;
	}


	DCRumDataRecord setPath(String path) {
		this.path = path;
		return this;
	}


	DCRumDataRecord setPPID(String pPID) {
		PPID = pPID;
		return this;
	}



	DCRumDataRecord setUserDefParams(Iterable<? extends NameValuePair> userDefParams) {
		this.userDefParams = userDefParams == null ? null : Lists.newArrayList(userDefParams);
		return this;
	}

	DCRumDataRecord setEventType(char eventType) {
		this.eventType = eventType;
		return this;
	}

	DCRumDataRecord setStatus(char status) {
		this.status = status;
		return this;
	}


}
