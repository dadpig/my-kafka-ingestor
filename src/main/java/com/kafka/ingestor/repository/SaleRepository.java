package com.kafka.ingestor.repository;

import com.kafka.ingestor.entity.SaleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<SaleEntity, String> {

    List<SaleEntity> findByProcessedFalseOrderByCreatedAtAsc();

    @Modifying
    @Query("UPDATE SaleEntity s SET s.processed = true WHERE s.saleId IN :ids")
    void markAsProcessed(@Param("ids") List<String> ids);

    long countByProcessedFalse();
}
