package com.asg.settings.dto;

import com.asg.common.lib.dto.LovGetListDto;
import com.asg.settings.entity.DocMasterAuthDtlEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
public class DocumentDto {
    private String docId;
    private String docShortName;
    private String docShortName2;
    private String docName;
    private String docName2;
    private LovGetListDto module;
    private String docType;
    private String isoDocument;
    private BigDecimal docRevision;
    private Date docRevisionDate;
    private byte[] docIcon;
    private String docDetails;
    private String taskflowUrl;
    private Integer seqno;
    private String createdBy;
    private Timestamp createdDate;
    private String lastModifiedBy;
    private Timestamp lastModifiedDate;
    private BigDecimal docPoid;
    private String userRoles;
    private String approvalRequired;
    private BigDecimal approvalRemHrs;
    private BigDecimal approvalAutoHrs;
    private String alertOnCreate;
    private String alertUserRolePoid;
    private String alertConditionalProc;
    private String active;
    private String approvalInfoFields;
    private String approvalViewRptFile;
    private String dataEntryPeriod;
    private String defaultListPeriod;
    private String deleted;
    private String attachmentChecklist;
    private String approvalCustomRule;
    private String docInfoFieldsSql;
    private String approvalAlerts;
    private String defaultSaveMode;
    private String docPrefix;
    private String approvalShowInMain;
    private String glPosting;
    private String glPostingDateField;
    private String mainTableName;
    private String editableOnSameDay;
    private String hideInMainMenu;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String listOfRecordsSql;
    private String docKeyField;
    private String docReturnFields;
    private String isoDocumentNo;
    private String isoIssueNo;
    private String docValidationFields;
    private String docKeyFieldName;
    private String autoRefreshFields;
    private String inventoryDocument;
    private String inventoryPosting;
    private String listOfDisplayColumnsAndTypes;
    private String uploadEdi;
    private String print;
    private String preview;
    private String enableSla;
    private String sendEmailSettings;
    private String sendEmailAttachments;
    private String enableCopy;
    private Long duration;
    private List<DocMasterApprovalDtlDto> documentApprovalDetails;
    private List<DocMasterAuthDtlEntity> documentAuthDetails;
}