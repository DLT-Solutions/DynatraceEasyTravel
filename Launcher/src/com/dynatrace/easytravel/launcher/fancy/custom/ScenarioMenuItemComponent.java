package com.dynatrace.easytravel.launcher.fancy.custom;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.engine.BatchStateListener;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateListener;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.engine.ThreadEngine;
import com.dynatrace.easytravel.launcher.fancy.LayoutCallback;
import com.dynatrace.easytravel.launcher.fancy.MenuItemComponent;
import com.dynatrace.easytravel.launcher.fancy.MenuPage;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * UI component that represents a scenario within the fancy menu.
 *
 * @author martin.wurzinger
 */
public class ScenarioMenuItemComponent extends MenuItemComponent<ScenarioMenuItem> implements BatchStateListener, ProcedureStateListener {

    private static final Logger LOGGER = Logger.getLogger(ScenarioMenuItemComponent.class.getName());

    private final Map<StatefulProcedure, ProcedureComponent> procedureComponents = new IdentityHashMap<StatefulProcedure, ProcedureComponent>();

    private Composite batchPanel;
    private Composite scenarioPanel;
    private StopComponent stopPanel;

    private final ScenarioMenuItem item;
    private GridData layoutData;
    private final LayoutCallback layoutCallback;

    public ScenarioMenuItemComponent(Composite parent, int style, ScenarioMenuItem item, MenuPage page, LayoutCallback layoutCallback) {
        super(parent, style, item, page);

        this.item = item;
        this.layoutCallback = layoutCallback;
        item.addBatchStateListener(this);
        item.addProcedureStateListener(this);
    }

    @Override
    protected void createContent() {
        super.createContent();

        createScenarioPanel();

        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent event) {
                scenarioPanel.dispose();
                batchPanel.dispose();
                stopPanel.dispose();
                clearProcedureComponents();
            }
        });
    }

    protected void createScenarioPanel() {
        scenarioPanel = new Composite(this, SWT.NONE);

        GridLayout scenarioLayout = new GridLayout(2, false);
        scenarioPanel.setLayout(scenarioLayout);

        createBatchPanel(scenarioPanel);
        createStopButton(scenarioPanel);

        layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        layoutData.exclude = true;
        scenarioPanel.setLayoutData(layoutData);
    }

    protected void createBatchPanel(Composite parent) {
        batchPanel = new Composite(parent, SWT.NONE);
        batchPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        RowLayout layout = new RowLayout();
        layout.wrap = true;

        batchPanel.setLayout(layout);
    }

    protected void createStopButton(Composite parent) {

        stopPanel = new StopComponent(parent, new Listener() {
			@Override
			public void handleEvent(Event e) {
            	LOGGER.warning("Restarting Batch on user request from " + Launcher.getOrigin());
            	item.restartScenario();
			}
		});
        stopPanel.setEnabled(false);
        stopPanel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        stopPanel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	LOGGER.warning("Stopping Batch on user request");
                item.stopScenario();
            }
        });
    }

    @Override
    public void notifyBatchStateChanged(Scenario scenario, final State oldState, final State newState) {
    	if (Launcher.isWidgetDisposed(this)) {
    		return;
    	}
        ThreadEngine.runInDisplayThread(new Runnable() {
            @Override
            public void run() {
            	if (Launcher.isWidgetDisposed(ScenarioMenuItemComponent.this)) {
            		return;
            	}
                // the first notification does not characterize a state change and is only fired for initializations
                if (oldState != newState) {
                    if (LOGGER.isLoggable(Level.FINE)) LOGGER.fine(TextUtils.merge("Batch state changed to ''{0}''", newState));
                }

                switch (newState) {
                    case STARTING:
                    case OPERATING:
                    case TIMEOUT:
                    	if (oldState == State.STOPPED) {
                            stopPanel.setEnabled(true);
                            showScenarioPanel();
                    	}
                        break;
                    case STOPPING:
						stopPanel.setEnabled(false);
                        break;
                    case STOPPED:
                    case UNKNOWN:
                    case FAILED:
                    case ACCESS_DENIED:
                        stopPanel.setEnabled(false);
                        clearProcedureComponents();
                        hideScenarioPanel();
                        break;
                }
            }
        }, this);
    }

    @Override
    public void notifyProcedureStateChanged(final StatefulProcedure subject, final State oldState, final State newState) {
    	if (Launcher.isWidgetDisposed(this)) {
    		return;
    	}
    	if (LOGGER.isLoggable(Level.FINE)) LOGGER.fine("TRANSITION: " + subject.getName() + ": " + oldState + "->" + newState);
    	ThreadEngine.runInDisplayThread(new Runnable() {
            @Override
            public void run() {
            	if (Launcher.isWidgetDisposed(ScenarioMenuItemComponent.this)) {
            		return;
            	}
            	if (LOGGER.isLoggable(Level.FINE)) LOGGER.fine("TRANSITION2: " + subject.getName() + ": " + oldState + "->" + newState);
                // update UI controls in each case
                getOrCreateProcedureComponent(subject).update();

                final Runnable removeFinishedComponent = new Runnable() {
					@Override
					public void run() {
						// don't remove procedure if it is not enabled and thus was not run at all
						if(!subject.isEnabled()) {
							return;
						}
						//FIXME Here, a SWTException("Invalid Thread Access") is often thrown when
						//Reloading the WebLauncher in a second browser window while starting.
	                    removeProcedureComponent(subject);
	                    layout(true, true);
	                    redraw();
					}
				};

				// TODO: when is this triggered? When do we send a state-update with oldState == newState?!?
                if (oldState == State.STOPPED && newState == State.STOPPED && subject.isSynchronous()) {
                	removeFinishedComponent.run();  // remove immediately, don't wait 5 secs
                    return;
                }

                if (oldState != newState) {
                	if (LOGGER.isLoggable(Level.FINE)) LOGGER.fine(TextUtils.merge("Procedure ''{0}'' state changed to ''{1}''", subject.getName(), newState));
                }

                if (State.STOPPED == newState && subject.isSynchronous()) {
            		ThreadEngine.createBackgroundThread("Finished Procedure Component Remover", new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // wait 5 seconds before hiding finished component
                                Thread.sleep(5 * 1000);
                            } catch (InterruptedException e) {
                                LOGGER.log(Level.WARNING, TextUtils.merge("Interruption while waiting to hide ''{0}'' component.", subject.getName()), e);
                            }
                        	if (Launcher.isWidgetDisposed(ScenarioMenuItemComponent.this)) {
                            	return;
                            }
                            ThreadEngine.runInDisplayThread(removeFinishedComponent, ScenarioMenuItemComponent.this);
                        }
                    }, true, getDisplay()).start();
                }
            }
        }, this);
    }

    private void showScenarioPanel() {
        scenarioPanel.setVisible(true);
        layoutData.exclude = false;
        layout(true, true);
        MenuItemComponent.scrollToVisible(this);
    }

    private void hideScenarioPanel() {
        scenarioPanel.setVisible(false);
        layoutData.exclude = true;
        layout(true, true);
    }

    /**
     *
     * @param procedure
     * @return the the UI component that visualizes the stateful procedure; must not return
     *         <code>null</code>
     * @author martin.wurzinger
     */
    private ProcedureComponent getOrCreateProcedureComponent(StatefulProcedure procedure) {
        ProcedureComponent component = null;

        synchronized (procedureComponents) {
            component = procedureComponents.get(procedure);

            if (component == null) {
                LOGGER.fine(TextUtils.merge("Create UI component for procedure ''{0}''.", procedure.getName()));
                component = new ProcedureComponent(batchPanel, procedure);

                // need to create a unique id, so we need to attach the how many nth item we add currently
                component.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_PROC_COMPONENT + "_" + procedureComponents.size() + "_" + component.hashCode());

                RowData layoutData = new RowData();
                layoutData.width = 130;
                component.setLayoutData(layoutData);

                procedureComponents.put(procedure, component);
            }

            component.updateState();

            layout(true, true);
            redraw();
        }

        return component;
    }

    private void clearProcedureComponents() {
        synchronized (procedureComponents) {
            Iterator<Entry<StatefulProcedure, ProcedureComponent>> iterator = procedureComponents.entrySet().iterator();

            while (iterator.hasNext()) {
                iterator.next().getValue().dispose();
                iterator.remove();
            }
        }
    }

    private void removeProcedureComponent(StatefulProcedure procedure) {
        synchronized (procedureComponents) {
            ProcedureComponent component = procedureComponents.remove(procedure);
            if (component == null) {
                // no component available for specified procedure
                return;
            }

            component.dispose();
        }
    }

    private void notifyLayoutCallback() {
        if (layoutCallback != null) {
            layoutCallback.notifyLayoutEvent();
        }
    }

    @Override
    public void layout() {
        layout(true, true);
    }

    @Override
    public void layout(boolean changed) {
        layout(changed, true);
    }

    @Override
    public void layout(boolean changed, boolean all) {
    	if (Launcher.isWidgetDisposed(this) || Launcher.isWidgetDisposed(getParent())) {
    		return;
    	}
    	getParent().layout(changed, all);
        notifyLayoutCallback();
    }

    @Override
    public void redraw() {
    	if (Launcher.isWidgetDisposed(getParent())) {
    		return;
    	}
    	getParent().redraw();
    }
}
