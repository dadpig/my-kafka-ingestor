package com.kafka.ingestor.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_category", columnList = "category"),
    @Index(name = "idx_product_manufacturer", columnList = "manufacturer")
})
public class ProductEntity {

    @Id
    @Column(name = "product_id", length = 50)
    private String productId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(name = "manufacturer", length = 200)
    private String manufacturer;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "processed", nullable = false)
    private Boolean processed = false;

    public ProductEntity() {
    }

    public ProductEntity(String productId, String name, String category, BigDecimal price, String manufacturer, Instant createdAt) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.manufacturer = manufacturer;
        this.createdAt = createdAt;
        this.processed = false;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductEntity that = (ProductEntity) o;
        return Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}
