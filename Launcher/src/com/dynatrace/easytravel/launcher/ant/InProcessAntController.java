package com.dynatrace.easytravel.launcher.ant;

import java.io.File;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.launcher.engine.AbstractStopListener;
import com.dynatrace.easytravel.launcher.engine.StopListener;
import com.dynatrace.easytravel.util.TextUtils;


public class InProcessAntController extends AntController {

    private static final Logger LOGGER = Logger.getLogger(InProcessAntController.class.getName());
    private final AtomicReference<List<AntThread>> executor = new AtomicReference<List<AntThread>>(null);
    private final AtomicBoolean isStarting = new AtomicBoolean(true);

    public InProcessAntController(File buildFile, String buildTarget) {
        super(buildFile, buildTarget);

        addStopListener(new AbstractStopListener() {

            @Override
            public void notifyProcessStopped() {
                synchronized (executor) {
                    executor.set(null);
                }
            }
        });
    }

    @Override
    public void start() {
        isStarting.set(true);

        boolean alreadyStarted;
        synchronized (executor) {
            alreadyStarted = !executor.compareAndSet(null, createExecThreads());
        }

        if (alreadyStarted) {
            LOGGER.warning(getAlreadyStartedLogMsg());
            return;
        }

        setRestartRequired(false); // reset the 'restart required' flag
        int counter = 0;

        try {
            while (isStarting.get()) {
                synchronized (executor) {
                    AntThread antThread = getNextUnstartedThread(executor.get());
                    if (antThread == null) {
                        isStarting.set(false);
                    } else {
                        antThread.start();
                    }
                }

                counter++;
                if (counter < instances) {
                    Thread.sleep(startIntervalMs);
                }
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Starting multiple ant threads was canceled", e);
        }
    }

    /**
     * Find a thread that has not been started yet.
     * @param <T> a subtype of {@link Thread}
     * @param threads the collection of threads to look on
     * @return a thread that has not been started yet or <code>null</code> if no thread could be found that has not been start
     * @author martin.wurzinger
     */
    private <T extends Thread> T getNextUnstartedThread(Collection<T> threads) {
        if (threads == null) {
            return null;
        }

        for (T thread : threads) {
            if (thread.getState() == State.NEW) {
                return thread;
            }
        }

        // no unstarted thread found
        return null;
    }

    private List<AntThread> createExecThreads() {
        List<AntThread> antExecThreads = new ArrayList<AntThread>();
        for (int i = 0; i < instances; i++) {
            String name = TextUtils.merge("Ant script executor {0}", Integer.toString(i));
            AntThread execThread = new AntThread(name, buildFile, buildTarget, recurrence, recurrenceIntervalMs, properties);
            execThread.addStopListener(new StopListenerDelegate(execThread));
            antExecThreads.add(execThread);
        }
        return antExecThreads;
    }

    @Override
    public void stopHard() {
        isStarting.set(false);

        synchronized (executor) {
            List<AntThread> antExecThreads = executor.get();

            if (antExecThreads == null) {
                return; // already stopped
            }

            LOGGER.info("Stop all Ant executions immediately");
            for (AntThread antExecThread : antExecThreads) {
                antExecThread.interrupt();
            }

            executor.set(null);
        }
    }

    @Override
    public void stopSoft() {
        isStarting.set(false);

        synchronized (executor) {
            List<AntThread> antExecThreads = executor.get();
            if (antExecThreads == null) {
                return; // already stopped
            }

            LOGGER.info("Request all Ant executions to stop");
            for (AntThread antExecThread : antExecThreads) {
                antExecThread.softStop();
            }
        }
    }

    @Override
    public boolean isProcessing() {
        synchronized (executor) {
            List<AntThread> antThreads = executor.get();
            if (antThreads == null) {
                return false;
            }

            for (AntThread antThread : antThreads) {
                if (antThread.isAlive()) {
                    return true;
                }
            }
        }

        return false;
    }

    private void notifyStopListeners() {
        for (StopListener listener : runningSubject.getStopListeners()) {
            listener.notifyProcessStopped();
        }
    }

    private class StopListenerDelegate extends AbstractStopListener {

        private AntThread subject = null;

        public StopListenerDelegate(AntThread subject) {
            this.subject = subject;
        }

        @Override
        public void notifyProcessStopped() {
            synchronized (executor) {
                List<AntThread> antThreads = executor.get();
                if (antThreads == null) {
                    return;
                }

                antThreads.remove(subject);

                if (antThreads.isEmpty()) {
                    notifyStopListeners();
                }
            }

            if (subject.isVmErrorDetected()) {
                LOGGER.log(Level.WARNING, "Instruct restart of all Ant executions because of a severe VM error", subject.getVmError());
                setRestartRequired(true);
                stopHard();
                notifyStopListeners();
            }
        }
    }
    
    
    @Override
    public boolean setContinuously(boolean continuously) {
        for (AntThread antThread : executor.get()) {
            antThread.setContinuously(continuously);
        }
        return true;
    }
}
