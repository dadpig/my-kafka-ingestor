package com.kafka.ingestor.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class Customer implements Serializable {

    private String customerId;
    private String name;
    private String email;
    private String segment;
    private String region;
    private Instant createdAt;

    public Customer() {
    }

    public Customer(String customerId, String name, String email, String segment, String region, Instant createdAt) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.segment = segment;
        this.region = region;
        this.createdAt = createdAt;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(customerId, customer.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId='" + customerId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", segment='" + segment + '\'' +
                ", region='" + region + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
