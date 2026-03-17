package com.kafka.ingestor.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class TopSalesperson implements Serializable {
    private String salespersonId;
    private String salespersonName;
    private String city;
    private String country;
    private long totalSales;
    private BigDecimal totalRevenue;
    private long rank;

    public TopSalesperson() {
    }

    public TopSalesperson(String salespersonId, String salespersonName, String city, String country,
                          long totalSales, BigDecimal totalRevenue, long rank) {
        this.salespersonId = salespersonId;
        this.salespersonName = salespersonName;
        this.city = city;
        this.country = country;
        this.totalSales = totalSales;
        this.totalRevenue = totalRevenue;
        this.rank = rank;
    }

    public String getSalespersonId() {
        return salespersonId;
    }

    public void setSalespersonId(String salespersonId) {
        this.salespersonId = salespersonId;
    }

    public String getSalespersonName() {
        return salespersonName;
    }

    public void setSalespersonName(String salespersonName) {
        this.salespersonName = salespersonName;
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
        TopSalesperson that = (TopSalesperson) o;
        return Objects.equals(salespersonId, that.salespersonId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(salespersonId);
    }
}
