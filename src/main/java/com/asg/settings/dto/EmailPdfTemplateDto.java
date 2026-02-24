package com.asg.settings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailPdfTemplateDto {

    private Long templatePoid;
    private String templateDocId;
    private String templateName;
    private String type;
    private String emailSubject;
    private String emailContent;
    private String pdfContent;
    private String fieldsToUse;
    private String sqlQuery;
    private Integer seqNo;
    private String active;
    private String remarks;
    private String createdBy;
    private java.sql.Timestamp createdDate;
    private String lastModifiedBy;
    private java.sql.Timestamp lastModifiedDate;
}
