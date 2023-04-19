package com.dynatrace.easytravel.launcher.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dynatrace.easytravel.launcher.engine.RunningSubject;
import com.dynatrace.easytravel.launcher.engine.StopListener;


public class DefaultRunningSubject implements RunningSubject {

    private final List<StopListener> stopListeners = new ArrayList<StopListener>();

    @Override
    public void addStopListener(StopListener stopListener) {
        stopListeners.add(stopListener);
    }

    @Override
    public void removeStopListener(StopListener stopListener) {
        stopListeners.remove(stopListener);
    }

    @Override
    public void clearStopListeners() {
        stopListeners.clear();
    }

    public List<StopListener> getStopListeners() {
        return Collections.unmodifiableList(stopListeners);
    }

}
