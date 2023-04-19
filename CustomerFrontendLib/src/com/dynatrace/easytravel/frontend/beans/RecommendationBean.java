package com.dynatrace.easytravel.frontend.beans;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;

import com.codahale.metrics.Timer.Context;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.frontend.data.DataProviderInterface;
import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.frontend.lib.CustomerFrontendUtil;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.metrics.Metrics;

import ch.qos.logback.classic.Logger;
/**
 * Runnable called from the asynchronous servlet CalculateRecommendations
 * returning randomly selected journeys.
 *
 * Note:
 * This bean is also used to render special-offers.jsp page
 *
 * @author cwat-ceiching
 * @author cwpl-rorzecho
 *
 */

@ManagedBean
@SessionScoped
public class RecommendationBean implements Runnable {
	private static final Logger log = LoggerFactory.make();
	protected final DecimalFormat amountFormat = new DecimalFormat("'$ '0.00");
	public static int NUM_OF_JOURNEY_PICS = 3;

	/**
	 * Variables for modifying the output
	 */
	protected int amountOfJourneys = 2;
	protected String headerText = "Recommendations";

	/**
	 * In tests we are setting mock here
	 */
	private DataBean dataBean = new DataBean();
	private boolean processable;
	private AsyncContext asyncContext;

	public RecommendationBean() {
		this.processable = false;
		this.asyncContext = null;
	}

	public AsyncContext getAsyncContext() {
		return asyncContext;
	}

	public void setAsyncContext(AsyncContext asyncContext) {
		this.asyncContext = asyncContext;
		this.processable = true;
	}

	/**
	 * Method used in tests to set mocked object here
	 * @param dataBean
	 */
	public void setDataBean(DataBean dataBean) {
		this.dataBean = dataBean;
	}

	@Override
	public void run() {
		if (!processable) {
			return;
		}
        Context context = Metrics.getTimerContext(this, "run");
		try {
			ServletResponse response = asyncContext.getResponse();
			response.setContentType("text/html;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.print(printRecommendations());
            writer.flush();
		} catch (Exception e) {
            log.debug(e.getMessage(), e);
        } finally {
        	try {
				// async processing is done -> complete execution
        		asyncContext.complete();

	            context.stop();
	            context.close();
        	}
        	catch (Exception ex) {
        		log.debug(ex.getMessage(), ex);
        	}
        	finally {
    			// session gets invalidated on complete and thus any further use of
				// the bean must be prevented
				processable = false;
        	}
        }
	}

    /**
     *  Print Recommendations as HTML div components ready
     *  to be rendered on a web page
     *
     * @return
     */
    public String printRecommendations() {

    	DataProviderInterface dataProvider = dataBean.getDataProvider();

        StringBuilder builder = new StringBuilder();
        builder.append("<div class=\"icePnlPos resultsArea\">" + BaseConstants.CRLF);
        printTitle(builder);
        printJourneys(builder, CustomerFrontendUtil.getRandomJourneys(dataProvider));
        builder.append("</div>"  + BaseConstants.CRLF);
        return builder.toString();
    }

    private void printTitle(StringBuilder builder) {
        builder.append("<div class=\"recommendation\"><p style=\"margin-left:10px\">").append(BaseConstants.CRLF);
        builder.append("<strong>").append(headerText).append("</strong></p></div>").append(BaseConstants.CRLF);
    }

    private void printJourneys(StringBuilder builder, List<JourneyDO> journeys) {
        Random r = new Random();
        int i = -1;

        JourneyDO jo = null;

        for (int j = 0; j < Math.min(amountOfJourneys, journeys.size()); j++) {
            i = r.nextInt(journeys.size());
            jo = journeys.get(i);
            printJourney(builder, jo);
        }
    }

    protected void printJourney(PrintWriter writer, JourneyDO jo) throws IOException {
        writer.print(printJourney(jo));
    }

    protected void printJourney(StringBuilder builder, JourneyDO jo) {
        builder.append(printJourney(jo));
    }

	// This method defines the URL prefix, which is context dependent.
	protected String urlPrefix() {
		return "img/"; // for the booking page context
	}

    protected String printJourney(JourneyDO jo) {
        StringBuilder builder = new StringBuilder();
        String imgRef = "result_pic_" + ((Math.abs(jo.getId()) % NUM_OF_JOURNEY_PICS)) + ".png";

        builder.append("<div class=\"icePnlGrp resultBox\">").append(BaseConstants.CRLF);
        builder.append("	<div class=\"icePnlGrp resultBoxImage\">").append(BaseConstants.CRLF);
        builder.append("		<a href=\"/orange-trip-details.jsf?journeyId=").append(jo.getId()).append("\">").append(BaseConstants.CRLF);
        builder.append("			<img onload=\"window.onResultImageLoaded && onResultImageLoaded('").append(imgRef).append("')\" alt=\"no image available\" class=\"iceGphImg etResultsImage\" height=\"130\" src=\"").append(urlPrefix()).append(imgRef).append("\" />").append(BaseConstants.CRLF);
        builder.append("		</a>").append(BaseConstants.CRLF);
        builder.append("	</div>").append(BaseConstants.CRLF);
        builder.append("	<div class=\"icePnlGrp resultBoxContent\">").append(BaseConstants.CRLF);
        builder.append("		<span class=\"iceOutTxt journeyName\">").append(jo.getName()).append("</span>").append(BaseConstants.CRLF);
        builder.append("		<span class=\"iceOutFrmt journeyAmount\">").append(amountFormat.format(jo.getAmount())).append("</span>").append(BaseConstants.CRLF);
        builder.append("		<span class=\"iceOutTxt journeyDesc\">If you wish to stay in a hotel that has friendly staff and an inviting ambience that reminds you of home, then head to the Grand Hotel.</span>").append(BaseConstants.CRLF);
        builder.append("		<span class=\"iceOutTxt journeyDate\">")
        	.append(jo.getFromDate().getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US))
            .append(" ")
            .append(jo.getFromDate().get(Calendar.DAY_OF_MONTH))
            .append(" - ")
            .append(jo.getToDate().getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US))
            .append(" ")
            .append(jo.getToDate().get(Calendar.DAY_OF_MONTH))
            .append("</span>")
            .append(BaseConstants.CRLF);
        builder.append("		<a class=\"commonButton orangeButton1 journeyBookButton journeyBooked_false\" href=\"/orange-booking-review.jsf?journeyId=").append(jo.getId()).append("\">Book Now</a>").append(BaseConstants.CRLF);
        builder.append("	</div>").append(BaseConstants.CRLF);
        builder.append("</div>").append(BaseConstants.CRLF);
        return builder.toString();
    }

}
