package com.dynatrace.easytravel.launcher.config;


/**
 * Created by IntelliJ IDEA.
 * User: cwpl-zsokolow
 * Date: 04.02.13
 * Time: 12:48
 */

/**
 * Notify if user interface has been changed and should UI be refreshed
 */
public interface UIChangeListener {
    /**
     * notifies registered observers
     * @param elem - UI elem changed
     */
    void notifyUIChanged(UIProperties elem);
}
