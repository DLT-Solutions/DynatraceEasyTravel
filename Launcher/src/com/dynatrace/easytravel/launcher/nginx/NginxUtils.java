package com.dynatrace.easytravel.launcher.nginx;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.ProcessExecutor;
import com.dynatrace.easytravel.util.TextUtils;
import ch.qos.logback.classic.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author cwpl-rorzecho
 */
public class NginxUtils {
    private static final Logger LOGGER = LoggerFactory.make();

    public static final String NGINX_PARENT_DIR = "nginx";
    public static final String NGINX_ROOT_PATH = Directories.getInstallDir().getAbsolutePath() + "/" + NGINX_PARENT_DIR;

    public static final String NGINX_INSTALL_PATH_32 = NGINX_ROOT_PATH + "/nginx-eT-1.6.2-32";
    public static final String NGINX_INSTALL_PATH_64 = NGINX_ROOT_PATH + "/nginx-eT-1.6.2-64";

    /**
     * Available executable files
     */
    public enum NginxExecutable {
        nginx32,
        nginx64;
    }

    /**
     * Get executable nginx file with parent dir
     * @return
     */
    public static String getExecutable() {
        return NGINX_PARENT_DIR + "/" +  pickNginxExecutable();
    }

    public static String stopNginx() throws IOException {
        return exec(stopNginxInstruction());
    }


    private static String[] stopNginxInstruction() {
        final String[] STOP_NGINX_INSTRUCTION = {NGINX_ROOT_PATH + "/" + pickNginxExecutable(),"-c", NginxConfSetup.NGINX_CONFIG_FILE.getAbsolutePath(), "-s", "stop"};
        return STOP_NGINX_INSTRUCTION;
    }

    private static String exec(String[] instruction) {
        ProcessExecutor processExecutor = new ProcessExecutor(Runtime.getRuntime(), instruction);
        String cmdOutput = null;

        try {
            cmdOutput = processExecutor.getInputAsString(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.warn(TextUtils.merge("The execution of {0} was interrupted", Arrays.toString(instruction)), e);
        } catch (ExecutionException e) {
            LOGGER.warn(TextUtils.merge("An exception happened executing {0}", Arrays.toString(instruction)), e);
        } catch (TimeoutException e) {
            LOGGER.warn(TextUtils.merge("The execution of {0} did not return in time", Arrays.toString(instruction)), e);
        }

        return cmdOutput;
    }

    /**
     *  According to the distribution build get the right executable file.
     *  In the case when all executable files are available use 64bit version
     *
     * @return NginxExecutable
     * @author cwpl-rorzecho
     */
    public static NginxExecutable pickNginxExecutable() {
        NginxExecutable nginxExecutable = null;
        boolean allExecutableAvailable = true;
        for (NginxExecutable exec : NginxExecutable.values()) {
            File execFile = new File(NGINX_ROOT_PATH, exec.name());
            allExecutableAvailable &= execFile.exists();

            if (!execFile.exists()) {
                LOGGER.warn(TextUtils.merge("Executable nginx file {0} does not exists.", execFile.getAbsolutePath()));
                continue;
            }
            nginxExecutable = exec;
        }

        if (nginxExecutable == null) {
            throw new IllegalStateException("Cannot find nginx executable file");
        }

        // if all executable nginx files are available run 64bit one
        return allExecutableAvailable ? NginxExecutable.nginx64 : nginxExecutable;
    }
}
