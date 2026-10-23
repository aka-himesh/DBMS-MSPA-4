package com.pawcare.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model class representing a grooming service in the GroomingServices table.
 */
public class GroomingService {
    private int serviceId;
    private String serviceName;
    private String description;
    private BigDecimal price;
    private int durationMinutes;
    private Timestamp createdAt;

    public GroomingService() {
    }

    public GroomingService(String serviceName, String description, BigDecimal price, int durationMinutes) {
        this.serviceName = serviceName;
        this.description = description;
        this.price = price;
        this.durationMinutes = durationMinutes;
    }

    public GroomingService(int serviceId, String serviceName, String description, BigDecimal price, int durationMinutes, Timestamp createdAt) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.description = description;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.createdAt = createdAt;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("GroomingService [ID=%d, Name='%s', Price=₹%.2f, Duration=%d mins]",
                serviceId, serviceName, price, durationMinutes);
    }
}
