package com.asg.settings.dto;

public record TaskUploadDto(
         String taskCategory,
         String taskSubCategory,
         String taskDescription,
         String taskPriority,
         String taskReportedBy,
         String allocatedTo,
         String startDate,
         String dueDate
) {}
