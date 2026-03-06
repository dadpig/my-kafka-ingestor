package com.kafka.ingestor.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class CityAggregation implements Serializable {
    private String city;
    private String country;
    private long salesCount;
    private BigDecimal totalRevenue;
    private int totalQuantity;

    public CityAggregation() {
    }

    public CityAggregation(String city, String country, long salesCount, BigDecimal totalRevenue, int totalQuantity) {
        this.city = city;
        this.country = country;
        this.salesCount = salesCount;
        this.totalRevenue = totalRevenue;
        this.totalQuantity = totalQuantity;
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

    public long getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(long salesCount) {
        this.salesCount = salesCount;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CityAggregation that = (CityAggregation) o;
        return Objects.equals(city, that.city) && Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, country);
    }
}
