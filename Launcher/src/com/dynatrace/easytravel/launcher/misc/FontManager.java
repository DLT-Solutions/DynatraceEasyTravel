package com.dynatrace.easytravel.launcher.misc;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import com.dynatrace.easytravel.util.TextUtils;

public class FontManager {

	private static final Logger LOGGER = Logger.getLogger(FontManager.class.getName());

	private final Collection<Font> instantiatedFonts = Collections.synchronizedList(new LinkedList<Font>());

	private final Font defaultFont; // NOPMD
    private final FontData systemFontData; // NOPMD
    private final String systemFontName;
    private final int systemFontSize;

	public FontManager() {
	    Display display = Display.getCurrent();
	    if (display == null) {
	        throw new IllegalStateException("Unable to retrieve current Display");
	    }

	    defaultFont = display.getSystemFont();

	    FontData[] fontData = defaultFont.getFontData();
	    if (fontData == null || fontData.length == 0) {
            throw new IllegalStateException("No system font data available");
	    }

	    systemFontData = fontData[0];

	    systemFontName = systemFontData.getName();
	    systemFontSize = systemFontData.getHeight();
	}

	public Font createFont(int sizeDifference, int style, String name, Display display) {
	    Font font = new Font(display, name, systemFontSize + sizeDifference, style);
	    instantiatedFonts.add(font);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, TextUtils.merge("Font of type ''{0}'', style ''{1}'' and size {2} was created", name, style, systemFontSize + sizeDifference));
	    }

	    return font;
	}

	public Font createFont(int sizeDifference, int style, Display display) {
	    return createFont(sizeDifference, style, systemFontName, display);
	}

	public Font createFont(int sizeDifference, Display display) {
        return createFont(sizeDifference, SWT.NORMAL, display);
    }

    public void disposeFonts() {
        synchronized (instantiatedFonts) {
            LOGGER.fine(TextUtils.merge("Disposing {0} fonts...", instantiatedFonts.size()));

            Iterator<Font> iterator = instantiatedFonts.iterator();

            while (iterator.hasNext()) {
                Font font = iterator.next();
                font.dispose();
                iterator.remove();
            }
        }
    }
}
