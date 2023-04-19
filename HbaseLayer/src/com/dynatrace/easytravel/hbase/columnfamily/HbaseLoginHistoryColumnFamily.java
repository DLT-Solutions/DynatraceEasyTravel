/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: HbaseLoginHistoryColumnFamily.java
 * @date: 29.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.hbase.columnfamily;

import static com.dynatrace.easytravel.jpa.business.LoginHistory.LOGIN_HISTORY_USER;
import static com.dynatrace.easytravel.jpa.business.User.USER_NAME;
import static org.apache.hadoop.hbase.util.Bytes.toBytes;

import java.util.Collection;

import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import com.dynatrace.easytravel.hbase.HbaseDataController;
import com.dynatrace.easytravel.hbase.serializer.ColumnPrefix;
import com.dynatrace.easytravel.jpa.business.LoginHistory;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.persistence.provider.LoginHistoryProvider;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;


/**
 *
 * @author stefan.moschinski
 */
public class HbaseLoginHistoryColumnFamily extends HbaseColumnFamily<LoginHistory> implements LoginHistoryProvider {


	public final static String LOGIN_HISTORY_COLUMN_FAMILY_NAME = "LoginHistoryColumnFamily";

	private final static String LOGIN_COUNTER = "loginCnt";
	private final static String TOTAL_USER_LOGINS = "totalUserLogins";
	private final HbaseCounterColumnFamily counterColumnFamily;


	public HbaseLoginHistoryColumnFamily(HbaseDataController controller, HbaseCounterColumnFamily counterColumnFamily) {
		super(controller, LOGIN_HISTORY_COLUMN_FAMILY_NAME, "log");
		this.counterColumnFamily = counterColumnFamily;
	}

	@Override
	public LoginHistory add(LoginHistory loginHistory) {
		if (loginHistory.getId() == 0) {
			loginHistory.setId(loginHistory.hashCode());
		}

		counterColumnFamily.incrementByOne(loginHistory.getUser().getName(), LOGIN_COUNTER);
		counterColumnFamily.incrementByOne(TOTAL_USER_LOGINS, LOGIN_COUNTER);
		return super.add(loginHistory);
	}

	@Override
	public void reset() {
		counterColumnFamily.reset(); // also reset counter column family
		super.reset();
	}

	@Override
	public int getLoginCountForUser(String userName) {
		return (int) counterColumnFamily.getLong(userName, LOGIN_COUNTER);
	}

	@Override
	public void removeLoginHistoryById(Integer id) {
		byte[] byteId = toBytes(id);
		LoginHistory loginHistory = getByKey(byteId);

		if (loginHistory == null) {
			return;
		}

		String name = loginHistory.getUser().getName();
		counterColumnFamily.decrementByOne(name, LOGIN_COUNTER);
		counterColumnFamily.decrementByOne(TOTAL_USER_LOGINS, LOGIN_COUNTER);

		deleteByRowKey(byteId);
	}

	@Override
	public int getLoginCountExcludingUser(User userToExclude) {
		return (int) (counterColumnFamily.getLong(TOTAL_USER_LOGINS, LOGIN_COUNTER) - counterColumnFamily.getLong(
				userToExclude.getName(), LOGIN_COUNTER));
	}

	@Override
	public Collection<Integer> getLoginIdsExcludingUser(User userToExclude, int maxResults) {
		SingleColumnValueFilter exludeFilter = new SingleColumnValueFilter(
				Bytes.toBytes(columnFamilyName),
				Bytes.toBytes(ColumnPrefix.apprehendPrefix(prefix, LOGIN_HISTORY_USER)
						.getPrefixedColumnName(USER_NAME)),
				CompareOp.NOT_EQUAL,
				new BinaryComparator(toBytes(userToExclude.getName())));

		Collection<LoginHistory> filtered = getFiltered(exludeFilter, maxResults);
		return FluentIterable.from(filtered).transform(new Function<LoginHistory, Integer>() {

			@Override
			public Integer apply(LoginHistory input) {
				return input.getId();
			}
		}).toList();
	}

}
