package com.dynatrace.easytravel.launcher.engine;


public interface RunningSubject {

    public void addStopListener(StopListener stopListener);

    public void removeStopListener(StopListener stopListener);

    public void clearStopListeners();

}
