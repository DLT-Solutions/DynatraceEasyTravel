/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: HbaseUserColumnFamily.java
 * @date: 23.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.columnfamily;

import com.dynatrace.easytravel.hbase.HbaseDataController;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.persistence.provider.UserProvider;


/**
 *
 * @author stefan.moschinski
 */
public class HbaseUserColumnFamily extends HbaseColumnFamily<User> implements UserProvider {

	public static final String USER_COLUMN_FAMILY_NAME = "UserColumnFamily";


	/**
	 * 
	 * @param controller
	 * @author stefan.moschinski
	 */
	public HbaseUserColumnFamily(HbaseDataController controller) {
		super(controller, USER_COLUMN_FAMILY_NAME, "usr");
	}


	@Override
	public User getUserByName(String name) {
		return getByKey(name);
	}

}
