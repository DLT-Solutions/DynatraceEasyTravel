package com.dynatrace.easytravel.launcher.mysqld;

import java.io.File;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.utils.ExecutableUtils;


/**
 * MySQL's little helper
 * @author cwat-cchen
 *
 */
public class MysqlUtils {

	public static final String INSTALL_MYSQL_PATH = Directories.getMysqlDir().getAbsolutePath();
	public static final String MySQL_INI_CONF =  INSTALL_MYSQL_PATH + "/plain_my.ini";
	public static final String MySQL_DATA = INSTALL_MYSQL_PATH + "/data/mysql";
	public static final String EASYTRAVEL_CONFIG_PATH = Directories.getConfigDir().getAbsolutePath().replace("\\", "/");
	public static final String MySQL_INI = EASYTRAVEL_CONFIG_PATH + "/my.ini";
	public static final String MySQL_DATA_DIR = Directories.getDatabaseDirWithForwardSlashes() + "/" + BaseConstants.SubDirectories.EASYTRAVEL_BLOG;
	public static final String MySQL_ERROR_LOG = Directories.getExistingLogDirWithForwardSlashes() + "/MySQL.log";
	public static final String MySQL_SOCKET = EASYTRAVEL_CONFIG_PATH + "/mysql.socket";


	public static String getExecutableDependingOnOs() {
		return "mysql" + File.separator + getMysqlInstallPathForUsedOs() + File.separator + "bin" + File.separator + "mysqld";
	}
	
	private static String getMysqlInstallPathForUsedOs() {
		return ExecutableUtils.getInstallDirDependingOs(INSTALL_MYSQL_PATH);
	}

	public static String getMySqlbaseDir() {
		return INSTALL_MYSQL_PATH;
	}
	
	public static String getLibraryPath() {		
		return INSTALL_MYSQL_PATH + File.separator + getMysqlInstallPathForUsedOs() + File.separator + "lib" + ":" + System.getenv(Constants.Misc.ENV_VAR_MYSQL_LIBRARY_PATH);
	}
}
