package com.dynatrace.diagnostics.uemload.dcrum;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;

import com.dynatrace.diagnostics.uemload.utils.UemLoadUrlUtils;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;


public class DCRumDataRecordBuilder {

	private static final Logger log = Logger.getLogger(DCRumDataRecordBuilder.class.getName());


	String build(long interval, DCRumDataRecord dimension, Collection<String> additionalPps) {
		List<String> ppids = Lists.newArrayList(additionalPps);
		ppids.add(dimension.getPPID());

		try {
			return join(BaseConstants.WS,
					dimension.getRecordType(),
					interval,
					dimension.getSrvIP(),
					dimension.getCliIP(),
					dimension.getInCliIP(),
					encode(dimension.getCliName()),
					encode(dimension.getSoftwareService()),
					dimension.getAppType(),
					createOperation(dimension),
					dimension.getEventType(),
					dimension.getStatus(),
					dimension.getSlowType(),
					encode(BaseConstants.COMMA, ppids));
		} catch (URISyntaxException e) {
			log.log(Level.WARNING, String.format("Could not create a dimension record for %s", dimension), e);
			return BaseConstants.EMPTY_STRING;
		}
	}

	private static String encode(String toEnc) {
		return UemLoadUrlUtils.encodeUrlUtf8(toEnc);
	}

	private static String encode(String separator, Collection<String> toEncs) {
		return join(separator, FluentIterable.from(toEncs)
				.transform(new Function<String, Object>() {

					@Override
					public String apply(String toEnc) {
						return encode(toEnc);
					}

				}).toArray(Object.class));
	}

	private static String join(String separator, Object... entries) {
		return Joiner.on(separator)
				.useForNull(BaseConstants.MINUS)
				.join(entries);
	}

	public static void main(String[] args) throws URISyntaxException {
		System.out.println(createOperation(new DCRumDataRecord("http://localhost/easyTravel/ss", "192.168.0.42", "user1")));
	}

	private static String createOperation(DCRumDataRecord dimension) throws URISyntaxException {
		String query = dimension.getUserDefParams() == null ? null : URLEncodedUtils.format(dimension.getUserDefParams(),
				BaseConstants.UTF8);
		return new URIBuilder()
				.setScheme(dimension.getScheme())
				.setHost(dimension.getHost())
				.setPath(dimension.getPath())
				.setQuery(query)
				.build().toString();

	}
}