package com.dynatrace.easytravel.launcher.procedures;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.misc.Constants;


/**
 * <p>
 * By this class the Derby DB is customized in order to write the derby log file to the easyTravel log directory.
 * This class is designed to be used in combination with derby's system property <code>derby.stream.error.method</code>.
 * </p>
 * <p>
 * Please note: If the Derby log file is not used any more, you have to close it by calling {@link #closeWriter()}!
 * Otherwise the file handler remains open.
 * </p>
 *
 * @author martin.wurzinger
 */
public final class DerbyLogger {

    public static final Logger LOGGER = Logger.getLogger(DerbyLogger.class.getName());
    private static final Writer LOG_WRITER = getDbLogWriter();

    private DerbyLogger() {
    }

    private static final String METHOD_NAME_GET_LOG_OUTPUT_STREAM = "getLogWriter";

    /**
     * Get the writer the derby database has to write it's log messages to.
     * @return the Derby log file writer
     * @author martin.wurzinger
     */
    public static Writer getLogWriter() {
        return LOG_WRITER;
    }

    /**
     * Get the name of the method that returns the log output stream.
     *
     * @return the name of the log writer or <code>null</code> if no one could be found
     * @author martin.wurzinger
     */
   public static String getLogMethod() {
       try {
           Method logMethod = DerbyLogger.class.getMethod(METHOD_NAME_GET_LOG_OUTPUT_STREAM);

           return DerbyLogger.class.getCanonicalName() + BaseConstants.DOT + logMethod.getName();
       } catch (SecurityException e) {
           LOGGER.severe("Not allowed to load Derby log method.");
       } catch (NoSuchMethodException e) {
           LOGGER.warning("Derby log method not found.");
       }

       return null;
   }

    private static PrintWriter getDbLogWriter() {
        File dbLogFile = new File(Directories.getExistingLogDir(), Constants.Misc.DERBY_LOG_FILE);

        try {
            return new PrintWriter(dbLogFile);
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "The derby log destination file cannot be found.", e);
            return null;
        }
    }

    /**
     * Close Derby log file writer.
     * @author martin.wurzinger
     */
    public static void closeWriter() {
        try {
            LOG_WRITER.close();
            LOGGER.fine("The Derby log writer was successfully closed.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to close Derby log writer", e);
        }
    }
}
