package com.dynatrace.easytravel.launcher.fancy.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.dynatrace.easytravel.launcher.ScenarioController;
import com.dynatrace.easytravel.launcher.engine.BatchStateListener;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateListener;
import com.dynatrace.easytravel.launcher.fancy.AbstractMenuItem;
import com.dynatrace.easytravel.launcher.fancy.AbstractMenuItemComponent;
import com.dynatrace.easytravel.launcher.fancy.LayoutCallback;
import com.dynatrace.easytravel.launcher.fancy.MenuItem;
import com.dynatrace.easytravel.launcher.fancy.MenuPage;
import com.dynatrace.easytravel.launcher.fancy.MenuVisibilityCallback;


public class ScenarioMenuItem extends MenuItem {

    private final ScenarioController scenarioController;

    public ScenarioMenuItem(String image, String title, String descriptionText, ScenarioController scenarioController, MenuVisibilityCallback visibility) {
        super(image, title, descriptionText, scenarioController, visibility);

        this.scenarioController = scenarioController;
    }

    @Override
    public AbstractMenuItemComponent<? extends AbstractMenuItem> createComponent(Composite parent, MenuPage page, LayoutCallback layoutCallback) {
        return new ScenarioMenuItemComponent(parent, SWT.NONE, this, page, layoutCallback);
    }

    public void addBatchStateListener(BatchStateListener listener) {
        scenarioController.addBatchStateListener(listener);
    }

    public void addProcedureStateListener(ProcedureStateListener listener) {
        scenarioController.addProcedureStateListener(listener);
    }

    public void stopScenario() {
        scenarioController.stop();
    }

	public void restartScenario() {
		scenarioController.restart();
	}
}
