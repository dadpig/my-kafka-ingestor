package com.kafka.ingestor.repository;

import com.kafka.ingestor.entity.SalespersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalespersonRepository extends JpaRepository<SalespersonEntity, String> {

    List<SalespersonEntity> findByProcessedFalseOrderByCreatedAtAsc();

    @Modifying
    @Query("UPDATE SalespersonEntity s SET s.processed = true WHERE s.salespersonId IN :ids")
    void markAsProcessed(@Param("ids") List<String> ids);

    long countByProcessedFalse();

    List<SalespersonEntity> findByCity(String city);

    List<SalespersonEntity> findByCountry(String country);
}
