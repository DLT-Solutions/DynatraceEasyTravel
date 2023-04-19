package com.dynatrace.easytravel.plugin.slowimages;

import java.util.Collection;
import java.util.Random;
import java.util.Vector;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

/**
 * this easyTravel plugin simulates a slow connection to resources with a certain file suffix.
 * 
 * note that the affected files are basically captured by the ServletFilter 'ResourcesFilter' which
 * is bound to the  /img/ folder as defined in web.xml.
 * 
 * @see ResourcesFilter 
 * @author cwat-shauser
 *
 */
public class SlowImagesPlugin extends AbstractGenericPlugin {

	private static final Random rnd = new Random(System.currentTimeMillis());
	
	private static Collection<String> imgsuffixes;
	
	private static int MIN_DELAY = 200;

	private static final Logger logger = LoggerFactory.make();

	static {
		imgsuffixes = new Vector<String>();
		imgsuffixes.add("jpg");
		imgsuffixes.add("jpeg");
		imgsuffixes.add("png");
		imgsuffixes.add("gif");
		imgsuffixes.add("bmp");
		imgsuffixes.add("ico");
	}
	
	/**
	 * executes the plugin. every requested resource with a given file suffix (see static constructor)
	 * will be slowed down using Thread.sleep. 
	 * 
	 * mininum slowdown value is 200ms.
	 * maximum slowdown value is specified by Integer parameter (context[1])
	 * 
	 * @param String location the extension point string
	 * @param Object[] the context parameters: when called from ResourcesServlet, context[0] contains the
	 * URI of the requested resource (String), context[1] contains an Integer object representing the maximum
	 * slowdown value
	 * 
	 * @return null
	 * @author cwat-shauser
	 */
	@Override
	protected Object doExecute(String location, Object... context) {
		
		String uri = context[0].toString().toLowerCase();
		
		int maxdelay = ((Integer)context[1]).intValue();
		int delayspan = maxdelay - MIN_DELAY;
		int slowdown = MIN_DELAY;
		if(delayspan > 0) slowdown += rnd.nextInt(delayspan);
		
		if(imgsuffixes.contains(this.getFileSuffix(uri))){
			try {
				Thread.sleep(slowdown);
				logger.info("slowing down " + uri + " by " + slowdown + "ms");
			} catch (InterruptedException ignore) { }
		}
		return null;
	}
	
	/**
	 * returns the file suffix of a given filename
	 * 
	 * @param filename the name of the file
	 * @return String the file suffix
	 */
	private String getFileSuffix(String filename){
		int index = filename.lastIndexOf('.');
		return index == -1 ? null : filename.substring(index + 1);
	}
}
