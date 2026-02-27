package com.asg.settings.dto.response;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KpiCompanyMasterResponseDto {

    private Long companyPoid;
    private String companyCode;
    private String companyName;
}



