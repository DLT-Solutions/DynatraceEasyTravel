package com.dynatrace.easytravel.launcher.agent;

import com.dynatrace.easytravel.constants.BaseConstants;


public enum OperatingSystem {

    UNKNOWN(BaseConstants.EMPTY_STRING, BaseConstants.EMPTY_STRING, BaseConstants.UNKNOWN, false),

    /* Windows 7, Windows 2000, Windows 95, Windows 98, Windows NT, Windows Vista, Windows XP */
    WINDOWS(".dll", ".exe", "Windows", true, "win"),

    /* Linux */
    LINUX(".so", BaseConstants.EMPTY_STRING, "Linux", true, "linux"),

    /* Mac OS X */
    MAC_OS(".so", BaseConstants.EMPTY_STRING, "Mac OS", true, "mac"),

    /* some other but not supported operating systems */
    AIX(".so", BaseConstants.EMPTY_STRING, "AIX", false, "aix"),
    UNIX(".so", BaseConstants.EMPTY_STRING, "Unix", false, "unix", "digital unix"),
    FREE_BSD(BaseConstants.EMPTY_STRING, BaseConstants.EMPTY_STRING, "Free BSD", false, "freebsd"),
    HP_UX(BaseConstants.EMPTY_STRING, BaseConstants.EMPTY_STRING, "HP Unix", false, "hp"),
    IRIX(BaseConstants.EMPTY_STRING, BaseConstants.EMPTY_STRING, "Irix", false, "irix"),
    OS_2(BaseConstants.EMPTY_STRING, BaseConstants.EMPTY_STRING, "OS/2", false, "os/2"),
    SOLARIS(".so", BaseConstants.EMPTY_STRING, "Solaris", false, "solaris");

    // static shortcut for windows as we mostly use this combination
    final public static boolean IS_WINDOWS = WINDOWS == pickUp();

    final private String libraryExtension;
    final private String executableExtension;
    final private String name;
    final private boolean isSupported;
    final private String[] namePrefixes;

    /**
     *
     * @param libraryExtension
     * @param name
     * @param isSupported
     * @param osNamePrefixes name prefix of the operating system to match in <em<lower case</em>
     * @author martin.wurzinger
     */
    private OperatingSystem(String libraryExtension, String executableExtension, String name, boolean isSupported, String... osNamePrefixes) {
        this.libraryExtension = libraryExtension;
        this.executableExtension = executableExtension;
        this.name = name;
        this.isSupported = isSupported;
        this.namePrefixes = osNamePrefixes;
    }

    public String getLibraryExtension() {
        return libraryExtension;
    }

    public String getExecutableExtension() {
        return executableExtension;
    }

    public static OperatingSystem pickUp() {
        String osName = System.getProperty(BaseConstants.SystemProperties.OS_NAME);
        if (osName == null || osName.isEmpty()) {
            return UNKNOWN;
        }

        osName = osName.toLowerCase(); // NOPMD

        for (OperatingSystem os : values()) {
            if (os == UNKNOWN) {
                continue;
            }

            for (String prefix : os.namePrefixes) {
                if (osName.startsWith(prefix)) {
                    return os;
                }
            }
        }

        return UNKNOWN;
    }

    public static boolean isCurrent(OperatingSystem operatingSystem) {
        return pickUp() == operatingSystem;
    }

    public boolean isSupported() {
        return isSupported;
    }

    public String getName() {
        return name;
    }

    public static String getCurrentExecutableExtension() {
        return OperatingSystem.pickUp().getExecutableExtension();
    }
}
