package com.dynatrace.easytravel.launcher.fancy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.FontManager;
import com.dynatrace.easytravel.launcher.misc.ImageManager;

/**
 * Widget for menu page button
 *
 * @author richard.vogl
 */
public class MenuPageButtonComponent extends Composite {
    private final List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>(1);
    private final MenuPage menuPage;

    private boolean selected = true;
    private boolean used = false;	/* indicates that a Scenario is started in this Page */
	private Label label;
	private Label imageLabel;
	private boolean underSelected = false;
	private boolean hover = false;
	private Image selectedHoverBG;
	private Image selectedBG;
	private Image shadowHoverBG;
	private Image shadowBG;
	private Image normalHoverBG;
	private Image normalBG;

	private final ImageManager imageManager;
    private final FontManager fontManager;

    private final Font normalFont;
    private final Font boldFont;

	/**
	 * Creates the MenuPageButtonComponent.
	 *
	 * @param parent
	 * @param style
	 * @author richard.vogl
	 * @param menuPage TODO
	 */
	public MenuPageButtonComponent(MenuPage menuPage, Composite parent, int style) {
		super(parent, style);

		imageManager = new ImageManager();
		fontManager = new FontManager();

		normalFont = fontManager.createFont(+7, SWT.NORMAL, parent.getDisplay());
		boldFont = fontManager.createFont(+7, SWT.BOLD, parent.getDisplay());


        this.menuPage = menuPage;
		createContent();
		registerListeners();
	}

	private void registerListeners() {
		this.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent event) {
				widgedDisposed();
			}
		});

		PaintListener listener = new PaintListener() {

			@Override
			public void paintControl(PaintEvent event) {
				onPaint();
			}
		};

		if (!Launcher.isWeblauncher()) {
			this.addPaintListener(listener);
		} else {
			// on WebLauncher simulate paint-event via resized as it does not support PaintListeners at all
			this.addControlListener(new ControlListener() {
				@Override
				public void controlMoved(ControlEvent arg0) {
				}

				@Override
				public void controlResized(ControlEvent arg0) {
					listener.paintControl(null);
				}
			});
		}
	}

	private void onPaint() {
		// FIXME RV 3.6: use enabled and visible checks and handle visibility
		if (menuPage.checkVisible() != label.isEnabled()) {
			enableControls(menuPage.checkVisible());
			this.layout();
		}
	}

	private void enableControls(boolean enabled) {
		label.setEnabled(enabled);
		if (imageLabel != null){
			imageLabel.setEnabled(enabled);
		}
		this.setEnabled(enabled);
	}

	private void createContent() {
		refreshImages();

		this.setBackgroundMode(SWT.INHERIT_DEFAULT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 2;
		layout.marginWidth = 10;
		this.setLayout(layout);
		this.setCursor(Constants.Cursors.HAND);

		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;

		this.setLayoutData(layoutData);

		MouseListener mouseSelectionListener = new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent mouseEvent) {
				if(mouseEvent.button == 1){
					Event event = new Event();
					event.button = mouseEvent.button;
					// it seems RAP does not support this element on MouseEvent?
					if(!Launcher.isWeblauncher()) {
						event.count = mouseEvent.count;
					}
					event.data = mouseEvent.data;
					event.display = mouseEvent.display;
					event.stateMask = mouseEvent.stateMask;
					event.time = mouseEvent.time;
					event.widget = mouseEvent.widget;
					event.x = mouseEvent.x;
					event.y = mouseEvent.y;

					select(event);
				}
			}
		};

		this.addMouseListener(mouseSelectionListener);

		MouseTrackAdapter mouseTrackListener = new MouseTrackAdapter() {
			@Override
			public void mouseExit(MouseEvent event) {
				hover = false;
				refreshBackground();
			}
			@Override
			public void mouseEnter(MouseEvent event) {
				hover = true;
				refreshBackground();
			}
		};

		if (!Launcher.isWeblauncher()) {
			this.addMouseTrackListener(mouseTrackListener);
		}

		if (menuPage.getImage() != null) {
			imageLabel = new Label(this, SWT.NONE);
			imageLabel.setImage(imageManager.createImage(menuPage.getImage()));
			imageLabel.addMouseListener(mouseSelectionListener);
			if (!Launcher.isWeblauncher()) {
				imageLabel.addMouseTrackListener(mouseTrackListener);
			}
		}

		label = new Label(this, SWT.NONE);
		label.setFont(normalFont);
		label.setBackground(null);
		label.setText(menuPage.getTitle());
		label.addMouseListener(mouseSelectionListener);
		if (!Launcher.isWeblauncher()) {
			label.addMouseTrackListener(mouseTrackListener);
		}

		GridData labelData = new GridData();
		labelData.verticalSpan = 10;
		labelData.horizontalIndent = 10;
		label.setLayoutData(labelData);

		this.deselect();
	}

	private void refreshImages() {
		Image image = imageManager.createImage(Constants.Images.FANCY_MENU_BUTTON_BG_SELECTED_HOVER);
		if (selectedHoverBG != null && !selectedHoverBG.isDisposed()){
			selectedHoverBG.dispose();
		}
		selectedHoverBG = MenuComponent.createImageWithTransparentBG(image, this);
		image = imageManager.createImage(Constants.Images.FANCY_MENU_BUTTON_BG_SELECTED);
		if (selectedBG != null && !selectedBG.isDisposed()){
			selectedBG.dispose();
		}
		selectedBG = MenuComponent.createImageWithTransparentBG(image, this);

		image = imageManager.createImage(Constants.Images.FANCY_MENU_BUTTON_BG_SHADOW_HOVER);
		if (shadowHoverBG != null && !shadowHoverBG.isDisposed()){
			shadowHoverBG.dispose();
		}
		shadowHoverBG = MenuComponent.createImageWithTransparentBG(image, this);
		image = imageManager.createImage(Constants.Images.FANCY_MENU_BUTTON_BG_SHADOW);
		if (shadowBG != null && !shadowBG.isDisposed()){
			shadowBG.dispose();
		}
		shadowBG = MenuComponent.createImageWithTransparentBG(image, this);

		image = imageManager.createImage(Constants.Images.FANCY_MENU_BUTTON_BG_HOVER);
		if (normalHoverBG != null && !normalHoverBG.isDisposed()){
			normalHoverBG.dispose();
		}
		normalHoverBG = MenuComponent.createImageWithTransparentBG(image, this);
		image = imageManager.createImage(Constants.Images.FANCY_MENU_BUTTON_BG);
		if (normalBG != null && !normalBG.isDisposed()){
			normalBG.dispose();
		}
		normalBG = MenuComponent.createImageWithTransparentBG(image, this);
	}

	/**
	 * Sets the state if this {@link MenuPageButtonComponent} to selected.
	 *
	 * @param event
	 * @author richard.vogl
	 */
	final public void select(Event event) {
		forceSelect(event, false);
	}

	/**
	 * Can force selection of page, even if the page is inactive.
	 *
	 * @param event
	 * @param force
	 * @author richard.vogl
	 */
	public final void forceSelect(Event event, boolean force) {
	    if (!menuPage.checkVisible() && !force) {
	        return;
	    }

	    if (selected) {
	        return;
	    }

	    selected = true;
        label.setFont(boldFont);
        refreshBackground();
        layout();
        redraw();
        fireSelectedEvent(event);

        MenuActionCallback action = menuPage.getAction();
        if (action != null) {
            action.run();
        }

        // refresh used display
        indicateUsage(used);
	}

	public final void indicateUsage(boolean used) {
		this.used = used;
		String text="";
		if(!label.isDisposed())
			text = label.getText();

		// do not indicate if this is the currently selected button
		if(used && !selected) {
			// nothing to do if already set
			if(text.endsWith("*")) {
				return;
			}

			label.setText(text + "*");
		} else {
			if(!text.endsWith("*")) {
				return;
			}

			label.setText(text.substring(0, text.length()-1));
		}
	}

	private void refreshBackground() {
		if(selected){
			if (hover){
				setBackgroundImage(selectedHoverBG);
			}else{
				setBackgroundImage(selectedBG);
			}
		}else{
			if (underSelected){
				if (hover){
					setBackgroundImage(shadowHoverBG);
				}else{
					setBackgroundImage(shadowBG);
				}
			}else{
				if (hover){
					setBackgroundImage(normalHoverBG);
				}else{
					setBackgroundImage(normalBG);
				}
			}
		}
	}

	/**
	 * Set the state of this {@link MenuPageButtonComponent} to deselected.
	 *
	 * @author richard.vogl
	 */
	final public void deselect() {
		if (selected || underSelected) {
			selected = false;
			underSelected = false;
			label.setBackground(null);
			label.setFont(normalFont);
			refreshBackground();
			layout();
			redraw();
		}
		selected = false;
		underSelected = false;

		// update indication if a scenario is started in this page
		indicateUsage(used);
	}

	/**
	 * Sets the state of this {@link MenuPageButtonComponent} to be the one under the selected.
	 *
	 * @author richard.vogl
	 */
	final public void setUnderSelected() {
		underSelected = true;
		refreshBackground();
	}

	/**
	 * Adds a listener to this button which will be notified when this button is selected.
	 *
	 * @param listener
	 * @return
	 * @author richard.vogl
	 */
	public boolean addSelectionListener(SelectionListener listener) {
		return this.selectionListeners.add(listener);
	}

	/**
	 * Removes a selection listener.
	 *
	 * @param listener
	 * @return
	 * @author richard.vogl
	 */
	public boolean removeSelectionListener(SelectionListener listener) {
		return this.selectionListeners.remove(listener);
	}

	private void fireSelectedEvent(Event event) {
		SelectionEvent selectionEvent = null;
		if (event != null) {
			selectionEvent = new SelectionEvent(event);
		}
		for (SelectionListener listener : selectionListeners) {
			listener.widgetSelected(selectionEvent);
		}
	}

	private void widgedDisposed() {
		this.normalBG.dispose();
		this.normalHoverBG.dispose();
		this.selectedBG.dispose();
		this.selectedHoverBG.dispose();
		this.shadowBG.dispose();
		this.shadowHoverBG.dispose();

		imageManager.disposeImages();
		fontManager.disposeFonts();
	}

	/**
	 * Make the button hidden, i.e. set the layout data exclude to true.
	 */
	public void setHidden(boolean hidden) {
		((GridData)this.getLayoutData()).exclude = hidden;
	}
}
