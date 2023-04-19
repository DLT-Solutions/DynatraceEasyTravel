package com.dynatrace.easytravel.cassandra.entities;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.UserTable;
import com.dynatrace.easytravel.jpa.business.User;
import com.google.common.io.BaseEncoding;

import java.util.Date;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
@Table(name = UserTable.USER_TABLE)
public class UserEntity implements CassandraEntity<User>{
    private String name;
    private String password; // clear-text password for authentication
    private String email;
    private String loyaltyStatus;
    private String fullName;
    private Date lastLogin;

    public UserEntity() {
    }

    public UserEntity(User user) {
        this.name = user.getName();
        this.password = encodePassword(user.getPassword());
        this.email = user.getEmail();
        this.loyaltyStatus = user.getLoyaltyStatus();
        this.fullName = user.getFullName();
        this.lastLogin = user.getLastLogin();
    }

    private String decodePassword(String hexPassword) {
        return new String(BaseEncoding.base16().lowerCase().decode(hexPassword));
    }

    private String encodePassword(String plainTextPassword) {
        return BaseEncoding.base16().lowerCase().encode(plainTextPassword.getBytes());
    }

    @Override
    public User createModel() {
        User user = new User();
        user.setName(name);
        user.setPassword(decodePassword(password));
        user.setEmail(email);
        user.setLoyaltyStatus(loyaltyStatus);
        user.setFullName(fullName);
        user.setLastLogin(lastLogin);
        return user;
    }

    @PartitionKey
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoyaltyStatus() {
        return loyaltyStatus;
    }

    public void setLoyaltyStatus(String loyaltyStatus) {
        this.loyaltyStatus = loyaltyStatus;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
}
