package com.kafka.ingestor.controller;

import com.kafka.ingestor.repository.CustomerRepository;
import com.kafka.ingestor.repository.ProductRepository;
import com.kafka.ingestor.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(MonitoringController.class)
@ActiveProfiles("test")
class MonitoringControllerTest {

    @MockBean
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private SaleRepository saleRepository;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void shouldReturnStatusSuccessfully() throws Exception {
        when(customerRepository.count()).thenReturn(50L);
        when(customerRepository.countByProcessedFalse()).thenReturn(5L);
        when(productRepository.count()).thenReturn(30L);
        when(productRepository.countByProcessedFalse()).thenReturn(3L);
        when(saleRepository.count()).thenReturn(100L);
        when(saleRepository.countByProcessedFalse()).thenReturn(10L);

        mockMvc.perform(get("/api/monitoring/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customers.total").value(50))
            .andExpect(jsonPath("$.customers.pending").value(5))
            .andExpect(jsonPath("$.products.total").value(30))
            .andExpect(jsonPath("$.products.pending").value(3))
            .andExpect(jsonPath("$.sales.total").value(100))
            .andExpect(jsonPath("$.sales.pending").value(10));
    }

    @Test
    void shouldReturnHealthSuccessfully() throws Exception {
        mockMvc.perform(get("/api/monitoring/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.application").value("kafka-stream-ingestor"));
    }
}
