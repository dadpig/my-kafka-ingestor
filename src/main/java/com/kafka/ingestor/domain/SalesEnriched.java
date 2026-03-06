package com.kafka.ingestor.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class SalesEnriched implements Serializable {

    private String saleId;
    private String customerId;
    private String customerName;
    private String customerSegment;
    private String customerRegion;
    private String productId;
    private String productName;
    private String productCategory;
    private String salespersonId;
    private String salespersonName;
    private String salespersonCity;
    private String salespersonCountry;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private Instant saleDate;
    private String channel;

    public SalesEnriched() {
    }

    public SalesEnriched(String saleId, String customerId, String customerName, String customerSegment,
                         String customerRegion, String productId, String productName, String productCategory,
                         Integer quantity, BigDecimal unitPrice, BigDecimal totalAmount, Instant saleDate, String channel) {
        this.saleId = saleId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerSegment = customerSegment;
        this.customerRegion = customerRegion;
        this.productId = productId;
        this.productName = productName;
        this.productCategory = productCategory;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.saleDate = saleDate;
        this.channel = channel;
    }

    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerSegment() {
        return customerSegment;
    }

    public void setCustomerSegment(String customerSegment) {
        this.customerSegment = customerSegment;
    }

    public String getCustomerRegion() {
        return customerRegion;
    }

    public void setCustomerRegion(String customerRegion) {
        this.customerRegion = customerRegion;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
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

    public String getSalespersonCity() {
        return salespersonCity;
    }

    public void setSalespersonCity(String salespersonCity) {
        this.salespersonCity = salespersonCity;
    }

    public String getSalespersonCountry() {
        return salespersonCountry;
    }

    public void setSalespersonCountry(String salespersonCountry) {
        this.salespersonCountry = salespersonCountry;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Instant getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(Instant saleDate) {
        this.saleDate = saleDate;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalesEnriched that = (SalesEnriched) o;
        return Objects.equals(saleId, that.saleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saleId);
    }

    @Override
    public String toString() {
        return "SalesEnriched{" +
                "saleId='" + saleId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerSegment='" + customerSegment + '\'' +
                ", customerRegion='" + customerRegion + '\'' +
                ", productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", productCategory='" + productCategory + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalAmount=" + totalAmount +
                ", saleDate=" + saleDate +
                ", channel='" + channel + '\'' +
                '}';
    }
}
