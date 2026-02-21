package com.asg.settings.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KpiEmployeeMasterResponseDto {

    private Long employeePoid;
    private String employeeCode;
    private String employeeName;
}
