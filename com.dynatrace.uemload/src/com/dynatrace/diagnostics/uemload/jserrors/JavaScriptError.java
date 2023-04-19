package com.dynatrace.diagnostics.uemload.jserrors;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import com.dynatrace.diagnostics.uemload.BrowserType;
import com.dynatrace.diagnostics.uemload.BrowserType.BrowserFamily;

/**
 * A class that allows to create a representation of a JavaScript error taking into
 * account browser & browser version differences.
 *
 * @author cwat-moehler
 *
 */
public class JavaScriptError {

	private String currentPageUrl;

	private String defaultMessage;
	private String code;
	private int defaultLine = -1;
	private int defaultColumn = -1;

	private String file;
	private List<String> userActions = new ArrayList<String>();

	public static final String ENGLISH = "en";
	public static final String GERMAN = "de";
	public static final String ITALIAN = "it";
	public static final String FRENCH = "fr";
	public static final String SPANISH = "es";

	private Map<BrowserFamily, String> messageByBrowserFamily = new HashMap<BrowserFamily, String>();
	private Map<BrowserType, String> messageByBrowserType = new HashMap<BrowserType, String>();
	private Map<String, String> messageByLanguage = new HashMap<String, String>();

	private Map<BrowserFamily, String> stackTraceByBrowserFamily = new HashMap<BrowserFamily, String>();
	private Map<BrowserType, String> stackTraceByBrowserType = new HashMap<BrowserType, String>();

	private Map<BrowserFamily, Integer> columnNumberByBrowserFamily = new HashMap<BrowserFamily, Integer>();
	private Map<BrowserType, Integer> columnNumberByBrowserType = new HashMap<BrowserType, Integer>();

	private Map<BrowserFamily, Integer> lineNumberByBrowserFamily = new HashMap<BrowserFamily, Integer>();
	private Map<BrowserType, Integer> lineNumberByBrowserType = new HashMap<BrowserType, Integer>();

	private static final Random randomNumberGenerator = new Random();

	public JavaScriptError(String currentPageUrl) {
		this.currentPageUrl = currentPageUrl;
	}

	public String getMessage(BrowserType browserType) {

		// determine default message
		String message = messageByBrowserType.get(browserType);
		if (message == null) {
			message = messageByBrowserFamily.get(browserType.getBrowserFamily());
		}
		if (message == null) {
			message = defaultMessage;
		}

		// determine if we should use a language specific message
		if (browserType.getBrowserFamily() == BrowserFamily.IE && messageByLanguage.size() > 0) {
			// make english error message 4 times more likely to appear than any other error message
			int maxRand = messageByLanguage.size();
			if (!messageByLanguage.containsKey(ENGLISH)) {
				maxRand++;
			}
			int random = getNextRandom(0, maxRand);
			List<String> keys = new ArrayList<String>(messageByLanguage.keySet());
			if (random > messageByLanguage.size() - 1) {
				if (messageByLanguage.containsKey(ENGLISH)) {
					return messageByLanguage.get(ENGLISH);
				}
				return message;
			} else {
				return messageByLanguage.get(keys.get(random));
			}
		}

		return message;
	}

	public final void setMessage(BrowserType browserType, String message) {
		messageByBrowserType.put(browserType, message);
	}

	public final void setMessage(BrowserFamily browserFamily, String message) {
		messageByBrowserFamily.put(browserFamily, message);
	}

	public void setLanguageSpecificMessage(String languageCode, String message) {
		messageByLanguage.put(languageCode, message);
	}

	public final void setDefaultMessage(String message) {
		defaultMessage = message;
	}

	public String getStackTrace(BrowserType browserType) {

		if (!browserType.isErrorObjectInOnErrorAvailable()) {
			return null;
		}

		String stackTrace = stackTraceByBrowserType.get(browserType);
		if (stackTrace == null) {
			stackTrace = stackTraceByBrowserFamily.get(browserType.getBrowserFamily());
		}

		return replaceDomain(stackTrace);
	}

	public final void setStackTrace(BrowserType browserType, String stackTrace) {
		stackTraceByBrowserType.put(browserType, stackTrace);
	}

	public final void setStackTrace(BrowserFamily browserFamily, String stackTrace) {
		stackTraceByBrowserFamily.put(browserFamily, stackTrace);
	}

	public String getFile() {
		return replaceDomain(file);
	}

	public final void setFile(String file) {
		this.file = file;
	}

	public int getLine(BrowserType browserType) {
		int line = -1;
		if (lineNumberByBrowserType.containsKey(browserType)) {
			line = lineNumberByBrowserType.get(browserType);
		} else if (lineNumberByBrowserFamily.containsKey(browserType.getBrowserFamily())) {
			line = lineNumberByBrowserFamily.get(browserType.getBrowserFamily());
		} else {
			line = defaultLine;
		}
		return line;
	}

	public final void setLine(int defaultLine) {
		this.defaultLine = defaultLine;
	}

	public final void setLine(BrowserType browserType, int line) {
		lineNumberByBrowserType.put(browserType, line);
	}

	public final void setLine(BrowserFamily browserFamily, int line) {
		lineNumberByBrowserFamily.put(browserFamily, line);
	}

	public int getColumn(BrowserType browserType) {
		int line = -1;
		if (!browserType.isColumnNumberInOnErrorAvailable()) {
			return -1;
		}
		if (columnNumberByBrowserType.containsKey(browserType)) {
			line = columnNumberByBrowserType.get(browserType);
		} else if (columnNumberByBrowserFamily.containsKey(browserType.getBrowserFamily())) {
			line = columnNumberByBrowserFamily.get(browserType.getBrowserFamily());
		} else {
			line = defaultColumn;
		}
		return line;
	}

	public void setColumn(int defaultColumn) {
		this.defaultColumn = defaultColumn;
	}

	public void setColumn(BrowserType browserType, int column) {
		columnNumberByBrowserType.put(browserType, column);
	}

	public void setColumn(BrowserFamily browserFamily, int column) {
		columnNumberByBrowserFamily.put(browserFamily, column);
	}

	public String getUserAction() {
		if (userActions.size() == 0) {
			return null;
		}
		int i = getNextRandom(0, userActions.size());
		return userActions.get(i);
	}

	public void setUserAction(String userAction) {
		this.userActions.add(userAction);
	}

	public final void addUserAction(String userAction) {
		this.userActions.add(userAction);
	}

	public final void setCode(String code) {
		this.code = code;
	}

	public String getCode(String code) {
		return code;
	}

	public String getLocationString(BrowserType browserType) {
		StringBuilder location = new StringBuilder();
		location.append(file);
		int line = getLine(browserType);
		int column = getColumn(browserType);
		if (line >= 0) {
			location.append("^p");
			location.append(line);
		}
		if (column >= 0) {
			location.append("^p");
			location.append(column);
		}
		return location.toString();
	}

	public BrowserSpecificJavaScriptError getBrowserSpecificJavaScriptError(BrowserType browserType) {

		BrowserSpecificJavaScriptError error = new BrowserSpecificJavaScriptError(getMessage(browserType), getFile(), getLine(browserType),
				getColumn(browserType), getStackTrace(browserType), getUserAction());
		if (browserType.getBrowserFamily() == BrowserFamily.IE) {
			error.setCode(code);
		}

		return error;
	}

	public static int getNextRandom(int min, int max) {
		return randomNumberGenerator.nextInt(max - min) + min;
	}

	protected String replaceDomain(String string) {
		if (string == null) {
			return null;
		}
		return string.replaceAll("<domain>", getDomain());
	}

	protected String getDomain() {
		try {
			URL url = new URL(currentPageUrl);
			return url.getProtocol() + "://" + url.getHost();
		} catch (MalformedURLException exception) {
			// return default value
		}

		return "http://localhost:8080";
	}

	protected final void setMessageForMobileIOSDevices(String message) {
		setMessage(BrowserType.IPHONE_40, message);
		setMessage(BrowserType.IPHONE_4S, message);
		setMessage(BrowserType.SAFARI_IPAD, message);
	}

	protected final void setMessageForMobileAndroidDevices(String message) {
		setMessage(BrowserType.ANDROID_22, message);
		setMessage(BrowserType.ANDROID_24, message);
		setMessage(BrowserType.ANDROID_403, message);
	}

	protected final void setMessageForMobileWindowsDevices(String message) {
		setMessage(BrowserType.WINDOWS_PHONE_7, message);
		setMessage(BrowserType.WINDOWS_PHONE_8, message);
		setMessage(BrowserType.WINDOWS_PHONE_81, message);
	}
}

