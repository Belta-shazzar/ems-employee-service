package com.ems.employeeservice.kafka;

import com.ems.employeeservice.event.EmployeeCreatedEvent;
import com.ems.employeeservice.event.EmployeeStatusUpdateEvent;
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

  @Value("${kafka.topic.employee-status-update}")
  private String employeeStatusUpdateTopic;

  public void publishEmployeeCreatedEvent(EmployeeCreatedEvent event) {
    log.info("Publishing employee created event for employee: {}", event.getEmployeeId());

//    Round-robin partition publishing
    kafkaTemplate.send(employeeCreatedTopic, event);

//    Key-based partition publishing
//    kafkaTemplate.send(employeeCreatedTopic, event.getEmployeeId().toString(), event);
    log.info("Employee created event published successfully");
  }

  public void publishEmployeeStatusUpdateEvent(EmployeeStatusUpdateEvent event) {
    log.info("Publishing employee status update event for employee: {}", event.getEmployeeId());
    kafkaTemplate.send(employeeStatusUpdateTopic, event);
    log.info("Employee status update event published successfully");
  }
}
