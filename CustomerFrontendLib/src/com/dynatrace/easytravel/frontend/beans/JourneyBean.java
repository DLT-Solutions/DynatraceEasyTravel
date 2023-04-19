package com.dynatrace.easytravel.frontend.beans;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import com.dynatrace.easytravel.metrics.Metrics;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.GenericPluginList;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.util.PHPEnablementCheck;
import com.dynatrace.easytravel.util.SlowTransactionsCheck;

import static com.codahale.metrics.Timer.Context;

/**
 * Just a bean that stores the current journey of the trip details page.
 *
 * @author philipp.grasboeck
 */
@ManagedBean
@SessionScoped
public class JourneyBean implements Serializable {
	private static final Logger log = LoggerFactory.make();

	private static final GenericPluginList additionalContentPlugins = new GenericPluginList(PluginConstants.FRONTEND_TRIPDETAILS_PAGE);

	private static final long serialVersionUID = -7264655903283603100L;

	@ManagedProperty("#{dataBean}")
	private DataBean dataBean;

	private int selectedJourneyId;
	private JourneyDO selectedJourney;

	public int getSelectedJourneyId() {
		return selectedJourneyId;
	}

	/**
	 * check if the php server is running
	 *
	 * @author cwat-cchen
	 * @return
	 */
	public boolean isStartPhpServer() {
		return PHPEnablementCheck.isPHPEnabled();
	}

	public boolean areWordpressPostPresent(int journeyId) {
		if (!PHPEnablementCheck.isPHPEnabled()) {
			return false;
		}
		// get information if there are posts for specified tag
		return getPostsTotal(journeyId) > 0;
	}

	public int getJourneyPostCount(int journeyId) {
		if (!PHPEnablementCheck.isPHPEnabledOnAngularFrontend()) {
			return 0;
		}
		return getPostsTotal(journeyId);
	}

	// provide a link to some page from inside the plugin

	public String getHead()  {
		if (!PHPEnablementCheck.isPHPEnabled()) {
			return "";
		}
		// get link in menu if PHP is enabled
		return "<a href='/blog/'>Blog</a>";
		}

	private static final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig
			.read();

	private int getPostsTotal(int journeyId) {

		int ratingResult = 0;
		try {
			String http_part;

			if (!SlowTransactionsCheck.isPHPBlogEnabled()) {
				http_part = "/blog/count.php?tag=" + journeyId + "&slow=0";}
			else{http_part = "/blog/count.php?tag=" + journeyId + "&slow=1";}


			HttpGet request = new HttpGet("http://"
					+ EASYTRAVEL_CONFIG.apacheWebServerHost + ":"
					+ EASYTRAVEL_CONFIG.apacheWebServerPort
					+ http_part);


			CloseableHttpClient httpClient = HttpClientBuilder.create().build();

			try {
				HttpResponse response = httpClient.execute(request);


				StringBuilder result = new StringBuilder();
				InputStream in = response.getEntity().getContent();

				try {
					BufferedReader rd = new BufferedReader(
							new InputStreamReader(in));
					try {
						String line;
						while ((line = rd.readLine()) != null) {
							result.append(line);
						}
					} finally {
						rd.close();
					}
				} finally {
					in.close();
				}

				ratingResult = Integer.valueOf(result.toString());

			} finally {
				httpClient.close();
			}

		} catch (Exception e) {
			log.info("Cannot load posts for journey: " + e.getMessage());
			if (log.isDebugEnabled())
				log.debug(e.getMessage(), e);
		}

		return ratingResult;
	}

	public void setSelectedJourneyId(int selectedJourneyId) {
		this.selectedJourneyId = selectedJourneyId;
	}

	public JourneyDO getSelectedJourney() {
		return selectedJourney;
	}

	public String getTripDetailsInclude() {
		return Pages.INCLUDE_TRIP_DETAILS;
	}

	public String getRatingResultsInclude() {
		return Pages.INCLUDE_RATING_RESULTS;
	}

	public String getRatingActionInclude() {
		return Pages.INCLUDE_RATING_ACTION;
	}

	/**
	 * Load the journey from the selectedJourneyId view parameter.
	 *
	 * @author philipp.grasboeck
	 */
	public void loadJourney() {
		if (selectedJourney == null
				|| selectedJourney.getId() != selectedJourneyId) {
            final Context context = Metrics.getTimerContext(this, "loadJourney");
			try {
				selectedJourney = dataBean.getDataProvider().getJourneyById(
						selectedJourneyId);
				if (selectedJourney == null) {
					FacesUtils.sendError(FacesUtils.ERROR_404, "Journey "
							+ selectedJourneyId);
				}
			} catch (RemoteException ex) {
				throw new IllegalStateException("Cannot load journey: "
						+ ex.getMessage());
			} finally {
                context.stop();
                context.close();
            }
        }
	}

	public DataBean getDataBean() {
		return dataBean;
	}

	public void setDataBean(DataBean dataBean) {
		this.dataBean = dataBean;
	}

	public String getAdditionalContent() {
		StringBuilder buf = new StringBuilder();

		for (Object object : additionalContentPlugins.execute(PluginConstants.FRONTEND_TRIPDETAILS_PAGE, selectedJourney)) {
			if (object != null) {
				buf.append(object);
			}
		}

		return buf.toString();
	}
}
