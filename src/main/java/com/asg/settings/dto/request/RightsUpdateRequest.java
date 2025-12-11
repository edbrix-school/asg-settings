package com.asg.settings.dto.request;

import com.asg.settings.dto.RightUpdateEntry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RightsUpdateRequest {
    @NotEmpty
    @Valid
    private List<RightUpdateEntry> rightsUpdateList;
}
