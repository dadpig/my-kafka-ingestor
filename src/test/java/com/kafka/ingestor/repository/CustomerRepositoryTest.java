package com.kafka.ingestor.repository;

import com.kafka.ingestor.entity.CustomerEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CustomerRepositoryTest {

    @MockBean
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldFindUnprocessedCustomers() {
        CustomerEntity customer1 = new CustomerEntity("CUST001", "Customer 1", "test1@example.com",
            "Premium", "North", Instant.now());
        customer1.setProcessed(false);

        CustomerEntity customer2 = new CustomerEntity("CUST002", "Customer 2", "test2@example.com",
            "Standard", "South", Instant.now());
        customer2.setProcessed(true);

        entityManager.persist(customer1);
        entityManager.persist(customer2);
        entityManager.flush();

        List<CustomerEntity> unprocessed = customerRepository.findByProcessedFalseOrderByCreatedAtAsc();

        assertEquals(1, unprocessed.size());
        assertEquals("CUST001", unprocessed.get(0).getCustomerId());
    }

    @Test
    void shouldCountUnprocessedCustomers() {
        CustomerEntity customer1 = new CustomerEntity("CUST001", "Customer 1", "test1@example.com",
            "Premium", "North", Instant.now());
        customer1.setProcessed(false);

        CustomerEntity customer2 = new CustomerEntity("CUST002", "Customer 2", "test2@example.com",
            "Standard", "South", Instant.now());
        customer2.setProcessed(false);

        entityManager.persist(customer1);
        entityManager.persist(customer2);
        entityManager.flush();

        long count = customerRepository.countByProcessedFalse();

        assertEquals(2, count);
    }

    @Test
    void shouldMarkCustomersAsProcessed() {
        CustomerEntity customer1 = new CustomerEntity("CUST001", "Customer 1", "test1@example.com",
            "Premium", "North", Instant.now());
        customer1.setProcessed(false);

        CustomerEntity customer2 = new CustomerEntity("CUST002", "Customer 2", "test2@example.com",
            "Standard", "South", Instant.now());
        customer2.setProcessed(false);

        entityManager.persist(customer1);
        entityManager.persist(customer2);
        entityManager.flush();

        customerRepository.markAsProcessed(List.of("CUST001"));
        entityManager.clear();

        CustomerEntity updated = customerRepository.findById("CUST001").orElse(null);
        assertNotNull(updated);
        assertTrue(updated.getProcessed());

        CustomerEntity notUpdated = customerRepository.findById("CUST002").orElse(null);
        assertNotNull(notUpdated);
        assertFalse(notUpdated.getProcessed());
    }
}
