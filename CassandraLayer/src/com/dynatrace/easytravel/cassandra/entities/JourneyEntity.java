package com.dynatrace.easytravel.cassandra.entities;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.dynatrace.easytravel.cassandra.tables.objectmapper.JourneyTable;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.jpa.business.Tenant;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * 
 * @author Rafal.Psciuk
 *
 */
@Table(name = JourneyTable.JOURNEY_TABLE_NAME)
public class JourneyEntity implements CassandraEntity<Journey> {

    private String name;
    private int id;
    private String depLocation;
    private String destLocation;
    private String tenant;
    private Date fromDate;
    private Date toDate;
    private String description;
    private double amount;
    private ByteBuffer picture;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @PartitionKey(0)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDepLocation() {
        return depLocation;
    }

    public void setDepLocation(String depLocation) {
        this.depLocation = depLocation;
    }

    public String getDestLocation() {
        return destLocation;
    }

    public void setDestLocation(String destLocation) {
        this.destLocation = destLocation;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public ByteBuffer getPicture() {
        return picture;
    }

    public void setPicture(ByteBuffer picture) {
        this.picture = null;
    }

    public JourneyEntity() {
    }

    public JourneyEntity(Journey model) {
        this.name = model.getName();
        this.id = model.getId();
        this.depLocation = model.getStart().getName();
        this.destLocation = model.getDestination().getName();
        this.tenant = model.getTenant().getName();
        this.fromDate = model.getFromDate();
        this.toDate = model.getToDate();
        this.description = model.getDescription();
        this.amount = model.getAmount();
        if(model.getPicture() != null) {
            this.picture = ByteBuffer.wrap(ArrayUtils.clone(model.getPicture()));
        }
    }

    @Override
    public Journey createModel() {
        Journey journey = new Journey();
        journey.setName(name);
        journey.setId(id);
        journey.setStart(new Location(depLocation));
        journey.setDestination(new Location(destLocation));
        journey.setFromDate(fromDate);
        journey.setToDate(toDate);
        journey.setDescription(description);
        journey.setAmount(amount);
        journey.setTenant(new Tenant(tenant, null, null));
        if(picture != null) {
            journey.setPicture(picture.array());
        }

        return journey;
    }

    @Override
    public String toString() {
        return "JourneyEntity{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", depLocation='" + depLocation + '\'' +
                ", destLocation='" + destLocation + '\'' +
                ", tenant='" + tenant + '\'' +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", picture=" + picture +
                '}';
    }
}
