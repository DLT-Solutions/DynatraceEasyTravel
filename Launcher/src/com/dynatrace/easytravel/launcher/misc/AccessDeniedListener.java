package com.dynatrace.easytravel.launcher.misc;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateListener;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;


public class AccessDeniedListener implements ProcedureStateListener {

	private static final String ERROR_MESSAGE = "The %s cannot be started. Please run EasyTravel as administrator!";
	private static final String APACHE_WEBSERVER = "Apache webserver";
	private Shell shell;


	public AccessDeniedListener(Shell shell) {
		this.shell = shell;
	}

	@Override
	public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {
		if (newState == State.ACCESS_DENIED && isSubjectApacheHttpdProcedure(subject)) {
			openPopup(APACHE_WEBSERVER);
		}
	}

	private boolean isSubjectApacheHttpdProcedure(StatefulProcedure subject) {
		return Constants.Procedures.APACHE_HTTPD_ID.equals(subject.getMapping().getId()) ||
				Constants.Procedures.APACHE_HTTPD_PHP_ID.equals(subject.getMapping().getId());
	}

	private void openPopup(final String errorProcedure) {
		shell.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				Launcher.getLauncherUI(shell.getDisplay()).messageBox(shell, SWT.ICON_ERROR | SWT.ERROR | SWT.OK,
						"Access denied", getErrorMessage(errorProcedure), null);
			}

		});
	}

	private String getErrorMessage(String errorProcedure) {
		return String.format(ERROR_MESSAGE, errorProcedure);
	}



}
