package com.kafka.ingestor.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "sales", indexes = {
    @Index(name = "idx_sale_customer", columnList = "customer_id"),
    @Index(name = "idx_sale_product", columnList = "product_id"),
    @Index(name = "idx_sale_salesperson", columnList = "salesperson_id"),
    @Index(name = "idx_sale_date", columnList = "sale_date"),
    @Index(name = "idx_sale_channel", columnList = "channel"),
    @Index(name = "idx_sale_processed_created", columnList = "processed, created_at")
})
public class SaleEntity {

    @Id
    @Column(name = "sale_id", length = 50)
    private String saleId;

    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;

    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;

    @Column(name = "salesperson_id", nullable = false, length = 50)
    private String salespersonId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "sale_date", nullable = false)
    private Instant saleDate;

    @Column(name = "channel", length = 50)
    private String channel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "processed", nullable = false)
    private Boolean processed = false;

    public SaleEntity() {
    }

    public SaleEntity(String saleId, String customerId, String productId, String salespersonId, Integer quantity,
                     BigDecimal unitPrice, BigDecimal totalAmount, Instant saleDate, String channel, Instant createdAt) {
        this.saleId = saleId;
        this.customerId = customerId;
        this.productId = productId;
        this.salespersonId = salespersonId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.saleDate = saleDate;
        this.channel = channel;
        this.createdAt = createdAt;
        this.processed = false;
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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSalespersonId() {
        return salespersonId;
    }

    public void setSalespersonId(String salespersonId) {
        this.salespersonId = salespersonId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaleEntity that = (SaleEntity) o;
        return Objects.equals(saleId, that.saleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(saleId);
    }
}
