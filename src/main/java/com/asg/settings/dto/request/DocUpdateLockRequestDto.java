package com.asg.settings.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocUpdateLockRequestDto {
    @NotBlank
    private String userId;
    @NotBlank
    private String sessionId;
    private String sessionIp;
    private String sessionBrowser;
    private String lastPageVisited;
    private String status;
}
