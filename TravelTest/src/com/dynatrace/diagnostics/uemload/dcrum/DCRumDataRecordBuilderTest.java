package com.dynatrace.diagnostics.uemload.dcrum;

import static com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils.encodeUrlUtf8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;
import com.google.common.collect.Lists;


public class DCRumDataRecordBuilderTest {


	@Test
	public void testWithOnePurepath() {
		DCRumDataRecord record = new DCRumDataRecord("M")
				.setBegT(1328881197364L)
				.setSrvIP("192.168.2.101")
				.setCliIP("8.166.158.72")
				.setInCliIP("133.109.178.56")
				.setCliName("9DhUZ")
				.setSs("HTTP/Gomez")
				.setAppType((short) -1)
				.setScheme("http")
				.setHost("157.25.157.170")
				.setPath("/bntnet/default.aspx")
				.setUserDefParams(null)
				.setEventType('o')
				.setStatus('N')
				.setSlow('S')
				.setPPID("PT=524134;PA=1765973510;RS=BNTNET/20120207071650_0.session;PS=823473634");

		String build = new DCRumDataRecordBuilder().build(132888119700L, record,
				Collections.<String> emptySet());
		String encodedPp = UemLoadUrlUtils.encodeUrlUtf8("PT=524134;PA=1765973510;RS=BNTNET/20120207071650_0.session;PS=823473634");
		assertThat(
				build,
				equalTo("M 132888119700 192.168.2.101 8.166.158.72 133.109.178.56 9DhUZ HTTP%2FGomez -1 SECRET o N S " +
						encodedPp));
	}


	@Test
	public void testWithMultiplePurepaths() {


		String pp = "PT=524134;PA=1765973510;RS=BNTNET/20120207071650_0.session;PS=823473634";
		String pp2 = "PT=111;PA=222;RS=BNTNET/20120207071650_0.session;PS=823473634";
		String pp3 = "PT=112;PA=223;RS=BNTNET/20120207071650_0.session;PS=823473634";
		DCRumDataRecord record = new DCRumDataRecord("M")
				.setBegT(1328881197364L)
				.setSrvIP("192.168.2.101")
				.setCliIP("8.166.158.72")
				.setInCliIP("133.109.178.56")
				.setCliName("9DhUZ")
				.setSs("HTTP/Gomez")
				.setAppType((short) -1)
				.setScheme("https")
				.setHost("157.25.157.170")
				.setPath("/bntnet/default.aspx")
				.setUserDefParams(Lists.newArrayList(new BasicNameValuePair("name", "b\u00F6ttcher")))
				.setEventType('o')
				.setStatus('N')
				.setSlow('S')
				.setPPID(pp);


		String build = new DCRumDataRecordBuilder().build(132888119700L, record,
				Arrays.asList(pp2, pp3));

		assertThat(
				build,
				startsWith(
				"M 132888119700 192.168.2.101 8.166.158.72 133.109.178.56 9DhUZ HTTP%2FGomez -1 SECRET o N S "));

		// we don't care about the pp order
		assertThat(build, containsString(encodeUrlUtf8(pp)));
		assertThat(build, containsString(encodeUrlUtf8(pp2)));
		assertThat(build, containsString(encodeUrlUtf8(pp3)));
	}

}
