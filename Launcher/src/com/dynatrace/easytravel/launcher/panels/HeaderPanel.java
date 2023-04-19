package com.dynatrace.easytravel.launcher.panels;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

import com.dynatrace.easytravel.config.CustomerTrafficScenarioEnum;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.Version;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.baseload.BaseLoadManager;
import com.dynatrace.easytravel.launcher.engine.ThreadEngine;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.DocumentStarter;
import com.dynatrace.easytravel.launcher.misc.FontManager;
import com.dynatrace.easytravel.launcher.misc.ImageManager;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.procedures.utils.CentralTechnologyActivator;
import com.dynatrace.easytravel.launcher.procedures.utils.TechnologyActivatorListener;
import com.dynatrace.easytravel.launcher.rap.RAPLink;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.DynatraceUrlUtils;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Strings;

import ch.qos.logback.classic.Logger;

public class HeaderPanel extends HeaderPanelBase implements TechnologyActivatorListener {
	private static final Logger LOGGER = LoggerFactory.make();

	private static final int WEBLAUNCHER_URL_SIZE = 400;

	private final ImageManager imageManager;
	private final FontManager fontManager;

	private Label versionLabelET;
	private Scale trafficSlider;

	private RAPLink mobileNativeLink;
	private RAPLink mobileBrowserLink;
	private Label visitsMobileNativeLabel;
	private Label visitsMobileBrowserLabel;
	private RAPLink javaLink;
	private Label visitsJavaLabel;
	private RAPLink dotNetLink;
	private Label visitsDotNetLabel;
	private final Color grayColor;

	private Button headerTaggingButton;

	private Group trafficLabel;

	private Composite mainComp;

	private Button createVisitsButton;
	private Button createNowButton;

	private Button createBrowserVisitsButton;
	private Button createB2bVisitsButton;
	private Button createMobileNativeVisitsButton;
	private Button createMobileBrowserVisitsButton;

	private Button dotNetCheck;
	private Button customBrowserCheck;
	private Button mobileBrowserCheck;
	private Button mobileNativeCheck;

	private RAPLink dcrumButton;
	private RAPLink dcrumErrorButton;
	private Composite dcrumComposite;

	private Composite mainComposite;

	private Label usedScenarioLabel;

	public HeaderPanel(final Composite parent, int style, boolean taggedWebRequest, boolean createNowEnabled, int sliderPos) {
		mainComposite = new Composite(parent, style);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		grayColor = new Color(mainComposite.getDisplay(), 84, 84, 84);


		CentralTechnologyActivator.getIntance().registerFrontendListener(this);
		imageManager = new ImageManager();
		fontManager = new FontManager();
		parent.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent event) {
				imageManager.disposeImages();
				fontManager.disposeFonts();
				grayColor.dispose();
			}
		});

		init(taggedWebRequest, createNowEnabled, sliderPos);
	}

	private void init(boolean taggedWebRequest, boolean createNowEnabled, int sliderPos) {
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();

		// add a listener which openes the links to the frontends in a browser
		SelectionAdapter listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				LOGGER.info("Opening header-link: " + event.text);
				DocumentStarter starter = new DocumentStarter();
				starter.openURL(event.text);
			}
		};

		mainComposite.setBackground(Constants.Colors.WHITE);
		mainComposite.setBackgroundMode(SWT.INHERIT_FORCE);

		GridLayout headerLayout = new GridLayout();
		headerLayout.numColumns = 1;
		headerLayout.marginWidth = 10;
		headerLayout.verticalSpacing = 0;
		mainComposite.setLayout(headerLayout);

		createImageVersionConfigArea();

		// composite to have multiple columns
		mainComp = new Composite(mainComposite, SWT.NONE);
		GridLayout compLayout = new GridLayout();
		compLayout.numColumns = 2;
		compLayout.marginWidth = 0;
		compLayout.marginRight = 0;
		compLayout.verticalSpacing = 0;
		compLayout.horizontalSpacing = 0;
		mainComp.setLayout(compLayout);

		// use the same indent as the text above and add some space to the previous item
		GridData compLayoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
		compLayoutData.horizontalIndent = 10;
		mainComp.setLayoutData(compLayoutData);

		createLeftLinksArea(listener);

		updateDescription();

		createTrafficSliderArea(CONFIG, listener, taggedWebRequest, createNowEnabled, sliderPos);

		dcrumButton = createDCRumButton(dcrumComposite, "DC-RUM Dashboard", 130, "DCRUM Integration");
		dcrumErrorButton = createDCRumButton(dcrumComposite, "DC-RUM Errors Dashb.", 150, "DCRUM Integration - Errors");

		dcrumButton.setVisible(false);
		dcrumErrorButton.setVisible(false);
	}

	protected void createImageVersionConfigArea() {
		Composite topComposite = new Composite(mainComposite, SWT.NONE);
		GridLayout rcLayout = new GridLayout();
		rcLayout.numColumns = 3;
		rcLayout.marginWidth = 0;
		rcLayout.marginHeight = 0;
		topComposite.setLayout(rcLayout);
		GridData compLayoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
		topComposite.setLayoutData(compLayoutData);

		// put image and static text
		Label image = new Label(topComposite, SWT.NONE);
		image.setImage(imageManager.createImage(DtVersionDetector.getHeaderImageName()));
		GridData imageLayoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
		image.setLayoutData(imageLayoutData);
		image.setToolTipText(MessageConstants.EASYTRAVEL_ABSTRACT);

		Version version = Version.read();
		versionLabelET = new Label(topComposite, SWT.RIGHT);

		if (Launcher.getLoggedInUser() != null) {
			versionLabelET.setText(BaseConstants.EASYTRAVEL + BaseConstants.WS + version + " Build Date: " + version.getOnlyDateString() + BaseConstants.CRLF + "Logged in: " + Launcher.getLoggedInUser().getName());
		} else {
			versionLabelET.setText(BaseConstants.EASYTRAVEL + BaseConstants.WS + version + " Build Date: " + version.getOnlyDateString());
		}

		GridData versionLayoutData = new GridData(SWT.END, SWT.TOP, false, false);
		versionLabelET.setLayoutData(versionLayoutData);

		String agentPath = getAgentPath(EasyTravelConfig.read());
		versionLabelET.setToolTipText(agentPath);

		ConfigButton button = new ConfigButton(imageManager);
		Button configButton = button.createButton(topComposite);
		GridData configButtonLayoutData = new GridData(SWT.END, SWT.TOP, false, false);
		configButton.setLayoutData(configButtonLayoutData);
	}

	private void createLeftLinksArea(SelectionAdapter listener) {
		Composite linksComposite = new Composite(mainComp, SWT.NONE);
		GridLayout linksCompositeLayout = new GridLayout();
		linksCompositeLayout.numColumns = 4;
		linksCompositeLayout.marginWidth = 0;
		linksCompositeLayout.marginBottom = 0;
		linksCompositeLayout.marginRight = 10;
		linksCompositeLayout.verticalSpacing = 10;
		linksComposite.setLayout(linksCompositeLayout);

		GridData compLayoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
		linksComposite.setLayoutData(compLayoutData);

		createDtServerLink(linksComposite, listener);

		createVerticalLine(linksComposite);

		createVerticalText(linksComposite, "Customer Frontend:");

		createJavaArea(linksComposite, listener);
		createMobileNativeArea(linksComposite, listener);
		createMobileBrowserArea(linksComposite, listener);

		createVerticalLine(linksComposite);

		createDotNetArea(linksComposite, listener);
	}

	private void createVerticalText(Composite linksComposite, String text) {
		Label label = new Label(linksComposite, SWT.NONE);
		label.setText(text);
		GridData labelLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		labelLayoutData.horizontalIndent = 20;
		labelLayoutData.verticalIndent = 0;
		labelLayoutData.horizontalSpan = 4;
		label.setLayoutData(labelLayoutData);
	}

	protected void createVerticalLine(Composite linksComposite) {
		Label shadow_sep_h = new Label(linksComposite, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL);
		GridData labelLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		labelLayoutData.horizontalIndent = 20;
		labelLayoutData.verticalIndent = 0;
		labelLayoutData.horizontalSpan = 4;
		shadow_sep_h.setLayoutData(labelLayoutData);
	}

	private void createMobileBrowserArea(Composite linksComposite, SelectionAdapter listener) {
		mobileBrowserCheck = new Button(linksComposite, SWT.CHECK);
		mobileBrowserCheck.setToolTipText(MessageConstants.MOBILE_BROWSER_CHECKBOX_HINT_TEXT);
		mobileBrowserCheck.setSelection(true);
		mobileBrowserCheck.setEnabled(true);
		mobileBrowserCheck.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				final boolean enabled = mobileBrowserCheck.getSelection();
				final int sliderValue = trafficSlider.getSelection();
				ThreadEngine.createBackgroundThread("Mobile Browser activate/deactivate", new Runnable() {

					@Override
					public void run() {
						if (enabled) {
							BaseLoadManager.getInstance().getMobileBrowserLoadController().disableBlocking();
							BaseLoadManager.getInstance().setMobileBrowserBaseLoad(sliderValue);
						} else {
							BaseLoadManager.getInstance().stopMobileBrowserLoadAndBlock();
						}
					}

				}, linksComposite.getDisplay()).start();
				setTrafficSliderToolTip();
			}
		});
		mobileBrowserCheck.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_MOBILE_BROWSER_CHECK);

		Label label = new Label(linksComposite, SWT.NONE);
		label.setText(MessageConstants.EASYTRAVEL_LINK_MOBILE_BROWSER);

		mobileBrowserLink = new RAPLink(linksComposite, SWT.NONE, WEBLAUNCHER_URL_SIZE, 17);
		mobileBrowserLink.setFont(fontManager.createFont(+1, linksComposite.getDisplay()));
		mobileBrowserLink.addSelectionListener(listener);

		visitsMobileBrowserLabel = new Label(linksComposite, SWT.NONE);
		visitsMobileBrowserLabel.setForeground(grayColor);
		visitsMobileBrowserLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
	}

	private void createMobileNativeArea(Composite linksComposite, SelectionAdapter listener) {
		mobileNativeCheck = new Button(linksComposite, SWT.CHECK);
		mobileNativeCheck.setToolTipText(MessageConstants.MOBILE_NATIVE_CHECKBOX_HINT_TEXT);
		mobileNativeCheck.setSelection(true);
		mobileNativeCheck.setEnabled(true);
		mobileNativeCheck.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				final boolean enabled = mobileNativeCheck.getSelection();
				final int sliderValue = trafficSlider.getSelection();
				ThreadEngine.createBackgroundThread("Mobile Native activate/deactivate", new Runnable() {

					@Override
					public void run() {
						if (enabled) {
							BaseLoadManager.getInstance().getMobileNativeLoadController().disableBlocking();
							BaseLoadManager.getInstance().setMobileNativeBaseLoad(sliderValue);
						} else {
							BaseLoadManager.getInstance().stopMobileNativeLoadAndBlock();
						}
					}

				}, linksComposite.getDisplay()).start();
				setTrafficSliderToolTip();
			}
		});
		mobileNativeCheck.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_MOBILE_NATIVE_CHECK);

		Label label = new Label(linksComposite, SWT.NONE);
		label.setText(MessageConstants.EASYTRAVEL_LINK_MOBILE_NATIVE);

		mobileNativeLink = new RAPLink(linksComposite, SWT.NONE, WEBLAUNCHER_URL_SIZE, 17);
		mobileNativeLink.setFont(fontManager.createFont(+1, linksComposite.getDisplay()));
		mobileNativeLink.addSelectionListener(listener);

		visitsMobileNativeLabel = new Label(linksComposite, SWT.NONE);
		visitsMobileNativeLabel.setForeground(grayColor);
		visitsMobileNativeLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
	}

	private void createJavaArea(Composite linksComposite, SelectionAdapter listener) {
		customBrowserCheck = new Button(linksComposite, SWT.CHECK);
		customBrowserCheck.setToolTipText(MessageConstants.BROWSER_CHECKBOX_HINT_TEXT);
		customBrowserCheck.setSelection(true);
		customBrowserCheck.setEnabled(true);
		customBrowserCheck.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				final boolean enabled = customBrowserCheck.getSelection();
				final int sliderValue = trafficSlider.getSelection();
				ThreadEngine.createBackgroundThread("Custom Browser activate/deactivate", new Runnable() {

					@Override
					public void run() {
						if (enabled) {
							BaseLoadManager.getInstance().getCustomerLoadController().disableBlocking();
							BaseLoadManager.getInstance().setCustomerBaseLoad(sliderValue);
						} else {
							BaseLoadManager.getInstance().stopCustomerLoadAndBlock();
						}
					}

				}, linksComposite.getDisplay()).start();
				setTrafficSliderToolTip();
			}
		});
		customBrowserCheck.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_BROWSER_CHECK);

		Label label = new Label(linksComposite, SWT.NONE);
		label.setText(MessageConstants.EASYTRAVEL_LINK_BROWSER);

		javaLink = new RAPLink(linksComposite, SWT.NONE, WEBLAUNCHER_URL_SIZE, 17);
		javaLink.setFont(fontManager.createFont(+1, linksComposite.getDisplay()));
		javaLink.addSelectionListener(listener);

		visitsJavaLabel = new Label(linksComposite, SWT.NONE);
		visitsJavaLabel.setForeground(grayColor);
		visitsJavaLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
	}

	private void createDotNetArea(Composite linksComposite, SelectionAdapter listener) {
		dotNetCheck = new Button(linksComposite, SWT.CHECK);
		dotNetCheck.setToolTipText(MessageConstants.DOTNET_CHECKBOX_HINT_TEXT);
		dotNetCheck.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				final boolean enabled = dotNetCheck.getSelection();
				ThreadEngine.createBackgroundThread("DotNet activate/deactivate", new Runnable() {

					@Override
					public void run() {
						notifyUserChangedState(Technology.DOTNET_20, enabled);
					}

				}, linksComposite.getDisplay()).start();
				setTrafficSliderToolTip();
			}
		});
		Label label = new Label(linksComposite, SWT.NONE);
		label.setText(MessageConstants.EASYTRAVEL_LINK_DOTNET);
		dotNetCheck.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_DOT_NET_CHECK);


		dotNetLink = new RAPLink(linksComposite, SWT.NONE, WEBLAUNCHER_URL_SIZE, 17);
		dotNetLink.setFont(fontManager.createFont(+1, linksComposite.getDisplay()));
		dotNetLink.addSelectionListener(listener);

		visitsDotNetLabel = new Label(linksComposite, SWT.NONE);
		visitsDotNetLabel.setForeground(grayColor);
		visitsDotNetLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
	}

    private void createTrafficSliderArea(EasyTravelConfig CONFIG, SelectionAdapter listener, boolean taggedWebRequest,
    		boolean createNowEnabled, int sliderPos) {
		Group overallComposite = new Group(mainComp, SWT.NONE);
		GridLayout overallLayout = new GridLayout();
		overallLayout.numColumns = 1;
		overallLayout.marginWidth = 0;
		overallLayout.marginHeight = 0;
		overallLayout.marginTop = 0;
		overallLayout.verticalSpacing = 0;
		overallLayout.horizontalSpacing = 0;
		overallComposite.setLayout(overallLayout);
		GridData overallLayoutData = new GridData(SWT.END, SWT.FILL, false, true);
		overallComposite.setLayoutData(overallLayoutData);
		trafficLabel = overallComposite;
		trafficLabel.setText(MessageConstants.TRAFFIC);

		Composite trafficComposite = new Composite(overallComposite, SWT.NONE);
		GridLayout rcLayout = new GridLayout();
		rcLayout.numColumns = 2;
		rcLayout.marginWidth = 0;
		rcLayout.marginHeight = 0;
		rcLayout.marginTop = 0;
		rcLayout.verticalSpacing = 0;
		rcLayout.horizontalSpacing = 0;
		trafficComposite.setLayout(rcLayout);
		GridData compLayoutData = new GridData(SWT.END, SWT.TOP, false, true);
		trafficComposite.setLayoutData(compLayoutData);

		Composite labelComposite = new Composite(trafficComposite, SWT.NONE);
		GridLayout labelLayout = new GridLayout();
		labelLayout.numColumns = 1;
		rcLayout.marginWidth = 0;
		rcLayout.marginHeight = 0;
		rcLayout.marginTop = 0;
		rcLayout.verticalSpacing = 0;
		rcLayout.horizontalSpacing = 0;
		labelComposite.setLayout(labelLayout);

		GridData labelLayoutData = new GridData(SWT.END, SWT.CENTER, false, true);
		labelComposite.setLayoutData(labelLayoutData);

		createVisitsButton = new Button(labelComposite, SWT.RADIO);
		createVisitsButton.setText(MessageConstants.GENERATE_VISITS);
		createVisitsButton.setToolTipText(MessageConstants.GENERATE_VISITS_HINT);
		createVisitsButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, true));
		createVisitsButton.setSelection(!createNowEnabled);

		createNowButton = new Button(labelComposite, SWT.RADIO);
		createNowButton.setText(MessageConstants.CREATE_VISITS_IMMEDIATELY);
		createNowButton.setToolTipText(MessageConstants.CREATE_VISITS_IMMEDIATELY_HINT);
		createNowButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, true));
		createNowButton.setSelection(createNowEnabled);

		setCreateNowVisible(createNowEnabled);

		Composite sliderComposite = new Composite(trafficComposite, SWT.NONE);
		GridLayout slidLayout = new GridLayout();
		slidLayout.numColumns = 1;
		slidLayout.marginWidth = 0;
		slidLayout.marginHeight = 0;
		slidLayout.verticalSpacing = 0;
		slidLayout.horizontalSpacing = 0;
		sliderComposite.setLayout(slidLayout);
		sliderComposite.setLayoutData(new GridData(SWT.END, SWT.TOP, false, true));
		trafficSlider = new Scale(sliderComposite, SWT.NONE);
		// trafficSlider.setSize(175, 43);
		trafficSlider.setMinimum(0);
		trafficSlider.setMaximum(100);
		GridData data = new GridData(SWT.END, SWT.TOP, false, true);
		// necessary for Linux!!
		data.widthHint = 175;
		data.heightHint = 43;
		trafficSlider.setLayoutData(data);
		setTrafficSliderToolTip();
		trafficSlider.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setTrafficSliderToolTip();
				updateCreateNowButtons();
			}
		});
		trafficSlider.setSelection(sliderPos);
		trafficSlider.setData(MessageConstants.CUSTOM_WIDGET_ID, MessageConstants.ID_TRAFFIC_SLIDER);

		headerTaggingButton = new Button(sliderComposite, SWT.CHECK);
		headerTaggingButton.setText(MessageConstants.TAGGED_REQUESTS);
		headerTaggingButton.setToolTipText(MessageConstants.TAGGED_REQUESTS_HINT);
		headerTaggingButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, true));
		headerTaggingButton.setSelection(taggedWebRequest);
		if(DtVersionDetector.isAPM()) {//this button is only used by dynatrace
			headerTaggingButton.setVisible(false);
		}

		createScenarioLabel(overallComposite);

		createCustomVisitsButtons(overallComposite);
	}

    private void createScenarioLabel(Composite parent) {
    	CustomerTrafficScenarioEnum scenario = EasyTravelConfig.read().getCustomerTrafficScenario();
    	LOGGER.info("Using customer load scenario: " + scenario);
    	Composite labelComposite = new Composite(parent, SWT.NONE);
    	labelComposite.setLayout(new FillLayout());
    	usedScenarioLabel = new Label(labelComposite, SWT.NONE);
    	usedScenarioLabel.setText(TextUtils.merge(MessageConstants.SCENARIO_LABEL_TEXT, getCustomerLoadScenarioName(scenario)));
    	usedScenarioLabel.setVisible(scenario != CustomerTrafficScenarioEnum.EasyTravel);
    }

    private String getCustomerLoadScenarioName(CustomerTrafficScenarioEnum scenario) {
    	String name;
    	switch(scenario) {
    	case EasyTravelFixed:
    		name = MessageConstants.FIXED_LOAD_SCENARIO;
    		break;
    	case EasyTravelPredictable:
    		name = MessageConstants.PREDICTABLE_LOAD_SCENARIO;
    		break;
    	default:
    		name = MessageConstants.STANDARD_LOAD_SCENARIO;
    	}
    	return name;
    }

	private void createCustomVisitsButtons(Composite parent) {
		Composite createButtonComposite = new Composite(parent, SWT.NONE);
		GridLayout createButtonCompositeLayout = new GridLayout();
		createButtonCompositeLayout.numColumns = 2;
		createButtonCompositeLayout.marginWidth = 0;
		createButtonCompositeLayout.marginBottom = 0;
		createButtonCompositeLayout.verticalSpacing = 3;
		createButtonCompositeLayout.horizontalSpacing = 3;
		createButtonComposite.setLayout(createButtonCompositeLayout);
		GridData gridData = new GridData(SWT.CENTER, SWT.TOP, false, true);
		//gridData.heightHint = 85; //23*3 buttons + 2 * 2 inner margin
		createButtonComposite.setLayoutData(gridData);

		createBrowserVisitsButton = new Button(createButtonComposite, SWT.PUSH);
		createBrowserVisitsButton.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true));
		createBrowserVisitsButton.setVisible(false);
		createBrowserVisitsButton.setEnabled(false);
		createBrowserVisitsButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setTrafficSliderToolTip();
				updateCreateNowButtons();
			}
		});

		createB2bVisitsButton = new Button(createButtonComposite, SWT.PUSH);
		createB2bVisitsButton.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true));
		createB2bVisitsButton.setVisible(false);
		createB2bVisitsButton.setEnabled(false);
		createB2bVisitsButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setTrafficSliderToolTip();
				updateCreateNowButtons();
			}
		});

		createMobileBrowserVisitsButton = new Button(createButtonComposite, SWT.PUSH);
		createMobileBrowserVisitsButton.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true));
		createMobileBrowserVisitsButton.setVisible(false);
		createMobileBrowserVisitsButton.setEnabled(false);
		createMobileBrowserVisitsButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setTrafficSliderToolTip();
				updateCreateNowButtons();
			}
		});

		createMobileNativeVisitsButton = new Button(createButtonComposite, SWT.PUSH);
		createMobileNativeVisitsButton.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true));
		createMobileNativeVisitsButton.setVisible(false);
		createMobileNativeVisitsButton.setEnabled(false);
		createMobileNativeVisitsButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setTrafficSliderToolTip();
				updateCreateNowButtons();
			}
		});
	}

	private void createDtServerLink(Composite parentComposite, SelectionAdapter listener) {
		EasyTravelConfig CONFIG = EasyTravelConfig.read();
		String agentPath = getAgentPath(CONFIG);
		String dtVersion = DtVersionDetector.determineDTVersion(null);
		if (dtVersion == null) {
			dtVersion = BaseConstants.EMPTY_STRING;
		}

		Label emptylabel = new Label(parentComposite, SWT.NONE);
		emptylabel.setText(BaseConstants.EMPTY_STRING);

		RAPLink versionLabelDT = new RAPLink(parentComposite, SWT.NONE, 150, 17);
		String url = getDynatraceAppMonUrl();
		versionLabelDT.setText(
				TextUtils.merge(BaseConstants.LINK_HREF, url, TextUtils.merge("{0} {1}", DtVersionDetector.getServerLabel(), dtVersion)), true);
		versionLabelDT.setToolTipText(url);
		versionLabelDT.addSelectionListener(listener);


		Composite linkComposite = new Composite(parentComposite, SWT.NONE);
		GridLayout linkLayout = new GridLayout();
		linkLayout.numColumns = 3;
		linkLayout.marginWidth = 0;
		linkLayout.marginBottom = 0;
		linkLayout.verticalSpacing = 0;
		linkComposite.setLayout(linkLayout);
		GridData gridData = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		gridData.horizontalSpan = 2;
		linkComposite.setLayoutData(gridData);

		dcrumComposite = linkComposite;

		createSystemProfileLink(linkComposite, agentPath, dtVersion);
	}

	private String getDynatraceAppMonUrl() {

		if (DtVersionDetector.isAPM()) {
			return DynatraceUrlUtils.getDynatraceUrl();
		}

		return EasyTravelConfig.read().dtServerWebURL;
	}


	private void createSystemProfileLink(Composite parentComposite, String agentPath, String dtVersion) {
		if (!isSystemProfileLinkVisible()) {
			Label emptylabel = new Label(parentComposite, SWT.NONE);
			emptylabel.setText(BaseConstants.EMPTY_STRING);
			return;
		}

		RAPLink systemProfile = new RAPLink(parentComposite, SWT.NONE, 140, 17);

//        systemProfile.setFont(fontManager.createFont(+1));

		final File systemProfileFile = getSystemProfile(agentPath, dtVersion);
		String sysProfileURL = "/download?filename=" + encode(systemProfileFile.getPath());
		String sysProfileLink = TextUtils.merge(BaseConstants.LINK_HREF, sysProfileURL,
				MessageConstants.EASYTRAVEL_INSTALL_SYSTEMPROFILE);
		systemProfile.setText(sysProfileLink, true);
		// add a listener which installs the systemProfile in the Client
		systemProfile.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				installPlugin(systemProfileFile);
			}
		});
		systemProfile.setToolTipText(systemProfileFile.getPath());
	}

	// TODO: this is a bit ugly, we need a static method and thus cause coupling with the HeaderPanel directly
	// this should be done differently via interfaces/listeners...
	@Override
	public void setDebugInfo(final String text) {
		ThreadEngine.runInDisplayThread(new Runnable() {

			@Override
			public void run() {
				if(versionLabelET.isDisposed()) {
					return;
				}
				versionLabelET.setToolTipText(addAgentPath(text));
			}
		}, this.mainComp);
	}

	private String addAgentPath(String text) {
		String agentPath = getAgentPath(EasyTravelConfig.read());
		if(Strings.isNullOrEmpty(agentPath)) {
			return text;
		}
		return new StringBuilder().append(text).append("-").append(agentPath).toString();
	}

	private RAPLink createDCRumButton(Composite sysProfileComposite, final String name, final int size, final String dashboardName) {
		RAPLink dcrumButton = new RAPLink(sysProfileComposite, SWT.NONE, size, 17);

		dcrumButton.setText(TextUtils.merge(BaseConstants.LINK_HREF, "", name), true);
		// add a listener which installs the systemProfile in the Client
		dcrumButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				openClientDashboard(dashboardName);
			}
		});
		dcrumButton.setToolTipText("Opens the DC-RUM Dashboard via the dynaTrace Webstart Client");

		return dcrumButton;
	}

	private void setTrafficSliderToolTip() {
		StringBuilder sb = new StringBuilder();
		int value = trafficSlider.getSelection();
		String visit = MessageConstants.getAdaptedVisitString(value);

		// reload of the config is necessary to get the current values
		EasyTravelConfig config = EasyTravelConfig.read();
		if (manualVisitsCreation) {
			sb.append(value + 1).append(" ").append(visit);
			if (visitsMobileNativeLabel != null && !visitsMobileNativeLabel.isDisposed()) {
				visitsMobileNativeLabel.setText("");
			}
			if (visitsMobileBrowserLabel != null && !visitsMobileBrowserLabel.isDisposed()) {
				visitsMobileBrowserLabel.setText("");
			}
			visitsJavaLabel.setText("");
			visitsDotNetLabel.setText("");
		} else {
			if (customBrowserCheck.getSelection()) {
				// config the tooltip based on whether headless is being used
				if (config.baseLoadHeadlessCustomerRatio >0) {
					sb.append(MessageConstants.MODULE_CUSTOMER_FRONTEND).append(": ").append(
						(int) Math.ceil(value * config.baseLoadHeadlessCustomerRatio)).append(" ").append(visit).append("/min").append(
						'\n');
				} else {
					sb.append(MessageConstants.MODULE_CUSTOMER_FRONTEND).append(": ").append(
						(int) Math.ceil(value * config.baseLoadCustomerRatio)).append(" ").append(visit).append("/min").append(
						'\n');
				}
			} else {
				sb.append(MessageConstants.MODULE_CUSTOMER_FRONTEND).append(": 0 visits/min\n");
			}
			if (mobileNativeCheck.getSelection()) {
				sb.append(TextUtils.merge(MessageConstants.MOBILE_NATIVE_VISITS_SLIDERTOOLTIP,
						(int) Math.ceil(value * config.baseLoadMobileNativeRatio))).append("\n");
			} else {
				sb.append(TextUtils.merge(MessageConstants.MOBILE_NATIVE_VISITS_SLIDERTOOLTIP,
						"0")).append("\n");
			}
			if (mobileBrowserCheck.getSelection()) {
				sb.append(TextUtils.merge(MessageConstants.MOBILE_BROWSER_VISITS_SLIDERTOOLTIP,
						(int) Math.ceil(value * config.baseLoadMobileBrowserRatio))).append("\n");
			} else {
				sb.append(TextUtils.merge(MessageConstants.MOBILE_BROWSER_VISITS_SLIDERTOOLTIP,
						"0")).append("\n");
			}
			if(dotNetCheck.getSelection()) {
				sb.append(MessageConstants.MODULE_B2B_FRONTEND).append(": ").append((int) Math.ceil(value * config.baseLoadB2BRatio)).
						append(" ").append(visit).append("/min");
			}

			if (visitsMobileNativeLabel != null && !visitsMobileNativeLabel.isDisposed()) {
				if (mobileNativeCheck.getSelection()) {
					visitsMobileNativeLabel.setText(": ~" + (int) Math.ceil(value * config.baseLoadMobileNativeRatio) +
							" mobile app " +
							visit + "/min");
				} else {
					visitsMobileNativeLabel.setText(": 0 mobile app visits/min");
				}
			}
			if (visitsMobileBrowserLabel != null && !visitsMobileBrowserLabel.isDisposed()) {
				if (mobileBrowserCheck.getSelection()) {
					visitsMobileBrowserLabel.setText(": ~" + (int) Math.ceil(value * config.baseLoadMobileBrowserRatio) +
							" mobile web " +
							visit + "/min");
				} else {
					visitsMobileBrowserLabel.setText(": 0 mobile web visits/min");
				}
			}
			if (customBrowserCheck.getSelection()) {
				// If Headless is being used then base visit generation on baseLoadHeadlessRatio ratio
				// This is the text shown next to the Web check box
				if (config.baseLoadHeadlessCustomerRatio > 0 ) {
					visitsJavaLabel.setText(": ~" +
							(int) Math.ceil(value * config.baseLoadHeadlessCustomerRatio) + " browser web " + visit + "/min");
				} else {
					visitsJavaLabel.setText(": ~" +
						(int) Math.ceil(value * config.baseLoadCustomerRatio) + " web " + visit + "/min");
				}
			} else {
				visitsJavaLabel.setText(": 0 web visits/min");
			}
			if(dotNetCheck.getSelection()) {
				visitsDotNetLabel.setVisible(true);
				visitsDotNetLabel.setText(": ~" + (int) Math.ceil(value * config.baseLoadB2BRatio) +
						" " + visit + "/min");
			} else {
				visitsDotNetLabel.setVisible(false);
			}
			visitsDotNetLabel.getParent().layout(true, true);
		}
		trafficSlider.setToolTipText(sb.toString());
	}

	private void updateCreateNowButtons() {
		if (trafficSlider == null) {
			return;
		}
		ThreadEngine.runInDisplayThread(new Runnable() {

			@Override
			public void run() {
				if(trafficSlider.isDisposed()) {
					return;
				}
				int selected = trafficSlider.getSelection() + 1;
				String pluralS = MessageConstants.getAdaptedVisitString(selected);

				EasyTravelConfig config = EasyTravelConfig.read();
				if (config.baseLoadHeadlessCustomerRatio > 0) {
					// to signify we are generating headless visits the text says '1 Browser Web visit'
					createBrowserVisitsButton.setText(TextUtils.merge(MessageConstants.CREATE_BROWSER_WEB_VISITS_BUTTON , selected, pluralS));
				} else {
					// 'Browser' visits (not headless)
					createBrowserVisitsButton.setText(TextUtils.merge(MessageConstants.CREATE_CUSTOMER_VISITS_BUTTON, selected, pluralS));
				}
				createBrowserVisitsButton.setData(MessageConstants.VISITS_ID, selected);
				createBrowserVisitsButton.setEnabled(customerEnabled);

				createB2bVisitsButton.setText(TextUtils.merge(MessageConstants.CREATE_B2B_VISITS_BUTTON, selected, pluralS));
				createB2bVisitsButton.setData(MessageConstants.VISITS_ID, selected);
				createB2bVisitsButton.setEnabled(b2bEnabled);

				createMobileNativeVisitsButton.setText(TextUtils.merge(MessageConstants.CREATE_MOBILE_NATIVE_VISITS_BUTTON, selected, pluralS));
				createMobileNativeVisitsButton.setData(MessageConstants.VISITS_ID, selected);
				createMobileNativeVisitsButton.setEnabled(customerEnabled);

				createMobileBrowserVisitsButton.setText(TextUtils.merge(MessageConstants.CREATE_MOBILE_BROWSER_VISITS_BUTTON, selected, pluralS));
				createMobileBrowserVisitsButton.setData(MessageConstants.VISITS_ID, selected);
				createMobileBrowserVisitsButton.setEnabled(customerEnabled);

				createBrowserVisitsButton.getShell().layout(true, true);
			}

		}, this.mainComp);
	}

	private void openClientDashboard(String dashboard) {
		final EasyTravelConfig CONFIG = EasyTravelConfig.read();

		// TODO: full URL encoding
		String sUrl = null;
		if (DtVersionDetector.isAPM()) {
			sUrl = TextUtils.appendTrailingSlash("http://" + CONFIG.apmServerHost + ":" + CONFIG.apmServerPort + "/");
		} else {
			sUrl = TextUtils.appendTrailingSlash(CONFIG.dtClientWebURL) + TextUtils.merge(Constants.REST.MANAGEMENT_CLIENT_OPEN_DASHBOARD, dashboard.replace(" ",  "%20"));
		}

		// try to install the plugin via REST-activate-command, this is the fastest way to do it
		try {

			String ret = UrlUtils.retrieveData(sUrl, null, 10000);
			if (ret != null && ret.contains("<error>")) {
				LOGGER.info("Opening the Dashboard reported an error: " + sUrl + ": " + ret);

				Launcher.getLauncherUI(getShell().getDisplay()).messageBox(getShell(), SWT.OK | SWT.ICON_WARNING,
						MessageConstants.PLUGIN_REST_OPEN_DASHBOARD_TITLE,
						TextUtils.merge(MessageConstants.PLUGIN_REST_OPEN_DASHBOARD_ERROR, ret.replace("\n", "").replace("</", "").replace("<", "").replace(">", "-")),
						null);
			} else {
				LOGGER.info("Opening the Dashboard was successfull: " + sUrl);
			}
		} catch (IOException e) {
			LOGGER.info("Exception when trying to open a Dashboard in the dynaTrace Client via '" + sUrl + "': " + e.getMessage());

			Launcher.getLauncherUI(getShell().getDisplay()).messageBox(getShell(), SWT.OK | SWT.ICON_ERROR,
					MessageConstants.PLUGIN_REST_OPEN_DASHBOARD_TITLE,
					TextUtils.merge(MessageConstants.PLUGIN_REST_OPEN_DASHBOARD_ERROR, e.getMessage()),
					null);
		}

	}

	private void installPlugin(File systemProfile) {
		LOGGER.info("Installing System Profile");
		String systemProfilePath = systemProfile.getPath();

		// check that file exists
		if (!systemProfile.exists()) {
			Launcher.getLauncherUI(getShell().getDisplay()).messageBox(getShell(), SWT.ERROR | SWT.ICON_ERROR,
					MessageConstants.PLUGIN_NOT_FOUND_TITLE,
					TextUtils.merge(MessageConstants.PLUGIN_NOT_FOUND, systemProfilePath),
					null);
			return;
		}

		final EasyTravelConfig CONFIG = EasyTravelConfig.read();

		// try to install the plugin via REST-activate-command, this is the fastest way to do it
		try {
			String sUrl = TextUtils.appendTrailingSlash(CONFIG.dtClientWebURL) + Constants.REST.MANAGEMENT_CLIENT_ACTIVATE;
			sUrl += "?commandline=-installplugin&commandline=" + encode(systemProfilePath);
			String ret = UrlUtils.retrieveData(sUrl, null, 10000);
			if ("Ok".equals(ret)) {
				LOGGER.info("Started installation of plugin '" + systemProfilePath + "' via Client REST interfaces at '" +
						CONFIG.dtClientWebURL);

				Launcher.getLauncherUI(getShell().getDisplay()).messageBox(getShell(), SWT.OK | SWT.ICON_INFORMATION,
						MessageConstants.PLUGIN_REST_INSTALL_TITLE,
						MessageConstants.PLUGIN_REST_INSTALL,
						null);

				// we are done if it was successful
				return;
			} else {
				LOGGER.info("dynaTrace Client at '" + CONFIG.dtClientWebURL + "' returned '" + ret + "', full request was '" +
						sUrl + "', will try to install the system profile in a different way.");
			}
		} catch (IOException e) {
			LOGGER.info("dynaTrace Client did not respond at '" + CONFIG.dtClientWebURL + "': " + e.getMessage() +
					"', will try to install the system profile in a different way.");
		}

		// if REST does not work, we can rely on the file association on Windows
		if (OperatingSystem.isCurrent(OperatingSystem.WINDOWS)) {
			DocumentStarter starter = new DocumentStarter();
			starter.openURL(systemProfilePath);
		} else {
			// on other platforms, we copy the path to the clipboard and ask the user to install it manually
			// note that including these classes currently breaks our Agent when running in RAP, see JLT-72668 for details
			Clipboard clipboard = new Clipboard(mainComposite.getDisplay());
			clipboard.setContents(new Object[] { systemProfilePath },
					new Transfer[] { TextTransfer.getInstance() });

			Launcher.getLauncherUI(getShell().getDisplay()).messageBox(getShell(), SWT.OK | SWT.ICON_INFORMATION,
					MessageConstants.PLUGIN_MANUAL_INSTALL_TITLE,
					MessageConstants.PLUGIN_MANUAL_INSTALL, null);
		}
	}

	@Override
	protected boolean isDisposed() {
		boolean disposed = javaLink == null || javaLink.isDisposed() || dotNetLink == null || dotNetLink.isDisposed()
				|| mobileBrowserLink == null || mobileBrowserLink.isDisposed() || mobileNativeLink == null || mobileNativeLink.isDisposed();

		return disposed;
	}

	@Override
	protected void updateLinks(final String customer, final String b2b, final String mobile) {
		ThreadEngine.runInDisplayThread(new Runnable() {

			@Override
			public void run() {
				if (javaLink.isDisposed()) {
					return;
				}
				javaLink.setText(customer, false);

				if(mobileNativeLink != null && !mobileNativeLink.isDisposed()) {
					mobileNativeLink.setText(mobile, false);
				}

				if (mobileBrowserLink != null && !mobileBrowserLink.isDisposed()) {
					mobileBrowserLink.setText(customer, false);
				}

				dotNetLink.setText(b2b, false);

				javaLink.getParent().layout(true, true);
			}
		}, this.mainComp);

		updateCreateNowButtons();
	}


	public void setTrafficSliderEnabled(final boolean enabled) {
		trafficSlider.setEnabled(enabled);
	}

	public void addTrafficSliderListener(SelectionListener listener) {
		trafficSlider.addSelectionListener(listener);
	}

	public void setTrafficSliderValue(int value) {
		trafficSlider.setSelection(value);
		setTrafficSliderToolTip();
	}

	public void addWebRequestTaggingListener(SelectionListener listener) {
		headerTaggingButton.addSelectionListener(listener);
	}

	public void addCreateNowListener(SelectionListener listener) {
		createNowButton.addSelectionListener(listener);
	}

	public void addGenerateVisitsListener(SelectionListener listener) {
		createVisitsButton.addSelectionListener(listener);
	}

	@Override
	public void activateUEMLoadPanel() {
		ThreadEngine.runInDisplayThread(new Runnable() {

			@Override
			public void run() {
				configControllingAllowed = true;
				trafficSlider.setEnabled(true);
				headerTaggingButton.setEnabled(true);
				createNowButton.setEnabled(true);
				createVisitsButton.setEnabled(true);
				usedScenarioLabel.setEnabled(true);
			}
		}, this.mainComp);
	}

	@Override
	public void deactivateUEMLoadPanel() {
		ThreadEngine.runInDisplayThread(new Runnable() {

			@Override
			public void run() {
				configControllingAllowed = false;
				trafficSlider.setEnabled(false);
				headerTaggingButton.setEnabled(false);
				createNowButton.setSelection(false);
				createNowButton.setEnabled(false);
				createVisitsButton.setSelection(true);
				createVisitsButton.setEnabled(false);
				usedScenarioLabel.setEnabled(false);
			}
		}, this.mainComp);
	}

	@Override
	public void enableTaggedWebRequest() {
		ThreadEngine.runInDisplayThread(new Runnable() {

			@Override
			public void run() {
				headerTaggingButton.setSelection(true);
			}
		}, headerTaggingButton);
	}

	@Override
	public void disableTaggedWebRequest() {
		ThreadEngine.runInDisplayThread(new Runnable() {

			@Override
			public void run() {
				headerTaggingButton.setSelection(false);
			}
		}, headerTaggingButton);
	}

	@Override
	public void setLoad(final int value) {
		ThreadEngine.runInDisplayThread(new Runnable() {
			@Override
			public void run() {
				if (Launcher.isWidgetDisposed(mainComposite)) {
					return;
				}
				setTrafficSliderValue(value);
			}
		}, mainComposite);
	}

	@Override
	public void setCustomerTrafficScenario(final CustomerTrafficScenarioEnum scenario) {
		ThreadEngine.runInDisplayThread(new Runnable() {

			@Override
			public void run() {
				usedScenarioLabel.setText(TextUtils.merge(MessageConstants.SCENARIO_LABEL_TEXT, getCustomerLoadScenarioName(scenario)));
				usedScenarioLabel.setVisible(scenario != CustomerTrafficScenarioEnum.EasyTravel);
			}
		}, this.mainComp);
	}

	@Override
	public void setTrafficLabel(final String label) {
		ThreadEngine.runInDisplayThread(new Runnable() {

			@Override
			public void run() {
				trafficLabel.setText(label);
				mainComp.layout();
			}
		}, this.mainComp);

	}

	@Override
	public void resetTrafficLabel() {
		ThreadEngine.runInDisplayThread(new Runnable() {

			@Override
			public void run() {
				trafficLabel.setText(MessageConstants.TRAFFIC);
				mainComp.layout();
			}
		}, this.mainComp);

	}

	public void setCreateNowVisible(final boolean visible) {
		ThreadEngine.runInDisplayThread(new Runnable() {

			@Override
			public void run() {
				createB2bVisitsButton.setVisible(visible);
				createBrowserVisitsButton.setVisible(visible);
				createMobileNativeVisitsButton.setVisible(visible);
				createMobileBrowserVisitsButton.setVisible(visible);
				createNowButton.setSelection(visible);	// if called during WebLauncher refresh!
				createVisitsButton.setSelection(!visible);
				manualVisitsCreation = visible;
				Launcher.setManualMode(visible);
				if(visible) {
					updateCreateNowButtons();
				}
				setTrafficSliderToolTip();
			}
		}, this.mainComp);

	}

	@Override
	public synchronized void setDCRUMVisible(final boolean visible) {

		ThreadEngine.runInDisplayThread(new Runnable() {

			@Override
			public void run() {
				if(visible) {
					makeRapLinkVisible(dcrumButton);
					makeRapLinkVisible(dcrumErrorButton);
				} else if (!visible) {
					hideRapLink(dcrumButton);
					hideRapLink(dcrumErrorButton);
				}
			}
		}, this.mainComp);

	}

	private void makeRapLinkVisible(RAPLink raplink){
		if(!raplink.isDisposed()){
			raplink.setVisible(true);
		}
	}

	private void hideRapLink(RAPLink raplink){
		if(!raplink.isDisposed()){
			raplink.setVisible(false);
		}
	}

	public void addCreateCustomerVisitListener(SelectionAdapter listener) {
		createBrowserVisitsButton.addSelectionListener(listener);
	}

	public void addCreateB2bVisitListener(SelectionAdapter listener) {
		createB2bVisitsButton.addSelectionListener(listener);
	}

	public void addCreateMobileNativeVisitListener(SelectionAdapter listener) {
		createMobileNativeVisitsButton.addSelectionListener(listener);
	}

	public void addCreateMobileBrowserVisitListener(SelectionAdapter listener) {
		createMobileBrowserVisitsButton.addSelectionListener(listener);
	}

	@Override
	public void notifyTechnologyStateChanged(Technology technology, final boolean enabled, Collection<String> plugins,
			Collection<String> substitutes) {
		if (dotNetCheck == null) {
			return;
		}
		ThreadEngine.runInDisplayThread(new Runnable() {

			@Override
			public void run() {
				if (dotNetCheck.getSelection() == enabled) {
					return;
				}
				dotNetCheck.setSelection(enabled);
				setTrafficSliderToolTip();
			}
		}, this.mainComp);
	}

	private Shell getShell() {
		return mainComposite.getShell();
	}

	public Composite getMainComposite() {
		return mainComposite;
	}
}
