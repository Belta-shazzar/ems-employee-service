package com.ems.employeeservice.kafka;

import com.ems.employeeservice.event.EmployeeCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeEventProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Value("${kafka.topic.employee-created}")
  private String employeeCreatedTopic;

  public void publishEmployeeCreatedEvent(EmployeeCreatedEvent event) {
    log.info("Publishing employee created event for employee: {}", event.getEmployeeId());
    kafkaTemplate.send(employeeCreatedTopic, event.getEmployeeId().toString(), event);
    log.info("Employee created event published successfully");
  }
}
