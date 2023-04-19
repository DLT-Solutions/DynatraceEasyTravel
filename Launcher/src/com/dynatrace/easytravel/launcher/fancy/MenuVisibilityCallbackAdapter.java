package com.dynatrace.easytravel.launcher.fancy;


/**
 *
 * @author patrick.haruksteiner
 */
public class MenuVisibilityCallbackAdapter implements MenuVisibilityCallback {

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

    public static final MenuVisibilityCallback VISIBLE_AND_ENABLED = new MenuVisibilityCallback() {

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public boolean isVisible() {
            return true;
        }
    };

    public static final MenuVisibilityCallback VISIBLE_AND_DISABLED = new MenuVisibilityCallback() {

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public boolean isVisible() {
            return true;
        }
    };
}
