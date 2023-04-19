package com.dynatrace.easytravel.launcher.agent;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;

public class Version implements Comparable<Version> {

    private final int major;
    private final int minor;
    private final int revision;

    private final boolean hasMajor;
    private final boolean hasMinor;
    private final boolean hasRevision;

    private final String separator;

    /**
     * Instantiate a {@link Version} out of a string representation. The string representation has
     * to use a dot separating major, minor and revision part.
     *
     * @param versionString the string representation of a version
     * @throws IllegalArgumentException if the argument is null or no valid string representation of
     *         a version
     * @author martin.wurzinger
     */
    public Version(String versionString) throws IllegalArgumentException {
        this(versionString, BaseConstants.DOT);
    }

    /**
     * Instantiate a {@link Version} out of a string representation.
     *
     * @param versionString the string representation of a version
     * @param separator the String that separates major, minor and revision parts of the version
     * @throws IllegalArgumentException if the argument is null or no valid string representation of
     *         a version
     * @author martin.wurzinger
     */
    public Version(String versionString, String separator) throws IllegalArgumentException {
        if (versionString == null || versionString.isEmpty()) {
            throw new IllegalArgumentException("Empty version string specified.");
        }

        this.separator = separator;

        int major = 0;
        int minor = 0;
        int revision = 0;
        boolean hasMajor = false;
        boolean hasMinor = false;
        boolean hasRevision = false;

        String version = versionString.trim();
        if (version == null || version.isEmpty() || version.startsWith(separator)) {
            throw new IllegalArgumentException("Invalid version format");
        }

        try {
            StringTokenizer tokenizer = new StringTokenizer(version, separator);
            major = Integer.parseInt(tokenizer.nextToken().trim());
            hasMajor = true;

            if (tokenizer.hasMoreTokens()) {
                minor = Integer.parseInt(tokenizer.nextToken().trim());
                hasMinor = true;

                if (tokenizer.hasMoreTokens()) {
                    revision = Integer.parseInt(tokenizer.nextToken().trim());
                    hasRevision = true;
                }
            }
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Invalid version format", e);
        }

        if(major < 0 || minor < 0 || revision < 0) {
        	throw new IllegalArgumentException("Detected invalid version-number for string '" + versionString + "', " +
        			"found major: " + major + ", minor: " + minor + ", revision: " + revision);
        }

        // assignment is done via separate local variables to have the fields "final" here
        this.major = major;
        this.minor = minor;
        this.revision = revision;

        this.hasMajor = hasMajor;
        this.hasMinor = hasMinor;
        this.hasRevision = hasRevision;
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

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        if (!hasMajor) {
            return MessageConstants.UNKNOWN_VERSION;
        }
        result.append(this.major);

        if (!hasMinor) {
            return result.toString();
        }
        result.append(separator);
        result.append(this.minor);

        if (!hasRevision) {
            return result.toString();
        }
        result.append(separator);
        result.append(this.revision);

        return result.toString();
    }

    @Override
    public int compareTo(Version other) {
        if (other == null) {
            return 1;
        } else {
            return compareMajor(other);
        }
    }

    private int compareMajor(Version other) {
        if (this.hasMajor) {
            if (other.hasMajor) {
                int majorComp = this.major - other.major;
                if (majorComp == 0) {
                    return compareMinor(other);
                } else {
                    return majorComp;
                }
            } else {
                return 1;
            }
        } else {
            if (other.hasMajor) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private int compareMinor(Version other) {
        if (this.hasMinor) {
            if (other.hasMinor) {
                int minorComp = this.minor - other.minor;
                if (minorComp == 0) {
                    return compareRevision(other);
                } else {
                    return minorComp;
                }
            } else {
                return 1;
            }
        } else {
            if (other.hasMinor) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private int compareRevision(Version other) {
        if (this.hasRevision) {
            if (other.hasRevision) {
                return this.revision - other.revision;
            } else {
                return 1;
            }
        } else {
            if (other.hasRevision) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}