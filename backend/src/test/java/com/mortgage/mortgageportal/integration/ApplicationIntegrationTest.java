package com.mortgage.mortgageportal.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mortgage.mortgageportal.dto.ApplicationRequestDTO;
import com.mortgage.mortgageportal.dto.ApplicationResponseDTO;
import com.mortgage.mortgageportal.enums.ApplicationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class ApplicationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "applicant@example.com", roles = {"APPLICANT"})
    void createApplication_ShouldCreateApplicationSuccessfully() throws Exception {
        // Given
        ApplicationRequestDTO request = ApplicationRequestDTO.builder()
                .amount(new BigDecimal("100000"))
                .loanTermMonths(240)
                .build();

        // When & Then
        String response = mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value("100000"))
                .andExpect(jsonPath("$.loanTermMonths").value(240))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApplicationResponseDTO responseDTO = objectMapper.readValue(response, ApplicationResponseDTO.class);
        assertThat(responseDTO.getId()).isNotNull();
        assertThat(responseDTO.getAmount()).isEqualTo(new BigDecimal("100000"));
        assertThat(responseDTO.getLoanTermMonths()).isEqualTo(240);
        assertThat(responseDTO.getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    @WithMockUser(username = "applicant@example.com", roles = {"APPLICANT"})
    void createApplication_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Given
        ApplicationRequestDTO request = ApplicationRequestDTO.builder()
                .amount(new BigDecimal("-1000")) // Invalid negative amount
                .loanTermMonths(-12) // Invalid negative term
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    @WithMockUser(username = "officer@example.com", roles = {"OFFICER"})
    void listApplications_ShouldReturnApplications_WhenOfficerAccess() throws Exception {
        // Given - Create an application first
        ApplicationRequestDTO request = ApplicationRequestDTO.builder()
                .amount(new BigDecimal("100000"))
                .loanTermMonths(240)
                .build();

        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    @WithMockUser(username = "applicant@example.com", roles = {"APPLICANT"})
    void listApplications_ShouldReturnForbidden_WhenApplicantAccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "applicant@example.com", roles = {"APPLICANT"})
    void getApplication_ShouldReturnApplication_WhenExists() throws Exception {
        // Given - Create an application first
        ApplicationRequestDTO request = ApplicationRequestDTO.builder()
                .amount(new BigDecimal("100000"))
                .loanTermMonths(240)
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApplicationResponseDTO createdApp = objectMapper.readValue(createResponse, ApplicationResponseDTO.class);

        // When & Then
        mockMvc.perform(get("/api/v1/applications/{id}", createdApp.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(createdApp.getId().toString()))
                .andExpect(jsonPath("$.amount").value("100000"))
                .andExpect(jsonPath("$.loanTermMonths").value(240));
    }

    @Test
    @WithMockUser(username = "applicant@example.com", roles = {"APPLICANT"})
    void getApplication_ShouldReturnNotFound_WhenApplicationDoesNotExist() throws Exception {
        // Given
        String nonExistentId = "550e8400-e29b-41d4-a716-446655440000";

        // When & Then
        mockMvc.perform(get("/api/v1/applications/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").exists());
    }
}
