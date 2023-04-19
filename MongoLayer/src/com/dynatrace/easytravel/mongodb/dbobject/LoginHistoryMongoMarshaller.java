/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: LoginHistoryMongoMarshaller.java
 * @date: 20.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.dbobject;

import static com.dynatrace.easytravel.jpa.business.LoginHistory.LOGIN_HISTORY_LOGIN_DATE;
import static com.dynatrace.easytravel.jpa.business.LoginHistory.LOGIN_HISTORY_USER;

import java.util.Map;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.jpa.business.LoginHistory;
import com.dynatrace.easytravel.mongodb.MongoConstants;
import com.google.common.collect.Maps;
import com.mongodb.DBObject;

/**
 *
 * @author stefan.moschinski
 */
public class LoginHistoryMongoMarshaller extends MongoObjectMarshaller<LoginHistory> {

	private static final long serialVersionUID = 1L;

	private static Map<String, Class<? extends MongoObjectMarshaller<? extends Base>>> LOGIN_HISTORY_SUB_MARSHALLER;

	static {
		LOGIN_HISTORY_SUB_MARSHALLER = Maps.newHashMapWithExpectedSize(1);
		LOGIN_HISTORY_SUB_MARSHALLER.put(LOGIN_HISTORY_USER, UserMongoMarshaller.class);
	}

	public LoginHistoryMongoMarshaller() {
		super(LOGIN_HISTORY_SUB_MARSHALLER);
	}

	@Override
	protected DBObject marshalTypeSpecific(LoginHistory loginHistory, boolean withId) {
		if (withId) {
			put(MongoConstants.ID, loginHistory.getId());
		}
		put(LOGIN_HISTORY_LOGIN_DATE, loginHistory.getLoginDate());
		put(LOGIN_HISTORY_USER, new UserMongoMarshaller().marshal(loginHistory.getUser()));

		return this;
	}

	@Override
	protected LoginHistory unmarshalTypeSpecific() {
		LoginHistory loginHistory = new LoginHistory();
		loginHistory.setId(getInt(MongoConstants.ID));
		loginHistory.setLoginDate(getDate(LOGIN_HISTORY_LOGIN_DATE));
		loginHistory.setUser(((UserMongoMarshaller) get(LOGIN_HISTORY_USER)).unmarshal());

		return loginHistory;
	}

	@Override
	public MongoObjectMarshaller<LoginHistory> newInstance() {
		return new LoginHistoryMongoMarshaller();
	}


}
