package com.asg.settings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailPdfTemplateDto {

    private Long templatePoid;
    
    @Size(max = 50)
    private String templateDocId;
    
    @NotBlank
    @Size(max = 200)
    private String templateName;
    
    @NotBlank
    @Size(max = 20)
    private String type;
    
    @Size(max = 1000)
    private String emailSubject;
    
    private String emailContent;
    private String pdfContent;
    
    @Size(max = 4000)
    private String fieldsToUse;
    
    @Size(max = 8000)
    private String sqlQuery;
    
    private Integer seqNo;
    
    @Size(max = 1)
    private String active;
    
    @Size(max = 1000)
    private String remarks;
    
    private String createdBy;
    private java.time.LocalDateTime createdDate;
    private String lastModifiedBy;
    private java.time.LocalDateTime lastModifiedDate;
}
