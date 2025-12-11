package com.asg.settings.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateParameterRequestDTO {
    @NotNull
    private Long loginUserPoid;
    @NotEmpty
    private List<@Valid UpdateParameterDTO> parameters;
}
