package com.dynatrace.easytravel.launcher.rap;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import com.dynatrace.easytravel.launcher.Launcher;

/**
 * Simulates an SWT/RWT Control which provides functionality that shows an SWT Link Control in standalone
 * SWT GUI Applications and uses a Browser widget if the application is executed as RAP Web Application.
 *
 * Note: This is by no means general-purpose, because we use a fixed height and fixed font, but does what it
 * needs to do for WebLauncher.
 *
 * @author dominik.stadler
 */
public class RAPLink {
	// either Link or Browser is active depending on RAPSupport.isStandalone()
	final Link link;
	final Label label;

	// points to one of the two above, just used to make the implementation below easier to read
	final Control control;

	// copied from RWT/RAP source to not have to link against RWT classes here
	public static final String MARKUP_ENABLED = "org.eclipse.rap.rwt.markupEnabled";

	public RAPLink(Composite parent, int flags, int widthHint, int heightHint) {
		if (!Launcher.isWeblauncher()) {
			link = new Link(parent, flags);
			label = null;
			control = link;
		} else {
			link = null;
			label = new Label(parent, flags);

			label.setData( MARKUP_ENABLED, Boolean.TRUE );

			control = label;
		}
	}

	public void setFont(Font font) {
		// not sure if this has any effect on Browser...
		control.setFont(font);
	}

	/**
	 * Only useful in SWT GUI Applications, the Browser widget will not react on links,
	 * but the actual Browser will point to wherever the link points to.
	 *
	 * @param listener
	 * @author dominik.stadler
	 */
	public void addSelectionListener(SelectionAdapter listener) {
		if(!Launcher.isWeblauncher()) {
			link.addSelectionListener(listener);
		}
	}

	public boolean isDisposed() {
		if (link != null) {
			return link.isDisposed();
		}

		return label.isDisposed();
	}

	public void setText(String string, boolean rightAlign) {
		if (!Launcher.isWeblauncher()) {
			link.setText(string);
		} else {
			//String align = "text-align:left;";
			// JLT-39036: using right-align causes scrollbars to appear in Firefox 3.6, use left-align only for now
			//String align = "text-align:" + (rightAlign ? "right" : "left") + ";";
			//label.setText("<div style=\"font-family:Verdana;font-size:12px;white-space:nowrap;" + align + "\">" + string + "</div>");
			// TODO: check if we can now do this with RAP 2.0
			label.setText(string.replace("&", "&amp;"));
		}
	}

	public Composite getParent() {
		return control.getParent();
	}

	public void setToolTipText(String toolTip) {
		control.setToolTipText(toolTip);
	}

	public void setVisible(boolean visible) {
		control.setVisible(visible);
	}

	public void dispose() {
		control.dispose();
	}
}
