package com.dynatrace.easytravel.launcher.agent;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.util.TextUtils;


public class AgentDetector {
    private static final String DYNATRACE_INSTALL_DIR_REGEX = TextUtils.merge("(?i)(?:{0})[\\s-](Agent[\\s-])?\\d*(\\{1}\\d)*.*",
    		BaseConstants.DYNATRACE, BaseConstants.DOT);
    /*private static final String DYNATRACE_INSTALL_DIR_REGEX = TextUtils.merge("(?i)(?:{0}|{1})[\\s-](Agent[\\s-])?\\d*(\\{2}\\d)*.*",
    		BaseConstants.DYNATRACE, BaseConstants.COMPUWARE, BaseConstants.DOT);*/

    private static final Logger LOGGER = Logger.getLogger(AgentDetector.class.getName());
    private static final FileFilter DT_DIR_FILTER = createDynaTraceDirFilter();

    private final Architecture architecture;
    private final OperatingSystem operatingSystem;

    private File installDir = Directories.getInstallDir();

    private static final Comparator<VersionedDir> DESC_COMPARATOR = new Comparator<VersionedDir>() {
        @Override
        public int compare(VersionedDir versionDirA, VersionedDir versionDirB) {
            return -versionDirA.compareTo(versionDirB);
        }
    };

    public AgentDetector() {
        Architecture arch = Architecture.pickUp();
        if (arch == Architecture.UNKNOWN) {
            LOGGER.warning("Unknown bit count: resume with 32 bit settings.");
            arch = Architecture.BIT32;
        }

        OperatingSystem operatingSystem = OperatingSystem.pickUp();
        if (!operatingSystem.isSupported()) {
            throw new UnsupportedOperationException(TextUtils.merge("The operating system ''{0}'' is not supported.", operatingSystem.getName()));
        }

        this.architecture = arch;
        this.operatingSystem = operatingSystem;
    }

    public AgentDetector(Architecture architecture, OperatingSystem operatingSystem) {
        this.architecture = architecture;
        this.operatingSystem = operatingSystem;
    }

    public AgentDetector(Architecture architecture) {
    	this(architecture, OperatingSystem.pickUp());
    }

    public void setInstallDir(File installDir) {
        this.installDir = installDir;
    }

    public File getAgent(Technology technology) {
        final String agentSubPath = getAgentSubpath(technology);

        File devDir = getAgentLookupDirForDevs();
        if (devDir != null) {
	        File devAgent = new File(devDir, agentSubPath);
	        if (devAgent.exists()) {
	            return devAgent;
	        }
        }

        for (File dtInstallDir : detectDtInstallDirs()) {
            File agent = new File(dtInstallDir, agentSubPath); // NOPMD

            if (agent.exists()) {
                return agent;
            }
        }

        return null;
    }

    public String getAgentSubpath(Technology technology) {
        return BaseConstants.SubDirectories.AGENT + File.separator + architecture.getLibDir() + File.separator + technology.getAgentName(operatingSystem) + operatingSystem.getLibraryExtension();
    }

    public List<File> detectDtInstallDirs() {
        List<File> dynaTraceProgramsDirs = detectDynaTraceDirs(getAgentLookupDir());
        if (dynaTraceProgramsDirs == null) {
            return Collections.emptyList();
        }

        return detectDtInstallDirsByProgDir(dynaTraceProgramsDirs);
    }

    private List<File> detectDtInstallDirsByProgDir(Collection<File> dynaTraceProgramsDirs) {
        if (dynaTraceProgramsDirs == null) {
            return Collections.emptyList();
        }

        Collection<File> dtInstallDirs = getDtInstallationDirectories(dynaTraceProgramsDirs);

        // create a list of VersionedDir instance in order to sort it by version
        List<VersionedDir> sortingDirs = new ArrayList<VersionedDir>();
        for (File dtInstallDir : dtInstallDirs) {
            try {
                sortingDirs.add(new VersionedDir(dtInstallDir)); // NOPMD
            } catch (IllegalArgumentException iae) {
                LOGGER.log(Level.FINE, "Unable to detect dynaTrace installation directory: " + dtInstallDir.getAbsolutePath(), iae);
            }
        }

        // sorts the install directories in descending order
        Collections.sort(sortingDirs, DESC_COMPARATOR);

        // create the result list
        List<File> sortedDtInstallDirs = new ArrayList<File>(sortingDirs.size());
        for (VersionedDir versionedDir : sortingDirs) {
            sortedDtInstallDirs.add(versionedDir.getDirectory());
        }
        LOGGER.fine("Had agent directories: " + sortedDtInstallDirs);

        return sortedDtInstallDirs;
    }

    /**
     * Get all installation directories of dynaTrace products within the specified program
     * directories.
     *
     * @param dynaTraceProgramsDirs
     * @return all dynaTrace installation directories or an empty list if none was found
     * @author martin.wurzinger
     */
    private Collection<File> getDtInstallationDirectories(Collection<File> dynaTraceProgramsDirs) {
        List<File> result = new ArrayList<File>();

        for (File dynaTraceProgramsDir : dynaTraceProgramsDirs) {
            if (dynaTraceProgramsDir == null || !dynaTraceProgramsDir.exists()) {
                continue;
            }

            // select only directories that matches a certain regex pattern
            File[] dtInstallDirs = dynaTraceProgramsDir.listFiles(DT_DIR_FILTER);
            if (dtInstallDirs != null) {
                Collections.addAll(result, dtInstallDirs);
            }
        }

        return result;
    }

    /**
     * Find the parent directory that represents the dynaTrace programs directory. On windows
     * machines it is typically called "dynaTrace". On Linux machines there does not exist a
     * standard programs directory. In this case we try to find a directory that contains dynaTrace
     * installation directories like "dynatrace-3.5.1" or "dynaTrace Agent 4.0.0".
     *
     * @param installDir a directory or file that is located within the dynaTrace programs directory
     * @return the dynaTrace programs directory or <code>null</code> if the path root is reached and
     *         no matching directory could be found
     * @author martin.wurzinger
     */
    private File detectDynaTraceDir(File installDir) {
    	File currentDirectory = installDir;

    	// first look for "Compuware" as this is used since version 5.5
        /*while (currentDirectory != null) {

	         * if current directory is called "Compuware" then dynaTrace programs directory was
	         * found (typical for dynaTrace installation for Windows systems starting with 2013 Spring)

	        if (BaseConstants.COMPUWARE.equalsIgnoreCase(currentDirectory.getName())) {
	            return currentDirectory;
	        }


             * if current directory has a sibling directory which represents a install directory of
             * Compuware which should contain agents

	        File dir = new File(currentDirectory.getParentFile(), BaseConstants.COMPUWARE);
			if(dir.exists()) {
	        	return dir;
	        }


             * if current directory itself is called like "dynaTrace 3.5.1" then parent directory is
             * used as dynaTrace programs directory (typical for dynaTrace installations for Linux
             * systems)

            if (currentDirectory.getName().matches(DYNATRACE_INSTALL_DIR_REGEX)) {
                return currentDirectory.getParentFile();
            }

	        currentDirectory = currentDirectory.getParentFile();
        }*/

        // start again, now looking for "dynaTrace" which was used up to 5.0
        currentDirectory = installDir;

        while (currentDirectory != null) {

            /*
             * if current directory is called "dynaTrace" then dynaTrace programs directory was
             * found (typical for dynaTrace installation for Windows systems)
             */
            if (BaseConstants.DYNATRACE.equalsIgnoreCase(currentDirectory.getName())) {
                return currentDirectory;
            }

            /*
             * if current directory has a sibling directory which represents a install directory of
             * a dynaTrace product which should contain agents
             */
            if (hasDynaTraceSibling(currentDirectory)) {
                return currentDirectory.getParentFile();
            }

            /*
             * if current directory itself is called like "dynaTrace 3.5.1" then parent directory is
             * used as dynaTrace programs directory (typical for dynaTrace installations for Linux
             * systems)
             */
            if (currentDirectory.getName().matches(DYNATRACE_INSTALL_DIR_REGEX)) {
                return currentDirectory.getParentFile();
            }

            currentDirectory = currentDirectory.getParentFile();
        }

        return currentDirectory;
    }

    private boolean hasDynaTraceSibling(File directory) {
        if (directory == null) {
            return false;
        }

        File parent = directory.getParentFile();
        if (parent == null) {
            return false;
        }

        File[] children = parent.listFiles(DT_DIR_FILTER);
        if (children == null) {
            return false;
        }

        for (File child : children) {
            if (child == directory) {
                continue; // dir is not a sibling
            }
            return true; // dir has at least one dt sibling dir
        }

        return false;
    }

    /**
     * Get the dynaTrace installation directories. It returns a list because 64 bit Windows systems
     * have two programs directories. The first entry of the list represents the dynaTrace directory
     * the demo application is installed in. The further entries represent alternative directories.
     * Note that alternative directories are possibly not existing.
     *
     * @param installDir the dynaTrace installation directory
     * @return an ordered list of dynaTrace installation directories
     * @author martin.wurzinger
     */
    private List<File> detectDynaTraceDirs(File installDir) {
        List<File> result = new ArrayList<File>();

        File dynaTraceDir = detectDynaTraceDir(installDir);
        if (dynaTraceDir == null) {
            return Collections.emptyList();
        }

        result.add(dynaTraceDir);

        if (OperatingSystem.pickUp() == OperatingSystem.WINDOWS) {
            // identify dynaTrace directory in 'other' programs directory ("Program Files" and "Program Files (x86)" on 64 bit Windows)
            File otherDynaTraceDir = getOtherWinProgDir(dynaTraceDir.getParentFile());
            if (otherDynaTraceDir != null) {
                result.add(new File(otherDynaTraceDir, dynaTraceDir.getName()));
            }
        }

        return result;
    }

    /**
     * Method to be used only on Windows. Get the 'other' program files directory - with or without
     * " (x86)".
     */
    private File getOtherWinProgDir(File progDir) {
        if (progDir == null || !progDir.exists()) {
            return null;
        }

        if (progDir.getName().endsWith(Constants.Misc.WIN_PROGRAMS_DIR_X86_EXTENSION)) {
            String progDirPath = progDir.getAbsolutePath();
            String otherProgDirPath = progDirPath.substring(0, Math.max(0, progDirPath.length() - Constants.Misc.WIN_PROGRAMS_DIR_X86_EXTENSION.length()));

            File otherProgDir = new File(otherProgDirPath);
            if (otherProgDir.exists()) {
                return otherProgDir;
            }
        } else {
            String progDirPath = progDir.getAbsolutePath();
            String otherProgDirPath = progDirPath + Constants.Misc.WIN_PROGRAMS_DIR_X86_EXTENSION;

            File otherProgDir = new File(otherProgDirPath);
            if (otherProgDir.exists()) {
                return otherProgDir;
            }
        }

        return null;
    }

	/**
	 * Get the easyTravel installation directory or the simulated dynaTrace
	 * installation directory if it is set via system property
	 * "com.dynatrace.easytravel.agent.lookup.dir" (useful in development
	 * stage).
	 *
	 * @return the easyTravel installation directory or the simulated dynaTrace
	 *         installation directory.
	 *
	 * @author martin.wurzinger
	 */
	private File getAgentLookupDir() {
		return getCustomizableDir(BaseConstants.SystemProperties.AGENT_LOOKUP_DIR, installDir);
	}

    /**
     * If the system property "com.dynatrace.easytravel.agent.lookup.dir.dev" is set it will be used
     * as the agent lookup directory (useful in development stage).
     * @return the directory if an existing directory is specified via system property or <code>null</code> otherwise
     * @author martin.wurzinger
     */
    private File getAgentLookupDirForDevs() {
    	return getCustomizableDir(BaseConstants.SystemProperties.AGENT_LOOKUP_DIR_DEV, null);
    }

    private File getCustomizableDir(String systemPropertyName, File defaultDir) {
        String dtAgentLookupPath = StringUtils.trimToNull(System.getProperty(systemPropertyName));

        if (dtAgentLookupPath != null && !dtAgentLookupPath.isEmpty()) {
            File dtAgentLookupDir = new File(dtAgentLookupPath);
            if (dtAgentLookupDir.exists()) {
                return dtAgentLookupDir;
            }
        }

        return defaultDir;
    }

    private static FileFilter createDynaTraceDirFilter() {
        IOFileFilter dirFilter = DirectoryFileFilter.DIRECTORY;
        IOFileFilter dynaTraceFilter = new RegexFileFilter(DYNATRACE_INSTALL_DIR_REGEX);

        return new AndFileFilter(dirFilter, dynaTraceFilter);
    }

}
