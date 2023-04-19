package com.dynatrace.easytravel.launcher.engine;


@SuppressWarnings("serial")
public class CorruptInstallationException extends Exception {

    public CorruptInstallationException(String message) {
        super(message);
    }

    public CorruptInstallationException(Throwable cause) {
        super(cause);
    }

    public CorruptInstallationException(String message, Throwable cause) {
        super(message, cause);
    }
}
