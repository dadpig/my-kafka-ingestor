package com.kafka.ingestor.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class Product implements Serializable {

    private String productId;
    private String name;
    private String category;
    private BigDecimal price;
    private String manufacturer;
    private Instant createdAt;
    private String dataSource;

    public Product() {
    }

    public Product(String productId, String name, String category, BigDecimal price, String manufacturer, Instant createdAt) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.manufacturer = manufacturer;
        this.createdAt = createdAt;
    }

    public Product(String productId, String name, String category, BigDecimal price, String manufacturer, Instant createdAt, String dataSource) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.manufacturer = manufacturer;
        this.createdAt = createdAt;
        this.dataSource = dataSource;
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
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", manufacturer='" + manufacturer + '\'' +
                ", createdAt=" + createdAt +
                ", dataSource='" + dataSource + '\'' +
                '}';
    }
}
