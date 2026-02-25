package com.asg.settings.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KpiLineMasterResponseDto {

    private Long linePoid;
    private String lineCode;
    private String lineName;
}
