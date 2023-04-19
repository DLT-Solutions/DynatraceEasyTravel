package com.dynatrace.easytravel.iptables;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.ProcessExecutor;
import com.dynatrace.easytravel.util.TextUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import ch.qos.logback.classic.Logger;

/**
 * Executes commands by creating child {@link Process}
 *
 * cwpl-rorzecho
 */
public class IptablesCmdExecutor implements CmdExecutor {
    private static final Logger LOGGER = LoggerFactory.make();

    @Override
    public String execute(String[] cmd) throws IOException {
        ProcessExecutor processExecutor = new ProcessExecutor(Runtime.getRuntime(), cmd);
        String cmdOutput = null;

        try {
            cmdOutput = processExecutor.getInputAsString(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.warn(TextUtils.merge("The execution of {0} was interrupted", Arrays.toString(cmd)), e);
        } catch (ExecutionException e) {
            LOGGER.warn(TextUtils.merge("An exception happened executing {0}", Arrays.toString(cmd)), e);
        } catch (TimeoutException e) {
            LOGGER.warn(TextUtils.merge("The execution of {0} did not return in time", Arrays.toString(cmd)), e);
        }

        return cmdOutput;
    }

}

