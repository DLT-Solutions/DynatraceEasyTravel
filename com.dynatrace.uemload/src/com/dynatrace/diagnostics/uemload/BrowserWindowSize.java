package com.dynatrace.diagnostics.uemload;

import com.dynatrace.easytravel.util.TextUtils;

/**
* set of common browser window size
*
* @author cwat-shauser
*
*
*/
public enum BrowserWindowSize {

	_800x600(800, 600, false),
	_1024x768(1024, 768, false),
	_1280x1024(1280, 1024, false),
	_1366x768(1366, 768, false),
	_1280x800(1280, 800, false),
	_1920x1080(1920, 1080, false),
	_1440x900(1440, 900, false),
	_1680x1050(1680, 1050, false),
	_1600x900(1600, 900, false),
	_1920x1200(1920, 1200, false),
	_1360x768(1360, 768, false),
	_1280x768(1280, 768, false),
	_1280x720(1280, 720, false),
	_1280x960(1280, 960, false),
	_2560x2048(2560, 2048, false),
	_2048x1536(2048, 1536, false),
	_1600x1200(1600, 1200, false),
	_1440x960(1440, 960, false),
	_1400x1050(1400, 1050, false),
	_640x360(640, 360, false),
	_1536x864(1536, 864, false),
	_2560x1440(2560, 1440, false),
	_1024x600(1024, 600, false),
	_1364x768(1364, 768, false),
	_1152x864(1152, 864, false),

	// RESOLUTIONS OF MOBILE DEVICES

	_m320x240(320, 240, true),
	_m800x480(800, 480, true),
	_m960x640(960, 640, true),
	_m640x480(640, 480, true),
	_m640x360(640, 360, true),
	_m667x375(667, 375, true),
	_m568x320(568, 320, true),
	_m1280x720(1280, 720, true),
	_m534x320(534, 320, true),
	_m960x540(960, 540, true),
	_m854x480(854, 480, true),
	_m736x414(736, 414, true),
	_m570x320(570, 320, true),
	_m1920x1080(1920, 1080, true),
	_m732x412(732, 412, true),
	_m1184x720(1184, 720, true),
	_m592x360(592, 360, true),
	_m352x288(352, 288, true),
	_m480x320(480, 320, true),
	_m2560x1440(2560, 1440, true); 

	/** window width (px) */
	private final int windowWidth;
	/** window height (px) */
	private final int windowHeight;
	
	/** screen width (px) */
	private final int screenWidth;
	/** screen height (px) */
	private final int screenHeight;

	public static final String BrowserWindowSize_WINDOW_SIZE_DESCRIPTION = "{0} x {1}";

	/**
	 * BrowserWindowSize C'tor
	 *
	 * @author cwat-shauser
	 * @param width window width
	 * @param height window height
	 */
	BrowserWindowSize(int width, int height, boolean mobile) {
		
		this.screenWidth = width;
		this.screenHeight = height;
		
		if (mobile) {
			this.windowWidth = width;
			this.windowHeight = height;
		} else {		
			if (width > 2000) {
				this.windowWidth = (width - 10) / 2;
				this.windowHeight = width - 50;
			} else if (width > 1600) {
				this.windowWidth = (int) Math.round(width - 10 * 0.7);
				this.windowHeight = (int) Math.round(width - 50 * 0.7);
			} else {
				this.windowWidth = width - 10;
				this.windowHeight = height - 50;
			}	
		}
	}

	/**
	 * get the window's width
	 *
	 * @author cwat-shauser
	 * @return int window width
	 */
	public int getWidth() {
		return this.windowWidth;
	}

	/**
	 * get the window's height
	 *
	 * @author cwat-shauser
	 * @return int window height
	 */
	public int getHeight() {
		return this.windowHeight;
	}
	
	/**
	 * get the window's width
	 *
	 * @return int window width
	 */
	public int getScreenWidth() {
		return this.screenWidth;
	}

	/**
	 * get the window's height
	 *
	 * @return int window height
	 */
	public int getScreenHeight() {
		return this.screenHeight;
	}

	/**
	 * determines if the current BrowserWindowSize object is smaller than another.
	 *
	 * only returns true if both width and height are equal or smaller than the other object's values
	 *
	 * @author cwat-shauser
	 * @param other the other BrowserWindowSize for comparison
	 * @return true if current BrowserWindowSize is smaller than the other one
	 */
	public boolean isSmallerThan(BrowserWindowSize other) {
		if (other == null)
			return false;
		return this.isSmallerThan(other.getWidth(), other.getHeight());
	}

	/**
	 * determines if the current BrowserWindowSize object is smaller than a given dimension
	 *
	 * @author cwat-shauser
	 * @param width the width to compare with
	 * @param height the height to compare with
	 * @return true if current BrowserWindowSie is smaller than the given dimensions
	 */
	public boolean isSmallerThan(int width, int height) {
		if (width == this.getWidth() && height == this.getHeight())
			return false;
		return this.getWidth() <= width && this.getHeight() <= height;
	}

	/**
	 * determines if the BrowserWindowSize object fits into another one
	 *
	 * @author cwat-shauser
	 * @param other the BrowserWindowSize to compare
	 * @return true if Object fits into anoher BrowserWindowSize
	 */
	public boolean fits(BrowserWindowSize other) {
		if (other == null)
			return false;
		return this.fits(other.getWidth(), other.getHeight());
	}

	/**
	 * determines if the BrowserWindowSize object fits into a given dimension
	 *
	 * @author cwat-shauser
	 * @param width the given dimension's width
	 * @param height the given dimension's height
	 * @return true if object fits into dimensions
	 */
	public boolean fits(int width, int height) {
		return width <= this.getWidth() && height <= this.getHeight();
	}

	@Override
	public String toString() {
		return this.toString(BrowserWindowSize_WINDOW_SIZE_DESCRIPTION);
	}

	/**
	 * returns a string representation of the BrowserWindowSize object
	 *
	 * @author cwat-shauser
	 * @param pattern String pattern containing "{0}" and "{1}" to display width and height
	 * @return string representation
	 * @internal used for JUnit tests
	 */
	public String toString(String pattern) {
		return TextUtils.merge(pattern, this.getWidth(), this.getHeight());
	}

	/**
	 * creates a string containing the window size category where a given window dimension fits into.
	 *
	 * Example:
	 * - a window resolution of 600 x 400 fits minimal into the BrowserWindowSize VGA, so "640 x 480" is
	 *   being returned
	 * - 1024 x 680 fits minimal into XGA, so "1024 x 768" is returned
	 * - 2800 x 2048 fits into none of the given BrowserWindowSize objects; therefore, a string like
	 *   "> 2560 x 2048" is returned (where "2560 x 2048" represents the biggest available screen resolution QSXGA)
	 *
	 *
	 * @author cwat-shauser
	 * @param width the given window width
	 * @param height the given window height
	 * @return a String representation of the fitting BrowserWindowSize
	 */
	public static String createWindowSizeString(int width, int height) {
		return BrowserWindowSize.createWindowSizeString(width, height, BrowserWindowSize_WINDOW_SIZE_DESCRIPTION);
	}

	/**
	 * @see createWindowSize
	 * @internal used for JUnit tests with variable return String-pattern
	 */
	public static String createWindowSizeString(int width, int height, String pattern) {
		BrowserWindowSize foundSize = getWindowSize(width, height);
		return foundSize != null
			? foundSize.toString(pattern)
			: "> " + BrowserWindowSize._2560x2048.toString(pattern); //$NON-NLS-1$
	}

	/**
	 * @see createWindowSize
	 */
	public static BrowserWindowSize getWindowSize(int width, int height) {
		BrowserWindowSize foundsize = null;
		for (BrowserWindowSize size : BrowserWindowSize.values()) {
			// window size does not fit
			if (!size.fits(width, height))
				continue;
			if (foundsize == null) {
				foundsize = size;
				continue;
			}
			if (size.isSmallerThan(foundsize))
				foundsize = size;
		}
		return foundsize;
	}
	
	public String createChromiumArgumentString() {
		return String.format("window-size=%dx%d", this.screenWidth, this.screenHeight);
	}
}