package com.asg.settings.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class UpdateDocumentRequest {
    // Original ticket requirements
    private String transactionDate; // Mandatory from original ticket
    private String approvalStatus;
    private String docDeleted;

    // New FE team requirements
    @Size(max = 20, message = "Doc Id must not exceed 20 characters")
    private String docId;
    @Size(max = 100,message = "Doc Short Name must not exceed 100 characters")
    private String docShortName;
    @Size(max = 20,message = "Doc Short Name 2 must not exceed 20 characters")
    private String docShortName2;
    @Size(max = 100,message = "Doc Name must not exceed 100 characters")
    private String docName;
    @Size(max = 20,message = "Doc Name 2 must not exceed 20 characters")
    private String docName2;
    @Size(max = 20,message = "Module Id must not exceed 20 characters")
    private String moduleId;
    private String dataEntryPeriod;
    @Size(max = 20,message = "Doc Type must not exceed 20 characters")
    private String docType;
    private Boolean isoDocument;
    private Boolean enableSla;
    private Integer duration;
    private String docRevision;
    private String docRevisionDate;
    private Boolean approvalShowInMain;
    @Size(max = 50,message = "Main Table Name must not exceed 50 characters")
    private String mainTableName;
    @Size(max = 100,message = "Send Email Settings must not exceed 100 characters")
    private String sendEmailSettings;
    @Size(max = 500,message = "Send Email Attachments must not exceed 500 characters")
    private String sendEmailAttachments;
    private Boolean glPosting;
    private Boolean inventoryPosting;
    private Boolean uploadEdi;
    private Boolean editableOnSameDay;
    private Boolean hideInMainMenu;
    private Boolean enableCopy;
    @Size(max = 20,message = "Default Save Mode must not exceed 20 characters")
    private String defaultSaveMode;
    @Size(max = 200,message = "Attachment Checklist must not exceed 200 characters")
    private String attachmentChecklist;
    @Size(max = 500,message = "Doc Details must not exceed 500 characters")
    private String docDetails;
    private Boolean active;
    private Integer seqNo;
    private Map<String, Object> otherFields;
    private Boolean print;
    private Boolean preview;

    @Size(max = 20, message = "Default List Period must not exceed 20 characters")
    private String defaultListPeriod;

    @Size(max = 500, message = "Doc Info Fields SQL must not exceed 500 characters")
    private String docInfoFieldsSql;

    @Size(max = 4000, message = "List Of Records SQL must not exceed 4000 characters")
    private String listOfRecordsSql;

    @Size(max = 4000, message = "List Of Display Columns And Types must not exceed 4000 characters")
    private String listOfDisplayColumnsAndTypes;
    @Size(max = 200, message = "User Roles must not exceed 200 characters")
    private String userRoles;

    @Size(max = 200, message = "Approval Info Fields must not exceed 200 characters")
    private String approvalInfoFields;

    @Size(max = 50, message = "Approval View Report File must not exceed 50 characters")
    private String approvalViewRptFile;

    @Size(max = 100, message = "Doc Key Field must not exceed 100 characters")
    private String docKeyField;

    @Size(max = 500, message = "Auto Refresh Fields must not exceed 500 characters")
    private String autoRefreshFields;


}

