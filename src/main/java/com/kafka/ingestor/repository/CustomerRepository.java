package com.kafka.ingestor.repository;

import com.kafka.ingestor.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, String> {

    List<CustomerEntity> findByProcessedFalseOrderByCreatedAtAsc();

    @Modifying
    @Query("UPDATE CustomerEntity c SET c.processed = true WHERE c.customerId IN :ids")
    void markAsProcessed(@Param("ids") List<String> ids);

    long countByProcessedFalse();
}
