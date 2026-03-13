package com.kafka.ingestor.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class Salesperson implements Serializable {
    private String salespersonId;
    private String name;
    private String email;
    private String city;
    private String country;
    private Instant createdAt;
    private String dataSource;

    public Salesperson() {
    }

    public Salesperson(String salespersonId, String name, String email, String city, String country, Instant createdAt) {
        this.salespersonId = salespersonId;
        this.name = name;
        this.email = email;
        this.city = city;
        this.country = country;
        this.createdAt = createdAt;
    }

    public Salesperson(String salespersonId, String name, String email, String city, String country, Instant createdAt, String dataSource) {
        this.salespersonId = salespersonId;
        this.name = name;
        this.email = email;
        this.city = city;
        this.country = country;
        this.createdAt = createdAt;
        this.dataSource = dataSource;
    }

    public String getSalespersonId() {
        return salespersonId;
    }

    public void setSalespersonId(String salespersonId) {
        this.salespersonId = salespersonId;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Salesperson that = (Salesperson) o;
        return Objects.equals(salespersonId, that.salespersonId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(salespersonId);
    }

    @Override
    public String toString() {
        return "Salesperson{" +
                "salespersonId='" + salespersonId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", createdAt=" + createdAt +
                ", dataSource='" + dataSource + '\'' +
                '}';
    }
}
