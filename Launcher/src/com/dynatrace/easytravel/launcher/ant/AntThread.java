package com.dynatrace.easytravel.launcher.ant;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.listener.CommonsLoggingListener;
import org.apache.tools.ant.taskdefs.Ant;

import com.dynatrace.easytravel.launcher.engine.StopListener;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * A Thread which handles execution of an ant build in a separate thread.
 * It directly uses the Ant classes and starts an in-process ant build.
 *
 * It can also call the targeted repeatedely, either for a number of times or endlessly.
 *
 *  Furthermore stop-handling is provided and listeners can be informed about stop activity.
 *
 * @author martin.wurzinger
 */
public class AntThread extends Thread {

	private static final Logger LOGGER = Logger.getLogger(AntThread.class.getName());

	private final AtomicBoolean isStopRequested = new AtomicBoolean(false);
	private final List<StopListener> stopListeners = new ArrayList<StopListener>();

	private final Ant ant;
	private final CommonsLoggingListener commonsLogging;
	private final VmErrorSupervisor vmErrorSupervisor = new VmErrorSupervisor(this);
	private final int recurrence;
	private final long intervalMs;

	private final String file; // required for logging
	private final String target; // required for logging

	private volatile boolean continuously;

	private static Set<String> IGNORE_ERROR_MSGS = new HashSet<String>(Arrays.asList("startrecording", "stoprecording"));

	/**
	 * Creates the AntThread with all parameters that are necessary for execution.
	 *
	 * @param name The name of the thread.
	 * @param buildFile The build-file, needs to be an absolute path so that getParentFile() returns a useful value.
	 * @param buildTarget The target to call.
	 * @param recurrence How many times to execute the target, 0 means endlessly.
	 * @param intervalMs How long to wait between repeated executions.
	 * @param properties A Map with properties, null is not allowed, provide an empty Map if no properties are set
	 */
	public AntThread(String name, File buildFile, String buildTarget, int recurrence, long intervalMs, Map<String, String> properties) {
		super(name);
		setDaemon(true);

		this.recurrence = recurrence;
		this.intervalMs = intervalMs;

		this.file = buildFile.getName();
		this.target = buildTarget;

		commonsLogging = new FailureFilteringLoggingListener();
		commonsLogging.setMessageOutputLevel(Project.MSG_INFO);
		commonsLogging.setOutputPrintStream(System.out);
		commonsLogging.setErrorPrintStream(System.err);

		Project project = new Project();
		project.setBaseDir(buildFile.getParentFile());
		project.addBuildListener(commonsLogging);
		project.addBuildListener(vmErrorSupervisor);
		project.init();
		addProperties(project, properties);

		ant = new Ant();
		ant.setProject(project);
		ant.setDir(buildFile.getParentFile());
		ant.setAntfile(buildFile.getAbsolutePath());
		ant.setTarget(buildTarget);
	}

	public void addStopListener(StopListener stopListener) {
		synchronized (stopListeners) {
			stopListeners.add(stopListener);
		}
	}

	private void addProperties(Project project, Map<String, String> properties) {
		if (project == null || properties == null) {
			throw new IllegalArgumentException("Arguments must not be null.");
		}
		for (Map.Entry<String, String> property : properties.entrySet()) {
			project.setProperty(property.getKey(), property.getValue());
		}
	}

	@Override
	public void run() {
		LOGGER.info(TextUtils.merge("{0}: Start execution of target ''{1}'' in ''{2}''.", getName(), target, file));

		// initialize ant logging
		commonsLogging.buildStarted(null);

		try {
			for (int iteration = 0; !isFinished(iteration); iteration++) {

				// execute ant task
				ant.execute();

				// skip sleeping if ant has not to do a further execution
				if (isFinished(iteration + 1)) {
					break;
				}

				sleep(Math.max(0, intervalMs));
			}
		} catch (InterruptedException ie) {
			LOGGER.info(TextUtils.merge("{0}: Waiting for next execution of target ''{1}'' in ''{2}'' was interrupted.", getName(), target, file));
			vmErrorSupervisor.superviseException(ie);
		} catch (BuildException be) {
			String message = TextUtils.merge("{0}: Ant build failed. Target ''{1}'' in file ''{2}''.", getName(), target, file);
			LOGGER.log(Level.WARNING, message, be);
			vmErrorSupervisor.superviseException(be);
		} catch (Throwable t) {	// NOSONAR - on purpose here to report all things that happen in the thread
			String message = TextUtils.merge("{0}: Execution of Ant target ''{1}'' in file ''{2}'' had to be stopped because of a severe problem.", getName(), target, file);
			LOGGER.log(Level.SEVERE, message, t);
			vmErrorSupervisor.superviseException(t);
		}

		LOGGER.info(TextUtils.merge("{0}: Finished execution of target ''{1}'' in ''{2}''.", getName(), target, file));

		notifyStopListeners();
	}

	private void notifyStopListeners() {
		ArrayList<StopListener> listenersClone = new ArrayList<StopListener>();
		synchronized (stopListeners) {
			listenersClone.addAll(stopListeners);
		}

		for (StopListener listener : listenersClone) {
			listener.notifyProcessStopped();
		}
	}

	public void softStop() {
		isStopRequested.set(true);
	}

	private boolean isStopRequested() {
		return isStopRequested.get();
	}

	private boolean isFinished(int iteration) {
		if (isVmErrorDetected()) {
			return true;
		}

		if (isStopRequested()) {
			return true;
		}

		if (isInterrupted()) {
			return true;
		}

		if (recurrence <= 0 || continuously) {
			return false; // 0 represents infinite loop
		}
		return iteration >= recurrence;
	}

	/**
	 * @return <code>true</code> if a virtual machine error was detected (e.g. out of memory)
	 * @author martin.wurzinger
	 */
	public boolean isVmErrorDetected() {
		return vmErrorSupervisor.getError() != null;
	}

	/**
	 * Get the detected virtual machine error.
	 * @return the detected virtual machine error or <code>null</code> if no error was detected
	 * @author martin.wurzinger
	 */
	public VirtualMachineError getVmError() {
		return vmErrorSupervisor.getError();
	}

	private static class VmErrorSupervisor implements BuildListener {

		private final Thread supervisedThread;
		private VirtualMachineError error = null;

		/**
		 *
		 * @param supervisedThread a thread that has to be interrupted if a VM error was detected
		 * @author martin.wurzinger
		 */
		public VmErrorSupervisor(Thread supervisedThread) {
			this.supervisedThread = supervisedThread;
		}

		private void superviseBuildEvent(BuildEvent event) {
			// check if VM Error was already detected
			if (error != null) {
				return;
			}

			if (event == null) {
				return;
			}

			superviseException(event.getException());
		}

		public void superviseException(Throwable exception) {
			VirtualMachineError vmError = findVmErrorCause(exception);
			if (vmError == null) {
				return;
			}

			this.error = vmError;

			if (supervisedThread != null) {
				supervisedThread.interrupt();
			}
		}

		private VirtualMachineError findVmErrorCause(Throwable throwable) {
			if (throwable == null) {
				return null;
			}

			if (throwable instanceof VirtualMachineError) {
				return (VirtualMachineError) throwable;
			}

			return findVmErrorCause(throwable.getCause());
		}

		public VirtualMachineError getError() {
			return error;
		}

		@Override
		public void taskStarted(BuildEvent event) {
		}

		@Override
		public void taskFinished(BuildEvent event) {
			superviseBuildEvent(event);
		}

		@Override
		public void targetStarted(BuildEvent event) {

		}

		@Override
		public void targetFinished(BuildEvent event) {
			superviseBuildEvent(event);
		}

		@Override
		public void messageLogged(BuildEvent event) {
		}

		@Override
		public void buildStarted(BuildEvent event) {
		}

		@Override
		public void buildFinished(BuildEvent event) {
			superviseBuildEvent(event);
		}
	}


	public void setContinuously(boolean continuously) {
		this.continuously = continuously;
	}

	private class FailureFilteringLoggingListener extends CommonsLoggingListener {

		@Override
		public void taskFinished(BuildEvent event) {
			if (event.getException() != null && isMsgToIgnore(event.getException().toString())) {
				event.setException(null); // ignore exception, but log event finishing if configured; see JLT-43137
			}
			super.taskFinished(event);
		}

		private boolean isMsgToIgnore(String errorMsg) {
			for (String ignore : IGNORE_ERROR_MSGS) {
				if (errorMsg.contains("/rest/management/profiles/easyTravel/" + ignore)) {
					return true;
				}
			}
			return false;
		}


	}
}