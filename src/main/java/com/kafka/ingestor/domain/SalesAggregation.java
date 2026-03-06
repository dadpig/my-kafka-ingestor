package com.kafka.ingestor.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class SalesAggregation implements Serializable {

    private String key;
    private Long totalSales;
    private BigDecimal totalRevenue;
    private Integer totalQuantity;
    private BigDecimal averageOrderValue;
    private String dimension;

    public SalesAggregation() {
    }

    public SalesAggregation(String key, Long totalSales, BigDecimal totalRevenue,
                           Integer totalQuantity, BigDecimal averageOrderValue, String dimension) {
        this.key = key;
        this.totalSales = totalSales;
        this.totalRevenue = totalRevenue;
        this.totalQuantity = totalQuantity;
        this.averageOrderValue = averageOrderValue;
        this.dimension = dimension;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(Long totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalesAggregation that = (SalesAggregation) o;
        return Objects.equals(key, that.key) && Objects.equals(dimension, that.dimension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, dimension);
    }

    @Override
    public String toString() {
        return "SalesAggregation{" +
                "key='" + key + '\'' +
                ", totalSales=" + totalSales +
                ", totalRevenue=" + totalRevenue +
                ", totalQuantity=" + totalQuantity +
                ", averageOrderValue=" + averageOrderValue +
                ", dimension='" + dimension + '\'' +
                '}';
    }
}
