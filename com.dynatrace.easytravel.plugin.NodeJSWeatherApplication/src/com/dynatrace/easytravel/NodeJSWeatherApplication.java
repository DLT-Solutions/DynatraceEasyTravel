package com.dynatrace.easytravel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.logging.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.spring.AbstractPagePlugin;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.util.TextUtils;
import com.google.common.base.Strings;

/**
 *
 *
 * @author cwat-moehler
 */
public class NodeJSWeatherApplication extends AbstractPagePlugin {

	private static final Logger log = Logger
			.getLogger(NodeJSWeatherApplication.class.getName());

	private static final String TRIP_FORECAST = "<div class=\"recommendation\">"
			+ "<p style=\"margin-left: 10px;\"><strong>Weather forecast for Trip:</strong></p>"
			+ "</div>"
			+ "<iframe id=\"weather-app\" src=\""
			+ "{0}forecast?loc={1}&days={2}&date={3}&tpl=medium\" "
			+ "scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\" "
			+ "style=\"border: 1px white solid; width:100%; height:550px; margin:0px;\""
			+ "></iframe>";

	private static final String WEATHER_AT_LOCATION = "<div class=\"recommendation\">"
			+ "<p style=\"margin-left: 10px;\"><strong>Weather forecast for Destination:</strong></p>"
			+ "</div>"
			+ "<iframe id=\"weather-app\" src=\""
			+ "{0}current?loc={1}&days={2}&tpl=small\" "
			+ "scrolling=\"no\" frameborder=\"0\" allowTransparency=\"true\" "
			+ "style=\"border: 1px white solid; width:100%; height:70px; margin:0px;\""
			+ "></iframe>";

	private static final String WEATHER_FORECAST_POPUP_LINK = "<span class=\"journeyDesc\" style=\"margin-top: 0px\"><a class=\"forecast-link\" target=\"popup\" onclick=\"window.open"
			+ "(this.href, ''popup'', ''width=800,height=400,scrollbars=no, toolbar=no,status=no,"
			+ "resizable=yes,menubar=no,location=no,directories=no,top=10,left=10'')\""
			+ " href=\"{0}forecast?loc={1}&days={2}&from={3}\">Weather forecast</a></span>";

	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	final EasyTravelConfig CONFIG = EasyTravelConfig.read();

	public int port;
	public String host = null;
	public String url = null;

	public NodeJSWeatherApplication() {
		host = CONFIG.nodejsHost;
		port = CONFIG.nodejsPort;
		url = CONFIG.nodejsURL;
	}

	@Override
	public Object doExecute(String location, Object... context) {
		if (location.equals(PluginConstants.FRONTEND_TRIPDETAILS_PAGE)) {
			return embedTripWeatherForecastInline(context);
		} else if (location.equals(PluginConstants.FRONTEND_RESULTS_BEFORE)) {
			if (context[0] != null && context[0] instanceof String) {
				return embedWeatherForcastForDestination(context[0].toString());
			}
		} else if (location.equals(PluginConstants.FRONTEND_TRIPDETAILS_WEATHER_FORECAST_LINK)) {
			return generateWeatherForecastLink(context);
		}

		return null;
	}

	private String embedTripWeatherForecastInline(Object... context) {
		if (context[0] != null && context[0] instanceof JourneyDO) {

			JourneyDO journey = (JourneyDO) context[0];

			if (Strings.isNullOrEmpty(host)
					|| Strings.isNullOrEmpty(CONFIG.nodejsURL)) {
				log.warning("Node JS Weather Plugin enabled but node JS host or url not configured!");
				return null;
			}

			try {
				return TextUtils.merge(
						TRIP_FORECAST,
						CONFIG.nodejsURL,
						URLEncoder.encode(journey.getDestination(), "UTF-8"),
						calculateDays(journey),
						df.format(journey.getFromDate().getTime()));
			} catch (UnsupportedEncodingException e) {
				log.severe("Ecnoding UTF-8 is not supported on this platform!");
			}
		}
		return null;
	}

	private String generateWeatherForecastLink(Object... context) {
		if (context[0] != null && context[0] instanceof JourneyDO) {
			JourneyDO journey = (JourneyDO) context[0];

			if (Strings.isNullOrEmpty(host)
					|| Strings.isNullOrEmpty(CONFIG.nodejsURL)) {
				log.warning("Node JS Weather Plugin enabled but node JS host or url not configured!");
				return null;
			}

			try {
				return TextUtils.merge(
						WEATHER_FORECAST_POPUP_LINK,
						CONFIG.nodejsURL,
						URLEncoder.encode(journey.getDestination(), "UTF-8"),
						calculateDays(journey),
						df.format(journey.getFromDate().getTime()));
			} catch (UnsupportedEncodingException e) {
				log.severe("Encoding UTF-8 is not supported on this platform!");
			}

		}
		return null;
	}
	
	private int calculateDays(JourneyDO journey) {
		int days = journey.getToDate().get(Calendar.DAY_OF_YEAR) - journey.getFromDate().get(Calendar.DAY_OF_YEAR) + 1;
		
		return days > 0 ? days : days + journey.getFromDate().getActualMaximum(Calendar.DAY_OF_YEAR);
	}

	private String embedWeatherForcastForDestination(String destination) {
		if (destination != null) {

			if (Strings.isNullOrEmpty(host)
					|| Strings.isNullOrEmpty(CONFIG.nodejsURL)) {
				log.warning("Node JS Weather Plugin enabled but node JS host or url not configured!");
				return null;
			}

			String content;
			try {
				content = TextUtils.merge(WEATHER_AT_LOCATION,
						CONFIG.nodejsURL,
						URLEncoder.encode(destination, "UTF-8"), 3);
				return content;
			} catch (UnsupportedEncodingException e) {
			}
		}
		return null;
	}

}
