package com.dynatrace.easytravel.config;


public class ConfigurationException extends Exception {

    private static final long serialVersionUID = -8363042516257146736L;

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
