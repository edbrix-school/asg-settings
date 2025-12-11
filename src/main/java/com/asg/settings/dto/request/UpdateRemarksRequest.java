package com.asg.settings.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request to update remarks")
public class UpdateRemarksRequest {
    private String docId;            // Document identifier
    private Long docKeyPoid;        // Document key POID
    private Long seqNo;             // Sequence number
    private String originalFileName; // Original file name
    private String fileNameMapped;   // Mapped file name (ASG_xxx.pdf)
    private String remarks;          // Remarks to update
    private String checklistName;    // Checklist value to update
    private String updatedBy;        // User performing the update
}
