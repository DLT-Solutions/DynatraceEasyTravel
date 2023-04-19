package com.dynatrace.easytravel.launcher.fancy.custom;

import com.dynatrace.easytravel.launcher.fancy.MenuActionCallback;
import com.dynatrace.easytravel.launcher.fancy.MenuPage;
import com.dynatrace.easytravel.launcher.fancy.MenuVisibilityCallback;


public class ScenarioMenuPage extends MenuPage {

    public ScenarioMenuPage(String title, MenuActionCallback action, MenuVisibilityCallback visibilityCallback) {
        this(title, action, visibilityCallback, null);
    }
    
    public ScenarioMenuPage(String title, MenuActionCallback action, MenuVisibilityCallback visibilityCallback, String image) {
        super(image, title, action, visibilityCallback);
    }

}
