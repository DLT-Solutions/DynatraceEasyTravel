package com.dynatrace.easytravel.config;


/**
 * Define modes in which easyTravel can run. Currently Installationtype.Classic and
 * InstallationType.APM are supported.
 *
 *  Special installation types for "Both" and "Unknown" are available for special cases.
 *
 * @author cwat-rpilz
 */
public enum InstallationType {
	Classic((byte)1),
	APM((byte)2),

	// Both means we allow either Classic or APM, so we bitwise-combine the value for APM and for Classic
	Both((byte)(1|2)),

	// Unknown is used for cases where the mode is not known yet.
	Unknown((byte)0);

	private final byte bits;

	private InstallationType(byte bits) {
		this.bits = bits;
	}

	/**
	 * Returns if the current InstallationType and the given InstallationType
	 * match, i.e.
	 *
	 * Classic and APM never match
	 * Both will match with either Classic or APM
	 * Unknown or null will not match with anything, not even itself!
	 *
	 * @param other
	 * @return
	 * @author cwat-dstadler
	 */
	public boolean matches(InstallationType other) {
		if (other == null) {
			return false;
		}
		return (bits & other.bits) != 0;
	}

	/**
	 * Parse the String into an InstallationType, use InstallationType.Both
	 * if unsure.
	 *
	 * @param type
	 * @return
	 * @author cwat-dstadler
	 */
	public static InstallationType fromString(String type) {
        if (type == null || InstallationType.Both.toString().equals(type)) {
        	return InstallationType.Both;
        } else if (InstallationType.Classic.toString().equals(type)) {
        	return InstallationType.Classic;
        } else if (InstallationType.APM.toString().equals(type)) {
        	return InstallationType.APM;
        }

    	return InstallationType.Both;
	}
}
