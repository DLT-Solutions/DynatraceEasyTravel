package com.dynatrace.diagnostics.uemload;

import static com.dynatrace.diagnostics.uemload.utils.UemLoadHttpUtils.createPair;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;

import com.dynatrace.diagnostics.uemload.DetectedFrameworks.Framework;
import com.dynatrace.diagnostics.uemload.NavigationTiming.NullNavigationTiming;
import com.dynatrace.diagnostics.uemload.http.base.HttpResponse;
import com.dynatrace.diagnostics.uemload.http.base.UemLoadHttpClient;
import com.dynatrace.diagnostics.uemload.http.callback.HttpResponseCallback;
import com.dynatrace.diagnostics.uemload.perceivedrendertime.PerceivedRenderTime;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravel;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.EtPageType;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.diagnostics.uemload.thirdpartycontent.ResourceRequestSummary;
import com.dynatrace.diagnostics.uemload.thirdpartycontent.ThirdPartyContentSummary;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.Version;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.Uem.DtCookieName;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;


public class JavaScriptAgent {
    private static final Logger LOGGER = Logger.getLogger(JavaScriptAgent.class.getName());

	public static final int JSAGENT_ACTION_ID_INFO_DTPC_VERSION = 60;
    public static final int DTVERSION_JSAGENT_NO_URLPARAM_IN_SIGNAL = 42; // JLT-52460 - remove parameter url and use referer
	public static final int DT_VERSION_62 = 62;
	public static final int DT_VERSION_63 = 63;
// instead
    public static final int JSAGENT_ACTION_INFO_SUPPORT_VERSION = 6552; // send with dtV param for older dynatrace servers that still rely on this

    // should support following injection examples of the dt-javascript agent
    // src="/dtagent_bij3_.js"
    // src="/dtagent_bij3_1244.js"
    // src="/dtagent_1244.js"
    // src="/dtagent_.js"
    private final static String DT_JAVASCRIPT_AGENT_REGEX = "([-a-zA-Z0-9+&@#%?=~_|!:,.;]*)([/]?)([a-zA-Z]+(\\d+)?(_\\w*)?_([-\\d]*).js)";
    private final static String DT_JAVASCRIPT_AGENT_REGEX_NEW = "([-a-zA-Z0-9+&@#%?=~_|!:,.;]*)([/]?)([a-zA-Z]+(_\\w*)?_([-\\d]*).js)";
    private final static String DT_JAVASCRIPT_AGENT_PARAMS_HOOK = "data-dtconfig=\"";
    private final static String DT_JAVASCRIPT_AGENT_PARAMS_HOOK_NEW = "window.dT_={cfg:\"";

    private final static String DT_JAVASCRIPT_API_FUNCTIONS = "rumapi.([a-zA-Z]*)\\((.*?)\\)";
    
    private final static String APPMON_AGENT = "dtagent";
    private final static String DYNATRACE_AGENT = "ruxitagent";

    private final UemLoadHttpClient http;
    private final String url;
    private final String title;
    private static String applicationVersion;

    private HashMap<String,String> dtConfig;
    private String pageId;
    private long loadStart;
    private int enabledFeaturesBitSet = 0;

    private ArrayList<String> UAMTagList;
    private Random UAMTagListRand;

    private int customActionId = -1;
    private int hierarchy = 1;
    private String customActionName;
    private long customActionStart;
    private String customActionType;
    private String customActionInfo;
    private String sourceAction;
    private List<String> xhrActions;
    private int currentActionId = 1;
    private int currentTestActionSeq = 1;
	private String xhrUrl = "";
	private String pageLoadReferer = null;
	private String agentVersion = null;

    private final Bandwidth bandwidth;
    private SignalMode signalMode;

    private static boolean shouldNotSendWithURL = false;
    private static boolean sendActionWithActionId = true;
    private static boolean sendActionWithActionInfo = true;
    private static boolean dtPC_newVersion = true;
    private static boolean dtPC_visitIDVersion = false;
    private static boolean disableNavTimingsFromConfig = true;

    private static String syntheticTestDefinitionId;
    private static List<Integer> syntheticTestVuControllerIds;

    public static boolean shouldSendActionWithActionId() {
    	return sendActionWithActionId;
    }

    public static boolean shouldSendActionWithActionInfo() {
    	return sendActionWithActionInfo;
    }

    public static boolean shouldGetnewdtPC() {
    	return dtPC_newVersion;
    }

	static {
		//If the dt server is old, we will send the beacon signal in the old format
		if (DtVersionDetector.isClassic()) {
			String version = DtVersionDetector.determineDTVersion(null);
			if (version != null) {
				try {
					String[] spl = version.split("\\.");
					String releaseVersionString = spl[0] + "" + spl[1];
					int releaseVersion = Integer.parseInt(releaseVersionString);
					if (releaseVersion < JSAGENT_ACTION_ID_INFO_DTPC_VERSION) {
						sendActionWithActionInfo = false;
						sendActionWithActionId = false;
						dtPC_newVersion = false;
					}

					if (releaseVersion >= DTVERSION_JSAGENT_NO_URLPARAM_IN_SIGNAL) {
						shouldNotSendWithURL = true;
					}

					if (releaseVersion < DT_VERSION_63) {
						disableNavTimingsFromConfig = false;
					}
				} catch (Exception e) {
					LOGGER.warning("Failed to parse releaseVersion, sending beacon signals with actionids");
				}
			}
		} else {
			dtPC_visitIDVersion = true; //Ruxit only. No backwards compability needed. The ruxit core will consider the visitid to be part of the sessionID, but it won't break anything

			syntheticTestDefinitionId = EasyTravelConfig.read().syntheticTestDefinitionId;
			syntheticTestVuControllerIds = parseVuControllerIds(EasyTravelConfig.read().syntheticTestVuControllerIds);

			if ((StringUtils.isBlank(syntheticTestDefinitionId) || syntheticTestVuControllerIds.isEmpty()) && LOGGER.isLoggable(Level.SEVERE)) {
    			LOGGER.severe("syntheticTestDefinitionId is not defined or the syntheticTestVuControllerIds are invalid in the easytravel config properties. Simulated ruxit synthetic data will be worthless and therefore ignored when sending UEM-Load traffic!\n"
    					+ "To simulate synthetic visits you have to manually create a synthetic app, afterwards the config property syntheticTestDefinitionId must be set to the app id of this application!");
    		}
		}
	}

    private List<List<String>> adkFunctions = null;

    private final LoadTimeWatcher loadTimeWatcher = new LoadTimeWatcher();

    JavaScriptAgent(UemLoadHttpClient http, String url, String title, Bandwidth bandwidth, String sourceAction,
            SignalMode signalMode, int enabledFeatureSet) {
        this(http, url, title, bandwidth, sourceAction, signalMode, enabledFeatureSet, null, Collections.<Cookie> emptyList(), null, null);
    }

	JavaScriptAgent(UemLoadHttpClient http, String url, String title, Bandwidth bandwidth, String sourceAction,
            SignalMode signalMode, int enabledFeatureSet, List<List<String>> adkFunctions, Collection<Cookie> cookies, 
            String pageLoadReferer, String agentVersion) {
        this.http = http;
        this.url = url;
        this.title = title;
        generateAppVersion(); // generates a random application version minor number 0-9;
        this.sourceAction = sourceAction;
        this.bandwidth = bandwidth;
        this.signalMode = signalMode;
        this.enabledFeaturesBitSet = enabledFeatureSet;
        this.adkFunctions = adkFunctions;
        this.dtConfig = new HashMap<String,String>();
        this.pageLoadReferer = pageLoadReferer;
        this.agentVersion = signalMode == SignalMode.Dynatrace ? "10203000000000000" : agentVersion;

        ArrayList<String> tagList = new ArrayList<String>();
        tagList.add("A");
        tagList.add("INPUT");

        this.UAMTagList = tagList;
        this.UAMTagListRand = new Random();

        extractSessionAndServerId(cookies);
    }

    private void extractSessionAndServerId(Collection<Cookie> cookies) {
		if (http != null && http.getServerID() == null && http.getVisitID() == null) {
			for (Cookie cookie: cookies) {
				if (cookie.getName().equals(DtCookieName.DT_COOKIE)) {
					final String dtCookieValue = cookie.getValue();
					if (dtCookieValue.startsWith("v_4_")) {//session state cookie v4
						setServerIdAndVisitIdFromSessionStateCookieV4(dtCookieValue);
					}
					else if (dtCookieValue.startsWith("=3=")) {//session state cookie v3	
						setServerIdAndVisitIdFromSessionStateCookieV3(dtCookieValue);
					}
					else if (dtCookieValue.contains("$")) {//session state cookie v1
						setServerIdAndVisitIdFromSessionStateCookieV1(dtCookieValue);
	    			}
	    			http.setSessionId(dtCookieValue);
	    		}
	    	}
    	}
    }

	private void setServerIdAndVisitIdFromSessionStateCookieV1(final String dtCookieValue) {
		String[] split = dtCookieValue.split("\\$");
		if (split.length == 2) {
			http.setServerID(split[0]);
			String sessionId = split[1];
			if (split[1].contains("|")) {
				String[] split2 = split[1].split("\\|");
				sessionId = split2[0];
			}
			http.setVisitID(calculateVisitId(sessionId));
		}
	}

	private void setServerIdAndVisitIdFromSessionStateCookieV4(final String dtCookieValue) {
		String[] keyValuePairs = dtCookieValue.substring(4).split("_");		
		setServerIdAndVisitIdFromSessionStateCookieInternal(keyValuePairs);
		
	}
	
	private void setServerIdAndVisitIdFromSessionStateCookieV3(final String dtCookieValue) {
		String[] keyValuePairs = dtCookieValue.substring(3).split("=");		
		setServerIdAndVisitIdFromSessionStateCookieInternal(keyValuePairs);
	}
	
	private void setServerIdAndVisitIdFromSessionStateCookieInternal(final String[] keyValuePairs) {
		for(int i=0; i+1<keyValuePairs.length; i=i+2) {
			String key = keyValuePairs[i];
			String value = keyValuePairs[i+1];
			if("srv".equalsIgnoreCase(key)) {
				http.setServerID(value);
			}
			else if("sn".equalsIgnoreCase(key)) {
				http.setVisitID(calculateVisitId(value));
			}
		}
	}	

    private String calculateVisitId(String sessionID) {
    	char charCode = 'A';
    	String timeAsString = "" + System.currentTimeMillis();

		char[] visitId = new char[sessionID.length()];

	    //Calculate the actual visitID
	    int timeAsStringLength = timeAsString.length();
	    for (int i = 0; i < sessionID.length(); i++) {
	        visitId[i] = (char)(charCode + Math.abs((sessionID.charAt(i) ^ timeAsString.charAt(i % timeAsStringLength)) % 26));
	    }
	    return new String(visitId);
	}

	public interface JavaScriptAgentCallback {

        public void run(JavaScriptAgent agent) throws IOException;
    }

	public static String getAppVersion() {
		return applicationVersion;
	}

    private static List<Integer> parseVuControllerIds(final String commaSeparatedList) {
    	try {
			final List<Integer> tmp = new ArrayList<>();
    		final String[] vuControllerIds = StringUtils.split(commaSeparatedList, ',');
			for(final String vuControllerId: vuControllerIds) {
				tmp.add(Integer.parseInt(vuControllerId.trim()));
			}
			return tmp;
		}
		catch(Exception e) {
			if(LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.severe("syntheticTestVuControllerIds invalid in the easytravel config properties, must be a comma-separated list of ints but is: " + commaSeparatedList);
			}
			return Collections.emptyList();
		}
	}

	private void generateAppVersion() {
		Version v = Version.read();
		applicationVersion = v.toRevision() + BaseConstants.DOT + (v.getBuildnumber() - UemLoadUtils.randomInt(100));
	}
	
	private static boolean isDynatraceAgent(String agentUrl) {
		return agentUrl.contains(DYNATRACE_AGENT);
	}
	
	private static boolean isAppMonAgent(String agentUrl) {
		return agentUrl.contains(APPMON_AGENT);
	}
	
	private static boolean isModeMatchedWithAgent(String agentUrl) {
		return (DtVersionDetector.isAPM() && isDynatraceAgent(agentUrl)) || (DtVersionDetector.isClassic() && isAppMonAgent(agentUrl));
	}

	public static void getJavaScriptAgent(final String html, final UemLoadHttpClient http, final String url,
			final String title, final Bandwidth bandwidth, final String sourceAction, final String pageLoadReferer,
			final JavaScriptAgentCallback callback) throws IOException {
		final String[] agentUrl = JavaScriptAgent.getJavaScriptAgentUrl(html, url);
		final List<List<String>> adkFunctions = JavaScriptAgent.getADKFunctions(html);
		if (EasyTravelConfig.read().disableJavaScriptAgent) {
			callback.run(new NullJavaScriptAgent(http, url, title, bandwidth, sourceAction));
		} else if (agentUrl == null) {	
			// fallback: no java script agent URL found, but the agent could
			// still be inline injected...
			final String[] inlineInjected = JavaScriptAgent.getInlineInjectedJavaScriptAgent(html);
			if (inlineInjected != null) {
				JavaScriptAgent agent = createJavaScriptAgent(http, url, title, bandwidth, sourceAction, inlineInjected,
						adkFunctions, http.getCookies(), pageLoadReferer);
				callback.run(agent);
			} else {
				callback.run(new NullJavaScriptAgent(http, url, title, bandwidth, sourceAction));
			}
		} else if (!isModeMatchedWithAgent(agentUrl[0])) {
			LOGGER.severe(TextUtils.merge(
					"EasyTravel mode didn''t match found JavaScript agent. Check configuration of config.apmServerDefault property. Mode: {0}, agent: {1}",
					DtVersionDetector.isAPM() ? "APM" : "Classic",
					isDynatraceAgent(agentUrl[0]) ? DYNATRACE_AGENT : APPMON_AGENT));
			callback.run(new NullJavaScriptAgent(http, url, title, bandwidth, sourceAction));
		} else {
			http.request(agentUrl[0], new HttpResponseCallback() {

				@Override
				public void readDone(HttpResponse response) throws IOException {
					if (response.getStatusCode() == 404) {
						callback.run(new NullJavaScriptAgent(http, url, title, bandwidth, sourceAction));
					} else {
						JavaScriptAgent agent = createJavaScriptAgent(http, url, title, bandwidth, sourceAction,
								agentUrl, adkFunctions, response.getCookies(), pageLoadReferer);
						callback.run(agent);
					}
				}
			});
		}
	}

    /**
	 * Convenience method for creating a JavaScriptAgent
	 * reads the agentUrl array (see {@link #getJavaScriptAgentUrl(String, String)} and
	 * {@link #getInlineInjectedJavaScriptAgent(String)})and sets SignalMode and enabled features accordingly
	 *
	 * @param http
	 * @param url
	 * @param title
	 * @param bandwidth
	 * @param sourceAction
	 * @param agentUrl
	 * @param adkFunctions
	 * @param cookies
	 * @return
	 */
    private static JavaScriptAgent createJavaScriptAgent(final UemLoadHttpClient http, final String url,
			final String title, final Bandwidth bandwidth, final String sourceAction,
			final String[] agentUrl, final List<List<String>> adkFunctions, final Collection<Cookie> cookies, final String pageLoadReferer) {	
		int enabledFeatures = JsAgentOptionsSet.parseFeatureHash(agentUrl);
		return new JavaScriptAgent(http, url, title, bandwidth, sourceAction, getSignalMode(agentUrl), enabledFeatures, adkFunctions, cookies, pageLoadReferer, agentUrl[2]);
	}
    
    private static SignalMode getSignalMode(String[] agentUrl) {
		if(isDynatraceAgent(agentUrl[0])) {
    		return SignalMode.Dynatrace;
    	} else if(isAppMonAgent(agentUrl[0])) {
    		if (agentUrl[2] != null && !agentUrl[2].isEmpty() && shouldNotSendWithURL) {
    		    return SignalMode.AppMon_v42_NoUrlParam;
    		} else {
    			return SignalMode.AppMon_v41;
    		}
    	} else {
    		return SignalMode.NoAgent;
    	}
    }

    public void pageLoadStarted(String html) throws IOException {
    	dtConfig = extractDTConfig(html);
        loadStart = System.currentTimeMillis();
        pageId = "G_" + new Random().nextLong();

        if (dtPC_visitIDVersion) {
        	http.setCookie(DtCookieName.DT_PAGE_COOKIE, http.getServerID() + "$"+pageId+"h1v" + http.getVisitID(), url);
        } else if(dtPC_newVersion){
        	http.setCookie(DtCookieName.DT_PAGE_COOKIE, pageId + "h1", url);
        }
        else{
        	if(sendActionWithActionId){
        		http.setCookie(DtCookieName.DT_PAGE_COOKIE, pageId + "#1", url);
        	}
        	else{
        		http.setCookie(DtCookieName.DT_PAGE_COOKIE, pageId + "#_load_", url);
        	}
        }

        loadTimeWatcher.startPageLoad(new PageLoad(this));
    }

	public void pageLoadFinished(List<ResourceRequestSummary> loadedResources, NavigationTiming nt, BrowserWindowSize bws,
			long viewDuration)
            throws IOException {
		pageLoadFinished(loadedResources, nt, bws, /* preview */false, viewDuration);
    }

    public void pageLoadFinished(List<ResourceRequestSummary> loadedResources, NavigationTiming nt, BrowserWindowSize bws,
			boolean preview, long viewDuration)
            throws IOException {

		PerceivedRenderTime prt;
		boolean useNavTimings = nt != null && nt != NavigationTiming.NONE;
    	long reportedLoadStart = Math.min(loadStart, useNavTimings ? nt.getNavigationStartTime() : Long.MAX_VALUE);
    	long loadEnd = Math.max(System.currentTimeMillis(), useNavTimings ? nt.getResponseEnd() : Long.MIN_VALUE);
    	long onloadStart = loadEnd;
    	long onloadEnd = loadEnd;

        if (nt != null) {
        	nt.createNavigationTimingDataForOnload();
        	onloadEnd = nt.getLoadEventEnd();
        }
		int latency = bandwidth.getLatency();

		if (DtVersionDetector.isDetectedVersionGreaterOrEqual(DT_VERSION_62)) {
			prt = PerceivedRenderTime.create(loadedResources, bws, reportedLoadStart, latency);
		} else {
			prt = PerceivedRenderTime.create(loadedResources, bws, loadStart);
		}
		
		final VisualCompleteTime visuallyComplete = VisualCompleteTime.create(prt);
		final SpeedIndex speedIndex = SpeedIndex.create(visuallyComplete);

        List<String> actions = new ArrayList<String>();
        if (sourceAction != null && !preview) {
        	//Source Actions are not sent with preview signals
            actions.add(sourceAction);
            sourceAction = null;
        }
        currentActionId = 1;

        if(pageLoadReferer != null) {
        	actions.add(createActionString(1,currentActionId++,"_load_","_load_",BaseConstants.MINUS,reportedLoadStart,onloadEnd,19, "", "", "", false, "", pageLoadReferer));
        }
        else {
        	actions.add(createActionString(1,currentActionId++,"_load_","_load_",BaseConstants.MINUS,reportedLoadStart,onloadEnd,19));
        }
        actions.add(createActionString(2,currentActionId++,"_onload_","_load_",BaseConstants.MINUS,onloadStart,onloadEnd,19));
        actions.add(createActionString(3,currentActionId++,"dt_appversion=" + applicationVersion,"_rs_",BaseConstants.MINUS,onloadStart + 1,onloadStart + 1,19));

        addADKGeneratedActions(actions, loadEnd + 1);
        
        if(xhrActions!=null) {
        	List<String> lst = new ArrayList<>();
        	int i = 4;
        	for(String act : xhrActions) {
        		lst.add(act.replace("<actionId>", String.valueOf(i)));
        		i++;
        	}
        	actions.addAll(lst);
        	xhrActions = null;
        }

        ThirdPartyContentSummary tpSummaryHelper = null;
        if (JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(enabledFeaturesBitSet)) {
            tpSummaryHelper = new ThirdPartyContentSummary();
            tpSummaryHelper.setPageUrl(url);
            tpSummaryHelper.setPageloadStart(reportedLoadStart);
            tpSummaryHelper.setPageloadFinished(loadEnd);
            tpSummaryHelper.setLoadedResources(loadedResources);
        }

        // determine if the url points to a front-end host
        boolean isFrontendHostUrl = isFrontendUrl(url);
        // determine if the url is a magento shop url
        boolean isMagentoShopURL = isMagentoShopUrl(url);

        DetectedFrameworks df = new DetectedFrameworks();
        df.addFramework(Framework.JQUERY, "1.8.1");
        if (isFrontendHostUrl) {
        	df.addFramework(Framework.PROTOTYPE, "1.7");
        	df.addFramework(Framework.ICEFACES, "x");
        }
        if (isMagentoShopURL) {
        	df.addFramework(Framework.PROTOTYPE, "1.7");
        }

        if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.JAVASCRIPT_FRAMEWORK_DETECTION)) {
        	if (PluginChangeMonitor.isPluginEnabled("JavascriptFrameworkDetectionUpdate")) {
        		df.addFramework(Framework.ANGULARJS, "1.4.4");
        	} else {
        		df.addFramework(Framework.ANGULARJS, "1.2.28");
        	}
        }

		if (isFrontendHostUrl) {
			LOGGER.finest("[JavaScriptIncreasedErrorCount] reognized a front-end url, candidate for JavaScript errors <"
					+ url + ">");
		} else {
			LOGGER.finest("[JavaScriptIncreasedErrorCount] a NON-front-end url: no JavaScript errors will be generated <"
					+ url + ">");
		}

		/**
		 * Always generate a JavaScriptError for IE 10 on special offers page
		 */
		if (isFrontendHostUrl && url.contains(EtPageType.SPECIAL_OFFERS.getPath()) && http.getBrowserType().equals(BrowserType.IE_10)) {
			JavaScriptErrorAction errorAction_RENAME = JavaScriptErrorActionHelper.generateSpecificJavascriptAction(url,
					http.getBrowserType(), 3);
			if (errorAction_RENAME != null) {
				String action = errorAction_RENAME.toString(currentActionId++);
				currentActionId += JavaScriptErrorActionHelper.getJavaScriptErrorChildActionCount(action);
				LOGGER.fine("[JavaScriptIncreasedErrorCount] Adding JavaScriptError with action: " + action + " into page load.");
				actions.add(action);
			}
		}

        // Generate some JavaScript errors that appear randomly during page load
        // By default 1 of 25 page load actions will have a javascript error,
        // however if the JavaScriptIncreasedErrorCount problem pattern is enabled
        // this will increase to 1 out of 10 page load actions.
        boolean addJavaScriptErrorAction = JavaScriptErrorActionHelper.shouldJavascriptErrorActionBeAdded(
        		PluginChangeMonitor.isPluginEnabled("JavascriptIncreasedErrorCount"));
        if (isFrontendHostUrl && addJavaScriptErrorAction) {
        	JavaScriptErrorAction errorAction = JavaScriptErrorActionHelper.generateRandomJavascriptAction(url, http.getBrowserType(), 3);

        	if (errorAction != null) {
        		String action = errorAction.toString(currentActionId++);
        		currentActionId += JavaScriptErrorActionHelper.getJavaScriptErrorChildActionCount(action);
        		LOGGER.fine("[JavaScriptIncreasedErrorCount] Adding JavaScriptError with action: " + action + " into page load.");
        		actions.add(action);
        	}
        }

        if(isFrontendHostUrl && PluginChangeMonitor.isPluginEnabled("JavascriptChangeDetectionWithError")) {

        	String action = JavaScriptErrorActionHelper.generateChangeDetectionJavaScriptErrorAction(url, http.getBrowserType(), 3).toString(currentActionId++);
           	LOGGER.fine("[JavaScriptChangeDetection] Adding JavaScriptError with action: " + action + " into page load.");
        	actions.add(action);
        }

        // Add Streaming media actions
        if (PluginChangeMonitor.isPluginEnabled("StreamingMediaTraffic")
                && JsAgentOptionsSet.isStreamingMediaEnabled(enabledFeaturesBitSet))
        {
            boolean addStreamingMediaAction = StreamingMediaActionHelper.shouldStreamingMediaActionBeAdded();
            if (addStreamingMediaAction) {
                String action = StreamingMediaActionHelper.generateRandomMediaAction(url).toString(currentActionId++);
                actions.add(action);
            }
        }

		http.setCookie(DtCookieName.DT_LATENCY_COOKIE, Integer.toString(latency), url);
        removeCurrentPageLoad(preview);
		sendXhr(actions, preview, tpSummaryHelper, nt, bws, prt, df, viewDuration, getUserTimings(reportedLoadStart, loadEnd), visuallyComplete, speedIndex);
    }

	private String getUserTimings(long reportedLoadStart, long loadEnd) {
		long totalLoadTime = loadEnd - reportedLoadStart;
        String userTimings = null;
        if(url.contains(EtPageType.SPECIAL_OFFERS.getPath())){
			userTimings = "mark_special_offers_loaded," + totalLoadTime * .75 + ",0.00";
        }else if(url.contains(EtPageType.START.getPath())){
        	userTimings = String.format("mark_recommendations_loaded,%d.00,0.00", (int)(totalLoadTime * .9));
        	if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.THIRD_PARTY_CONTENT)) {
				int randomLoadStart = UemLoadUtils.randomInt(1, (int)Math.max(2, (totalLoadTime * .5)));
	        	int randomLoadEnd = UemLoadUtils.randomInt(randomLoadStart, (int)Math.max(randomLoadStart + 1, totalLoadTime));
        		userTimings += String.format(";3rdpartyloadstart,%d.00,0.00;3rdpartyloadend,%d.00,0.00;measure_social_bar_loadtime,%d.00,%d.00;", 
        			randomLoadStart, randomLoadEnd, randomLoadStart, randomLoadEnd - randomLoadStart);
            }
        }
		return userTimings;
	}

	static boolean isMagentoShopUrl(String testUrl) {
		boolean isMagentoShopURL = false;
		for (String magentoUrl : EtPageType.EtMagentoShopUrls) {
			if (testUrl.contains(magentoUrl)) {
				isMagentoShopURL = true;
				break;
			}
		}
		return isMagentoShopURL;
	}

	static boolean isFrontendUrl(String testUrl) {
		boolean isFrontendHostUrl = false;
		for (String frontEndUrl : EtPageType.EtCustomerPartialUrls) {
			if (testUrl.contains(frontEndUrl)) {
				isFrontendHostUrl = true;
				break;
			}
		}
		return isFrontendHostUrl;
	}

    public void subPageLoadFinished(List<ResourceRequestSummary> loadedResources, NavigationTiming nt, BrowserWindowSize bws,
			boolean preview, String subPageUrl, long viewDuration)
            throws IOException {

		PerceivedRenderTime prt;
		boolean useNavTimings = nt != null && nt != NavigationTiming.NONE;
		long reportedLoadStart = Math.min(loadStart, useNavTimings ? nt.getNavigationStartTime() : Long.MAX_VALUE);
        long loadEnd = Math.max(System.currentTimeMillis(), useNavTimings ? nt.getResponseEnd() : Long.MIN_VALUE);
        long onloadStart = loadEnd;
        long onloadEnd = loadEnd;
        if (useNavTimings) {
        	nt.createNavigationTimingDataForOnload();
        	onloadEnd = nt.getLoadEventEnd();
        }
		int latency = bandwidth.getLatency();

		if (DtVersionDetector.isDetectedVersionGreaterOrEqual(DT_VERSION_62)) {
			prt = PerceivedRenderTime.create(loadedResources, bws, reportedLoadStart, latency);
		} else {
			prt = PerceivedRenderTime.create(loadedResources, bws, loadStart);
		}

		final VisualCompleteTime visuallyComplete = VisualCompleteTime.create(prt);
		final SpeedIndex speedIndex = SpeedIndex.create(visuallyComplete);

        List<String> actions = new ArrayList<String>();
        currentActionId = 1;
        actions.add(createActionString(1,currentActionId++,"_load_","_load_",BaseConstants.MINUS,reportedLoadStart,loadEnd,19));
        actions.add(createActionString(2,currentActionId++,"_onload_","_load_",BaseConstants.MINUS,onloadStart,onloadEnd,19));

		http.setCookie(DtCookieName.DT_LATENCY_COOKIE, Integer.toString(latency), subPageUrl);
        removeCurrentPageLoad(preview);

        sendXhr(actions, preview, null, nt, bws, prt, null, viewDuration, getUserTimings(reportedLoadStart, loadEnd), visuallyComplete, speedIndex);
    }

    private void addADKGeneratedActions(List<String> actions, long time) {
    	if (adkFunctions != null) {
            for (List<String> adkFunction: adkFunctions) {
                String functionName = adkFunction.get(0);
                if ("reportString".equals(functionName)) {
                	String key = adkFunction.get(1);
                	String value = adkFunction.get(2);
                	if (key != null && value != null) {
                		key = key.replace("\"", "").replace("'", "");
                		value = value.replace("\"", "").replace("'", "");
                		actions.add(createActionString(3,currentActionId++,key + "=" + value,"_rs_",BaseConstants.MINUS,time,time,19));
                	}
                } else if ("reportValue".equals(functionName)) {
                	String key = adkFunction.get(1);
                	String value = adkFunction.get(2);
                	if (key != null && value != null) {
                		key = key.replace("\"", "").replace("'", "");
                		value = value.replace("\"", "").replace("'", "");
                		actions.add(createActionString(3,currentActionId++,key + "=" + value,"_rv_",BaseConstants.MINUS,time,time,19));
                	}
                } else if ("tagVisit".equals(functionName)) {
                	String key = "dt_visittag";
                	if (DtVersionDetector.isAPM()) {
                		key = "rx_visittag";
                	}
                	String value = adkFunction.get(1);
                	if (key != null && value != null) {
                		value = value.replace("\"", "").replace("'", "");
                    	if (DtVersionDetector.isAPM()) {
                    		//For ruxit, the tag has to be a root action. To prevent errors with the depth of the actions, we add it as the first action
                    		actions.add(0, createActionString(1,currentActionId++,key + "=" + value,"_rs_",BaseConstants.MINUS,time,time,19));
                    	} else {
                    		actions.add(createActionString(3,currentActionId++,key + "=" + value,"_rs_",BaseConstants.MINUS,time,time,19));
                    	}
                	}
            	} else if ("enterAction".equals(functionName)) {
            		String actionName = adkFunction.get(1);
            		actionName = actionName.replace("\"", "").replace("'", "");
            		if (DtVersionDetector.isAPM()) {
            			actions.add(0, createActionString(1,currentActionId++,actionName,"",BaseConstants.MINUS,time,time,19,"","","",true,"",""));
            		}
            	}
            }
        }
    }

    /**
     * sends a third party resources beacon signal
     * @param loadedResources
     * @param nt
     * @param xhrActionId
     * @throws IOException
     */
	public void sendThirdPartyResourcesForXhr(List<ResourceRequestSummary> loadedResources, NavigationTiming nt, int xhrActionId,
			long viewDuration) throws IOException {
         if (JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(enabledFeaturesBitSet) && xhrActionId >= 0) {

        	 long reportedLoadStart = Math.min(loadStart, nt != null ? nt.getNavigationStartTime() : Long.MAX_VALUE);
             long loadEnd = Math.max(System.currentTimeMillis(), nt != null ? nt.getResponseEnd() : Long.MIN_VALUE);

             ThirdPartyContentSummary tpSummaryHelper = new ThirdPartyContentSummary();
             tpSummaryHelper.setXhrActionId(xhrActionId);
             tpSummaryHelper.setPageUrl(url);
             tpSummaryHelper.setPageloadStart(reportedLoadStart);
             tpSummaryHelper.setPageloadFinished(loadEnd);
             tpSummaryHelper.setLoadedResources(loadedResources);

			sendXhr(ImmutableList.<String> of(), false, tpSummaryHelper, nt, null, null, null, viewDuration, null, null, null);
         }
    }


	private void sendXhr(Collection<String> actions, boolean preview, ThirdPartyContentSummary tpSummaryHelper,
            NavigationTiming nt,
            BrowserWindowSize bws,
            PerceivedRenderTime prt,
			DetectedFrameworks df, long viewDuration, String userTimings,
			VisualCompleteTime vt,
			SpeedIndex sI) throws IOException {

        List<NameValuePair> xhrParams = createXHR(
                url,
                preview,
                title,
                pageId,
                pageId,
                getRequestId(),
                getResponseId(),
                bandwidth.toString(),
                actions,
                // only pass summaryHelper if third party content detection is enabled
                JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(enabledFeaturesBitSet) ? tpSummaryHelper : null,
                nt,
                bws,
                prt,
                df,
				http.getVisitorId(),
				viewDuration,
				userTimings,
				vt,
				sI);
        
        if(xhrParams == null) {
        	//currently there's only one case in which this can happen: ruxit synthetic data was generated but the synthetic app id is unknown (see config properties!)
        	//no further logging is necessary, the problem is already logged in createXHR()
        	return;
        }

        // onyl set referer if signalMode for referer is desired
        String referer = null;
        if (signalMode == SignalMode.AppMon_v42_NoUrlParam || signalMode == SignalMode.Dynatrace) {
            referer = url;
        }

        if (EasyTravel.isUemCorrelationTestingMode) {
            LOGGER.info("javaAgent Signal " +
                    URLDecoder.decode(URLEncodedUtils.format(xhrParams, BaseConstants.UTF8), BaseConstants.UTF8));
        }

        StringBuilder query = new StringBuilder();
		query.append(UemLoadUrlUtils.getUrl(url, getReportUrl())); // no xhrParams because send it in postbody
		extractSessionAndServerId(http.getCookies()); 
		//since 6.5 beacon protocol 2 is supported. for details see @link(SECRET)
		//        requiredOnAgent = [typeParamKey, sessionName, serverIdParamKey, flavorParamKey, refererParamKey, visitIdParamKey];
		// optional: app
		if(dtConfig.get("bp2") != null){
			query.append("?type=js");
			if(http.getSessionId() != null){
				query.append("&dtCookie=");
				query.append(URLEncoder.encode(http.getSessionId(), "UTF-8"));
			}
			if(http.getServerID() != null){
				query.append("&svrid=");
				query.append(http.getServerID());
			}
			query.append("&referer=");
			query.append(URLEncoder.encode(referer, "UTF-8"));
			if(dtConfig.get("app") != null && !dtConfig.get("app").equals("#APP#")){
				query.append("&app=");
				query.append(dtConfig.get("app"));
			}
			if(http.getVisitID() != null){
				query.append("&visitId=");
				query.append(http.getVisitID());
			}
		}
		
        UemLoadHttpClient httpClient = preview
				? new UemLoadHttpClient(Bandwidth.UNLIMITED, BrowserType.NONE, this.http.getCookies(), this.http.getUserAgent()) : this.http;

		httpClient.post(query.toString(), referer, HttpResponseCallback.NONE, null, xhrParams);

        if (preview) {
            httpClient.close();
        }

        httpClient = null;
    }

	private String getRequestId() {
      return dtConfig.get("rid");
	}

	private String getResponseId() {
		return dtConfig.get("rpid");
	}

	private String getReportUrl() {
		return dtConfig.get("reportUrl");
	}

	private boolean isUAMEnabled() {
		String s = dtConfig.get("uam");
		return (s != null && s.equals("true"));
	}

    public static HashMap<String, String> extractDTConfig(String html) {
    	HashMap<String, String> params = new HashMap<String, String>();
    	String needle = DT_JAVASCRIPT_AGENT_PARAMS_HOOK_NEW;
    	String notInjectedErr = "JavaScript has not been injected! UEM enabled? Verify that User Experience sensor is placed and injection is configured correctly.";

    	if (html.indexOf(needle) == -1) { // try old format
			needle = DT_JAVASCRIPT_AGENT_PARAMS_HOOK;
		}
    	
    	boolean isAmpPage = isAmpPage(html);

    	int pos = html.indexOf(needle);
    	if (pos < 0 && !isAmpPage) {
    		LOGGER.warning(notInjectedErr);
            return params;
    	}

    	pos += needle.length();

    	int posEnd = html.indexOf('"', pos);
    	if (posEnd < 0 && !isAmpPage) {
    		LOGGER.warning(notInjectedErr);
            return params;
    	}

    	String p = html.substring(pos, posEnd);
    	String[] pairs = p.split("\\|");

    	// generate parameter map
		for (String pair : pairs) {
    		String[] kv = pair.split("=");
    		if (kv.length == 2) {
    			params.put(kv[0], kv[1]);
    		}
    	}
    	return params;
    }
    
	private static boolean isAmpPage(String html) {
		Scanner scanner = new Scanner(html);
		try {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (Pattern.matches("\\s*<html.*\\s*>\\s*", line)) {
					return Pattern.matches("\\s*<html.*amp.*\\s*>\\s*", line);
				}
			}
		} finally {
			scanner.close();
		}
		return false;
	}

    
    /**
     * parse html source to check which dynaTrace javascript agent (version) is injected.
     *
     * @param htmlSourceCode html sourcecode of page to parse
     * @param host
     * @return string[] containing 0 ... full url of javascript agent,
     *         1 ... featurehash (may be null),
     *         2 ... agentVersion (may be null),
     *         3 ... agentParaemters (may be null)
     *
     * @throws MalformedURLException
     */
    static String[] getJavaScriptAgentUrl(String htmlSourceCode, String host) throws MalformedURLException {

        Pattern javaScriptAgentPattern = Pattern.compile(DT_JAVASCRIPT_AGENT_REGEX_NEW);
        Matcher javaScriptAgentMatcher = javaScriptAgentPattern.matcher(htmlSourceCode);

        if (javaScriptAgentMatcher.find() && !htmlSourceCode.contains(DT_JAVASCRIPT_AGENT_PARAMS_HOOK)) { // new dyntrace format (+ async capable JS agent
    		String javaScriptPath = javaScriptAgentMatcher.group();
    		while (javaScriptPath.startsWith(BaseConstants.FSLASH)) {
                javaScriptPath = javaScriptPath.substring(1);
            }
    		String url = UemLoadUrlUtils.getUrl(host, javaScriptPath);
    		String featurehash = javaScriptAgentMatcher.group(4);
    		String agentVersion = javaScriptAgentMatcher.group(5);

    		String buildNumber = agentVersion.isEmpty() || isDynatraceAgent(javaScriptPath) ? agentVersion : agentVersion.substring(agentVersion.length()-4);

    		  int prefixPos = htmlSourceCode.indexOf(DT_JAVASCRIPT_AGENT_PARAMS_HOOK_NEW);
    		  if (prefixPos < 0) {
                  return new String[] { url, featurehash, buildNumber };
              }
    		  int endOfAttribute = htmlSourceCode.indexOf('"', prefixPos + DT_JAVASCRIPT_AGENT_PARAMS_HOOK_NEW.length());
              String parameters = htmlSourceCode.substring(prefixPos + DT_JAVASCRIPT_AGENT_PARAMS_HOOK_NEW.length(), endOfAttribute);
              return new String[] { url, featurehash, buildNumber, parameters };
        } else { // try new version format
        	javaScriptAgentPattern = Pattern.compile(DT_JAVASCRIPT_AGENT_REGEX);
        	javaScriptAgentMatcher = javaScriptAgentPattern.matcher(htmlSourceCode);

        	if (javaScriptAgentMatcher.find()) {
	        	String javaScriptPath = javaScriptAgentMatcher.group();
	            while (javaScriptPath.startsWith(BaseConstants.FSLASH)) {
	                javaScriptPath = javaScriptPath.substring(1);
	            }
	            String url = UemLoadUrlUtils.getUrl(host, javaScriptPath);
	            String featurehash = javaScriptAgentMatcher.group(5);
	            String buildNumber = javaScriptAgentMatcher.group(6);

	            int prefixPos = htmlSourceCode.indexOf(DT_JAVASCRIPT_AGENT_PARAMS_HOOK);
	            if (prefixPos < 0) {
	                return new String[] { url, featurehash, buildNumber };
	            }
	            int endOfAttribute = htmlSourceCode.indexOf('"', prefixPos + DT_JAVASCRIPT_AGENT_PARAMS_HOOK.length());
	            String parameters = htmlSourceCode.substring(prefixPos + DT_JAVASCRIPT_AGENT_PARAMS_HOOK.length(), endOfAttribute);
	            return new String[] { url, featurehash, buildNumber, parameters };
        	}
        }
        return null;
    }

    /**
     * parses html source and checks if the javascript agent is inline injected. Returns an array that is similar to the return value of {@link #getJavaScriptAgentUrl(String, String)}
     * @param htmlSourceCode
     * @return null (not inline injected) or string[] containing 0 ... static dummy value, there is no url if the agent is inline injected,
     *         1 ... featurehash (not null),
     *         2 ... agentVersion (not null),
     *         3 ... agentParaemters (may be null)
     */
    static String[] getInlineInjectedJavaScriptAgent(String htmlSourceCode) {
		final Map<String, String> dtConfig = extractDTConfig(htmlSourceCode);
        if(dtConfig != null && !dtConfig.isEmpty()) {
        	final String featureHash = dtConfig.get("featureHash");
        	final String agentVersion = dtConfig.get("dtVersion");
        	final StringBuilder parameterBuilder = new StringBuilder();
        	for(Entry<String, String> entry: dtConfig.entrySet()) {
        		if(!entry.getKey().equals("featureHash") && !entry.getKey().equals("dtVersion")) {
        			if(parameterBuilder.length() > 0) {
        				parameterBuilder.append('|');
        			}
        			parameterBuilder.append(entry.getKey())
        				.append("=")
        				.append(entry.getValue());
        		}
        	}
        	if(featureHash != null && agentVersion != null) {
        		if(parameterBuilder.length() > 0) {
            		return new String[] { "no url", featureHash, agentVersion, parameterBuilder.toString() };
				}
            	return new String[] { "no url", featureHash, agentVersion };
			}
        }
        return null;
    }

    static List<List<String>> getADKFunctions(String htmlSourceCode) {
        if (htmlSourceCode.contains("rumapi.reportString")
                || htmlSourceCode.contains("rumapi.reportValue")
                || htmlSourceCode.contains("rumapi.tagVisit")
                || htmlSourceCode.contains("rumapi.enterAction"))
        {
            List<List<String>> result = new ArrayList<List<String>>();

            Pattern adkFunctionPatterns = Pattern.compile(DT_JAVASCRIPT_API_FUNCTIONS);
            Matcher adkFunctionMatcher = adkFunctionPatterns.matcher(htmlSourceCode);

            int index = 0;
            while (adkFunctionMatcher.find(index)) {
                index = adkFunctionMatcher.end();
                if (adkFunctionMatcher.groupCount() >= 2) {
                    String functionName = adkFunctionMatcher.group(1);
                    String parametersString = adkFunctionMatcher.group(2).trim();
                    String[] parameters = parametersString.split(",");
                    for (int i = 0; i < parameters.length; i++) {
                        parameters[i] = parameters[i].trim();
                    }

                    List<String> functionDetails = new ArrayList<String>();
                    functionDetails.add(functionName);
                    for (String parameter: parameters) {
                    	functionDetails.add(parameter);
                    }
                    result.add(functionDetails);
                }
            }
            return result;
        }
        return null;
    }

	private boolean canMaskActionType(String actionType) {
		return (actionType.equals("C") ||		// click
				actionType.equals("CC") ||	// click
				actionType.equals("U") ||	// click
				actionType.equals("D") ||	// click
				actionType.equals("S") ||	// scroll
				actionType.equals("TS") ||	// touch
				actionType.equals("TE") ||	// touch
				actionType.equals("B") ||	// blur
				actionType.equals("H") ||	// change
				actionType.equals("A"));	// autocomplete
	}

    private String maskAction(String action) {
    	if (!isUAMEnabled()) {
    		return action;
    	}

    	// Get the action type to check if we are allowed to mask it.
    	// Usually the action type is the 4th entry in the list.
    	boolean canMask = false;
    	String[] p = action.split("\\|");

    	if (p.length >= 4) {
    		canMask = canMaskActionType(p[3]);
    	}

    	// The JS script has the advantage that it knows the node name,
    	// unfortunately we don't, so we choose a random element tag.
    	if (canMask) {
    		int i = UAMTagListRand.nextInt(UAMTagList.size());
    		String tag = UAMTagList.get(i);

    		p[2] = "dTMasked_" + tag;

    		return Joiner.on(BaseConstants.PIPE).join(p);
    	}

    	return action;
    }


    private List<NameValuePair> createXHR(String url, boolean preview, String title, String pageId, String frameId,
            String requestId,
            String responseId,
            String bw,
            Collection<String> actions, ThirdPartyContentSummary tpSummaryHelper, NavigationTiming nt, BrowserWindowSize bws,
			PerceivedRenderTime prt, DetectedFrameworks df, VisitorId visitorId, long viewDuration, String userTimings, VisualCompleteTime vt, SpeedIndex sI) {
    	
        List<NameValuePair> query = new ArrayList<NameValuePair>();
		boolean load = false;
		boolean endVisit = false;
		
		ArrayList<String> maskedActions = new ArrayList<String>();

		for (String action : actions) {
			if (action.contains("|_load_|")) {
				load = true;
			} else if (action.contains("|_endVisit_|")) {
				endVisit = true;
			}
			maskedActions.add(maskAction(action));
		}

        if (signalMode == SignalMode.AppMon_v41 || signalMode == SignalMode.Dynatrace) {
            query.add(createPair("url", url));
        }

    	if (DtVersionDetector.isAPM()) {
    		query.add(createPair("svrid", http.getServerID()));
    	}
        if (preview) {
            query.add(createPair("PV", 1));
        }

        query.add(createPair("title", title));

        if (bws != null && load) {
            query.add(createPair("w", bws.getWidth()));
            query.add(createPair("h", bws.getHeight()));
            
            if (DtVersionDetector.isAPM()) {
            	query.add(createPair("sw", bws.getScreenWidth()));
                query.add(createPair("sh", bws.getScreenHeight()));
            }
        }

        if (JsAgentOptionsSet.isPerceivedRenderTimeEnabled(enabledFeaturesBitSet) && prt != null && prt.getValue() > 0 && load) {
			if (DtVersionDetector.isDetectedVersionGreaterOrEqual(DT_VERSION_62)) {
				String slowestImageUrl = prt.getSlowestImageUrl();
				if (slowestImageUrl.length() > 0) {
					query.add(createPair("p", Joiner.on(BaseConstants.PIPE).join(prt.getValue(), slowestImageUrl)));
				} else {
					query.add(createPair("p", prt.getValue()));
				}
			} else {
				query.add(createPair("p", prt.getValue()));
			}
        }

        if (JsAgentOptionsSet.isVisualCompleteTimeEnabled(enabledFeaturesBitSet) && vt != null && !vt.getValue().isEmpty() && load) {
			query.add(createPair("V", vt.getValue()));
			if (JsAgentOptionsSet.isSpeedIndexEnabled(enabledFeaturesBitSet) && sI != null && sI.getTime() != 0) {
				query.add(createPair("S", sI.getTime()));
			}
        }

        //APM-22095 since dt 6.0 bandwidth is always allowed and sent
        if (JsAgentOptionsSet.isBandwidthEnabled(enabledFeaturesBitSet) && bw != null) {
			query.add(createPair("bw", bw));
		}
        query.add(createPair("pId", pageId));
        query.add(createPair("fId", frameId));
        query.add(createPair("pFId", BaseConstants.EMPTY_STRING));
        query.add(createPair("rId", requestId));
        query.add(createPair("rpId", responseId));
        query.add(createPair("a", Joiner.on(BaseConstants.COMMA).join(maskedActions)));
        query.add(createPair("dtV", JSAGENT_ACTION_INFO_SUPPORT_VERSION));
        query.add(createPair("time", System.currentTimeMillis()));
        query.add(createPair("vd", viewDuration));

        // add framework detection information
        if (JsAgentOptionsSet.isFrameworkDetectionEnabled(enabledFeaturesBitSet) && df != null && load && !df.isEmpty()) {
        	query.add(createPair("fd", df.getQueryValue()));
        }
        
        // add usertiming information
        if(JsAgentOptionsSet.isUserTimingsEnabled(enabledFeaturesBitSet) && userTimings != null){
        	query.add(createPair("ut", userTimings));
        }

        // check for third party content summary
		if (tpSummaryHelper != null) {
			if (load || tpSummaryHelper.getXhrActionId() >= 0) {
				String thirdPartySummarySignal = tpSummaryHelper.createJavaAgentSignal(JsAgentOptionsSet.isThirdPartyIncludeOwnResources(enabledFeaturesBitSet));
				if (!StringUtils.isEmpty(thirdPartySummarySignal)) {
					query.add(createPair("3p", thirdPartySummarySignal));
					if (nt != null && sendResourceTimings()) {
						String resourceTimingSignal = tpSummaryHelper.createResourceTimingsSignal(url, nt, http.getBrowserType().isRuxitSynthetic(), tpSummaryHelper.getXhrActionId());
						query.add(createPair("rt", resourceTimingSignal));
					}
				}
			}
		}
		
        if (JsAgentOptionsSet.isNavigationTimingEnabled(enabledFeaturesBitSet) && nt != null) {
        	if (load) {
	            String navigationTimingSignal = nt.createSignal();
	            if (!navigationTimingSignal.isEmpty()) {
	                query.add(createPair("nt", navigationTimingSignal));
	            }
        	}
        }
        // determine DOM ready time; use current time, if nt is unavailable
        long domR = System.currentTimeMillis();
        if (nt != null && nt.getResponseEnd() > 0) {
            domR = Math.min(domR, nt.getResponseEnd());
        }

        query.add(createPair("domR", domR));

        if (DtVersionDetector.isAPM()) {
	        if (visitorId.isNewVisitor()) {
	        	query.add(createPair("nV", "1"));
	        }
	        query.add(createPair("vID", visitorId.getVisitorId()));
        }

        if (http.getBrowserType().isRuxitSynthetic()) {
        	if (StringUtils.isBlank(syntheticTestDefinitionId) || syntheticTestVuControllerIds.isEmpty()) {
        		return null;
        	}
			query.add(createPair("testDefId", syntheticTestDefinitionId));
			query.add(createPair("vucId", syntheticTestVuControllerIds.get(UemLoadUtils.randomInt(syntheticTestVuControllerIds.size()))));
        	if (!endVisit) {
	        	String relativePath = "";
	    		try {
					URL urlObj = new URL(url);
					relativePath = urlObj.getPath();
				} catch (MalformedURLException e) {
				}
	    		if (relativePath.isEmpty()) {
	    			relativePath = "/";
	    		}
	    		if (load) {
	    			query.add(createPair("testActionName", maskAction("Loading of page " + relativePath)));
	    		} else {
	    			query.add(createPair("testActionName", maskAction("action on page " + relativePath)));
	    		}
	        	query.add(createPair("testActionSeq", currentTestActionSeq++));	        	
        	} else {
	        	query.add(createPair("errorCode", 0));
	        	query.add(createPair("errorDetail", ""));
	        	query.add(createPair("ignore",0));
        	}
        }
        if (agentVersion != null) {
        	query.add(createPair("v", agentVersion));
        }
        return query;
    }

	private boolean sendResourceTimings() {
		String rt = dtConfig.get("rt");

		if (rt == null) {
			return true;
		}
		int rtValue = Integer.valueOf(rt);
		if (rtValue == 0) {
			return false;
		} else if (rtValue == 10000) {
			return true;
		}
		long samplingNumber = calculateVisitIdNumberForSampling();
		return (samplingNumber % 10000) < rtValue;
	}

	private long calculateVisitIdNumberForSampling() {
		String visitId = http.getVisitID();
		int visitIdNumber = 0;
		int charCode1, charCode2;
		for (int i = 0; i + 1 < visitId.length(); i += 2) {
			charCode1 = visitId.charAt(i);
			charCode2 = visitId.charAt(i + 1);
			visitIdNumber += charCode1 + charCode2;
		}
		// Calculate "fake" random number by abusing math.sin. This is not a truly uniform distribution
		// since the digits of Math.sin are not truly uniformely distributed but it comes close
		// enough for our purposes. As seed we use a number we calculate from the visit id.
		// See http://stackoverflow.com/questions/521295/javascript-random-seeds for a discussion of
		// this function and it's up and downsides. I picked it because it is

		return (long) Math.floor(Math.abs(Math.sin(visitIdNumber) * 10000000));
	}

	public void startCustomAction(String customActionName, String customActionType, String customActionInfo) {
    	this.customActionId = currentActionId;
        this.customActionName = customActionName;
        this.customActionType = customActionType;
        this.customActionInfo = customActionInfo;
        this.customActionStart = System.currentTimeMillis();

        if (dtPC_visitIDVersion) {
        	http.setCookie(DtCookieName.DT_PAGE_COOKIE, http.getServerID() + "$"+pageId + BaseConstants.delimiter_h + currentActionId + "v" + http.getVisitID(), url);
        } else if(dtPC_newVersion){
        	http.setCookie(DtCookieName.DT_PAGE_COOKIE,
                    pageId + BaseConstants.delimiter_h + currentActionId, url);
        }
        else{
        	if(sendActionWithActionId){
        		http.setCookie(DtCookieName.DT_PAGE_COOKIE,
                        pageId + BaseConstants.HASH + currentActionId, url);
        	}
        	else{
        		http.setCookie(DtCookieName.DT_PAGE_COOKIE,
                        pageId + BaseConstants.HASH + customActionName, url);
        	}

        }
        currentActionId++; //not incremented earlier, because the cookie would be wrong otherwise

		loadTimeWatcher.startCustomAction(new CustomAction(this));
    }

	public void startCustomAction(String customActionName, String customActionType, String customActionInfo, String xhrUrl) {
		startCustomAction(customActionName, customActionType, customActionInfo, xhrUrl, 1);
	}

	public void startCustomAction(String customActionName, String customActionType, String customActionInfo, String xhrUrl, int hierarchy) {
		if (DtVersionDetector.isDetectedVersionGreaterOrEqual(DT_VERSION_62) || DtVersionDetector.isAPM()) {
			this.xhrUrl = xhrUrl;
		}
		this.hierarchy = hierarchy;
		startCustomAction(customActionName, customActionType, customActionInfo);
	}

	/**
	 * stops a custom action, returns the custom action id
	 * @param isIncomplete
	 * @param loadedResources
	 * @param nt
	 * @param bws
	 * @return
	 * @throws IOException
	 */
    public int stopCustomAction(boolean isIncomplete, List<ResourceRequestSummary> loadedResources, NavigationTiming nt,
			BrowserWindowSize bws, long viewDuration) throws IOException {
		return stopCustomAction(isIncomplete, loadedResources, nt, bws, /* preview */false, viewDuration);
    }

    /**
     * stops a custom action, returns the custom action id
     * @param isIncomplete
     * @param loadedResources
     * @param nt
     * @param bws
     * @param preview
     * @return
     * @throws IOException
     */
    int stopCustomAction(boolean isIncomplete, List<ResourceRequestSummary> loadedResources, NavigationTiming nt,
			BrowserWindowSize bws, boolean preview, long viewDuration) throws IOException {
		return stopCustomAction(isIncomplete, false, loadedResources, nt, bws, preview, null, viewDuration);
    }
    
    
    int stopCustomAction(boolean isIncomplete, boolean xhrInOnLoad, List<ResourceRequestSummary> loadedResources, NavigationTiming nt,
			BrowserWindowSize bws, boolean preview, Collection<String> additionalActions, long viewDuration) throws IOException {

		PerceivedRenderTime prt;
		long reportedActionStart = Math.min(customActionStart,
				(nt != null && !(nt instanceof NullNavigationTiming)) ? nt.getNavigationStartTime() : Long.MAX_VALUE);
		long currentTime = System.currentTimeMillis();
		long customActionEnd = isIncomplete ? 0 : currentTime;
		if (nt != null) {
			nt.setLoadEventEnd(currentTime);
		}

		if (DtVersionDetector.isDetectedVersionGreaterOrEqual(DT_VERSION_62)) {
			// No latency information available
			prt = PerceivedRenderTime.create(loadedResources, bws, reportedActionStart);

		} else {
			prt = PerceivedRenderTime.create(loadedResources, bws, loadStart);
		}

		final VisualCompleteTime vt = VisualCompleteTime.create(prt);
		final SpeedIndex speedIndex = SpeedIndex.create(vt);

        if (customActionName != null && customActionType != null && customActionId != -1 && customActionInfo != null) {
            ThirdPartyContentSummary tpSummaryHelper = null;
            if (JsAgentOptionsSet.isThirdPartyContentDetectionEnabled(enabledFeaturesBitSet)) {
                tpSummaryHelper = new ThirdPartyContentSummary();
                tpSummaryHelper.setPageUrl(url);
                tpSummaryHelper.setPageloadStart(reportedActionStart);
                tpSummaryHelper.setPageloadFinished(customActionEnd);
                tpSummaryHelper.setLoadedResources(loadedResources);
            }

			String action;
			if (xhrUrl.length() > 0) {
				action = Joiner.on(BaseConstants.PIPE).join(hierarchy, 
						xhrInOnLoad?"<actionId>":customActionId, 
						actionEscape(customActionName), 
						actionEscape(customActionType), 
						actionEscape(customActionInfo), 
						reportedActionStart, 
						customActionEnd, 
						19,
						"",
						"", 
						xhrUrl,
						"-",
						tpSummaryHelper != null ? tpSummaryHelper.buildResourceTimingValueForAction(nt) : "" );
			} else {
				action = Joiner.on(BaseConstants.PIPE).join(hierarchy, xhrInOnLoad?"<actionId>":customActionId, actionEscape(customActionName), 
						actionEscape(customActionType), actionEscape(customActionInfo), reportedActionStart, customActionEnd, 19);
			}

            List<String> actions = new ArrayList<String>();
            actions.add(action);
            if (additionalActions != null) {
	            for (String additionalAction: additionalActions) {
	            	additionalAction = additionalAction.replaceAll("<starttime>", Long.toString(customActionStart));
	            	additionalAction = additionalAction.replaceAll("<endtime>", Long.toString(customActionEnd));
	            	if(additionalAction.contains("<actionId>")) {
	            		String actionId = Integer.toString(xhrInOnLoad?-1:customActionId++);
	            		additionalAction = additionalAction.replace("<actionId>", actionId);
	            	}
            		//For ruxit, the tag has to be a root action. To prevent errors with the depth of the actions, we add it as the first action
	            	if (additionalAction.contains("rx_visittag")) {
	            		actions.add(0, additionalAction);
	            	} else {
	            		actions.add(additionalAction);
	            	}
	            }
            }

            if(xhrInOnLoad) {
            	xhrActions = actions;
            } else {
            	sendXhr(actions, preview, tpSummaryHelper, nt, bws, prt, new DetectedFrameworks(), viewDuration, null, vt, speedIndex);
            }


            if (isIncomplete && !preview) {
            	sourceAction = createSourceActionString(
                		customActionId,
                		actionEscape(customActionName),
                		actionEscape(customActionType),
                		actionEscape(customActionInfo),
                		pageId,
                		currentTime);
            }
        }

        removeCurrentAction(preview);
		// Reset the xhr url information only if it is not a preview action
		if (!preview) {
			this.xhrUrl = "";
		}

		return this.customActionId;
    }

    void sendJavaScriptErrors(Collection<JavaScriptErrorAction> actions) throws IOException {
    	List<String> actionsAsString = new ArrayList<String>();

    	for (JavaScriptErrorAction action: actions) {
    		String actionAsString = action.toString(currentActionId++);
    		// count up for each child node that was added
    		currentActionId += JavaScriptErrorActionHelper.getJavaScriptErrorChildActionCount(actionAsString);
    		actionsAsString.add(actionAsString);
    	}
		sendXhr(actionsAsString, false, null, null, null, null, new DetectedFrameworks(), 0, null, null, null);
    }

	/**
	 *
	 * @param hierarchy Action hierarchy value. Top most action has value 1.
	 * @param actionId The id of the action. These are generated in order of creation.
	 * @param actionName The name of the action. Either the name derived from an element such as a button with the text "Start",
	 *        or actions such as 'load' or 'error'
	 * @param type The type of action, for example 'click', 'load', 'error', 'warn', 'log'. Some types are shortened to one or two
	 *        letters (e.g. "click" is "C")
	 * @param info Framework info if coming from an XHR module (e.g. "j1.9.1" for jQuery 1.9.1)
	 * @param startTime start time of action in milliseconds (UTC)
	 * @param endTime end time of action in milliseconds (UTC) or "0" if it's a preview
	 * @param domNodeCount Number of DOM nodes on page.
	 * @return
	 */
	private String createActionString(int hierarchy, int actionId, String actionName,
			String type, String info, long startTime, long endTime, int domNodeCount) {
		if (sendActionWithActionInfo) {
			return Joiner.on(BaseConstants.PIPE).join(hierarchy, actionId, actionName, type, info, startTime, endTime,
					domNodeCount);
		}
		else if (sendActionWithActionId) {
			return Joiner.on(BaseConstants.PIPE).join(hierarchy, actionId, actionName, type, startTime, endTime, domNodeCount);
		} else {
			return Joiner.on(BaseConstants.PIPE).join(hierarchy, actionName, BaseConstants.MINUS, type, startTime, endTime,
					domNodeCount);
		}
	}

	private String createActionString(int hierarchy, int actionId, String actionName,
			String type, String info, long startTime, long endTime, int domNodeCount, 
			String actionLinksToMeString, String webRequestIdString, String xhrURL, 
			boolean customAction, String resourceTimings, String referer) {
		
		if (resourceTimings == null || resourceTimings.isEmpty()) {
			resourceTimings = "-";
		}
    	if (sendActionWithActionInfo) {
			return Joiner.on(BaseConstants.PIPE).join(hierarchy, actionId, actionName, type, info, startTime, endTime,
					domNodeCount, actionLinksToMeString, webRequestIdString, xhrURL, customAction ? "1" : "0", resourceTimings, referer);
		}
		else if (sendActionWithActionId) {
			return Joiner.on(BaseConstants.PIPE).join(hierarchy, actionId, actionName, type, startTime, endTime, domNodeCount,
					actionLinksToMeString, webRequestIdString, xhrURL, customAction ? "1" : "0", resourceTimings, referer);
		} else {
			return Joiner.on(BaseConstants.PIPE).join(hierarchy, actionName, BaseConstants.MINUS, type, startTime, endTime, domNodeCount,
					actionLinksToMeString, webRequestIdString, xhrURL, customAction ? "1" : "0", resourceTimings, referer);
		}

	}

    private String createSourceActionString(int actionId,
			String actionName, String type, String info,
			String pageId, long currentTime) {

    	if (sendActionWithActionInfo) {
    		return Joiner.on(BaseConstants.PIPE).join("s",actionId,actionName,type,info,pageId,currentTime);
		}
		else if (sendActionWithActionId) {
			return Joiner.on(BaseConstants.PIPE).join("s",actionId,actionName,info,pageId,currentTime);
		} else {
			return Joiner.on(BaseConstants.PIPE).join("s",actionName,type,pageId,currentTime);
		}
	}

	public static String actionEscape(String str) {
        return str.replace("^", "^^").replace(BaseConstants.PIPE, "^p").replace(BaseConstants.COMMA, "^c");
    }

    private void removeCurrentAction(boolean preview) {
        if (!preview) {
            loadTimeWatcher.stopCustomAction();
        }
    }

    private void removeCurrentPageLoad(boolean preview) {
        if (!preview) {
            loadTimeWatcher.stopPageLoad();
        }
    }

    public String getSourceAction() {
        return sourceAction;
    }

    String getCustomActionName() {
        return customActionName;
    }

    int getCustomActionId() {
        return customActionId;
    }

    long getCustomActionStart() {
        return customActionStart;
    }

    String getUrl() {
        return url;
    }

    String getPageId() {
        return pageId;
    }

    long getLoadStart() {
        return loadStart;
    }

    String getCustomActionType() {
        return customActionType;
    }

    String getCustomActionInfo() {
        return customActionInfo;
    }

    private static enum SignalMode {
        AppMon_v41, AppMon_v42_NoUrlParam, Dynatrace, NoAgent
    }


    static class NullJavaScriptAgent extends JavaScriptAgent {

        public NullJavaScriptAgent(UemLoadHttpClient http, String url, String title, Bandwidth bandwidth, String sourceAction) {
            super(http, url, title, bandwidth, sourceAction, SignalMode.AppMon_v41, 0);
        }

        @Override
        public void pageLoadStarted(String html) throws IOException {
        	// Null implementation of JavaScriptAgent
        }

        @Override
		public void pageLoadFinished(List<ResourceRequestSummary> loadedResources, NavigationTiming nt, BrowserWindowSize bws,
				long viewDuration)
                throws IOException {
        	// Null implementation of JavaScriptAgent
        }

        @Override
		public void startCustomAction(String customActionName, String customActionType, String customActionInfo) {
        	// Null implementation of JavaScriptAgent
        }

        @Override
        public int stopCustomAction(boolean isIncomplete, List<ResourceRequestSummary> loadedResources, NavigationTiming nt,
				BrowserWindowSize bws, long viewDuration) throws IOException {
        	return -1;
        }

        @Override
        public String getSourceAction() {
            return BaseConstants.EMPTY_STRING;
        }

		@Override
		public void pageLoadFinished(List<ResourceRequestSummary> loadedResources, NavigationTiming nt,
				BrowserWindowSize bws, boolean preview, long viewDuration) throws IOException { //NOSONAR
		}

		@Override
		public void subPageLoadFinished(List<ResourceRequestSummary> loadedResources, NavigationTiming nt,
				BrowserWindowSize bws, boolean preview, String subPageUrl, long viewDuration) throws IOException { //NOSONAR
		}

		@Override
		public void sendThirdPartyResourcesForXhr(List<ResourceRequestSummary> loadedResources, NavigationTiming nt,
				int xhrActionId, long viewDuration) throws IOException {//NOSONAR
		}

		@Override
		public void startCustomAction(String customActionName, String customActionType, String customActionInfo,
				String xhrUrl) {//NOSONAR
		}

		@Override
		public void startCustomAction(String customActionName, String customActionType, String customActionInfo,
				String xhrUrl, int hierarchy) {//NOSONAR
		}

		@Override
		public void sendSyntheticEndVisit() throws IOException {//NOSONAR
		}
               
    }


    /* protected */static class JsAgentOptionsSet {
    	
    	private JsAgentOptionsSet() {
    		throw new IllegalStateException("Utility class");
		}

        private static final int
            FLAG_THIRDPARTY_CONTENT_DETECTION  = 1 << 0,
            FLAG_NAVIGATIONTIMING              = 1 << 1,
            FLAG_PERCEIVEDRENDERTIME           = 1 << 2,
            FLAG_STREAMING_MEDIA               = 1 << 3,
            FLAG_FRAMEWORK_DETECTION           = 1 << 4,
        	FLAG_THIRDPARTY_OWNRESOURCES       = 1 << 5,
        	FLAG_BANDWIDTH					   = 1 << 6,
        	FLAG_VISUAL_COMPLETE_TIME		   = 1 << 7,
        	FLAG_SPEEDINDEX		  			   = 1 << 8;
        private static final int FLAG_USERTIMINGS = 1 << 8;
        	

        static int setThirdPartyContentDetection(int flags, boolean value) {
            if (value)
                flags |= FLAG_THIRDPARTY_CONTENT_DETECTION;
            else
                flags &= ~FLAG_THIRDPARTY_CONTENT_DETECTION;
            return flags;
        }

        static int setNavigationTiming(int flags, boolean value) {
            if (value)
                flags |= FLAG_NAVIGATIONTIMING;
            else
                flags &= ~FLAG_NAVIGATIONTIMING;
            return flags;
        }

        static int setPerceivedRenderTime(int flags, boolean value) {
            if (value) {
                flags |= FLAG_PERCEIVEDRENDERTIME;
            } else {
                flags &= FLAG_PERCEIVEDRENDERTIME;
            }
            return flags;
        }

        static int setVisualCompleteTime(int flags, boolean value) {
            if (value) {
                flags |= FLAG_VISUAL_COMPLETE_TIME;
            } else {
                flags &= FLAG_VISUAL_COMPLETE_TIME;
            }
            return flags;
        }
        
        static int setSpeedIndex(int flags, boolean value) {
            if (value) {
                flags |= FLAG_SPEEDINDEX;
            } else {
                flags &= FLAG_SPEEDINDEX;
            }
            return flags;
        }

        static int setStreamingMedia(int flags, boolean value) {
            if (value) {
                flags |= FLAG_STREAMING_MEDIA;
            } else {
                flags &= FLAG_STREAMING_MEDIA;
            }
            return flags;
        }

        static int setFrameworkDetection(int flags, boolean value) {
            if (value) {
                flags |= FLAG_FRAMEWORK_DETECTION;
            } else {
                flags &= FLAG_FRAMEWORK_DETECTION;
            }
            return flags;
        }

        static int setThirdPartyIncludeOwnResources(int flags, boolean value) {
        	if (value) {
        		flags |= FLAG_THIRDPARTY_OWNRESOURCES;
        	} else {
        		flags &= FLAG_THIRDPARTY_OWNRESOURCES;
        	}
        	return flags;
        }

        static int setBandwidthEnabled(int flags, boolean value) {
        	if (value) {
        		flags |= FLAG_BANDWIDTH;
        	} else {
        		flags &= FLAG_BANDWIDTH;
        	}
        	return flags;
        }
        
        static int setUserTimingsEnabled(int flags, boolean value){
        	if(value){
        		flags |= FLAG_USERTIMINGS;
        	}else{
        		flags &= FLAG_USERTIMINGS;
        	}
        	return flags;
        }

        static boolean isThirdPartyContentDetectionEnabled(int flags) {
            return (flags & FLAG_THIRDPARTY_CONTENT_DETECTION) > 0;
        }

        static boolean isNavigationTimingEnabled(int flags) {
            return (flags & FLAG_NAVIGATIONTIMING) > 0;
        }

        static boolean isPerceivedRenderTimeEnabled(int flags) {
            return (flags & FLAG_PERCEIVEDRENDERTIME) > 0;
        }

        static boolean isVisualCompleteTimeEnabled(int flags) {
            return (flags & FLAG_VISUAL_COMPLETE_TIME) > 0;
        }
        
        static boolean isSpeedIndexEnabled(int flags) {
            return (flags & FLAG_SPEEDINDEX) > 0;
        }

        static boolean isStreamingMediaEnabled(int flags) {
            return (flags & FLAG_STREAMING_MEDIA) > 0;
        }

        static boolean isFrameworkDetectionEnabled(int flags) {
            return (flags & FLAG_FRAMEWORK_DETECTION) > 0;
        }

        static boolean isThirdPartyIncludeOwnResources(int flags) {
        	return (flags & FLAG_THIRDPARTY_OWNRESOURCES) > 0;
        }

        static boolean isBandwidthEnabled(int flags) {
        	return (flags & FLAG_BANDWIDTH) > 0;
        }
        
        static boolean isUserTimingsEnabled(int flags){
        	return (flags & FLAG_USERTIMINGS) > 0;
        }

        /**
         * calculates the features enabled by injected javascript agent - takes
         * featurehash and agent parameters into account
         *
         * @param agentUrl as calculated by {@link JavaScriptAgent#getJavaScriptAgentUrl(String, String)}.
         * @author peter.lang
         */
        static int parseFeatureHash(String[] agentUrl) {
            if (agentUrl == null) {
                return 0;
            }
            int result = 0;
            if (agentUrl.length >= 2) {
                String featureHash = agentUrl[1];
                result = setNavigationTiming(result, featureHash != null && featureHash.contains("n"));
                result |= setPerceivedRenderTime(result, featureHash != null && featureHash.contains("p"));
                result |= setStreamingMedia(result, featureHash != null && featureHash.contains("s"));
                result |= setFrameworkDetection(result, featureHash != null && featureHash.contains("f"));
                result |= setBandwidthEnabled(result, featureHash != null && featureHash.contains("b"));
                result |= setVisualCompleteTime(result, featureHash != null && featureHash.contains("V"));
                result |= setSpeedIndex(result, featureHash != null && featureHash.contains("S"));
                result |= setUserTimingsEnabled(result, featureHash != null && featureHash.contains("T"));
            }
            if (agentUrl.length >= 4) {
                String parameters = agentUrl[3];
                if (disableNavTimingsFromConfig) {
                	result = setNavigationTiming(result, parameters != null && !parameters.contains("ntd="));
                }
                result |= setThirdPartyContentDetection(result, parameters != null && parameters.contains("tp="));

                if (isThirdPartyContentDetectionEnabled(result)) {
                	int startIndexOfThirdPartyParameters = parameters.indexOf("tp=") + 3;
                	int endIndexOfThirdPartyParameters = parameters.indexOf('|', startIndexOfThirdPartyParameters);
                	if (endIndexOfThirdPartyParameters == -1) {
                		endIndexOfThirdPartyParameters = parameters.length();
                	}
                	String thirdPartyParameters = parameters.substring(startIndexOfThirdPartyParameters, endIndexOfThirdPartyParameters);
                	String[] thirdPartyParameterSettings = thirdPartyParameters.split(",");

                	result |=  setThirdPartyIncludeOwnResources(result, (thirdPartyParameterSettings.length >= 4 && "1".equals(thirdPartyParameterSettings[3])));
                }
            }
            return result;
        }
    }

	public void sendSyntheticEndVisit() throws IOException {
		startCustomAction("endVisit", "_endVisit_", "");
		stopCustomAction(false, null, null, null, 0);
	}
}