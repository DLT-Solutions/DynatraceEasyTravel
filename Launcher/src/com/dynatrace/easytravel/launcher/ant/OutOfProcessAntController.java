package com.dynatrace.easytravel.launcher.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.engine.AbstractStopListener;
import com.dynatrace.easytravel.launcher.engine.StopListener;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.process.JavaProcess;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.util.TextUtils;


public class OutOfProcessAntController extends AntController  {

    private static final Logger LOGGER = Logger.getLogger(OutOfProcessAntController.class.getName());
    private final AtomicReference<JavaProcess> process = new AtomicReference<JavaProcess>(null);

    public OutOfProcessAntController(File buildFile, String buildTarget) {
        super(buildFile, buildTarget);
    }

    @Override
    public void start() {
        synchronized (process) {
            if (process.get() != null) {
                LOGGER.warning(getAlreadyStartedLogMsg());
                return;
            }

            try {
                setRestartRequired(false); // reset the 'restart required' flag

                JavaProcess javaProcess = new JavaProcess(getJar());
                javaProcess.setMainClass(AntFork.class.getCanonicalName());
                javaProcess.setJavaArguments(getJavaOptions());

                AntFork.addArguments(javaProcess, this);

                javaProcess.addStopListener(new AbstractStopListener() {
                    @Override
                    public void notifyProcessStopped() {
                        JavaProcess javaProcess = process.get();
                        if (javaProcess == null) {
                            return; // already stopped
                        }

                        setRestartRequired(javaProcess.getExitValue() == AntFork.EXIT_STATUS_RESTART);

                        synchronized (process) {
                            process.set(null);
                        }

                        for (StopListener listener : runningSubject.getStopListeners()) {
                            listener.notifyProcessStopped();
                        }
                    }
                });

                process.set(javaProcess);

                javaProcess.start();
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Unable to start forked Ant controller", e);
            }
        }
    }

    @Override
    public void stopHard() {
        JavaProcess javaProcess = process.getAndSet(null);
        if (javaProcess == null) {
            return; // already stopped
        }
        LOGGER.log(Level.INFO, "Stop Ant fork immediatley");
        javaProcess.stop();
    }

    @Override
    public void stopSoft() {
        JavaProcess javaProcess = process.get();

        if (javaProcess == null) {
            return; // already stopped
        }

        try {
            final EasyTravelConfig CONFIG = EasyTravelConfig.read();
            UrlUtils.retrieveData(TextUtils.merge("http://localhost:{0}/{1}", Integer.toString(CONFIG.antForkHttpService), Constants.REST.SHUTDOWN));
            LOGGER.log(Level.INFO, "Shutdown request was sent to Ant fork");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to send shutdown request to Ant fork");
        }
    }

    @Override
    public boolean isProcessing() {
        JavaProcess javaProcess = process.get();

        return javaProcess != null && javaProcess.isRunning();
    }

    private static File getJar() {
        return new File(Directories.getInstallDir(), Constants.Modules.LAUNCHER);
    }

    private static String[] getJavaOptions() {
    	final EasyTravelConfig CONFIG = EasyTravelConfig.read();
        if (CONFIG.antJavaopts != null && CONFIG.antJavaopts.length > 0) {
            return CONFIG.antJavaopts;
        } else if (CONFIG.javaopts != null && CONFIG.javaopts.length > 0) {
            return CONFIG.javaopts;
        } else {
            return new String[0];
        }
    }

    @Override
    public boolean supportRestart() {
        return true;
    }


    @Override
    public boolean setContinuously(boolean continuously) {
        JavaProcess javaProcess = process.get();
        if (javaProcess == null) {
            return true; // already stopped
        }

        try {
        	final EasyTravelConfig CONFIG = EasyTravelConfig.read();
            String response = UrlUtils.retrieveData(TextUtils.merge("http://localhost:{0}/{1}/{2}", Integer.toString(CONFIG.antForkHttpService), Constants.REST.CONTINUOUSLY, continuously));
            LOGGER.log(Level.INFO, "SetContinuously request was sent to Ant fork");
            return Boolean.valueOf(response);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to send setContinuously request to Ant fork");
            return false;
        }
    }

}
