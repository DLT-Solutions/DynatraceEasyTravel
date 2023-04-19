package com.dynatrace.diagnostics.uemload.dcrum;

import static com.dynatrace.easytravel.constants.BaseConstants.DOT;
import static com.dynatrace.easytravel.constants.BaseConstants.MINUS;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * Helper class that generates the rest response (as text), that is polled by the dT server
 * @author stefan.moschinski
 */
public class DCRumResponse {

	private static final Logger logger = Logger.getLogger(DCRumResponse.class.getName());

	private static final String HEADER_RECORD = "type=P interval:ulong srvIP:IP cliIP:IP inCliIP:IP cliName:txt softwareService:txt appType:short operation:txt eventType:char status:char slowType:char ppids:json";
	private static final String AMD_UUID = randomAlphanumeric(8) + MINUS + randomAlphanumeric(4) + MINUS + randomAlphanumeric(4) + MINUS + randomAlphanumeric(12);
	private static final String PRODUCER_ID = "ndw" + DOT + randomNumeric(2) + DOT + randomNumeric(1) + DOT + randomNumeric(1) + DOT + randomNumeric(2);


	public Response get(String cmd) {
		if (PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.DC_RUM_EMULATOR) == false) {
			String msg = "Cannot provide emulated DC-RUM appliance data. This feature is only available if the '" + BaseConstants.Plugins.DC_RUM_EMULATOR + "' is enabled.";
			logger.warning(msg);
			return Response.status(Status.NOT_FOUND).entity(msg).build();
		}
		return Response.ok(getInternal(cmd)).build();
	}

	public Response getHeader(String cmd) {
		if (!"version".equals(cmd)) {
			String msg;
			if (cmd == null) {
				msg = "The query parameter 'cmd' must be provided";
			} else {
				msg = TextUtils.merge("The query parameter ''cmd={0}'' is not supported.", cmd);
			}
			logger.warning(msg);
			return Response.status(Status.NOT_FOUND).entity(msg).build();
		}
		return get(cmd);
	}

	private String getInternal(String cmd) {
		StringBuilder builder = new StringBuilder();
		if ("version".equals(cmd)) {
			EasyTravelConfig config = EasyTravelConfig.read();

			addEntry(builder, AMDMetaRecordType.ND_RTM.getEntry(config.amdVersion));
			addEntry(builder, AMDMetaRecordType.TIME_STAMP.getEntry(String.valueOf(System.currentTimeMillis())));
			addEntry(builder, AMDMetaRecordType.OS.getEntry("easyTravel DCRUM Simulator"));
			addEntry(builder, AMDMetaRecordType.INSTANCES.getEntry("true"));

		} else {
			addEntry(builder, DCRumRecordType.VERSION.getEntry("ET1"));
			addEntry(builder, DCRumRecordType.PRODUCER.getEntry(PRODUCER_ID));
			addEntry(builder, DCRumRecordType.HEADER.getEntry(HEADER_RECORD));
			addEntry(builder, DCRumRecordType.KEY_VALUE.getEntry("AmdUUID", AMD_UUID));

			if ("dump".equals(cmd)) {
				addEntry(builder, DCRumdDataRecordsHolder.getInstance().getDataRecords());
			}
		}
		return builder.toString();
	}

	private void addEntry(StringBuilder builder, Collection<String> dataRecords) {
		for (String record : dataRecords) {
			addEntry(builder, record);
		}
	}

	private void addEntry(StringBuilder builder, CharSequence value) {
		builder.append(value);
		builder.append(BaseConstants.CRLF);
	}

	enum DCRumRecordType {
		VERSION("Version"),
		PRODUCER("Producer"),
		HEADER("Fields"),
		KEY_VALUE("?") {

			@Override
			CharSequence getEntry(CharSequence key, CharSequence value) {
				StringBuilder builder = new StringBuilder();
				builder.append(toKey(key));
				builder.append(BaseConstants.WS);
				builder.append(value);
				return builder;
			}
		};

		private String key;

		private static Map<String, DCRumRecordType> keyRecordType = new HashMap<String, DCRumRecordType>(
				DCRumRecordType.values().length + 1, 1F);

		static {
			for (DCRumRecordType type : values()) {
				keyRecordType.put(type.toKey(), type);
			}
		}

		DCRumRecordType(String byteVal) {
			this.key = byteVal;
		}

		static DCRumRecordType get(String type) {
			DCRumRecordType recType;
			return (recType = keyRecordType.get(type)) == null ? KEY_VALUE : recType;
		}

		String toKey() {
			return toKey(key);
		}

		String toKey(CharSequence key) {
			return "#" + key + ":";
		}

		CharSequence getEntry(CharSequence value) {
			StringBuilder builder = new StringBuilder();
			builder.append(toKey());
			builder.append(BaseConstants.WS);
			builder.append(value);
			return builder;
		}

		CharSequence getEntry(CharSequence key, CharSequence value) {
			throw new UnsupportedOperationException(TextUtils.merge("This method is only supported by the type ''{0}''", KEY_VALUE.name()));
		}

	}

	enum AMDMetaRecordType {
		ND_RTM("ND-RTM") {
			@Override
			CharSequence getEntry(CharSequence value) {
				StringBuilder builder = new StringBuilder();
				builder.append("ND-RTM v. ndw.");
				builder.append(value);
				builder.append(" Copyright (C) 2012 Compuware Corp.");
				return builder;
			}
		},
		TIME_STAMP("time_stamp"),
		OS("os"),
		INSTANCES("instances");

		private String key;

		AMDMetaRecordType(String key) {
			this.key = key;
		}

		CharSequence getEntry(CharSequence value) {
			StringBuilder builder = new StringBuilder();
			builder.append(key);
			builder.append('=');
			builder.append(value);
			return builder;
		}
	}
}
