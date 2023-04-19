package com.dynatrace.easytravel.launcher.fancy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.ImageManager;

/***
 *
 *
 * @author christoph.neumueller
 */
final class FilterComponent {

	private int controlCount = 6;

	private Filterable filterable;
	private Text filterText;
	private Label closeLabel;
	private boolean isVisible = false;
	private final ImageManager imageManager = new ImageManager();
	private GridData filterGridData;
	private Label separator;
    private Button showAllRadioBox;
    private Button showEnabledRadioBox;
    private Button showDisabledRadioBox;
	private Composite filterComposite;

	private Listener escListener = new Listener() {

		@Override
		public void handleEvent(Event e) {
			if (e.detail == SWT.TRAVERSE_ESCAPE) {
				e.doit = false;
			}
		}
	};

    private Listener allRadioBoxListener = new Listener() {

        @Override
        public void handleEvent(Event event) {
            if(showAllRadioBox.getSelection()) {
                filterable.applyFilter(null);
                filterText.setText("");
//                showAllRadioBox.setSelection(true);
            }

        }
    };

    private Listener enabledRadioBoxListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            if(showEnabledRadioBox.getSelection()) {
                  filterable.applyFilter(new FilterTaskParams(true));
                  filterText.setText("");
            }
        }
    };

    private Listener disabledRadioBoxListener = new Listener() {
        @Override
        public void handleEvent(Event event) {
            if(showDisabledRadioBox.getSelection()) {
                filterable.applyFilter((new FilterTaskParams(false)));
                filterText.setText("");
            }
        }
    };

	public FilterComponent(Filterable filterable) {
		this.filterable = filterable;
	}

	/**
	 * Creates the Composite containing the search box
	 *
	 * @author christoph.neumueller
	 */
	public Composite createControl(Composite parent, int style) {
		filterComposite = new Composite(parent, style);
		GridLayout gridLayout = new GridLayout(controlCount, false);
		filterGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		filterGridData.exclude = !isVisible;
		filterComposite.setLayout(gridLayout);
		filterComposite.setLayoutData(filterGridData);

		/** closeLabel **/
		closeLabel = new Label(filterComposite, SWT.NONE);
		closeLabel.setImage(imageManager.createImage(Constants.Images.SEARCH_CLOSE_NORMAL));
		closeLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				clear();
			}
		});

		if (!Launcher.isWeblauncher()) {
			closeLabel.addMouseTrackListener(new MouseTrackAdapter() {

				@Override
				public void mouseExit(MouseEvent e) {
					closeLabel.setImage(imageManager.createImage(Constants.Images.SEARCH_CLOSE_NORMAL));
				}

				@Override
				public void mouseEnter(MouseEvent e) {
					closeLabel.setImage(imageManager.createImage(Constants.Images.SEARCH_CLOSE_HIGHLIGHT));
				}
			});
		}

	    parent.addDisposeListener(new DisposeListener() {
	        @Override
	        public void widgetDisposed(DisposeEvent event) {
	        	imageManager.disposeImages();
	        }
	    });

		/** searchLabel **/
		Label searchLabel = new Label(filterComposite, SWT.NONE);
		searchLabel.setText("Filter:");

		/** filterText **/
		this.filterText = new Text(filterComposite, SWT.FILL);
		this.filterText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		this.filterText.setMessage("search here");
		if (!Launcher.isWeblauncher()) {
			this.filterText.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					handleFilterTextKeyEvent(e);
				}
			});
		} else { // WebLauncher
			this.filterText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
                        applyFilter();
				}
			});
		}

        showAllRadioBox = new Button(filterComposite, SWT.RADIO);
        showAllRadioBox.setText("All");
        showAllRadioBox.setSelection(true);
        showAllRadioBox.addListener(SWT.Selection, allRadioBoxListener);

        showEnabledRadioBox = new Button(filterComposite, SWT.RADIO);
        showEnabledRadioBox.setText("Enabled");
        showEnabledRadioBox.addListener(SWT.Selection, enabledRadioBoxListener);

        showDisabledRadioBox = new Button(filterComposite, SWT.RADIO);
        showDisabledRadioBox.setText("Disabled");
        showDisabledRadioBox.addListener(SWT.Selection, disabledRadioBoxListener);

        /** separator **/
		separator = new Label(filterComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gridData.horizontalSpan = controlCount;
		separator.setLayoutData(gridData);

		/** register shortcut **/
		if (!Launcher.isWeblauncher()) {
			registerShortcut();
		}

		return filterComposite;
	}

	/**
	 * Updates the filter text box with the text from filterTaskParams
	 *
	 * @param filterTaskParams
	 * @author christoph.neumueller
	 */
	public void updateFrom(FilterTaskParams filterTaskParams) {
        if(filterTaskParams == null) {
            this.filterText.setText("");
        } else if (filterTaskParams.getFilterText() != null) {
            this.filterText.setText(filterTaskParams.getFilterText());
            this.filterText.setSelection(this.filterText.getText().length());
        }
	}

	/**
	 * Hides the control and clears the filter
	 *
	 * @author christoph.neumueller
	 */
	public void hide() {
		if (!isVisible) {
			return;
		}
		clear();
		setVisible(false, false);
	}

	/**
	 * Shows the control
	 *
	 * @param forceFocusText if true, the text will be selected
	 * @author christoph.neumueller
	 */
	public void show(boolean forceFocusText) {
		setVisible(true, forceFocusText);
	}

	private void setVisible(boolean visible, boolean forceFocusText) {
		if (this.isVisible == visible) {
			return;
		}
		this.isVisible = visible;
		filterComposite.setVisible(isVisible);

		if (this.isVisible) {
			aboutToShow(forceFocusText);
		} else {
			aboutToHide();
		}

		filterGridData.exclude = !isVisible;
		filterComposite.getParent(); // TODO: Is this needed?
    	if (Launcher.isWidgetDisposed(filterComposite)) {
    		return;
    	}
		Composite parent = filterComposite.getParent();
    	if (Launcher.isWidgetDisposed(parent)) {
    		return;
    	}
		parent.layout(true);
	}

	/**
	 * @author christoph.neumueller
	 */
	private void applyFilter() {
        if(!filterText.getText().equals("")) {
        	filterable.applyFilter(new FilterTaskParams(filterText.getText()));
        	clearRadioBoxes();
        }

//       clearRadioBoxes();
	}

	/**
	 * @author christoph.neumueller
	 */
	private void handleDefaultKeyEvent(KeyEvent e) {
		if ((e.stateMask == SWT.MOD1) && (e.keyCode == 'f')) {
			forceFocusText();
		}
	}

	/**
	 * @author christoph.neumueller
	 */
	private void handleFilterTextKeyEvent(KeyEvent e) {
		if (e.keyCode == SWT.ESC) {
			clear();
		} else if (e.keyCode == SWT.CR) {
			applyFilter();
		}
	}

	/**
	 * @author christoph.neumueller
	 */
	private void registerShortcut() {
		// register CTRL+f to show/focus this control
		Display.getCurrent().addFilter(SWT.KeyDown, new Listener() {

			@Override
			public void handleEvent(Event event) {
				handleDefaultKeyEvent(new KeyEvent(event));
			}
		});
	}

	/**
	 * @author christoph.neumueller
	 */
	private void clear() {
		updateFrom(null);
		applyFilter();
	}

	/**
	 * @author christoph.neumueller
	 */
	private void aboutToShow(boolean foreFocusToText) {
		if (foreFocusToText) {
			forceFocusText();
		}
		filterComposite.addListener(SWT.Traverse, escListener);
	}

	/**
	 * @author christoph.neumueller
	 */
	private void aboutToHide() {
		filterComposite.removeListener(SWT.Traverse, escListener);
	}

	/**
	 * @author christoph.neumueller
	 */
	private void forceFocusText() {
		filterText.selectAll();
		filterText.forceFocus();
	}

    private void clearRadioBoxes() {
        showAllRadioBox.setSelection(false);
        showEnabledRadioBox.setSelection(false);
        showDisabledRadioBox.setSelection(false);
    }
}
