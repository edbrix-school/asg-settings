package com.asg.settings.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Attachment data returned to clients")
public class AttachmentDto {
   // private Long attachmentId;          // SEQNO
    private Long seqNo;
    private Long groupPoid;
    private Long docKeyPoid;
    private Long companyPoid;
    private String docId;
    private String originalFileName;    // FILE_NAME
    private String storedFileName;      // FILE_NAME_MAPPED
    private String fileType;            // derived from extension (optional)
    private String remarks;             // FILE_REMARKS
    private String checklistName;       // CHECKLIST_NAME
    private String uploadedBy;          // CREATED_BY
    private String createdDate;         // ISO string
    private boolean active;             // ACTIVE == "Y"
    private boolean deleted;            // DELETED == "N" -> false
}
