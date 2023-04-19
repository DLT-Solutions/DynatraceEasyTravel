package com.dynatrace.easytravel.launcher.process;


public interface FailureListener {
    void notifyFailureOccured(Exception e);
}
