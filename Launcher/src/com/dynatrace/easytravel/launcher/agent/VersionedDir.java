package com.dynatrace.easytravel.launcher.agent;

import java.io.File;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * Helper class to detect the version number within a dynaTrace installation directory.
 *
 * @author martin.wurzinger
 */
public class VersionedDir implements Comparable<VersionedDir> {

    private final File directory;
    private final Version version;

    public VersionedDir(File directory) throws IllegalArgumentException {
        this(directory, BaseConstants.DOT);
    }

    public VersionedDir(File directory, String versionSeparator) throws IllegalArgumentException {
        if (directory == null || !directory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory argument.");
        }

        this.directory = directory;
        this.version = detectVersion(directory, versionSeparator);
    }

    public File getDirectory() {
        return directory;
    }

    public Version getVersion() {
        return version;
    }

    /**
     * Detect the version of a dynaTrace installation directory.
     *
     * @param directory a dynaTrace installation directory
     * @param versionSeparator the string that separates major, minor and revision parts within a
     *        version number
     * @return the detected version that must not be <code>null</code>
     * @throws IllegalArgumentException if the directory is no valid dynaTrace installation
     *         directory
     * @author martin.wurzinger
     */
    public static Version detectVersion(File directory, String versionSeparator) throws IllegalArgumentException {
        String name = directory.getName();
        if (name == null || !name.toLowerCase().startsWith(BaseConstants.DYNATRACE.toLowerCase())) {
            throw new IllegalArgumentException(TextUtils.merge("Directory argument must be the root of a {0} installation.", BaseConstants.DYNATRACE));
        }

        if (name.toLowerCase().startsWith(BaseConstants.DYNATRACE_AGENT.toLowerCase())) {
	        name = name.substring(BaseConstants.DYNATRACE_AGENT.length());
	        if (name == null || name.isEmpty()) {
	            throw new IllegalArgumentException(TextUtils.merge("Directory argument must be the root of a {0} installation.", BaseConstants.DYNATRACE_AGENT));
	        }
        } else {
	        name = name.substring(BaseConstants.DYNATRACE.length());
	        if (name == null || name.isEmpty()) {
	            throw new IllegalArgumentException(TextUtils.merge("Directory argument must be the root of a {0} installation.", BaseConstants.DYNATRACE));
	        }
        }

        name = name.trim();

        int wsIndex = name.indexOf(BaseConstants.WS);
        if (wsIndex >= 0) {
            name = name.substring(0, wsIndex);
        }

        // cut away dashes, which are used on Linux
        while(name.startsWith("-")) {
        	name = name.substring(1);
        }

        return new Version(name, versionSeparator);
    }


    @Override
    public int compareTo(VersionedDir other) {
        if (other == null) {
            return 1;
        }

        if (other == this) {
            return 0;
        }

        int versionComp = compareVersion(other);
        if (versionComp == 0) {
            return compareDirectory(other);
        }

        return versionComp;
    }

    private int compareVersion(VersionedDir other) {
        if (this.version == null) {
            if (other.version == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (other.version == null) {
                return 1;
            } else {
                return this.version.compareTo(other.version);
            }
        }
    }

    private int compareDirectory(VersionedDir other) {
        return this.directory.compareTo(other.directory);
    }
}