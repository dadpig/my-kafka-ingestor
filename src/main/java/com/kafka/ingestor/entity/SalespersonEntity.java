package com.kafka.ingestor.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "salespersons", indexes = {
    @Index(name = "idx_salesperson_city", columnList = "city"),
    @Index(name = "idx_salesperson_country", columnList = "country"),
    @Index(name = "idx_salesperson_processed_created", columnList = "processed, created_at")
})
public class SalespersonEntity {

    @Id
    @Column(name = "salesperson_id", length = 50)
    private String salespersonId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "email", nullable = false, length = 200)
    private String email;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "processed", nullable = false)
    private Boolean processed = false;

    public SalespersonEntity() {
    }

    public SalespersonEntity(String salespersonId, String name, String email, String city, String country, Instant createdAt) {
        this.salespersonId = salespersonId;
        this.name = name;
        this.email = email;
        this.city = city;
        this.country = country;
        this.createdAt = createdAt;
        this.processed = false;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SalespersonEntity that = (SalespersonEntity) o;
        return Objects.equals(salespersonId, that.salespersonId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(salespersonId);
    }
}
