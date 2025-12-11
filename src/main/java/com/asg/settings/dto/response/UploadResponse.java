package com.asg.settings.dto.response;

import com.asg.settings.dto.AttachmentDto;
import lombok.Data;

import java.util.List;

@Data
public class UploadResponse {
    private final List<AttachmentDto> uploadedFiles;
    private final List<String> errors;
    private final boolean hasErrors;

    public UploadResponse(List<AttachmentDto> uploadedFiles, List<String> errors) {
        this.uploadedFiles = uploadedFiles;
        this.errors = errors;
        this.hasErrors = errors != null && !errors.isEmpty();
    }
}
