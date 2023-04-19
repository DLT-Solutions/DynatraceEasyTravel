package com.dynatrace.easytravel.launcher.agent;

import java.util.logging.Logger;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.TextUtils;


public enum Architecture {

    UNKNOWN("lib"),
    BIT32("lib", "x86", "i386", "i686", "ppc"),
    BIT64("lib64", "x86_64", "amd64", "ia64", "ppc64");

    private static final Logger LOGGER = Logger.getLogger(Architecture.class.getName());

    final private String libDir;
    final private String[] osArchValues;

    private Architecture(String libdir, String... osArchValues) {
        this.libDir = libdir;
        this.osArchValues = osArchValues;
    }

    public String getLibDir() {
        return libDir;
    }

    /**
     * Pick up the bit count of current JVM by reading architecture property "os.arch".
     *
     * @return the architecture of the current JVM
     * @author martin.wurzinger
     */
    public static Architecture pickUp() throws IllegalStateException {
        String arch = System.getProperty(BaseConstants.SystemProperties.OS_ARCH);
        if (arch == null || arch.isEmpty()) {
            return UNKNOWN;
        }

        for (String osArchValue64bit : BIT64.osArchValues) {
            if (osArchValue64bit.equalsIgnoreCase(arch)) {
                return BIT64;
            }
        }

        for (String osArchValue32bit : BIT32.osArchValues) {
            if (osArchValue32bit.equalsIgnoreCase(arch)) {
                return BIT32;
            }
        }

        LOGGER.warning(TextUtils.merge("Unable to identify bit count from architecture property ''{0}''.", arch));

        return UNKNOWN;
    }
}
