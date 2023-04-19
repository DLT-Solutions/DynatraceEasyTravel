package com.dynatrace.easytravel.launcher.scenarios.builder;

import static com.dynatrace.easytravel.launcher.engine.ProcedureFactory.isRemote;
import static com.dynatrace.easytravel.launcher.scenarios.builder.SettingBuilder.plugin;

import org.apache.commons.lang3.SystemUtils;

import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;


public abstract class ProcedureBuilder {

	private final ProcedureMapping procedure;

	private ProcedureBuilder(ProcedureMapping procedure) {
		this.procedure = procedure;
	}

	public static ProcedureBuilder get(String name){
		return new DefaultProcedureBuilder(name); //default
	}

	public static ProcedureBuilder get(String name, InstallationType compatibility){
		return new DefaultProcedureBuilder(name, compatibility); //default
	}

	public static ProcedureBuilder pluginService() {
		return get(Constants.Procedures.PLUGIN_SERVICE);
	}

	public static ProcedureBuilder dbms() {
		return get(Constants.Procedures.INPROCESS_DBMS_ID);
	}

	public static ProcedureBuilder mdbms() {
		return get(Constants.Procedures.INPROCESS_MYSQL_ID);
	}

	public static ProcedureBuilder preparevmware() {
		return get(Constants.Procedures.PREPARE_VMWARE_ID);
	}

	public static ProcedureBuilder vmotion() {
		return get(Constants.Procedures.VMOTION_ID);
	}

	public static ProcedureBuilder creditCard() {
		return get(Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID)
				/* APM-2614: use platform default for IPCMode always unless set manually in a Scenario.xml
				// on Linux/Unix use the Socket based CreditCard Authorization
				 * .set(
				property(CreditCardAuthorizationProcedure.SETTING_IPC_MODE).value(CreditCardAuthorizationProcedure.IpcMode.pickUp().name()))*/;
	}

	public static ProcedureBuilder contentCreator() {
		return get(Constants.Procedures.DATABASE_CONTENT_CREATOR_ID);
	}

	public static ProcedureBuilder mdcontentCreator() {
		return get(Constants.Procedures.MYSQL_CONTENT_CREATOR_ID);
	}

	public static ProcedureBuilder couchDB() {
		return get(Constants.Procedures.COUCHDB_ID);
	}

	public static ProcedureBuilder couchDBContentCreator() {
		return get(Constants.Procedures.COUCHDB_CONTENT_CREATOR_ID);
	}

	private static boolean isWindows = SystemUtils.IS_OS_WINDOWS;
	private static boolean isMacOS = SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX;

	/**
	 * Returns Business Backend Procedure with plugin switching settings for non-Windows environments,
	 * i.e. from NamedPipeNativeApplication to SocketNativeApplication,
	 * and from NamedPipeNativeApplication_NET to DummyNativeApplication_NET.
	 *
	 * @return
	 * @author cwat-pgrasboe
	 */
	public static ProcedureBuilder businessBackend() {
		// Note: This relies on bootPlugins to be correct in Windows setup, i.e. NamedPipeNativeApplication ON
		return get(Constants.Procedures.BUSINESS_BACKEND_ID).setIf(!isWindows,
				plugin(Constants.Plugin.NamedPipeNativeApplication).disable(),
				plugin(Constants.Plugin.NamedPipeNativeApplication_NET).disable(),
				plugin(Constants.Plugin.DummyNativeApplication_NET).enable(),
				plugin(Constants.Plugin.SocketNativeApplication).enable()).

				// on MacOS we don't have a CreditCard binary and .NET currently
				setIf(isMacOS,
						plugin(Constants.Plugin.SocketNativeApplication).disable(),
						plugin(Constants.Plugin.DummyNativeApplication).enable()
						).

				// on MacOS disable .NET if we are not starting .NET remotely
				setIf(isMacOS && !isRemote(Constants.Procedures.PAYMENT_BACKEND_ID),
						plugin(Constants.Plugin.DotNetPaymentService).disable(),
						plugin(Constants.Plugin.DummyPaymentService).enable()
						);
	}

	public static ProcedureBuilder customerFrontend() {
		return get(Constants.Procedures.CUSTOMER_FRONTEND_ID);
	}
	
	public static ProcedureBuilder angularFrontend() {
		return get(Constants.Procedures.ANGULAR_FRONTEND_ID);
	}

	public static ProcedureBuilder b2bFrontend() {
		return get(Constants.Procedures.B2B_FRONTEND_ID);
	}

	public static ProcedureBuilder paymentBackend() {
		return get(Constants.Procedures.PAYMENT_BACKEND_ID);
	}

	public static ProcedureBuilder ant() {
		return get(Constants.Procedures.ANT_ID);
	}

	public static ProcedureBuilder apacheHttpd() {
		return get(Constants.Procedures.APACHE_HTTPD_ID);
	}

    public static ProcedureBuilder nginxWebserver() {
		return get(Constants.Procedures.NGINX_WEBSERVER_ID);
	}
	public static ProcedureBuilder apacheHttpdPhp() {
		return get(Constants.Procedures.APACHE_HTTPD_PHP_ID);
	}

	public static ProcedureBuilder cassandra() {
		return get(Constants.Procedures.CASSANDRA_ID);
	}

	public static ProcedureBuilder mongodb() {
		return get(Constants.Procedures.MONGO_DB_ID);
	}

	public static ProcedureBuilder hbase() {
		return get(Constants.Procedures.HBASE_ID);
	}

	public static ProcedureBuilder thirdpartyContentServer() {
		return get(Constants.Procedures.THIRDPARTY_SERVER_ID);
	}		

	public static ProcedureBuilder webserverAgentControl() {
		return get(Constants.Procedures.WEBSERVER_AGENT_RESTART_ID, InstallationType.Classic);
	}

	public static ProcedureBuilder hostAgentControl() {
		return get(Constants.Procedures.HOST_AGENT_RESTART_ID, InstallationType.Classic);
	}

	public static ProcedureBuilder browser() {
		return get(Constants.Procedures.BROWSER_ID);
	}

	public <T extends SettingBuilder> ProcedureBuilder set(T... settings) {
		if (/*never null for varargs: settings == null ||*/ settings.length == 0) {
			return this;
		}
		for (T setting : settings) {
			procedure.addSetting(setting.create());
		}
		return this;
	}

	public ProcedureBuilder setIf(boolean condition, SettingBuilder... procedureSetting) {
		if (condition) {
			set(procedureSetting);
		}
		return this;
	}

	public  ProcedureBuilder disable() {
		return set(SettingBuilder.procedure().disable());
	}

	@SuppressWarnings("unchecked")
	public <T extends ProcedureMapping> T create() {
		return (T) procedure;
	}

	private static class DefaultProcedureBuilder extends ProcedureBuilder {

		public DefaultProcedureBuilder(String name) {
			super(new DefaultProcedureMapping(name));
		}

		public DefaultProcedureBuilder(String name, InstallationType compatibility) {
			super(new DefaultProcedureMapping(name, compatibility));
		}
	}
}
