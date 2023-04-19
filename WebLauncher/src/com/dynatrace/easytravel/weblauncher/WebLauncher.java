package com.dynatrace.easytravel.weblauncher;

import org.eclipse.rap.rwt.application.EntryPoint;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.LauncherUI;

public class WebLauncher implements EntryPoint {

	@Override
	public int createUI() {
		Launcher.setIsWeblauncher(true);

		LauncherUI launcherUI = new LauncherUIRAP();
		Launcher.run(launcherUI);

		return 0;
	}
}
