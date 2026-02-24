package com.asg.settings.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.DropdownStringDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.entity.DocumentEntity;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.repository.TableMetaRepository;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.LovDataService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.settings.dto.ApprovalActionRequest;
import com.asg.settings.dto.DocMasterApprovalDtlDto;
import com.asg.settings.dto.DocumentDto;
import com.asg.settings.dto.request.UpdateDocumentRequest;
import com.asg.settings.entity.DocMasterApprovalDtlEntity;
import com.asg.settings.entity.DocMasterAuthDtlEntity;
import com.asg.settings.entity.RoleEntity;
import com.asg.settings.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.asg.common.lib.security.util.UserContext.getUserId;


@CommonsLog
@Service
public class DocumentService {

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    DocumentApprovalDtlRepository documentApprovalDtlRepository;

    @Autowired
    DocumentAuthDtlRepository documentAuthDtlRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    TableMetaRepository tableMetaRepository;

    @Autowired
    LovDataService lovService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    LoggingService loggingService;

    @Autowired
    DocumentSearchService documentSearchService;

    @Autowired
    DocumentDeleteService documentDeleteService;

    public DocumentDto getDocumentById(String docId, Boolean includeSql) {
        DocumentEntity document = documentRepository.findByDocId(docId);
        if (document == null) {
            throw new ResourceNotFoundException("Document", "docId", docId);
        }

        DocumentDto documentDto = convertToDto(document);

        List<DocMasterApprovalDtlEntity> docApprDtl = documentApprovalDtlRepository.findAllById_DocId(document.getDocId());
        List<DocMasterApprovalDtlDto> docMasterApprovalDtlDtos = new ArrayList<>();

        docApprDtl.forEach(docMasterApprovalDtlEntity -> {
            DocMasterApprovalDtlDto docMasterApprovalDtlDto = new DocMasterApprovalDtlDto();
            docMasterApprovalDtlDto.setApprovalLevel(docMasterApprovalDtlEntity.getApprovalLevel());

            // ✅ Handle user role safely
            RoleEntity userRole = roleRepository.findByUserRolePoid(docMasterApprovalDtlEntity.getUserRolePoid());
            Map<String, String> userRolePoids = new HashMap<>();
            if (userRole != null) {
                userRolePoids.put("id", userRole.getUserRolePoid().toString());
                userRolePoids.put("name", userRole.getUserRoleName());
            } else {
                userRolePoids.put("id", String.valueOf(docMasterApprovalDtlEntity.getUserRolePoid()));
                userRolePoids.put("name", "N/A");
            }
            docMasterApprovalDtlDto.setUserRolePoid(userRolePoids);

            // ✅ Handle alternate role safely
            RoleEntity alternateUserRole = null;
            if (docMasterApprovalDtlEntity.getAlternateUserRolePoid() != null) {
                alternateUserRole = roleRepository.findByUserRolePoid(docMasterApprovalDtlEntity.getAlternateUserRolePoid());
            }

            Map<String, String> alternateUserRolePoids = new HashMap<>();
            if (alternateUserRole != null) {
                alternateUserRolePoids.put("id", alternateUserRole.getUserRolePoid().toString());
                alternateUserRolePoids.put("name", alternateUserRole.getUserRoleName());
            } else {
                alternateUserRolePoids.put("id", "");
                alternateUserRolePoids.put("name", "");
            }

            docMasterApprovalDtlDto.setAlternateUserRolePoid(alternateUserRolePoids);

            docMasterApprovalDtlDtos.add(docMasterApprovalDtlDto);
        });

        List<DocMasterAuthDtlEntity> docAuthDtl = documentAuthDtlRepository.findAllById_DocId(document.getDocId());
        documentDto.setDocumentApprovalDetails(docMasterApprovalDtlDtos);
        documentDto.setDocumentAuthDetails(docAuthDtl);

        // Hide SQL fields if includeSql is false
        if (!Boolean.TRUE.equals(includeSql)) {
            documentDto.setListOfRecordsSql(null);
            documentDto.setDocInfoFieldsSql(null);
        }

        return documentDto;
    }

    // Used by search method for filtering
    public List<String> getSearchableFieldNames(DocumentEntity doc) {
        String sql = doc.getListOfRecordsSql();
        return (sql != null && !sql.isBlank())
                ? tableMetaRepository.getColumnsFromSql(sql)
                : tableMetaRepository.getColumnsFromTable(doc.getMainTableName());
    }

    // Used by controller for dropdown
    public List<DropdownStringDto> getSearchableFieldsForDropdown(String docId) {
        DocumentEntity doc = getDocument(docId);
        List<String> columnNames = getSearchableFieldNames(doc);

        List<DropdownStringDto> dropdownDtos = new ArrayList<>();
        DropdownStringDto dropdownGen = new DropdownStringDto();
        dropdownGen.setLabel("Generic Search");
        dropdownGen.setValue("GLOBALSEARCH");

        dropdownDtos.add(dropdownGen);

        columnNames.forEach(columnName -> {
            DropdownStringDto dropdown = new DropdownStringDto();
            dropdown.setLabel(columnName.replace("_", " "));
            dropdown.setValue(columnName);

            dropdownDtos.add(dropdown);
        });
        return dropdownDtos;
    }

    // ----------------- helpers -----------------

    private DocumentEntity getDocument(String docId) {
        return documentRepository.findByDocId(docId);
    }

    @Transactional
    public void deleteDocument(String docId, DeleteReasonDto deleteReasonDto) {
        // Check if a document exists
        Map<String, Map<String, Boolean>> userRights = loadUserRights(getUserId());
        DocumentEntity document = documentRepository.findByDocId(docId);

        if (userRights.get(docId).get("DELETE")) {
            if (document == null) {
                throw new ValidationException("Document not found with id: " + docId);
            }

            if ("Y".equalsIgnoreCase(document.getDeleted())) {
                throw new ValidationException("Document with id " + docId + " is already deleted.");
            }
            
            documentDeleteService.deleteDocument(
                    document.getDocPoid().longValue(),
                    "GLOBAL_DOC_MASTER",
                    "DOC_ID",
                    deleteReasonDto,
                    null
            );
        }
    }

    Map<String, Map<String, Boolean>> loadUserRights(String userId) {
        try {
            String sql = "{ call PROC_GLOB_USR_RIGHTS_APPSTART(?, ?) }";

            return jdbcTemplate.execute(
                    (CallableStatementCreator) con -> {
                        CallableStatement cs = con.prepareCall(sql);
                        cs.setString(1, userId);
                        cs.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);
                        return cs;
                    },
                    (CallableStatementCallback<Map<String, Map<String, Boolean>>>) cs -> {
                        cs.execute();

                        try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                            BigDecimal userPoid = BigDecimal.ZERO;
                            Map<String, Map<String, Boolean>> userRights = new HashMap<>();

                            while (rs.next()) {
                                userPoid = rs.getBigDecimal("USER_POID");
                                String docId = rs.getString("DOC_ID");
                                String rights = rs.getString("RIGHTS");
                                System.out.println(rights);

                                Map<String, Boolean> docRights = new HashMap<>();
                                docRights.put("VIEW", rights.charAt(0) == '1');
                                docRights.put("CREATE", rights.charAt(1) == '1');
                                docRights.put("EDIT", rights.charAt(2) == '1');
                                docRights.put("DELETE", rights.charAt(3) == '1');
                                docRights.put("PRINT", rights.charAt(4) == '1');
                                docRights.put("EMAIL", rights.charAt(5) == '1');
                                userRights.put(docId, docRights);
                            }
                            return userRights;
                        }
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public List<DocumentDto> getInactiveAndDeletedDocuments() {
        List<DocumentEntity> inactiveOrDeletedDocuments = documentRepository.findAllByActiveAndDeleted("N", "Y");

        return inactiveOrDeletedDocuments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private DocumentDto convertToDto(DocumentEntity entity) {
        if (entity == null) {
            return null;
        }
        DocumentDto dto = new DocumentDto();

        dto.setDocId(entity.getDocId());
        dto.setDocShortName(entity.getDocShortName());
        dto.setDocShortName2(entity.getDocShortName2());
        dto.setDocName(entity.getDocName());
        dto.setDocName2(entity.getDocName2());

        if (entity.getModuleId() != null) {
            dto.setModule(lovService.getDetailsByPoidAndLovName(Long.valueOf(entity.getModuleId()),"MODULE"));
        }

        dto.setDocType(entity.getDocType());
        dto.setIsoDocument(entity.getIsoDocument());
        dto.setDocRevision(entity.getDocRevision());

        dto.setDocRevisionDate(entity.getDocRevisionDate());

        dto.setDocIcon(entity.getDocIcon());
        dto.setDocDetails(entity.getDocDetails());
        dto.setTaskflowUrl(entity.getTaskflowUrl());
        dto.setSeqno(entity.getSeqno());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedDate(entity.getLastModifiedDate());
        dto.setDocPoid(entity.getDocPoid());
        dto.setUserRoles(entity.getUserRoles());
        dto.setApprovalRequired(entity.getApprovalRequired());
        dto.setApprovalRemHrs(entity.getApprovalRemHrs());
        dto.setApprovalAutoHrs(entity.getApprovalAutoHrs());
        dto.setAlertOnCreate(entity.getAlertOnCreate());
        dto.setAlertUserRolePoid(entity.getAlertUserRolePoid());
        dto.setAlertConditionalProc(entity.getAlertConditionalProc());
        dto.setActive(entity.getActive());
        dto.setApprovalInfoFields(entity.getApprovalInfoFields());
        dto.setApprovalViewRptFile(entity.getApprovalViewRptFile());
        dto.setDataEntryPeriod(entity.getDataEntryPeriod());
        dto.setDefaultListPeriod(entity.getDefaultListPeriod());
        dto.setDeleted(entity.getDeleted());
        dto.setAttachmentChecklist(entity.getAttachmentChecklist());
        dto.setApprovalCustomRule(entity.getApprovalCustomRule());
        dto.setDocInfoFieldsSql(entity.getDocInfoFieldsSql());
        dto.setApprovalAlerts(entity.getApprovalAlerts());
        dto.setDefaultSaveMode(entity.getDefaultSaveMode());
        dto.setDocPrefix(entity.getDocPrefix());
        dto.setApprovalShowInMain(entity.getApprovalShowInMain());
        dto.setGlPosting(entity.getGlPosting());
        dto.setGlPostingDateField(entity.getGlPostingDateField());
        dto.setMainTableName(entity.getMainTableName());
        dto.setEditableOnSameDay(entity.getEditableOnSameDay());
        dto.setHideInMainMenu(entity.getHideInMainMenu());
        dto.setListOfRecordsSql(entity.getListOfRecordsSql());
        dto.setListOfDisplayColumnsAndTypes(entity.getListOfDisplayColumnsAndTypes());
        dto.setDocKeyField(entity.getDocKeyField());
        dto.setDocReturnFields(entity.getDocReturnFields());
        dto.setIsoDocumentNo(entity.getIsoDocumentNo());
        dto.setIsoIssueNo(entity.getIsoIssueNo());
        dto.setDocValidationFields(entity.getDocValidationFields());
        dto.setDocKeyFieldName(entity.getDocKeyFieldName());
        dto.setAutoRefreshFields(entity.getAutoRefreshFields());
        dto.setInventoryDocument(entity.getInventoryDocument());
        dto.setInventoryPosting(entity.getInventoryPosting());
        dto.setUploadEdi(entity.getUploadEdi());
        dto.setPrint(entity.getPrint());
        dto.setPreview(entity.getPreview());
        dto.setEnableSla(entity.getEnableSla());
        dto.setSendEmailSettings(entity.getSendEmailSettings());
        dto.setSendEmailAttachments(entity.getSendEmailAttachments());
        dto.setEnableCopy(entity.getEnableCopy());
        dto.setDuration(entity.getDuration());
        return dto;
    }

    @Transactional
    public Map<String, Object> performApprovalAction(String docId, ApprovalActionRequest request) {
        if (request.getDocKeyPoid() == null || request.getAction() == null) {
            throw new ValidationException("Doc Key Poid and action are required");
        }

        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery("PROC_GLOB_APPROVAL_ACTION")
                .registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_DOC_ID", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_DOC_KEY_POID", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_ACTION", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_ACTION_MESSAGE", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_DOC_SUMMARY_INFO", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_DOC_REF", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_DOC_DATE", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_SUBMIT_TO_USER_POID", Long.class, ParameterMode.IN)
                .registerStoredProcedureParameter("P_ACTION_RESULT", String.class, ParameterMode.OUT)
                .registerStoredProcedureParameter("P_ACTION_RESULT_USER_POID_LIST", String.class, ParameterMode.OUT)
                .registerStoredProcedureParameter("P_APPROVAL_POID", Long.class, ParameterMode.OUT)

                .setParameter("P_LOGIN_GROUP_POID", 1L)
                .setParameter("P_LOGIN_COMPANY_POID", 1L)
                .setParameter("P_LOGIN_USER_POID", 100L)
                .setParameter("P_DOC_ID", docId)
                .setParameter("P_DOC_KEY_POID", request.getDocKeyPoid().toString())
                .setParameter("P_ACTION", request.getAction())
                .setParameter("P_ACTION_MESSAGE", request.getComments())
                .setParameter("P_DOC_SUMMARY_INFO", null)
                .setParameter("P_DOC_REF", null)
                .setParameter("P_DOC_DATE", null)
                .setParameter("P_SUBMIT_TO_USER_POID", null);

        query.execute();

        Map<String, Object> result = new HashMap<>();
        result.put("actionResult", query.getOutputParameterValue("P_ACTION_RESULT"));
        result.put("userPoidList", query.getOutputParameterValue("P_ACTION_RESULT_USER_POID_LIST"));
        result.put("approvalPoid", query.getOutputParameterValue("P_APPROVAL_POID"));

        return result;
    }

    @Transactional
    public Map<String, Object> updateDocument(Long documentKeyPoid, UpdateDocumentRequest request) {

        // Validate mandatory fields from original ticket
        if (documentKeyPoid == null) {
            throw new ValidationException("Document Key Poid is required");
        }

        // Find existing document
        DocumentEntity document = documentRepository.findByDocPoid(documentKeyPoid);
        if (document == null) {
            throw new ValidationException("Document not found for Document Key Poid: " + documentKeyPoid);
        }
        DocumentEntity oldDocument = new DocumentEntity();
        BeanUtils.copyProperties(document, oldDocument);

        // Check deleted flag (original requirement)
        if ("Y".equalsIgnoreCase(document.getDeleted()) || "Y".equalsIgnoreCase(request.getDocDeleted())) {
            throw new ValidationException("Document has been deleted and cannot be edited.");
        }

        // Call PROC_GLOBAL_DOC_EDIT_RIGHT_GET
        // checkEditRights(documentKeyPoid, document.getDocId()); // Temporarily disabled

        // Approval status validation (original requirement) - Skip if null/empty
        String approvalStatus = request.getApprovalStatus() != null ? request.getApprovalStatus() : document.getApprovalRequired();
        if (approvalStatus != null && !approvalStatus.trim().isEmpty() &&
                !(approvalStatus.equalsIgnoreCase("DRAFT") || approvalStatus.equalsIgnoreCase("OPEN") ||
                        approvalStatus.equalsIgnoreCase("D") || approvalStatus.equalsIgnoreCase("O"))) {
            throw new ValidationException("Document approval status does not allow editing.");
        }

        // Run validation procedures (original requirements)
        // Run validation procedures (original requirements) - TEMPORARILY DISABLED
        // TODO: Enable after DB team fixes stored procedure signatures
        /*
        try {
            runValidationProc("PROC_DOC_BEFORE_SAVE", documentKeyPoid, document.getDocId());
            runValidationProc("PROC_APPROVAL_EDIT_VALIDATE", documentKeyPoid, document.getDocId());
            runValidationProc("PROC_VALIDATE_DOCUMENT", documentKeyPoid, document.getDocId());
            runValidationProc("PROC_VALIDATE_ATTACHMENT_CHECKLIST", documentKeyPoid, document.getDocId());
        } catch (Exception e) {
            throw new ValidationException("Validation failed: " + e.getMessage());
        }
        */

        if (StringUtils.isNotBlank(request.getApprovalStatus())) {
            // Convert approval status to single character (DB constraint: max 1 char)
            String approvalChar = "N"; // Default
            if ("DRAFT".equalsIgnoreCase(request.getApprovalStatus())) approvalChar = "D";
            else if ("OPEN".equalsIgnoreCase(request.getApprovalStatus())) approvalChar = "O";
            else if ("APPROVED".equalsIgnoreCase(request.getApprovalStatus())) approvalChar = "A";
            else if ("REJECTED".equalsIgnoreCase(request.getApprovalStatus())) approvalChar = "R";
            document.setApprovalRequired(approvalChar);
        }
        if (StringUtils.isNotBlank(request.getDocDeleted())) document.setDeleted(request.getDocDeleted());

        // Update new FE team fields with proper type conversions
        if (StringUtils.isNotBlank(request.getDocId())) document.setDocId(request.getDocId());
        if (StringUtils.isNotBlank(request.getDocShortName())) document.setDocShortName(request.getDocShortName());
        if (StringUtils.isNotBlank(request.getDocShortName2())) document.setDocShortName2(request.getDocShortName2());
        if (StringUtils.isNotBlank(request.getDocName())) document.setDocName(request.getDocName());
        if (StringUtils.isNotBlank(request.getDocName2())) document.setDocName2(request.getDocName2());
        if (StringUtils.isNotBlank(request.getModuleId())) document.setModuleId(request.getModuleId());
        if (StringUtils.isNotBlank(request.getDataEntryPeriod()))
            document.setDataEntryPeriod(request.getDataEntryPeriod());
        if (StringUtils.isNotBlank(request.getDocType())) document.setDocType(request.getDocType());
        if (request.getIsoDocument() != null) document.setIsoDocument(request.getIsoDocument() ? "Y" : "N");
        if (request.getEnableSla() != null) document.setEnableSla(request.getEnableSla() ? "Y" : "N");

        if (Boolean.TRUE.equals(request.getEnableSla())
                && request.getDuration() == null) {
            throw new ValidationException("Duration is required");
        }
        if (request.getDuration() != null) document.setDuration(request.getDuration().longValue());
        if (StringUtils.isNotBlank(request.getDocRevision()))
            document.setDocRevision(new BigDecimal(request.getDocRevision()));
        if (StringUtils.isNotBlank(request.getDocRevisionDate())) {
            try {
                document.setDocRevisionDate(LocalDate.parse(request.getDocRevisionDate(), DateTimeFormatter.ISO_LOCAL_DATE));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid docRevisionDate format: " + request.getDocRevisionDate());
            }
        }
        if (request.getApprovalShowInMain() != null)
            document.setApprovalShowInMain(request.getApprovalShowInMain() ? "Y" : "N");
        if (StringUtils.isNotBlank(request.getMainTableName())) document.setMainTableName(request.getMainTableName());
        if (StringUtils.isNotBlank(request.getSendEmailSettings()))
            document.setSendEmailSettings(request.getSendEmailSettings());
        if (StringUtils.isNotBlank(request.getSendEmailAttachments()))
            document.setSendEmailAttachments(request.getSendEmailAttachments());
        if (request.getGlPosting() != null) document.setGlPosting(request.getGlPosting() ? "Y" : "N");
        if (request.getInventoryPosting() != null)
            document.setInventoryPosting(request.getInventoryPosting() ? "Y" : "N");
        if (request.getUploadEdi() != null) document.setUploadEdi(request.getUploadEdi() ? "Y" : "N");
        if (request.getEditableOnSameDay() != null)
            document.setEditableOnSameDay(request.getEditableOnSameDay() ? "Y" : "N");
        if (request.getHideInMainMenu() != null) document.setHideInMainMenu(request.getHideInMainMenu() ? "Y" : "N");
        if (request.getEnableCopy() != null) document.setEnableCopy(request.getEnableCopy() ? "Y" : "N");
        if (StringUtils.isNotBlank(request.getDefaultListPeriod())) {
            document.setDefaultListPeriod(request.getDefaultListPeriod());
        }
        if (StringUtils.isNotBlank(request.getDefaultSaveMode()))
            document.setDefaultSaveMode(request.getDefaultSaveMode());
        if (StringUtils.isNotBlank(request.getAttachmentChecklist()))
            document.setAttachmentChecklist(request.getAttachmentChecklist());
        if (StringUtils.isNotBlank(request.getDocDetails())) document.setDocDetails(request.getDocDetails());
        if (request.getActive() != null) document.setActive(request.getActive() ? "Y" : "N");
        if (request.getSeqNo() != null) document.setSeqno(request.getSeqNo());
        if (request.getPrint() != null)
            document.setPrint(request.getPrint() ? "Y" : "N");
        if (request.getPreview() != null)
            document.setPreview(request.getPreview() ? "Y" : "N");

        if (StringUtils.isNotBlank(request.getUserRoles()))
            document.setUserRoles(request.getUserRoles());

        if (StringUtils.isNotBlank(request.getApprovalInfoFields()))
            document.setApprovalInfoFields(request.getApprovalInfoFields());

        if (StringUtils.isNotBlank(request.getApprovalViewRptFile()))
            document.setApprovalViewRptFile(request.getApprovalViewRptFile());

        if (StringUtils.isNotBlank(request.getDocKeyField()))
            document.setDocKeyField(request.getDocKeyField());

        if (StringUtils.isNotBlank(request.getAutoRefreshFields()))
            document.setAutoRefreshFields(request.getAutoRefreshFields());

        if (StringUtils.isNotBlank(request.getDocInfoFieldsSql()))
            document.setDocInfoFieldsSql(request.getDocInfoFieldsSql());


        // Other fields for flexibility
        if (request.getOtherFields() != null) {
            request.getOtherFields().forEach((key, value) -> {
                try {
                    String setter = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
                    var method = DocumentEntity.class.getMethod(setter, value.getClass());
                    method.invoke(document, value);
                } catch (Exception ignored) {
                    // ignore invalid fields
                }
            });
        }

        // Save updated document
        try {
            callBeforeSaveProc(document.getDocId(), documentKeyPoid);

        } catch (Exception e) {
            throw new ValidationException("Before Save Validation Failed : " + e.getMessage());
        }

        documentRepository.saveAndFlush(document);

        try {
            callAfterSaveProc(document.getDocId(), documentKeyPoid);

        } catch (Exception e) {
            throw new ValidationException("After Save Validation Failed : " + e.getMessage());
        }

        loggingService.logChanges(oldDocument, document, DocumentEntity.class, UserContext.getDocumentId(), document.getDocId(), LogDetailsEnum.MODIFIED, "DOC_ID");


        // Return response matching original ticket format
        Map<String, Object> response = new HashMap<>();
        response.put("documentKeyPoid", documentKeyPoid.toString());
        response.put("docId", document.getDocId());
        response.put("status", "UPDATED");
        return response;
    }
    private void callBeforeSaveProc(String docId, Long docKeyPoid) {
        log.info("[BEFORE SAVE PROC] docId: " + docId + ", docKeyPoid: " + docKeyPoid);

        StoredProcedureQuery sp = entityManager
                .createStoredProcedureQuery("PROC_DOC_BEFORE_SAVE");

        sp.registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_DOC_ID", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_DOC_KEY_POID", Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_DOC_DATE", java.sql.Date.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_FIELD_VALUES", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_STATUS", String.class, ParameterMode.OUT);

        sp.setParameter("P_LOGIN_GROUP_POID", UserContext.getGroupPoid());
        sp.setParameter("P_LOGIN_COMPANY_POID", UserContext.getCompanyPoid());
        sp.setParameter("P_LOGIN_USER_POID", UserContext.getUserPoid());
        sp.setParameter("P_DOC_ID", docId);
        sp.setParameter("P_DOC_KEY_POID", docKeyPoid);
        sp.setParameter("P_DOC_DATE", null);
        sp.setParameter("P_FIELD_VALUES", null);

        sp.execute();

        String status = (String) sp.getOutputParameterValue("P_STATUS");
        log.info("[BEFORE SAVE PROC RESULT] status: " + status);

        if (status != null && status.toUpperCase().contains("ERROR")) {
            log.error("[BEFORE SAVE PROC FAILED] status: " + status);
            throw new ValidationException(status);
        }
    }

    private void callAfterSaveProc(String docId, Long docKeyPoid) {
        log.info("[AFTER SAVE PROC] docId: " + docId + ", docKeyPoid: " + docKeyPoid);

        StoredProcedureQuery sp = entityManager
                .createStoredProcedureQuery("PROC_DOC_AFTER_SAVE");

        sp.registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_LOGIN_COMPANY_POID", Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_DOC_ID", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_DOC_KEY_POID", Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_DOC_DATE", java.sql.Date.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_FIELD_VALUES", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_STATUS", String.class, ParameterMode.OUT);

        sp.setParameter("P_LOGIN_GROUP_POID", UserContext.getGroupPoid());
        sp.setParameter("P_LOGIN_COMPANY_POID", UserContext.getCompanyPoid());
        sp.setParameter("P_LOGIN_USER_POID", UserContext.getUserPoid());
        sp.setParameter("P_DOC_ID", docId);
        sp.setParameter("P_DOC_KEY_POID", docKeyPoid);
        sp.setParameter("P_DOC_DATE", null);
        sp.setParameter("P_FIELD_VALUES", null);

        sp.execute();

        String status = (String) sp.getOutputParameterValue("P_STATUS");
        log.info("[AFTER SAVE PROC RESULT] status: " + status);

        if (status != null && status.toUpperCase().contains("ERROR")){
            log.error("[AFTER SAVE PROC FAILED] status: " + status);
            throw new ValidationException(status);
        }
    }

    private void checkEditRights(Long docKeyPoid, String docId) {
        StoredProcedureQuery sp = entityManager.createStoredProcedureQuery("PROC_GLOBAL_DOC_EDIT_RIGHT_GET");
        sp.registerStoredProcedureParameter("P_LOGIN_GROUP_POID", Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_DOC_ID", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_DOC_KEY_POID", Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_STATUS", String.class, ParameterMode.OUT);

        sp.setParameter("P_LOGIN_GROUP_POID", 1L);
        sp.setParameter("P_LOGIN_USER_POID", 1L);
        sp.setParameter("P_DOC_ID", docId);
        sp.setParameter("P_DOC_KEY_POID", docKeyPoid);

        sp.execute();
        String status = (String) sp.getOutputParameterValue("P_STATUS");
        if (status != null && status.startsWith("ERROR")) {
            throw new ValidationException("Edit rights validation failed: " + status);
        }
    }

    private void runValidationProc(String procName, Long docKeyPoid, String docId) {
        StoredProcedureQuery sp = entityManager.createStoredProcedureQuery(procName);
        sp.registerStoredProcedureParameter("P_DOC_ID", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_DOC_KEY_POID", Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("P_RESULT", String.class, ParameterMode.OUT);

        sp.setParameter("P_DOC_ID", docId);
        sp.setParameter("P_DOC_KEY_POID", docKeyPoid);

        sp.execute();
        String result = (String) sp.getOutputParameterValue("P_RESULT");
        if (result != null && result.startsWith("ERROR")) {
            throw new RuntimeException(procName + " failed: " + result);
        }
    }

    public Map<String, Object> listDocuments(String docId, FilterRequestDto request, Pageable pageable) {
        String operator = documentSearchService.resolveOperator(request);
        String isDeleted = documentSearchService.resolveIsDeleted(request);
        List<FilterDto> filters = documentSearchService.resolveFilters(request);

        RawSearchResult raw = documentSearchService.search(docId, filters, operator, pageable, isDeleted,
                "DOC_SHORT_NAME",   // label
                "DOC_ID");    // value);

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }
}