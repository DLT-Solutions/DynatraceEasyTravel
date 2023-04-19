/**
 *
 */
package com.dynatrace.easytravel.weblauncher;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.LauncherUI;
import com.dynatrace.easytravel.launcher.LauncherUIType;
import com.dynatrace.easytravel.launcher.engine.CloseCallback;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.engine.ThreadEngine;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.util.LocalUriProvider;

import ch.qos.logback.classic.Logger;

/**
 * @author tomasz.wieremjewicz
 * @date 12 gru 2019
 *
 */
public class LauncherUIRAP extends LauncherUI {
	private static final Logger LOGGER = LoggerFactory.make();

	public LauncherUIRAP() {
		this.setUiType(LauncherUIType.RAP);
	}

	@Override
	public void messageBox(Shell parent, int flags, String title, String message, CloseCallback callback) {
		MessageBox box = new MessageBox( parent, flags );
		box.setMessage(message);
		box.setText(title);

		box.open(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed( int returnCode ) {
				if(callback != null) {
					callback.dialogClosed(returnCode);
				}
			  }
			});
	}

	@Override
	public void openURL(String url) {
		UrlLauncher launcher = RWT.getClient().getService( UrlLauncher.class );
		launcher.openURL(url);
	}

	@Override
	public void shutdown() {
		// first stop any running scenario
		LaunchEngine.stop();

		// exit Launcher by disposing the Display, needs to run in the display thread again
		ThreadEngine.runInDisplayThread(new Runnable() {
			@Override
			public void run() {
				Launcher.exit();
			}
		}, getDisplay());

		// stop WebLauncher via REST
		try {
			UrlUtils.retrieveData(LocalUriProvider.getLocalUri(EasyTravelConfig.read().launcherHttpPort, "/shutdown"));
		} catch (IOException e) {
			LOGGER.warn("Could not shutdown WebLauncher via REST", e);
		}
	}

	@Override
	public void logout() {
		getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				new WebClient().getService(JavaScriptExecutor.class).execute("window.location.href=\"/logout\"");
			}
		});
	}

	@Override
	public void showFile(File file, Shell parent) {
		String log;
        try {
            log = FileUtils.readFileToString(file); //NOPMD
        } catch (IOException ioe) {
        	log = "Exception trying to show file '" + file.getAbsolutePath() + "': " + ioe.getMessage();
            LOGGER.warn(log, ioe);
        }
        Shell shell = new Shell(parent, SWT.TITLE | SWT.CLOSE | SWT.MAX | SWT.MIN | SWT.RESIZE);
        shell.setImage(parent.getImage());
        shell.setText(file.getName());
        shell.setLayout(new FillLayout());
        Composite compo = new Composite(shell, SWT.NONE);
        compo.setLayout(new FillLayout());
        Text text = new Text(compo, SWT.MULTI);
        text.setText(log);
        shell.open();
	}
}
