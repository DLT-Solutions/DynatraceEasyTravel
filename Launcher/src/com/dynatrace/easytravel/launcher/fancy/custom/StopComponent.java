package com.dynatrace.easytravel.launcher.fancy.custom;

import java.io.File;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.engine.CloseCallback;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.engine.Procedure;
import com.dynatrace.easytravel.launcher.engine.ProcedureFactory;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.engine.ThreadEngine;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.FontManager;
import com.dynatrace.easytravel.launcher.misc.ImageManager;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.procedures.RemoteProcedure;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;


public class StopComponent extends Composite {
    private static final Logger log = LoggerFactory.make();

    private final Button stopButton;

    private final ImageManager imageManager = new ImageManager();
    private final FontManager fontManager = new FontManager();

    public StopComponent(Composite parent, final Listener restartListener) {
        super(parent, SWT.NONE);

        GridLayout layout = new GridLayout(1, true);
        this.setLayout(layout);

        stopButton = new Button(this, SWT.NONE);
        stopButton.setText(MessageConstants.BUTTON_STOP);
        stopButton.setFont(fontManager.createFont(+5, stopButton.getDisplay()));

        GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        layoutData.verticalIndent = 43;
        layoutData.horizontalIndent = 10;
        layoutData.minimumWidth = 115;
        layoutData.heightHint = 64; // as large as the process state images
        stopButton.setLayoutData(layoutData);
        stopButton.setImage(imageManager.createImage(Constants.Images.PROCEDURE_POWER));

        this.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent event) {
                stopButton.dispose();
                imageManager.disposeImages();
                fontManager.disposeFonts();
            }
        });

        final Menu popupmenu = new Menu(getShell(), SWT.POP_UP);
		popupmenu.addListener(SWT.Show, new Listener() {
			@Override
			public void handleEvent(Event event) {
				MenuItem[] menuItems = popupmenu.getItems();
				for (int i = 0; i < menuItems.length; i++) {
					menuItems[i].dispose();
				}

	            final MenuItem addmenuitem = new MenuItem(popupmenu, SWT.CASCADE);
	            addmenuitem.setText(MessageConstants.CONTEXT_START_REMOTE);

	            final Menu addMenu = new Menu(getShell(), SWT.DROP_DOWN);
	            addmenuitem.setMenu(addMenu);

            	for(String mappingId : Constants.Procedures.ALL) {
	    	        // allow to start additional procedures on remote hosts
	    	        Collection<String> hosts = ProcedureFactory.getAllRemoteHosts();
	    	        if(!hosts.isEmpty() &&
	    	        		// only show option to start procedure on remote host if procedure supports remote operation
	    	        		ArrayUtils.contains(Constants.Procedures.ALL_REMOTE, mappingId)) {
	    	            final MenuItem startmenuitem = new MenuItem(addMenu, SWT.CASCADE);
	    	            startmenuitem.setText(ProcedureFactory.getNameOfProcedure(new DefaultProcedureMapping(mappingId)));

	    	            final Menu procMenu = new Menu(getShell(), SWT.DROP_DOWN);
	    	            startmenuitem.setMenu(procMenu);

	            		// also add "localhost" as separate entry
	            		hosts.add("localhost");

	    	            for(final String host : hosts) {
	    		        	MenuItem miStartRemote = new MenuItem(procMenu, SWT.POP_UP);
	    		        	miStartRemote.setText(host);
	    		        	miStartRemote.setData(new ProcedureParams(host, mappingId));	// for listener

	    		        	miStartRemote.addListener(SWT.Selection, startListener);
	    	            }
	    	        } else {
	    	            final MenuItem startmenuitem = new MenuItem(addMenu, SWT.PUSH);
	    	            startmenuitem.setText(ProcedureFactory.getNameOfProcedure(new DefaultProcedureMapping(mappingId)));

	    	            startmenuitem.setData(new ProcedureParams("localhost", mappingId));	// for listener

	    	            startmenuitem.addListener(SWT.Selection, startListener);
	    	        }
            	}

				MenuItem miRestart = new MenuItem(popupmenu, SWT.PUSH);
				miRestart.setText(MessageConstants.CONTEXT_RESTART_ALL);
				miRestart.addListener(SWT.Selection, restartListener);
			}

		});

		setMenu(popupmenu);
    }

    public void addSelectionListener(SelectionListener listener) {
        stopButton.addSelectionListener(listener);
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        stopButton.setEnabled(isEnabled);
    }

	public Menu createMenu() {
		return new Menu(stopButton);
	}

	@Override
	public void setMenu(Menu menu) {
		stopButton.setMenu(menu);
		super.setMenu(menu);
	}


    /**
     * Transfer object for passing params to
     * the listener
     */
    private class ProcedureParams {
    	public String host;
    	public String mappingId;

    	public ProcedureParams(String host, String mappingId) {
			super();
			this.host = host;
			this.mappingId = mappingId;
		}
    }

    private final Listener startListener = new Listener() {
        @Override
        public void handleEvent(Event e) {
        	ProcedureParams params = (ProcedureParams)e.widget.getData();
        	final Procedure proc;
        	ProcedureMapping mapping = mappingForProcedure(params.mappingId);
			if(params.host.equals("localhost")) {
        		proc = new ProcedureFactory().create(mapping, false);
        	} else {
        		proc = new RemoteProcedure(mapping, params.host);
        	}
        	if(proc == null) {
        		Launcher.getLauncherUI(getDisplay()).messageBox(getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO | SWT.ICON_WARNING,
        				"Starting Procedure failed",
        				"Could not start procedure '" + ProcedureFactory.getNameOfProcedure(mapping) + "', do you want to open the logfile to see details?",
        				new CloseCallback() {

							@Override
							public void dialogClosed(int returnCode) {
								if(SWT.YES == returnCode) {
									Launcher.getLauncherUI(getDisplay()).showFile(new File(Directories.getLogDir(),
					    	        		(Launcher.isWeblauncher() ? MessageConstants.WEBLAUNCHER : MessageConstants.LAUNCHER) + "_0-0.log"),
					                		getShell());
								}
							}
						});
				return;
        	}

        	runRemoteProcedure(new StatefulProcedure(proc));
        }
    };

	private ProcedureMapping mappingForProcedure(String mappingId) {
		for(ProcedureMapping mapping : LaunchEngine.getRunningBatch().getScenario().getProcedureMappings(InstallationType.Both)) {
			if(mapping.getId().equals(mappingId)) {
				return mapping;
			}
		}

		// no such procedure found in current Scenario, create a procedure from scratch
		return new DefaultProcedureMapping(mappingId);
	}

	private void runRemoteProcedure(final StatefulProcedure procedure) {
		log.warn("Starting procedure: " + procedure.getName());
		// remove possible "disabled" setting
		//ProcedureComponent.enableProcedure(procedure);

		ThreadEngine.createBackgroundThread("Procedure Runner " + procedure.getName(), new Runnable() {

			@Override
			public void run() {
				// hack: Add the Procedure to the currently running Batch
				// do this before starting to get visual feedback immediately
				LaunchEngine.addProcedure(procedure);

				// now start the procedure as usual
				boolean areAllProceduresOperating = procedure.run().isOk();
				areAllProceduresOperating = areAllProceduresOperating && ProcedureComponent.waitUntilOperating(procedure); // ATTENTION: short-circuit AND
			}
		}, getDisplay()).start();
	}
}
