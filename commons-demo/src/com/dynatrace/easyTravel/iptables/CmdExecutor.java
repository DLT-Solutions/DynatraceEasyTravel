package com.dynatrace.easytravel.iptables;

import java.io.IOException;

/**
 * Interface for command executors
 *
 * cwpl-rorzecho
 */
public interface CmdExecutor {

    /**
     * Execute command
     *
     * @param cmd
     * @return
     * @throws IOException
     */
    String execute(String[] cmd) throws IOException;

}
