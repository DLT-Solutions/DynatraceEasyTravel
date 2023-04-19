package com.dynatrace.easytravel.frontend.data;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.PHPEnablementCheck;

/**
 * Journey Display Object
 *
 * @author philipp.grasboeck
 *
 */
public class JourneyDO implements Serializable {
	private static final Logger log = LoggerFactory.make();

	private static final long serialVersionUID = 759700391290462810L;
	private static final EasyTravelConfig EASYTRAVEL_CONFIG = EasyTravelConfig
			.read();

	private int id;
	private String name;
	private Calendar fromDate;
	private Calendar toDate;
	private String start;
	private String destination;
	private String tenant;
	private double amount;
	private byte[] picture;
	private boolean hasPicture;

	public JourneyDO() {
	}

	public JourneyDO(int id, String name, Calendar fromDate, Calendar toDate,
			String start, String destination, String tenant, double amount,
			byte[] picture) {		// NOSONAR - changing this will cause a memory leak due to icefaces bugs, see JLT-84891
		this.id = id;
		this.name = name;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.start = start;
		this.destination = destination;
		this.tenant = tenant;
		this.amount = amount;
		this.picture = picture;		// NOSONAR - changing this will cause a memory leak due to icefaces bugs, see JLT-84891
		this.hasPicture = picture != null;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Calendar getFromDate() {
		return fromDate;
	}

	public void setFromDate(Calendar fromDate) {
		this.fromDate = fromDate;
	}

	public Calendar getToDate() {
		return toDate;
	}

	public void setToDate(Calendar toDate) {
		this.toDate = toDate;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public byte[] getPicture() {
		return picture;
	}

	public boolean isHasPicture() {
		return hasPicture;
	}

	public void setPicture(byte[] picture) {	// NOSONAR - changing this will cause a memory leak due to icefaces bugs, see JLT-84891
		this.picture = picture;		// NOSONAR - changing this will cause a memory leak due to icefaces bugs, see JLT-84891
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Deprecated
	// TODO ugly, this will be replaced with <ice:outputFormat>
	// open question: RoundingMode?
	public String getAmountAsString() {
		DecimalFormat df = new DecimalFormat("#,##0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(amount);
	}

	public String getTimeframe() {
		SimpleDateFormat df = new SimpleDateFormat("MMM d", Locale.US);

		return df.format(fromDate.getTimeInMillis()) + " - "
				+ df.format(toDate.getTimeInMillis());
	}

	@Override
	public String toString() {
		return "JourneyDO [id=" + id + ", name=" + name + ", fromDate="
				+ fromDate + ", toDate=" + toDate + ", start=" + start
				+ ", destination=" + destination + ", tenant=" + tenant
				+ ", amount=" + amount + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JourneyDO other = (JourneyDO) obj;
		if (id != other.id)
			return false;
		return true;
	}

	/**
	 * @author cwat-cchen
	 *
	 *         retrieve rating information from ratings.php
	 *
	 * @return An empty String if PHP Support is not enabled, otherwise
	 * 			the rating for the current journey.
	 */
	private String callGetAverageTotal() {
		String ratingResult = "";
		try {

			HttpPost request = new HttpPost("http://" + EASYTRAVEL_CONFIG.apacheWebServerHost + ":" + EASYTRAVEL_CONFIG.apacheWebServerPort
				+ "/rating/ratings.php");

			JSONObject jsonObj = new JSONObject();
			jsonObj.put("journey_id", id);
			jsonObj.put("info", 1);

			StringEntity params = new StringEntity(jsonObj.toString());
			params.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

			request.setEntity(params);
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			try {
				HttpResponse response = httpClient.execute(request);
				JSONObject decode = new JSONObject(IOUtils.toString(response.getEntity().getContent()));
				String reviews = decode.getString("reviews");
				String totalAvg = decode.getString("total_avg");
				ratingResult = "Score from " + reviews + " reviews: " + totalAvg;
			} finally {
				httpClient.close();
			}


		} catch (Exception e) {
			log.info("Ratings could not be retrieved, maybe PHP rating app is not running: " + e.getMessage());
			if (log.isDebugEnabled()) log.debug(e.getMessage(), e);
		}

		return ratingResult;
	}

	public String getAverageTotal() {
		return !PHPEnablementCheck.isPHPEnabled() ? "" : callGetAverageTotal();
	}

	public String getAverageTotalOnAngularFrontend() {
		return !PHPEnablementCheck.isPHPEnabledOnAngularFrontend() ? "" : callGetAverageTotal();
	}
}
