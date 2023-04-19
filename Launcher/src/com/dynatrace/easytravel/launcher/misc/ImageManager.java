/***************************************************************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: ImageRegistry.java
 * @date: 10.03.2008
 * @author: roland.mungenast
 */
package com.dynatrace.easytravel.launcher.misc;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.dynatrace.easytravel.util.TextUtils;


public final class ImageManager {

    private static final Logger LOGGER = Logger.getLogger(ImageManager.class.getName());

    private final Map<String,Image> instantiatedImages = new HashMap<String,Image>();

    public synchronized Image createImage(String name) {
    	Image image = instantiatedImages.get(name);
    	if(image != null) {
    		return image;
    	}

        
        try (InputStream resource = ImageManager.class.getResourceAsStream(name)) {
            if(resource == null) {
            	throw new IllegalStateException("Cannot load image '" + name + "', not found in current classpath.");
            }

			image = new Image(Display.getDefault(), resource);
        } catch (IOException e) {
        	LOGGER.severe("Resource cannot be closed: " + name);
		} 

        instantiatedImages.put(name, image);
        LOGGER.fine(TextUtils.merge("Image ''{0}'' was created.", name));

        return image;
    }

    public synchronized void disposeImages() {
        synchronized (instantiatedImages) {
            LOGGER.fine(TextUtils.merge("Disposing {0} images...", instantiatedImages.size()));

            for(Image image : instantiatedImages.values()) {
                image.dispose();
            }
            instantiatedImages.clear();
        }
    }
}
