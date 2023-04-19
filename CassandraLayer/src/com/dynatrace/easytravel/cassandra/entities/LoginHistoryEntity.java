package com.dynatrace.easytravel.cassandra.entities;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.LoginHistoryTable;
import com.dynatrace.easytravel.jpa.business.LoginHistory;
import com.dynatrace.easytravel.jpa.business.User;

import java.util.Date;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
@Table(name = LoginHistoryTable.LOGINHISTORY_TABLE_NAME)
public class LoginHistoryEntity implements CassandraEntity<LoginHistory> {
    private int id;
    private String userName;
    private Date loginDate;

    public LoginHistoryEntity() {}

    public LoginHistoryEntity(LoginHistory model) {
        this.id = model.getId();
        this.userName = model.getUser().getName();
        this.loginDate = model.getLoginDate();
    }

    @Override
    public LoginHistory createModel() {
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setUser(new User(userName));
        loginHistory.setId(id);
        loginHistory.setLoginDate(loginDate);

        return loginHistory;
    }

    @PartitionKey
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }
}
