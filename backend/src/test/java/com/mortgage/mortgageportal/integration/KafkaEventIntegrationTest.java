package com.mortgage.mortgageportal.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mortgage.mortgageportal.dto.ApplicationRequestDTO;
import com.mortgage.mortgageportal.event.ApplicationEventPayload;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"loan.applications"})
@ActiveProfiles("test")
class KafkaEventIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private MockMvc mockMvc;
    private Consumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Create Kafka consumer
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        ConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = cf.createConsumer();
        consumer.subscribe(Collections.singletonList("loan.applications"));
    }

    @Test
    @WithMockUser(username = "applicant@example.com", roles = {"APPLICANT"})
    void createApplication_ShouldPublishKafkaEvent() throws Exception {
        // Given
        ApplicationRequestDTO request = ApplicationRequestDTO.builder()
                .amount(new java.math.BigDecimal("100000"))
                .loanTermMonths(240)
                .build();

        // When
        String response = mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then - Verify Kafka event was published
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
        assertThat(records).isNotEmpty();

        ConsumerRecord<String, String> record = records.iterator().next();
        assertThat(record.topic()).isEqualTo("loan.applications");
        assertThat(record.key()).isNotNull();

        // Verify event payload structure
        String eventPayload = record.value();
        assertThat(eventPayload).isNotNull();
        assertThat(eventPayload).contains("application");
        assertThat(eventPayload).contains("metadata");
        assertThat(eventPayload).contains("CREATE");
    }

    @Test
    @WithMockUser(username = "applicant@example.com", roles = {"APPLICANT"})
    void createApplication_ShouldIncludeTraceIdInKafkaHeaders() throws Exception {
        // Given
        ApplicationRequestDTO request = ApplicationRequestDTO.builder()
                .amount(new java.math.BigDecimal("100000"))
                .loanTermMonths(240)
                .build();

        // When
        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then - Verify Kafka headers
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
        assertThat(records).isNotEmpty();

        ConsumerRecord<String, String> record = records.iterator().next();
        assertThat(record.headers()).isNotEmpty();
        
        // Check for traceId header
        boolean hasTraceIdHeader = record.headers().headers("traceId").iterator().hasNext();
        assertThat(hasTraceIdHeader).isTrue();
    }
}
