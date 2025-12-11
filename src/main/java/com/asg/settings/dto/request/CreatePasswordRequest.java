package com.asg.settings.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePasswordRequest {
    @NotNull(message = "User Poid is required")
    private Long userPoid;
}
