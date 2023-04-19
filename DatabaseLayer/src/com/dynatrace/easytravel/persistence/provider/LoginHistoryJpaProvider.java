/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: LoginHistoryJpaAccess.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider;

import java.util.Collection;

import javax.persistence.Query;

import com.dynatrace.easytravel.jpa.business.LoginHistory;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.persistence.JpaDatabaseController;


/**
 *
 * @author stefan.moschinski
 */
public class LoginHistoryJpaProvider extends JpaProvider<LoginHistory> implements LoginHistoryProvider {


	/**
	 * 
	 * @param controller
	 * @param cls
	 * @author stefan.moschinski
	 */
	public LoginHistoryJpaProvider(JpaDatabaseController controller) {
		super(controller, LoginHistory.class);
	}

	@Override
	public int getLoginCountForUser(String name) {
		Query query = createQuery("select count(lh) from LoginHistory lh where lh.user.name = :username");
		query.setParameter("username", name);
		return ((Number) query.getSingleResult()).intValue();
	}

	@Override
	public void removeLoginHistoryById(Integer id) {
		remove(find(id));
	}

	@Override
	public int getLoginCountExcludingUser(User userToExclude) {
		return createQuery("select count(m) from LoginHistory m where user <> :user", Long.class).setParameter("user",
				userToExclude).getSingleResult().intValue();
	}

	@Override
	public Collection<Integer> getLoginIdsExcludingUser(User userToExclude, int maxResults) {
		return createQuery("select b.id from LoginHistory b where user <> :user order by b.loginDate asc", Integer.class)
				.setParameter("user", userToExclude)
				.setFirstResult(0)
				.setMaxResults(maxResults)
				.getResultList();
	}

}
