package com.dynatrace.easytravel.frontend.beans;

import static com.dynatrace.easytravel.MiscConstants.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.frontend.data.LocationDO;

/**
 * @author cwpl-rpsciuk
 *
 */
public class RecommendationBeanTest extends BeanTestBase {

	private JourneyDO[] journeys;
	private LocationDO[] locations;
	private RecommendationBean recommendationBean;

	// mocks
	private AsyncContext asyncContextMock;
	private ServletResponse responseMock;
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private final PrintWriter writer = new PrintWriter(out);

	@Before
	public void setup() throws IOException {
		super.initMocks();

		// create mocks
		asyncContextMock = createNiceMock(AsyncContext.class);
		responseMock = createMock(ServletResponse.class);

		// init location
		LocationDO location1 = new LocationDO(LOCATION_NAME1);
		LocationDO location2 = new LocationDO(LOCATION_NAME2);
		locations = new LocationDO[] { location1, location2 };

		// init journeys
		JourneyDO journey = new JourneyDO(JOURNEY_ID, JOURNEY_NAME,
				Calendar.getInstance(), Calendar.getInstance(), LOCATION_NAME2,
				LOCATION_NAME1, TENANT_NAME, AMOUNT, null);
		journeys = new JourneyDO[] { journey };

		// init recommendationBean
		recommendationBean = new RecommendationBean();
		recommendationBean.setAsyncContext(asyncContextMock);
		recommendationBean.setDataBean(dataBeanMock);
	}

	/**
	 * Test if RecommendationBean sets proper encoding on response and if
	 * getWriter() method is used (not getOutputStream()). The getWriter()
	 * method must be used to use encoding set by setContentType() method.
	 *
	 * @throws IOException
	 */
	@Test
	public void testEncoding() throws IOException {
		expect(asyncContextMock.getResponse()).andReturn(responseMock);
		responseMock.setContentType("text/html;charset=UTF-8");
		expect(responseMock.getWriter()).andReturn(writer);

		expect(dataBeanMock.getDataProvider()).andReturn(dataProviderMock);
		expect(dataProviderMock.findJourneys(anyObject(String.class), anyObject(Date.class), anyObject(Date.class))).andStubReturn(journeys);
		expect(dataProviderMock.findLocations(anyObject(String.class),anyInt())).andReturn(locations);

		replayMocks();
		replay(asyncContextMock, responseMock);

		recommendationBean.run();

		// check if result is in
		String result = out.toString();
		assertFalse("Result must not be empty", result.isEmpty());

		verifyMocks();
		verify(asyncContextMock, responseMock);
	}

	/**
	 * Test the print journey function, especially the result picture id.
	 *
	 * @throws IOException
	 */
	@Test
	public void testPrintJourney() throws IOException {
		JourneyDO jo = new JourneyDO();
		jo.setFromDate(FROM_DATE1);
		jo.setToDate(TO_DATE1);

		jo.setId(-563);
		checkJourneyPicId(jo, 563 % RecommendationBean.NUM_OF_JOURNEY_PICS);
		jo.setId(0);
		checkJourneyPicId(jo, 0 % RecommendationBean.NUM_OF_JOURNEY_PICS);
		jo.setId(52);
		checkJourneyPicId(jo, 52 % RecommendationBean.NUM_OF_JOURNEY_PICS);
	}

	public void checkJourneyPicId(JourneyDO jo, int expectedId) throws NumberFormatException, IOException {
		StringWriter swriter = new StringWriter();
		PrintWriter writer = new PrintWriter(swriter);
		recommendationBean.printJourney(writer, jo);
		Pattern p = Pattern.compile("result_pic_(.*)\\.png'");
		Matcher m = p.matcher(swriter.toString());
		if (m.find()) {
//			System.out.println(m.group(1));  // The matched substring
			Integer imgId = Integer.valueOf(m.group(1));
			Assert.assertTrue("Image id should be greater than 0, was " + imgId, imgId >= 0);
			Assert.assertTrue("Image id should be greater less than " + RecommendationBean.NUM_OF_JOURNEY_PICS + ", was " + imgId,
					imgId < RecommendationBean.NUM_OF_JOURNEY_PICS);
			Assert.assertEquals(expectedId, imgId.intValue());
		}
	}
}
