package com.dynatrace.easytravel.launcher.ant;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.easytravel.launcher.engine.RunningSubject;
import com.dynatrace.easytravel.launcher.engine.StopListener;
import com.dynatrace.easytravel.launcher.process.DefaultRunningSubject;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * Abstract base class for processing Ant files.
 *
 * There is an implementation for in-process execution of ant file, {@link InProcessAntController}
 * and an implementation for out-of-process execution, {@link OutOfProcessAntController}.
 *
 * @author martin.wurzinger
 */
public abstract class AntController implements RunningSubject {

    protected final DefaultRunningSubject runningSubject = new DefaultRunningSubject();
    protected final HashMap<String, String> properties = new HashMap<String, String>();

    protected File buildFile = null;
    protected String buildTarget = null;
    protected int instances = 1;
    protected long startIntervalMs = 0;
    protected int recurrence = 1;
    protected long recurrenceIntervalMs = 0;
    private final AtomicBoolean isRestartRequired = new AtomicBoolean(false);
    protected boolean continuously = false;

    protected AntController(File buildFile, String buildTarget) {
        if (buildFile == null || !buildFile.exists()) {
            throw new IllegalArgumentException("Invalid build file argument");
        }
        this.buildFile = buildFile;

        if (buildTarget == null || buildTarget.isEmpty()) {
            throw new IllegalArgumentException("Invalid build target argument");
        }
        this.buildTarget = buildTarget;
    }

    public abstract void start();

    public abstract void stopHard();

    public abstract void stopSoft();

    public abstract boolean isProcessing();

    public void addProperty(String name, String value) {
        properties.put(name, value);
    }

    @Override
    public void addStopListener(StopListener stopListener) {
        runningSubject.addStopListener(stopListener);
    }

    @Override
    public void removeStopListener(StopListener stopListener) {
        runningSubject.removeStopListener(stopListener);
    }

    @Override
    public void clearStopListeners() {
        runningSubject.clearStopListeners();
    }

    protected String getAlreadyStartedLogMsg() {
        return TextUtils.merge("The ant target ''{0}'' of ''{1}'' has already been started.", buildTarget, buildFile.getName());
    }

    public File getBuildFile() {
        return buildFile;
    }

    public String getBuildTarget() {
        return buildTarget;
    }

    public int getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(int recurrence) {
        if (recurrence < 0) {
            throw new IllegalArgumentException("Invalid recurrence argument");
        }
        this.recurrence = recurrence;
    }

    public long getRecurrenceIntervalMs() {
        return recurrenceIntervalMs;
    }

    public void setRecurrenceIntervalMs(long recurrenceIntervalMs) throws IllegalArgumentException {
        if (recurrenceIntervalMs < 0) {
            throw new IllegalArgumentException("Invalid recurrence interval argument");
        }
        this.recurrenceIntervalMs = recurrenceIntervalMs;
    }

    public int getInstances() {
        return instances;
    }

    /**
     * Set the number of ant instances. The number of instances must be 1 or higher.
     *
     * @param instances the number of ant instances to start
     * @throws IllegalArgumentException if argument is no positive value
     * @author martin.wurzinger
     */
    public void setInstances(int instances) throws IllegalArgumentException {
        if (instances <= 0) {
            throw new IllegalArgumentException("Invalid instance argument");
        }
        this.instances = instances;
    }

    public long getStartIntervalMs() {
        return startIntervalMs;
    }

    public void setStartIntervalMs(long startIntervalMs) {
        if (startIntervalMs < 0) {
            throw new IllegalArgumentException("Invalid start interval argument");
        }
        this.startIntervalMs = startIntervalMs;
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Check if the controlled ant instance requires a restart.
     * @return <code>true</code> if the controlled ant instance requires a restart
     * @author martin.wurzinger
     */
    public boolean isRestartRequired() {
        return isRestartRequired.get();
    }

    /**
     * Check if this controller supports to restart the ant.
     *
     * @return if Ant restarting is supported
     * @author martin.wurzinger
     */
    public boolean supportRestart() { // NOPMD
        return false;
    }

    protected void setRestartRequired(boolean isRestartRequired) {
        this.isRestartRequired.set(isRestartRequired);
    }

    /**
     *
     *
     * @param continuously
     * @return true if setting the new value succeeded
     * @author peter.kaiser
     */
    public boolean setContinuously(boolean continuously) {
        this.continuously = continuously;
        return true;
    }
}
