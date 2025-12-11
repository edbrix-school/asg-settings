package com.asg.settings.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AttachmentUploadDto {
    private MultipartFile file;
    private String remarks;
    private String checklistName;
    private boolean attachEDI;
    private Long attachmentEDIJobPoid;
    private Long seqNo; // Auto-generated if null
}
