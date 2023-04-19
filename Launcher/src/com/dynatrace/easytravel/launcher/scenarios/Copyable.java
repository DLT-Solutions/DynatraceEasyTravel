package com.dynatrace.easytravel.launcher.scenarios;


public interface Copyable<T> {

    /**
     * Create copy of instance of <code>T</code>. The copy represents a <em>deep-copy</em> excepting
     * {@link String} members.
     * 
     * @return
     * @author martin.wurzinger
     */
    T copy();

}
