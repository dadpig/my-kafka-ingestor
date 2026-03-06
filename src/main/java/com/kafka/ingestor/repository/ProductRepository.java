package com.kafka.ingestor.repository;

import com.kafka.ingestor.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, String> {

    List<ProductEntity> findByProcessedFalseOrderByCreatedAtAsc();

    @Modifying
    @Query("UPDATE ProductEntity p SET p.processed = true WHERE p.productId IN :ids")
    void markAsProcessed(@Param("ids") List<String> ids);

    long countByProcessedFalse();
}
