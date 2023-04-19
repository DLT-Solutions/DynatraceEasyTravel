package com.dynatrace.easytravel.launcher.fancy;

import java.util.List;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;


public class PageButtonSelectionListener implements SelectionListener {

    private final Composite pageArea;
    private final int index;
    private final MenuComponent menuComponent;

    public PageButtonSelectionListener(Composite pageArea, int index, MenuComponent menuComponent) {
        this.pageArea = pageArea;
        this.index = index;
        this.menuComponent = menuComponent;
    }

    @Override
    public void widgetSelected(SelectionEvent event) {
        boolean lastWasSelected = false;
        MenuPageButtonComponent selectedButton = null;
        menuComponent.selectPage(index);

        List<Composite> contents = menuComponent.getContents();

        for (int index = 0; index < contents.size(); index++) {
            Composite pageContent = contents.get(index);
            GridData layoutData = (GridData) pageContent.getLayoutData();
            MenuPageButtonComponent currentButton = menuComponent.getButtons().get(index);

            if (index == this.index){
                layoutData.exclude = false;
                pageContent.setVisible(true);
                lastWasSelected = true;
                selectedButton = currentButton;
            } else {
                layoutData.exclude = true;
                pageContent.setVisible(false);

                currentButton.deselect();
                if (lastWasSelected) {
                    currentButton.setUnderSelected();
                }
                currentButton.redraw();
                lastWasSelected = false;
            }
        }

        menuComponent.setButtonBottomSelected(lastWasSelected);

        pageArea.layout(true);

        menuComponent.notifyPageSelectedListener(index, selectedButton);
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent event) {
         widgetSelected(event);
    }

}
