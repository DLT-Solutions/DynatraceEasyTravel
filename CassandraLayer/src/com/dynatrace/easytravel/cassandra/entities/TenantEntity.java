package com.dynatrace.easytravel.cassandra.entities;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.TenantTable;
import com.dynatrace.easytravel.jpa.business.Tenant;
import com.google.common.io.BaseEncoding;

import java.util.Date;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
@Table(name = TenantTable.TENANT_TABLE)
public class TenantEntity implements CassandraEntity<Tenant> {
    private String name;
    private String password;
    private String description;
    private Date lastLogin;

    public TenantEntity() {}

    public TenantEntity(Tenant model) {
        this.name = model.getName();
        this.password = encodePassword(model.getPassword());
        this.description = model.getDescription();
        this.lastLogin = model.getLastLogin();
    }

    @Override
    public Tenant createModel() {
        Tenant model = new Tenant(name, decodePassword(password), description);
        model.setLastLogin(lastLogin);
        return model;
    }

    private String decodePassword(String hexPassword) {
        return new String(BaseEncoding.base16().lowerCase().decode(hexPassword));
    }

    private String encodePassword(String plainTextPassword) {
        return BaseEncoding.base16().lowerCase().encode(plainTextPassword.getBytes());
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
}
