package com.dynatrace.easytravel.launcher.fancy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.misc.FontManager;
import com.dynatrace.easytravel.launcher.misc.ImageManager;


/**
 * Widget for menu item
 *
 * @author richard.vogl
 */
public abstract class AbstractMenuItemComponent<T extends AbstractMenuItem> extends Composite {
	protected final ImageManager imageManager;	// used in derived classes!
	protected final FontManager fontManager;

	private final MenuPage page;
	private final T item;

	private Font fontTitle;
	private Font font;
	private Font fontItalic;

	/**
	 * Create new menu item component
	 *
	 * @param parent
	 * @param style
	 * @author richard.vogl
	 * @param page
	 */
	public AbstractMenuItemComponent(Composite parent, int style, T item, MenuPage page) {
		super(parent, style);
		this.page = page;
		this.item = item;

		imageManager = new ImageManager();
		fontManager = new FontManager();

		fontTitle = fontManager.createFont(+4, parent.getDisplay());
	    font = fontManager.createFont(0, parent.getDisplay());
	    fontItalic = fontManager.createFont(0, SWT.ITALIC, parent.getDisplay());

		createContent();

		registerListeners(); // NOPMD
	}


	public MenuPage getPage() {
		return page;
	}

	private void registerListeners() {
		PaintListener listener = new PaintListener() {
			@Override
			public void paintControl(PaintEvent event) {
				updateState();
			}
		};

		if (!Launcher.isWeblauncher()) {
			getParent().addPaintListener(listener);
		} else {
			// on WebLauncher simulate paint-event via resized as it does not support PaintListeners at all
			getParent().addControlListener(new ControlListener() {
				@Override
				public void controlMoved(ControlEvent arg0) {
				}

				@Override
				public void controlResized(ControlEvent arg0) {
					listener.paintControl(null);
				}
			});
		}

		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent event) {
				widgedDisposed();
			}
		});
	}


	/**
	 * Returns the menu item data object for this menu item component.
	 *
	 * @return
	 * @author richard.vogl
	 */
	public T getItem() {
		return item;
	}

	/**
	 * Performs necessary operation when the component is painted
	 *
	 * @author richard.vogl
	 */
	protected void updateState() { // NOPMD
		// empty
	}

	/**
	 * Returns true if this menu item is enabled.
	 *
	 * @return
	 * @author richard.vogl
	 */
	protected boolean checkEnabled() {
		return item.checkEnabled() &&
			   page.checkEnabled();
	}

	/**
	 * Return true if this menu item is visible.
	 *
	 * @return
	 * @author richard.vogl
	 */
	protected boolean checkVisible(){
		return item.checkVisible() &&
			   page.checkVisible();
	}

	/**
	 * Will be called when component is disposed. Dispose all allocated resources here.
	 * <p>
	 * <b>Attention:</b> Don't forget to call <code>super.widgetDisposed()</code>, so
	 * the parent class can dispose its resources as well.
	 * </p>
	 * @author richard.vogl
	 */
	protected void widgedDisposed() {
		imageManager.disposeImages();
		fontManager.disposeFonts();
	}

	/**
	 * Creates all necessary widgets to display the menu item.
	 *
	 * @author richard.vogl
	 */
	abstract protected void createContent();

	/**
	 * @param fontTitle the fontTitle to set
	 */
	public void setFontTitle(Font fontTitle) {
		this.fontTitle = fontTitle;
	}

	/**
	 * @return the fontTitle
	 */
	public Font getFontTitle() {
		return fontTitle;
	}

	/**
	 * @param font the font to set
	 */
	@Override
	public void setFont(Font font) {
		this.font = font;
	}

	/**
	 * @return the font
	 */
	@Override
	public Font getFont() {
		return font;
	}

	/**
	 * @param fontItalic the fontItalic to set
	 */
	public void setFontItalic(Font fontItalic) {
		this.fontItalic = fontItalic;
	}

	/**
	 * @return the fontItalic
	 */
	public Font getFontItalic() {
		return fontItalic;
	}

	/**
	 * Checks if this item should be visible an performs necessary operations.
	 */
	public void updateVisibility(boolean doLayout) {
		GridData data = (GridData) getLayoutData();
		if (data == null){
			data = new GridData();
			setLayoutData(data);
		}
		boolean updateExclude = data.exclude == checkVisible();
		if (updateExclude || checkVisible() != this.isVisible()){
			setVisible(checkVisible());
			data.exclude = !checkVisible();
			if (doLayout)
				getParent().layout();
		}
	}
}
