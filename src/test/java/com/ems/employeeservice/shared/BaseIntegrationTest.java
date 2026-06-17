package com.ems.employeeservice.shared;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "employee-created",
                "employee-status-update"
        },
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

  @SuppressWarnings("resource")
  static final PostgreSQLContainer<?> postgres =
          new PostgreSQLContainer<>("postgres:15")
                  .withDatabaseName("test_db")
                  .withUsername("test")
                  .withPassword("test");

  static {
    postgres.start();
  }

  @DynamicPropertySource
  static void configureProperties(
          DynamicPropertyRegistry registry) {

    registry.add(
            "spring.datasource.url",
            postgres::getJdbcUrl);

    registry.add(
            "spring.datasource.username",
            postgres::getUsername);

    registry.add(
            "spring.datasource.password",
            postgres::getPassword);

    registry.add(
            "spring.datasource.driver-class-name",
            () -> "org.postgresql.Driver");
  }
}