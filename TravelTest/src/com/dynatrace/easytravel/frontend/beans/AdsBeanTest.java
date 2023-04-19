package com.dynatrace.easytravel.frontend.beans;

import static com.dynatrace.easytravel.MiscConstants.*;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.frontend.data.JourneyDO;

public class AdsBeanTest extends BeanTestBase {

	@Test
	public void testAds() throws Exception {

		AdsBean adsBean = new AdsBean();

		JourneyDO jo = new JourneyDO(JOURNEY_ID, JOURNEY_NAME,
				Calendar.getInstance(), Calendar.getInstance(), LOCATION_NAME1,
				LOCATION_NAME2, TENANT_NAME, AMOUNT, null);

		DecimalFormat amountFormat = new DecimalFormat("'$ '0.00");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(out) {

			/*
			 * Overwrite to trim new lines and leading/trailing white spaces. It will be simpler to compare result and expected.
			 * (non-Javadoc)
			 * 
			 * @see java.io.PrintWriter#println(java.lang.String)
			 */
			@Override
			public void println(String x) {
				super.print(x.trim());
			}
		};

		adsBean.printJourney(writer, jo);
		writer.flush();

        StringBuilder builder = new StringBuilder();

        String imgRef = "result_pic_" + ((Math.abs(jo.getId()) % 3)) + ".png";

        builder.append("<div class=\"icePnlGrp resultBox\">" + BaseConstants.CRLF);
        builder.append("	<div class=\"icePnlGrp resultBoxImage\">" + BaseConstants.CRLF);
        builder.append("		<a href=\"/orange-trip-details.jsf?journeyId=" + jo.getId() + "\">" + BaseConstants.CRLF);
        builder.append("			<img onload=\"window.onResultImageLoaded && onResultImageLoaded('" + imgRef + "')\" alt=\"no image available\" class=\"iceGphImg etResultsImage\" height=\"130\" src=\"/img/" + imgRef + "\" />" + BaseConstants.CRLF);
        builder.append("		</a>" + BaseConstants.CRLF);
        builder.append("	</div>" + BaseConstants.CRLF);
        builder.append("	<div class=\"icePnlGrp resultBoxContent\">" + BaseConstants.CRLF);
        builder.append("		<span class=\"iceOutTxt journeyName\">" + jo.getName() + "</span>" + BaseConstants.CRLF);
        builder.append("		<span class=\"iceOutFrmt journeyAmount\">" + amountFormat.format(jo.getAmount()) + "</span>" + BaseConstants.CRLF);
        builder.append("		<span class=\"iceOutTxt journeyDesc\">If you wish to stay in a hotel that has friendly staff and an inviting ambience that reminds you of home, then head to the Grand Hotel.</span>" + BaseConstants.CRLF);
        builder.append("		<span class=\"iceOutTxt journeyDate\">"
                + jo.getFromDate().getDisplayName(Calendar.MONTH,
                Calendar.SHORT, Locale.US)
                + " "
                + jo.getFromDate().get(Calendar.DAY_OF_MONTH)
                + " - "
                + jo.getToDate().getDisplayName(Calendar.MONTH, Calendar.SHORT,
                Locale.US) + " "
                + jo.getToDate().get(Calendar.DAY_OF_MONTH) + "</span>" + BaseConstants.CRLF);
        builder.append("		<a class=\"commonButton orangeButton1 journeyBookButton journeyBooked_false\" href=\"/orange-booking-review.jsf?journeyId=" + jo.getId() + "\">Book Now</a>" + BaseConstants.CRLF);
        builder.append("	</div>" + BaseConstants.CRLF);
        builder.append("</div>" + BaseConstants.CRLF);

        assertEquals(builder.toString(), out.toString());
    }
}
