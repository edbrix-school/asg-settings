package com.asg.settings.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoadDefaultRightsRequest {
    @NotNull
    private Long loginUserPoid;

    @NotNull
    private Long userRolePoid;
}
