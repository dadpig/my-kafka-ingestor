package com.kafka.ingestor.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class TopSalesByCity implements Serializable {
    private String city;
    private String country;
    private long totalSales;
    private BigDecimal totalRevenue;
    private long rank;

    public TopSalesByCity() {
    }

    public TopSalesByCity(String city, String country, long totalSales, BigDecimal totalRevenue, long rank) {
        this.city = city;
        this.country = country;
        this.totalSales = totalSales;
        this.totalRevenue = totalRevenue;
        this.rank = rank;
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

    public long getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(long totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopSalesByCity that = (TopSalesByCity) o;
        return Objects.equals(city, that.city) && Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, country);
    }
}
