package org.hospitalqueing.model;

public record TicketStatus(
    String token,
    int queueId,
    int queueNumber,
    String departmentName,
    String serviceName,
    String status,
    String priorityType,
    Integer currentNumber,
    int patientsAhead,
    int estimatedWaitMinutes,
    String counterName,
    String joinedAt,
    String calledAt
) {}
