package com.dynatrace.easytravel.launcher.fancy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.config.UIProperties;
import com.dynatrace.easytravel.launcher.engine.ThreadEngine;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.ImageManager;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;


/**
 * Main component for MainMenu.
 * A {@link MenuComponent} contains multiple pages ({@link MenuPage}).
 * A Page contains multiple items ({@link MenuItem}).
 *
 * <p>
 * <b>Important:</b> Note that although this class is a subclass of <code>Composite</code>, it does not make sense to set a layout
 * on it.
 * </p>
 *
 * @author richard.vogl
 */
public class MenuComponent extends Composite implements LayoutCallback, PageProvider, Filterable, FilterListener {
    private static final Logger LOGGER = LoggerFactory.make();

    private static final int TABS_WIDTH = 316;
    private static final Color PAGE_BG_COLOR = Constants.Colors.WHITE;

    // show 5 groups at max (plus Problem Patterns page), start paging if there are more
    protected static final int BUTTONS_PER_SCENARIO_GROUPS_PAGE = 5;

    private final List<MenuPage> pages = new ArrayList<MenuPage>(10);

    private final List<Composite> contents = new ArrayList<Composite>();
    private final List<MenuPageButtonComponent> buttons = new ArrayList<MenuPageButtonComponent>(10);

    private int selectedPageIndex;
    private Image lastButtonBG;
    private Image lastButtonShadowBG;
    private Image underLastButtonBG;
    private Image bottomRightBG;
    private Image bottomBG;
    private Image bottomLeftBG;
    private Image rigthBG;
    private Image topRightBG;
    private Image topBG;
    private Composite pageBackground;
    private ScrolledComposite scrollablePageArea;
    private Composite pageContent;
    private final List<PageSelectionListener> pageSelectionListeners = new ArrayList<PageSelectionListener>();
    private final ImageManager imageManager;

    private Composite buttonBottom;
    private Composite tabsBottomBorder;
    private Composite emptyBottomArea;
    private FilterComponent filterComponent;

    // 0-based index of the current page of groups, i.e. paging of the buttoms on the left side
    // only active if there are more than BUTTONS_PER_PAGE scenario groups
    private int currentScenarioGroupPage = 0;

    private final Listener RESIZE_LISTENER = new Listener() {

        @Override
        public void handleEvent(Event resizeEvent) {
            layout(true, true);
        }
    };

    /**
     * Creates empty MainMenu.
     *
     * @param parent
     * @author richard.vogl
     */
    public MenuComponent(Composite parent) {
        this(parent, null);
    }

    /**
     * Creates MainMenu with provided Pages.
     *
     * @param parent
     * @param pages
     * @author richard.vogl
     */
    public MenuComponent(Composite parent, List<MenuPage> pages) {
        super(parent, SWT.NONE);
        imageManager = new ImageManager();
        registerListeners();
        if (pages != null) {
            this.pages.addAll(pages);
        }

        createContent();
    }

    /**
     * Creates the Components
     *
     * @author richard.vogl
     */
    private void createContent() {
        GridLayout layout;
        layout = new GridLayout();
        layout.numColumns = 2;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginTop = 10;
        layout.marginWidth = 10;
        GridData layoutData = new GridData();
        layoutData.verticalIndent = 0;
        layoutData.widthHint = -1;
        super.setLayout(layout);
        super.setLayoutData(layoutData);

        final Composite tabs = new Composite(this, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 1;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginBottom = 10;
        tabs.setLayout(layout);
        layoutData = new GridData();
        layoutData.verticalAlignment = SWT.FILL;
        layoutData.grabExcessVerticalSpace = true;
        layoutData.widthHint = TABS_WIDTH;	//images have fixed width, therefore resizing the tabs is not doable by only adapting the layout
        layoutData.heightHint = -1;
        layoutData.verticalIndent = 0;
        layoutData.horizontalIndent = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        tabs.setLayoutData(layoutData);

        pageBackground = new Composite(this, SWT.NONE);
        pageBackground.setBackground(PAGE_BG_COLOR);
        pageBackground.setBackgroundMode(SWT.INHERIT_DEFAULT);
        layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginBottom = 0;
        layout.marginHeight = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.makeColumnsEqualWidth = false;
        pageBackground.setLayout(layout);

        layoutData = new GridData();
        layoutData.verticalAlignment = SWT.FILL;
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        layoutData.horizontalAlignment = SWT.FILL;
        layoutData.verticalIndent = 0;
        layoutData.horizontalIndent = 0;
        pageBackground.setLayoutData(layoutData);

        refreshImages(pageBackground);

        Composite topBorder = new Composite(pageBackground, SWT.NONE);
        layoutData = new GridData();
        layoutData.verticalAlignment = SWT.FILL;
        layoutData.horizontalAlignment = SWT.FILL;
        layoutData.verticalIndent = 0;
        layoutData.horizontalIndent = 0;
        layoutData.heightHint = 9;
        layoutData.horizontalSpan = 2;
        topBorder.setLayoutData(layoutData);
        topBorder.setBackgroundImage(topBG);

        Label topRightBorder = new Label(pageBackground, SWT.NONE);
        layoutData = new GridData();
        layoutData.verticalIndent = 0;
        layoutData.horizontalIndent = 0;
        layoutData.widthHint = 5;
        layoutData.heightHint = 9;
        topRightBorder.setLayoutData(layoutData);

        topRightBorder.setImage(topRightBG);

        final Composite pageArea = new Composite(pageBackground, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 1;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginTop = 10;
        layout.marginBottom = 0;
        layout.marginWidth = 20;
        pageArea.setLayout(layout);

        layoutData = new GridData();
        layoutData.verticalIndent = 0;
        layoutData.horizontalIndent = 0;
        layoutData.horizontalSpan = 2;
        layoutData.grabExcessVerticalSpace = true;
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.horizontalAlignment = SWT.FILL;
        layoutData.verticalAlignment = SWT.FILL;
        pageArea.setLayoutData(layoutData);

        pageArea.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_PAGE_AREA);

        Composite rightBorder = new Composite(pageBackground, SWT.NONE);
        layoutData = new GridData();
        layoutData.verticalAlignment = SWT.FILL;
        layoutData.horizontalAlignment = SWT.FILL;
        layoutData.verticalIndent = 0;
        layoutData.horizontalIndent = 0;
        layoutData.widthHint = 5;
        rightBorder.setLayoutData(layoutData);
        rightBorder.setBackgroundImage(rigthBG);

        Label bottomLeftBorder = new Label(pageBackground, SWT.NONE);
        layoutData = new GridData();
        layoutData.verticalIndent = 0;
        layoutData.horizontalIndent = 0;
        layoutData.widthHint = 5;
        layoutData.heightHint = 8;
        bottomLeftBorder.setLayoutData(layoutData);
        bottomLeftBorder.setImage(bottomLeftBG);

        Composite bottomBorder = new Composite(pageBackground, SWT.NONE);
        layoutData = new GridData();
        layoutData.verticalAlignment = SWT.FILL;
        layoutData.horizontalAlignment = SWT.FILL;
        layoutData.verticalIndent = 0;
        layoutData.horizontalIndent = 0;
        layoutData.heightHint = 8;
        bottomBorder.setLayoutData(layoutData);
        bottomBorder.setBackgroundImage(bottomBG);

        Label bottomRightBorder = new Label(pageBackground, SWT.NONE);
        layoutData = new GridData();
        layoutData.verticalIndent = 0;
        layoutData.horizontalIndent = 0;
        layoutData.widthHint = 5;
        layoutData.heightHint = 8;
        bottomRightBorder.setLayoutData(layoutData);
        bottomRightBorder.setImage(bottomRightBG);

        /** filterComponent **/
        this.filterComponent = new FilterComponent(this);
        filterComponent.createControl(pageArea, SWT.NONE);
        this.addPageSelectionListener(new PageSelectionListener() {

            @Override
            public void pageSelected(Composite source, int index) {
                MenuPage selectedPage = pages.get(index);
                filterComponent.updateFrom(selectedPage.getFilterTaskParams());

                if (selectedPage.isFilterEnabled())
                    filterComponent.show(false);
                else
                    filterComponent.hide();
            }
        });

        scrollablePageArea = new ScrolledComposite(pageArea, SWT.V_SCROLL | SWT.H_SCROLL);
        scrollablePageArea.setBackground(PAGE_BG_COLOR);
        scrollablePageArea.setBackgroundMode(SWT.INHERIT_DEFAULT);
        scrollablePageArea.setExpandHorizontal(true);
        scrollablePageArea.setExpandVertical(true);
        scrollablePageArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        scrollablePageArea.setLayout(new FillLayout());
        scrollablePageArea.addListener(SWT.Resize, RESIZE_LISTENER);
        fixScrollbarStepping(scrollablePageArea.getVerticalBar());

        pageContent = new Composite(scrollablePageArea, SWT.NONE);

        layout = new GridLayout();
        layout.numColumns = 1;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.marginWidth = 0;
        pageContent.setLayout(layout);
        //pageContent.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_PAGE_SCROLL_CONTENT);

	    createPages(tabs, pageContent);

        scrollablePageArea.setContent(pageContent);
        scrollablePageArea.setMinSize(pageContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        scrollablePageArea.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_PAGE_AREA_SCROLL);

        createBottomArea(tabs);

        // only add, if we have more than BUTTONS_PER_PAGE scenario groups (-1 to exclude the Problem Patterns, which are always shown)
        if(buttons.size() -1 > BUTTONS_PER_SCENARIO_GROUPS_PAGE) {
            addPagingLink(tabs);
        }
    }

    /**
     * Draw the Link with the Page-Links and install a selection listener which
     * switches buttons accordingly.
     *
     * @param tabs
     */
    private void addPagingLink(final Composite tabs) {
        final Link link = new Link(tabs, SWT.NONE);

        GridData layoutData = new GridData(SWT.RIGHT, SWT.BOTTOM, true, false);
        link.setLayoutData(layoutData);

        Listener groupPagingListener = new Listener () {
            @Override
            public void handleEvent(Event event) {
                //System.out.println("Selection: " + event.text);
                currentScenarioGroupPage = Integer.parseInt(event.text);

                refreshGroupButtonVisibility(tabs, link);
            }

        };
        link.addListener (SWT.Selection, groupPagingListener);

        refreshGroupButtonVisibility(tabs, link);
    }

    /**
     * Set the scenario group buttons visible/hidden depending on the current page
     * while ensuring that we do not do too much redrawing.
     *
     * @param tabs
     * @param link
     */
    private void refreshGroupButtonVisibility(final Composite tabs, final Link link) {
        // disable redraw to not cause flickering
        tabs.setRedraw(false);

        try {
            displayButtonPage(tabs);
            setPagingLinkText(link);
        } finally {
            tabs.setRedraw(true);
        }

        // now ensure that we redraw everything correctly
        tabs.layout(true, true);
        tabs.redraw();
    }

    private void setPagingLinkText(Link link) {
        StringBuilder text = new StringBuilder("Go to page:  ");
        for(int i = 0;i*5 < buttons.size();i++) {
            if(i == currentScenarioGroupPage) {
                text.append(TextUtils.merge("{0} ", i+1));
            } else {
                text.append(TextUtils.merge("<a href=\"{0}\">{1}</a> ", i, i+1));
            }
        }

        // note: Link has a bug (at least on Linux) when only the links change, the redraw of the changed link is not done,
        // therefore we set a dummy text first and then the changed text!
        link.setText("...");

        link.setText(text.toString());
    }

    /**
     * Switch to the page indicated by currentScenarioGroupPage to
     * show only buttons that match there
     *
     * @param tabs
     */
    private void displayButtonPage(Composite tabs) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Redrawing buttons for page: " + currentScenarioGroupPage);
        }

        // "-1" to always always show the last item "Problem Patterns"
        for (int i = 0; i < (buttons.size()); i++) {
            MenuPageButtonComponent button = buttons.get(i);
            /*On Windows, they are not visible sometimes until they are drawn? if(button.isVisible())*/ {
                LOGGER.info("Hiding button " + i + ": " + buttons.get(i).getToolTipText());
                button.setVisible(false);
                button.setHidden(true);
            }
        }

        // then disable only those that should be shown (-1 to keep Problem Patterns scenario group)
        int i = currentScenarioGroupPage*BUTTONS_PER_SCENARIO_GROUPS_PAGE;
        if(i + BUTTONS_PER_SCENARIO_GROUPS_PAGE > buttons.size()) {
            i = buttons.size() - BUTTONS_PER_SCENARIO_GROUPS_PAGE;
        }
        for(int j = 0;j < BUTTONS_PER_SCENARIO_GROUPS_PAGE;j++,i++) {
            MenuPageButtonComponent button = buttons.get(i);
            LOGGER.info("Showing button " + i + ": " + button.getToolTipText());
	       // if(!button.isVisible() && UIProperties.PROBLEM_PATTERNS.getEnabled()) {
	        if(UIProperties.PROBLEM_PATTERNS.getEnabled()) {
                button.setVisible(true);
                button.setHidden(false);
            }
        }
    }

    /**
     * fixes the scrolling speed of a scrollbars
     *
     * @param scrollbar
     * @author patrick.haruksteiner
     */
    public static void fixScrollbarStepping(ScrollBar scrollbar) {
    	if (!Launcher.isWeblauncher()) {
    		scrollbar.setIncrement(15);
    		scrollbar.setPageIncrement(50);
    	}
    }

    private void createBottomArea(Composite tabs) {
        GridData layoutData;
        buttonBottom = new Composite(tabs, SWT.NONE);
        buttonBottom.setBackgroundImage(lastButtonBG);
        buttonBottom.setBackgroundMode(SWT.INHERIT_DEFAULT);
        layoutData = new GridData();
        layoutData.horizontalAlignment = SWT.FILL;
        layoutData.verticalAlignment = SWT.FILL;
        layoutData.heightHint = 8;
        buttonBottom.setLayoutData(layoutData);

        tabsBottomBorder = new Composite(tabs, SWT.NONE);
        layoutData = new GridData();
        layoutData.verticalAlignment = SWT.FILL;
        layoutData.grabExcessVerticalSpace = true;
        layoutData.horizontalAlignment = SWT.FILL;
        layoutData.verticalIndent = 0;
        layoutData.horizontalIndent = 0;
        tabsBottomBorder.setLayoutData(layoutData);
        tabsBottomBorder.setBackgroundImage(underLastButtonBG);

        emptyBottomArea = new Composite(tabs, SWT.NONE);
        layoutData = new GridData();
        layoutData.verticalIndent = 0;
        layoutData.horizontalIndent = 0;
        layoutData.heightHint = 7;
        emptyBottomArea.setLayoutData(layoutData);
    }


    private void refreshImages(Composite pageBackground) {
        if (Launcher.isWidgetDisposed(this)) {
            return;
        }
        Image createImage = imageManager.createImage(Constants.Images.FANCY_MENU_PAGE_BG_TOP);
        if (topBG != null && !topBG.isDisposed()){
            topBG.dispose();
        }
        topBG = createImageWithTransparentBG(createImage, pageBackground);
        createImage = imageManager.createImage(Constants.Images.FANCY_MENU_PAGE_BG_TOP_RIGHT);
        if (topRightBG != null && !topRightBG.isDisposed()){
            topRightBG.dispose();
        }
        topRightBG = createImageWithTransparentBG(createImage);
        createImage = imageManager.createImage(Constants.Images.FANCY_MENU_PAGE_BG_RIGHT);
        if (rigthBG != null && !rigthBG.isDisposed()){
            rigthBG.dispose();
        }
        rigthBG = createImageWithTransparentBG(createImage);
        createImage = imageManager.createImage(Constants.Images.FANCY_MENU_PAGE_BG_BOTTOM_LEFT);
        if (bottomLeftBG != null && !bottomLeftBG.isDisposed()){
            bottomLeftBG.dispose();
        }
        bottomLeftBG = createImageWithTransparentBG(createImage);
        createImage = imageManager.createImage(Constants.Images.FANCY_MENU_PAGE_BG_BOTTOM);
        if (bottomBG != null && !bottomBG.isDisposed()){
            bottomBG.dispose();
        }
        bottomBG = createImageWithTransparentBG(createImage);
        createImage = imageManager.createImage(Constants.Images.FANCY_MENU_PAGE_BG_BOTTOM_RIGHT);
        if (bottomRightBG != null && !bottomRightBG.isDisposed()){
            bottomRightBG.dispose();
        }
        bottomRightBG = createImageWithTransparentBG(createImage);
        createImage = imageManager.createImage(Constants.Images.FANCY_MENU_LAST_BT_BG_FULL);
        if (lastButtonBG != null && !lastButtonBG.isDisposed()){
            lastButtonBG.dispose();
        }
        lastButtonBG = createImageWithTransparentBG(createImage);
        createImage = imageManager.createImage(Constants.Images.FANCY_MENU_LAST_BT_BG_SHADOW_FULL);
        if (lastButtonShadowBG != null && !lastButtonShadowBG.isDisposed()){
            lastButtonShadowBG.dispose();
        }
        lastButtonShadowBG = createImageWithTransparentBG(createImage);
        createImage = imageManager.createImage(Constants.Images.FANCY_MENU_LAST_BT_BG);
        if (underLastButtonBG != null && !underLastButtonBG.isDisposed()){
            underLastButtonBG.dispose();
        }
        underLastButtonBG = createImageWithTransparentBG(createImage);
    }

    private Image createImageWithTransparentBG(Image image){
        return createImageWithTransparentBG(image, this);
    }

    /**
     * Converts an image with alpha blend values to an non-transparent image using the
     * background color from the provided parent control.
     *
     * @param createImage
     * @param parent
     * @return
     * @author richard.vogl
     */
    static Image createImageWithTransparentBG(Image createImage, Control parent) {
        // Note: RAP cannot construct the Image like we do it below,
        // is there another way that we can use?
        // Not urgent as this just makes the image look slightly better, but does not affect functionality
    	if (Launcher.isWeblauncher()) {
            return createImage;
        }

        Image bg;
        GC gc;
        bg = new Image(createImage.getDevice(), createImage.getBounds());
        gc = new GC(bg);
        gc.setBackground(parent.getBackground());
        gc.fillRectangle(0, 0, createImage.getBounds().width, createImage.getBounds().height);
        gc.drawImage(createImage, 0, 0);
        return bg;
    }

    /**
     * Creates the pages and fills the pageContent areas.
     *
     */
    private int createPages(Composite tabs, final Composite pageArea) {
        int index = 0;

        for (MenuPage page : pages) {

	        if (!(!UIProperties.PROBLEM_PATTERNS.getEnabled() && page.getTitle().equals(MessageConstants.SCENARIO_GROUP_PLUGINS))) {

	        createPage(tabs, pageArea, index, page);
            index++;
	        }
        }

        return index;
    }

    private void createPage(Composite tabs, final Composite pageArea, int index, MenuPage page) {
        Composite content = page.createContent(pageArea, this);
        page.applyFilter(page.getFilterTaskParams());
        page.addFilterListener(this);

        GridData layoutData = (GridData) content.getLayoutData();

        contents.add(content);

        MenuPageButtonComponent currentButton = page.createButton(tabs);
        if (index == 0) { //activate first page
            currentButton.forceSelect(null, true);
            content.setVisible(true);
            layoutData.exclude = false;
        } else {
            if (index == 1) {// second button is a shadow button
                currentButton.setUnderSelected();
            }
            layoutData.exclude = true;
            content.setVisible(false);
        }
        content.setLayoutData(layoutData);

        buttons.add(currentButton);
        currentButton.addSelectionListener(new PageButtonSelectionListener(pageArea, index, this)); // NOPMD
    }

    private void destroyPage(int index) {
        // remove content
        if (index < contents.size()) {
	        Composite comp = contents.get(index);
	        contents.remove(index);
	        comp.dispose();
        }


        // remove button
	    if (index < buttons.size()) {
            MenuPageButtonComponent button = buttons.get(index);
            buttons.remove(index);
            button.dispose();
	    }
    }

    public List<Composite> getContents() {
        return contents;
    }

    public List<MenuPageButtonComponent> getButtons() {
        return buttons;
    }

    /**
     * Preselects the page of the menu with the provided index.
     *
     * @param index
     * @author richard.vogl
     */
    public void selectPage(int index){
        if (index >= buttons.size()){
            return;
        }

        int pageId = index;

        // select next visible page
        for (; !pages.get(pageId).checkVisible();){
            pageId = (pageId+1) % pages.size();
        }
        selectedPageIndex = pageId;
        MenuPageButtonComponent menuPageButtonComponent = buttons.get(pageId);
        menuPageButtonComponent.forceSelect(null, true);
    }

    public int getSelectedPageIndex() {
        return selectedPageIndex;
    }

    private void checkSelection(){
        selectPage(selectedPageIndex);
    }

    /**
     * Returns the current Page with this id.
     *
     * @param id
     * @return
     * @throws IndexOutOfBoundsException if the index is out of range.
     * @author dominik.stadler
     */
    public Composite getMenuPage(int id){
        return contents.get(id);
    }

    @Override
    public void layout(boolean changed, boolean all) {
        checkSelection();

        if (scrollablePageArea != null && pageContent != null) {
            ScrollBar scrollBar = scrollablePageArea.getVerticalBar();
            int scrollbarWidth = (scrollBar == null) ? 0 : scrollBar.getSize().x;

            Point size = pageContent.computeSize(scrollablePageArea.getBounds().width - scrollbarWidth, SWT.DEFAULT);
            scrollablePageArea.setMinSize(size);
        }

        super.layout(changed, all);
    }

    /**
     * notifies the page selection listeners.
     *
     * @param index Id of the selected button/page
     * @param selectedButton The button which was pressed to selecte the page.
     * @author richard.vogl
     */
    public void notifyPageSelectedListener(int index, MenuPageButtonComponent selectedButton) {
        synchronized(pageSelectionListeners){
            for (PageSelectionListener listener : pageSelectionListeners){
                listener.pageSelected(selectedButton, index);
            }
        }

        layout(true, true);
        redraw();
    }

    /**
     * Adds a {@link PageSelectionListener} to this MenuComponent. A <code>PageSelectionListener</code>'s <code>pageSelected</code>
     * method is called, after a button is clicked and the corresponding page is displayed.
     *
     * @param listener
     * @return
     * @author richard.vogl
     */
    public boolean addPageSelectionListener(PageSelectionListener listener){
        return pageSelectionListeners.add(listener);
    }

    /**
     * Removes a {@link PageSelectionListener} from this MenuComponent.
     *
     * @param listener
     * @return
     * @author richard.vogl
     */
    public boolean removePageSelectionListener(PageSelectionListener listener){
        return pageSelectionListeners.remove(listener);
    }

    /**
     * Registers necessary listeners for this dialog.
     *
     * @author richard.vogl
     */
    private void registerListeners() {
        addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent event) {
                MenuComponent.this.widgedDisposed();
            }
        });
    }

    @Override
    public final void addDisposeListener(DisposeListener listener) {
        // Note: just overridden to make this method final (--> is indirectly called by the constructor)
        super.addDisposeListener(listener);
    }

    /**
     * This method is called when the component's controls are disposed.
     * Implementations have to dispose resources like images, colors which have been created for the pageContent of the component.
     *
     * @author richard.vogl
     */
    protected final void widgedDisposed() {
        imageManager.disposeImages();

        if (this.bottomBG != null) {
            this.bottomBG.dispose();
        }

        if (this.bottomLeftBG != null) {
            this.bottomLeftBG.dispose();
        }

        if (this.bottomRightBG != null) {
            this.bottomRightBG.dispose();
        }

        if (this.rigthBG != null) {
            this.rigthBG.dispose();
        }

        if (this.topRightBG != null) {
            this.topRightBG.dispose();
        }

        if (this.topBG != null) {
            this.topBG.dispose();
        }
    }

    /* (non-Javadoc)
     * @see com.dynatrace.easytravel.launcher.fancy.PageProvider#replacePage(int, com.dynatrace.easytravel.launcher.fancy.MenuPage)
     */
    @Override
    public void replacePage(final int id, final MenuPage page) {
        if (Launcher.isWidgetDisposed(this)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Menu component is already disposed, cannot replace Page");
            }
            return;
        }

		// avoid unnecessary redrawing of menu items if nothing changed
		if (pages.get(id).itemsEqual(page)
				// re-draw the problem patterns tab if it was switched
				&& !(page.getTitle().equals(MessageConstants.SCENARIO_GROUP_PLUGINS) && UIProperties.PROBLEM_PATTERNS.isSwitched())) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Page did not change, it is not replaced");
            }
			return;
        }

        if(LOGGER.isInfoEnabled()) {
            LOGGER.info("Replacing menu page " + id + " with a new page");
        }


        // need to execute this in an SWT thread as it is called in a different thread usually
        ThreadEngine.runInDisplayThread(new Runnable() {
            @Override
            public void run() {
                if (Launcher.isWidgetDisposed(buttons.get(0))) {
                    return;
                }

                // remember parents for adding the new page
                Composite buttonParent = buttons.get(0).getParent();
                Composite contentParent = contents.get(0).getParent();

                buttonParent.setRedraw(false);
                contentParent.setRedraw(false);

                try {
                    // keep filter
                    page.applyFilter(pages.get(id).getFilterTaskParams());

                    // remove current page
                    destroyPage(id);

                    //problem patterns are should be redraw only when enabled
	                if (UIProperties.PROBLEM_PATTERNS.getEnabled() || !page.getTitle().equals(MessageConstants.SCENARIO_GROUP_PLUGINS)) {
		                // create the new one
	                    createPage(buttonParent, contentParent, id, page);

	                    // replace in 'this.pages'
	                    replacePageInCollection(id, page);
	                }

                    if ((page.getTitle().equals(MessageConstants.SCENARIO_GROUP_PLUGINS)
                         && UIProperties.PROBLEM_PATTERNS.isSwitched())) {
                        //when problem pattern page is gone, first page should be shown
                        UIProperties.PROBLEM_PATTERNS.clearSwitchFlag();
                        if (!UIProperties.PROBLEM_PATTERNS.getEnabled()) {
                            selectPage(0);
                        }
                    }
                    // recreate the shadow area below the buttons
                    buttonBottom.dispose();
                    tabsBottomBorder.dispose();
                    emptyBottomArea.dispose();
                    createBottomArea(buttonParent);
                } finally {
                    contentParent.setRedraw(true);
                    buttonParent.setRedraw(true);
                }

                buttonParent.layout(true);
                contentParent.layout(true);

                // make sure the page is selected again if it was selected before
                checkSelection();
            }

            /**
             * replaces a page in the pages collection
             *
             * @author christoph.neumueller
             */
            private void replacePageInCollection(int id, MenuPage page) {
                pages.remove(id);
                pages.add(id, page);
            }
        }, this);
    }

    /**
     * Return a list of all current menu pages
     *
     * @param page
     * @author richard.vogl
     * @return
     */
    public List<MenuPage> getPages() {
        return pages;
    }

    @Override
    public int getPageCount() {
        return pages.size();
    }

    @Override
    public void setLayout(Layout layout) {
        // do nothing, we don't want someone to destroy our layout from outside...
        throw new UnsupportedOperationException("This component does not support custom layouts."); //$NON-NLS-1$
    }

    /**
     * Redraws the whole menu.
     *
     * @author richard.vogl
     */
    public void refresh() {
        if (Launcher.isWidgetDisposed(this)) {
            return;
        }
        selectPage(selectedPageIndex); // check if we have to change the currently selected page because it is not visible anymore
        Rectangle bounds = this.getBounds();
        this.redraw(0, 0, bounds.width, bounds.height, true);
    }

    public void setButtonBottomSelected(boolean isSelected) {
        if (isSelected) {
            buttonBottom.setBackgroundImage(lastButtonShadowBG);
        } else {
            buttonBottom.setBackgroundImage(lastButtonBG);
        }
    }

    @Override
    public void notifyLayoutEvent() {
        layout(true, true);
        redraw();
    }

    @Override
    public void applyFilter(FilterTaskParams filterTaskParams) {
        pages.get(selectedPageIndex).applyFilter(filterTaskParams);
    }

    @Override
    public void clearFilter() {
        pages.get(selectedPageIndex).clearFilter();
    }

    @Override
    public void beforeApplyFilter() {
        this.setRedraw(false);
    }

    @Override
    public void afterApplyFilter() {
        this.layout(true, true);
        this.setRedraw(true);
    }

    public void indicateRunning(final String group, final String title, final boolean used) {
        // needs to run in Display Thread
        ThreadEngine.runInDisplayThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < pages.size(); i++) {
                    if (pages.get(i).getTitle().equals(group)) {
                        buttons.get(i).indicateUsage(used);
                    }
                }
            }
        }, this);
    }
}
