package com.dynatrace.easytravel;

import static com.dynatrace.easytravel.constants.BaseConstants.EMPTY_STRING;
import static com.dynatrace.easytravel.constants.BaseConstants.FSLASH;

import java.lang.management.ManagementFactory;

import com.dynatrace.easytravel.constants.BaseConstants;

class Config {
	
	private static final int DEFAULT_PORT = 27015;
	private static final String DEFAULT_FILE_NAME = "cpuload";
	
	private static final String PROPERTY_OS_ARCH = System.getProperty("os.arch");
	private static final String PROPERTY_OS_NAME = System.getProperty("os.name");
	
	private static final String BIN = "bin";
	private static final String LIN = "lnx";
	private static final String WIN = "win";
	
	
	private final String PROPERTY_PORT = new StringBuilder().append(NativeCPULoad.class.getName()).append(BaseConstants.DOT).append("port").toString();
	private final String PROPERTY_FILE = new StringBuilder().append(NativeCPULoad.class.getName()).append(BaseConstants.DOT).append("file").toString();

	String EXE_NAME = new StringBuilder().append(getSystemProperty(PROPERTY_FILE, DEFAULT_FILE_NAME)).append("-").append(getProcessId()).append(exe()).toString();
	int PORT = resolvePort();	

	private final int resolvePort() {
		String value = getSystemProperty(PROPERTY_PORT, String.valueOf(DEFAULT_PORT));
		try {
			int p = Integer.parseInt(value);
			if (p < 1024 || p > 65535) {
				NativeCPULoad.log.warn(new StringBuilder().append(value).append(" is not a valid port - using ").append(DEFAULT_PORT).toString());
				return DEFAULT_PORT;
			}
			return p;
		} catch (NumberFormatException t) {
			NativeCPULoad.log.warn(new StringBuilder().append(value).append(" is not a valid port - using ").append(DEFAULT_PORT).toString());
			return DEFAULT_PORT;
		}
		
	}
	
	/**
	 * @return the path for the resource within the class path for a suitable native binary
	 * 
	 * @author cwat-rpilz
	 */
	String getExecutablePath() {
		return new StringBuilder().append(BIN).append(FSLASH).append(os()).append(FSLASH).append(arch()).append(FSLASH).append("CPULoad").append(is64Bit()?"64":"").append(exe()).toString();
	}
	
	/**
	 * @return <tt>64</tt> if the current VM is 64 bit, 32 otherwise.
	 * 
	 * @author cwat-rpilz
	 */
	String arch() {
		return is64Bit() ? String.valueOf(64) : String.valueOf(32);
	}

	/**
	 * @return {@link #LIN} on Linux machines,
	 * 			{@link #WIN} on windows machines
	 * 
	 * @author cwat-rpilz
	 */
	String os() {
		return isLinux() ? LIN : WIN;
	}
	
	/**
	 * @return <tt>.exe</tt> on Windows machines,
	 * 			an empty string on other platforms.
	 * 
	 * @author cwat-rpilz
	 */
	String exe() {
		return isLinux() ? EMPTY_STRING : ".exe";
	}	
	
	private static final String getSystemProperty(String name, String defaultValue) {
		String value = System.getProperty(name);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}
	
	/**
	 * @return the process id of the current java vm or {@link System#currentTimeMillis()} as a fallback.
	 * 
	 * @author cwat-rpilz
	 */
	private static String getProcessId() {
		final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		final int index = jvmName.indexOf('@');
		if (index < 1) {
			return String.valueOf(System.currentTimeMillis());
	    }
	    try {
	        return Long.toString(Long.parseLong(jvmName.substring(0, index)));
	    } catch (NumberFormatException e) {
			return String.valueOf(System.currentTimeMillis());
	    }
	}
	
	/**
	 * @return <tt>true</tt> if the system property <tt>os.name</tt>
	 * contains the key word <tt>Linux</tt>, <tt>false</tt> otherwise.
	 * 
	 * @author cwat-rpilz
	 */
	static boolean isLinux() {
		return PROPERTY_OS_NAME != null && PROPERTY_OS_NAME.contains("Linux");
	}
	
	/**
	 * @return <tt>true</tt> if the system property <tt>os.arch</tt>
	 * contains the key word <tt>64</tt>, <tt>false</tt> otherwise.
	 * 
	 * @author cwat-rpilz
	 */
	private static boolean is64Bit() {
		return PROPERTY_OS_ARCH != null && PROPERTY_OS_ARCH.contains("64");
	}	
	
}
