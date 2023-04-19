/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: NetstatUtil.java
 * @date: 25.09.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.util;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.Validate;

import com.dynatrace.easytravel.constants.BaseConstants.SystemProperties;


/**
 * Uses the netstat tool to check which process is using a specific port.
 * Works currently only on Windows machines.
 *
 * @author stefan.moschinski
 */
public class NetstatUtil {
	private static final Logger log = Logger.getLogger(NetstatUtil.class.getName());

	private static final int NETSTAT_RESPONSE_TIMEOUT_SEC = 5;
	private static final String[] NETSTAT_CMD = new String[] { "netstat", SystemUtils.IS_OS_WINDOWS ? "-abno" : "-anp" };
	public static final String PROCESS_REGEX = "(\\[[\\.\\w]+\\]|Can not obtain ownership information)";
	public static final String PROCESS_REGEX_UNIX = "\\s[0-9]+/(.*)";
	public static final Pattern PROCESS_PATTERN = Pattern.compile(PROCESS_REGEX, Pattern.CASE_INSENSITIVE);
	public static final Pattern PROCESS_PATTERN_UNIX = Pattern.compile(PROCESS_REGEX_UNIX, Pattern.CASE_INSENSITIVE);

	private static final String IP_REGEX = "[a-f0-9:.]+";

	private final Runtime runtime;

	/**
	 * Creates a new {@link NetstatUtil} instance
	 *
	 * @param runtime the {@link Runtime} instance to use, must not be <code>null</code>
	 * @throws NullPointerException if the passed runtime is null
	 * @author stefan.moschinski
	 */
	public NetstatUtil(Runtime runtime) {
		this.runtime = Validate.notNull(runtime, "The runtime must not be null");
	}

	/**
	 *
	 * @param port port to check whether it is used by a process
	 * @return the process name that is using the port or <code>null</code> if no process was found, the used OS is not Windows, or Netstat
	 * checking was disabled by
	 * @author stefan.moschinski
	 */
	public String findProcessForPort(int port) {
		if (isNetstatPortCheckDisabled()) {
			log.info(format("Netstat checking is disabled via system property '%s'", SystemProperties.DISABLE_PORT_CHECK_VIA_NETSTAT));
			return null;
		}

		String netstatResp = getNetstatResponse(NETSTAT_RESPONSE_TIMEOUT_SEC, TimeUnit.SECONDS);
		if (netstatResp == null) {
			return null;
		}

		// Windows: TCP    127.0.0.1:9998         127.0.0.1:27942        TIME_WAIT       0\n" +
		// Linux:   tcp6       0      0 127.0.0.1:9092          127.0.0.1:58124         VERBUNDEN   1791240/java
		Pattern portRegex = Pattern.compile("(TCP|UDP|TCPv6|TCP6|UDPv6|UDP6)[0-9\\s]+?(" + IP_REGEX + "):" + port, Pattern.CASE_INSENSITIVE);
		Matcher matcher = portRegex.matcher(netstatResp);

		if (matcher.find()) {
			Matcher subMatcher = PROCESS_PATTERN.matcher(netstatResp);
			if (subMatcher.find(matcher.end())) {
				String processName = subMatcher.group(0);
				processName = StringUtils.removeStart(processName, "[");
				processName = StringUtils.removeEnd(processName, "]");

				return processName;
			} else {
				Matcher linuxMatcher = PROCESS_PATTERN_UNIX.matcher(netstatResp);
				if (linuxMatcher.find(matcher.end())) {
					return linuxMatcher.group(1).trim();
				}
			}
		}

		return null;
	}

	public String findProcessIdForPort(int port) {
		if (isNetstatPortCheckDisabled()) {
			log.info(format("Netstat checking is disabled via system property '%s'", SystemProperties.DISABLE_PORT_CHECK_VIA_NETSTAT));
			return null;
		}

		String netstatResp = getNetstatResponse(NETSTAT_RESPONSE_TIMEOUT_SEC, TimeUnit.SECONDS);
		if (netstatResp == null) {
			return null;
		}

		// Windows: TCP    127.0.0.1:9998         127.0.0.1:27942        TIME_WAIT       0\n" +
		// Linux:   tcp6       0      0 127.0.0.1:9092          127.0.0.1:58124         VERBUNDEN   1791240/java
		Pattern portRegex = Pattern.compile("(TCP|UDP|TCPv6|TCP6|UDPv6|UDP6)[0-9\\s]+?(" + IP_REGEX + "):" + port + ".*[A-Z ].*?([0-9]+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = portRegex.matcher(netstatResp);

		if (matcher.find()) {
			return matcher.group(3);
		}

		return null;
	}

	private boolean isNetstatPortCheckDisabled() {
		return Boolean.getBoolean(SystemProperties.DISABLE_PORT_CHECK_VIA_NETSTAT);
	}

	private String getNetstatResponse(int timeout, TimeUnit unit) {
		String netstatResp = null;
		try {
			netstatResp = getNetstatRespInternal(timeout, unit);
		} catch (InterruptedException e) {
			log.log(Level.WARNING, String.format("The execution of '%s' was interrupted", Arrays.toString(NETSTAT_CMD)), e);
		} catch (ExecutionException e) {
			log.log(Level.WARNING, String.format("An exeption happened executing '%s'", Arrays.toString(NETSTAT_CMD)),
					e.getCause());
		} catch (TimeoutException e) {
			log.log(Level.WARNING, String.format("The execution of '%s' did not return in time", Arrays.toString(NETSTAT_CMD)), e);
		}
		return netstatResp;
	}


	private String getNetstatRespInternal(int timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		ProcessExecutor netstatExec = new ProcessExecutor(runtime, NETSTAT_CMD);
		return netstatExec.getInputAsString(timeout, unit);
	}
}
