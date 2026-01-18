package com.asg.settings.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.LovGetListDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.LovDataService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.settings.dto.LastTaskDto;
import com.asg.settings.dto.TaskDto;
import com.asg.settings.dto.TaskSubCategoryDto;
import com.asg.settings.dto.TaskUploadDto;
import com.asg.settings.entity.Task;
import com.asg.settings.repository.TaskRepository;
import com.asg.settings.repository.TempTaskImportTemplateRepository;
import com.asg.settings.utility.TaskExcelParser;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import oracle.jdbc.OracleTypes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TaskService {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    TempTaskImportTemplateRepository tempTaskImportTemplateRepository;

    @Autowired
    DataSource dataSource;

    @Autowired
    private EntityManager em;

    @Autowired
    private DocumentSearchService documentService;

    @Autowired
    private UserService userService;

    @Autowired
    private LovDataService lovService;

    @Autowired
    private TaskCategoryService taskCategoryService;

    @Autowired
    LoggingService loggingService;

    @Autowired
    DocumentDeleteService documentDeleteService;

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    @Transactional
    // email Todo
    public String saveOrUpdateTask(Task task, Long userPoid) {

        // Handle task creation/update
        boolean isExistingTask = task.getTransactionPoid() != null && task.getTransactionPoid() != 0;
        Long updatedTaskId;

        // Adding validation for startdate ahead of enddate for update common for create also
        if (task.getStartDate() != null && task.getDueDate() != null) {
            if (task.getStartDate().after(task.getDueDate())) {
                throw new ValidationException("Start Date cannot be after Due Date");
            }
            if (task.getStartDate().equals(task.getDueDate())) {
                throw new ValidationException("Start Date cannot be equal to Due Date");
            }
        }

        if (!isExistingTask) {
            // Create new Task
            updatedTaskId = createNewTask(task, userPoid);
        } else {
            // Update existing Task
            updatedTaskId = updateExistingTask(task, userPoid);
        }

        return updatedTaskId.toString();
    }

    private Long createNewTask(Task task, Long userPoid) {

        Task newTask = new Task();

        Timestamp now = new Timestamp(System.currentTimeMillis());
        Date date = new Date(System.currentTimeMillis());

        // Creating auto increment docref from last category+1
        Long maxNum = taskRepository.findMaxDocRefNumberByCategory(task.getTaskCategory());
        String newDocRef = task.getTaskCategory() + (maxNum + 1);
        newTask.setDocRef(newDocRef);

        // Set all other fields from the input task
        setTaskFields(newTask, task, userPoid);

        newTask.setCompanyPoid(1L);
        newTask.setTransactionDate(date);
        newTask.setTransactionPoid(null);

        // Get user name instead of using POID
        String userName = userService.getUserNameByUserPoid(userPoid);
        newTask.setCreatedBy(userName != null ? userName : userPoid.toString());
        newTask.setCreatedDate(now.toLocalDateTime());

        newTask = taskRepository.saveAndFlush(newTask);
        String docId = task.getRefDocId();
        String key = newTask.getTransactionPoid().toString();

        Task emptyOldTask = new Task(); // empty object
        loggingService.logChanges(emptyOldTask, newTask, Task.class, docId, key,
                LogDetailsEnum.CREATED, "TASK");
        return newTask.getTransactionPoid();
    }

    private Long updateExistingTask(Task task, Long userPoid) {
        Task existingTask = taskRepository.findByTransactionPoid(task.getTransactionPoid());

        if (existingTask == null) {
            throw new ValidationException("Task not found with Poid: " + task.getTransactionPoid());
        }

        if (task.getTaskStatus() != null
                && (task.getTaskStatus().equalsIgnoreCase("Completed") || task.getTaskStatus().equalsIgnoreCase("Cancelled"))
                && task.getDateClosed() == null) {
            throw new ValidationException("Cancelled/Completed dated is required...");
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        Task oldTask = new Task();
        BeanUtils.copyProperties(existingTask, oldTask);

        // Set all other fields from the input task
        setTaskFields(existingTask, task, userPoid);

        // Get user name instead of using POID
        String userName = userService.getUserNameByUserPoid(userPoid);
        existingTask.setLastModifiedBy(userName != null ? userName : userPoid.toString());
        existingTask.setLastModifiedDate(now.toLocalDateTime());
        existingTask.setCompanyPoid(task.getCompanyPoid());

        existingTask = taskRepository.saveAndFlush(existingTask);
        String docId = existingTask.getRefDocId();
        String key = existingTask.getTransactionPoid().toString();

        loggingService.logChanges(oldTask, existingTask, Task.class, docId, key, LogDetailsEnum.MODIFIED, "TASK");

        return existingTask.getTransactionPoid();
    }

    private void setTaskFields(Task targetTask, Task sourceTask, Long userPoid) {
        //Updatefields
        targetTask.setTransactionDate(sourceTask.getTransactionDate());
        // Generated using repo method on new
//        targetTask.setDocRef(sourceTask.getDocRef());

        targetTask.setTaskDescription(sourceTask.getTaskDescription());
        targetTask.setTaskCategory(sourceTask.getTaskCategory());
        targetTask.setTaskSubCategory(sourceTask.getTaskSubCategory());
        targetTask.setTaskPriority(sourceTask.getTaskPriority());

        // set both to passed user poid
        targetTask.setTaskReportedBy(sourceTask.getTaskReportedBy());
        // set allocatedto in taskuserpoid
        targetTask.setTaskUserPoid(sourceTask.getTaskUserPoid());

        targetTask.setTaskStatus(sourceTask.getTaskStatus());

        targetTask.setProgressPercent(sourceTask.getProgressPercent());

        targetTask.setEstHours(sourceTask.getEstHours());
        targetTask.setDueDate(sourceTask.getDueDate());
        targetTask.setDateClosed(sourceTask.getDateClosed());
        targetTask.setDurationHrs(sourceTask.getDurationHrs());

        targetTask.setStartDate(sourceTask.getStartDate());
        targetTask.setActionDetails(sourceTask.getActionDetails());
        targetTask.setActionedBy(sourceTask.getActionedBy());

        targetTask.setRecurringDays(sourceTask.getRecurringDays());

        targetTask.setRefDocId(sourceTask.getRefDocId());

        targetTask.setRefDocRef(sourceTask.getRefDocRef());

        targetTask.setRefDocPoid(sourceTask.getRefDocPoid());

        targetTask.setHoldReason(sourceTask.getHoldReason());
        targetTask.setAutoTask(sourceTask.getAutoTask());

        targetTask.setDeleted(sourceTask.getDeleted());
        targetTask.setFaPoid(sourceTask.getFaPoid());

        targetTask.setRecurringSeriesPoid(sourceTask.getRecurringSeriesPoid());
        targetTask.setTaskType(sourceTask.getTaskType());


    }

    public List<LastTaskDto> getLastTask(Long userPoid, Long companyPoid) throws SQLException {
        String sql = "BEGIN PROC_TASK_GET_LAST_TASK_DETAIL(?, ?, ?, ?); END;";
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setLong(1, userPoid);
            cs.setLong(2, companyPoid);
            cs.registerOutParameter(3, Types.VARCHAR);
            cs.registerOutParameter(4, OracleTypes.CURSOR);
            cs.execute();

            String result = cs.getString(3);
            if (result != null && result.contains("SUCCESS")) {
                try (ResultSet rs = (ResultSet) cs.getObject(4)) {
                    List<LastTaskDto> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(new LastTaskDto(
                                rs.getString("TASK_CATEGORY"),
                                rs.getString("TASK_SUB_CATEGORY"),
                                rs.getLong("TASK_USER_POID")
                        ));
                    }
                    return list;
                }
            } else {
                throw new SQLException(result);
            }
        }
    }

    // need this to get data from excel and then store in temp table
    public String uploadTasks(MultipartFile file, Long companyPoid, Long userPoid) throws Exception {
        try {
            if (file == null || file.isEmpty() || file.getSize() == 0) {
                throw new IllegalArgumentException("CSV file must be provided and not be empty");
            }

            List<TaskUploadDto> parsed = TaskExcelParser.parse(file, userService);
            log.info("Parsed {} tasks from Excel.", parsed.size());
            tempTaskImportTemplateRepository.clearTempTable();
            log.info("Cleared TEMP_TASK_IMPORT_TEMPLATE table.");

            int insertedCount = 0;
            for (TaskUploadDto task : parsed) {
                tempTaskImportTemplateRepository.insertTemp(task.taskCategory(), task.taskSubCategory(), task.taskDescription(), task.taskPriority(), task.taskReportedBy(), task.allocatedTo(), task.startDate(), task.dueDate(), task.taskType());
                insertedCount++;
            }

            log.info("Saved {} records to TEMP_TASK_IMPORT_TEMPLATE.", insertedCount);
            String status = tempTaskImportTemplateRepository.importTasksFromExcel(userPoid, companyPoid);

            if (status == null || status.toLowerCase().contains("error") || status.toLowerCase().contains("warning")) {
                log.warn("Stored procedure returned error: {}", status);
                return status;
            }

            log.info("Stored procedure executed successfully: {}", status);
            return status;
        } catch (Exception e) {
            log.error("Exception during task upload", e);
            return "Upload failed due to error: " + e.getMessage();
        }
    }

    public TaskDto getTaskByTransactionPoid(String transactionPoid) {
        if (transactionPoid == null || transactionPoid.isBlank()) {
            throw new ValidationException("Transaction Poid must not be empty");
        }

        Task task = taskRepository.findByTransactionPoid(transactionPoid);
        if (task == null) {
            throw new ValidationException("Task not found for Transaction Poid " + transactionPoid);
        }
        return toTaskDto(task);
    }

    @Transactional
    public void softDeleteTask(Long taskPoid, DeleteReasonDto deleteReasonDto) {
        Task task = taskRepository.findByTransactionPoid(taskPoid);
        if (task == null) {
            throw new ValidationException("Task not found or already deleted");
        }
        documentDeleteService.deleteDocument(taskPoid, "GLOBAL_TASK_HDR", "TRANSACTION_POID", deleteReasonDto, null);
    }

    public Map<String, Object> listTasks(String documentId, FilterRequestDto request, LocalDate startDateValue, LocalDate endDateValue, Pageable pageable) {
        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);

        List<FilterDto> filters = documentService.resolveDateFilters(request, "TRANSACTION_DATE", startDateValue, endDateValue);

        RawSearchResult raw = documentService.search(documentId, filters, operator, pageable, isDeleted,
                "TASK_DESCRIPTION",   // label
                "TRANSACTION_POID");  // value

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    private TaskDto toTaskDto(Task task) {
        if (task == null) return null;

        TaskDto dto = new TaskDto();
        dto.setTransactionPoid(task.getTransactionPoid());
        dto.setTransactionDate(task.getTransactionDate());
        dto.setCompanyPoid(task.getCompanyPoid());
        dto.setLabel(task.getLabel());
        dto.setValue(task.getValue());
        dto.setDocRef(task.getDocRef());
        dto.setTaskDescription(task.getTaskDescription());
        dto.setTaskCategory(task.getTaskCategory());
        if (StringUtils.isNotBlank(task.getTaskCategory())) {
            try {
                dto.setTaskCategoryDet(
                        lovService.getDetailsByCodeAndLovName(
                                task.getTaskCategory(),
                                "TASK_CATEGORY"
                        )
                );
            } catch (Exception ex) {
                log.warn("LOV not found for TASK_CATEGORY : {}", task.getTaskCategory());
                dto.setTaskCategoryDet(null);
            }
        }

        dto.setTaskSubCategory(task.getTaskSubCategory());

        if (StringUtils.isNotBlank(task.getTaskCategory()) &&
                StringUtils.isNotBlank(task.getTaskSubCategory()) &&
                dto.getTaskCategoryDet() != null) {

            dto.setTaskSubCategoryDet(
                    getLovDetailsFromTaskCategory(
                            dto.getTaskCategoryDet().getPoid(),
                            task.getTaskSubCategory()
                    )
            );

    } else {
            dto.setTaskSubCategoryDet(null);
        }

        dto.setTaskPriority(task.getTaskPriority());
        dto.setTaskUserPoid(task.getTaskUserPoid());
        if (task.getTaskUserPoid() != null) {
            dto.setTaskUserDet(lovService.getDetailsByPoidAndLovName(task.getTaskUserPoid(), "USER_MASTER"));
        }
        dto.setTaskStatus(task.getTaskStatus());
        dto.setProgressPercent(task.getProgressPercent());
        dto.setEstHours(task.getEstHours());
        dto.setDueDate(task.getDueDate());
        dto.setDateClosed(task.getDateClosed());
        dto.setDurationHrs(task.getDurationHrs());
        dto.setStartDate(task.getStartDate());
        dto.setActionDetails(task.getActionDetails());
        dto.setActionedBy(task.getActionedBy());
        dto.setRecurringDays(task.getRecurringDays());
        dto.setRefDocId(task.getRefDocId());
        dto.setRefDocRef(task.getRefDocRef());
        dto.setRefDocPoid(task.getRefDocPoid());
        dto.setHoldReason(task.getHoldReason());
        dto.setAutoTask(task.getAutoTask());
        dto.setDeleted(task.getDeleted());
        dto.setCreatedBy(task.getCreatedBy());
        dto.setCreatedDate(task.getCreatedDate());
        dto.setLastModifiedBy(task.getLastModifiedBy());
        dto.setLastModifiedDate(task.getLastModifiedDate());
        dto.setFaPoid(task.getFaPoid());
        dto.setTaskReportedBy(task.getTaskReportedBy());
        if (task.getTaskReportedBy() != null) {
            dto.setTaskReportedByDet(lovService.getDetailsByPoidAndLovName(task.getTaskReportedBy(), "USER_MASTER"));
        }
        dto.setRecurringSeriesPoid(task.getRecurringSeriesPoid());
        dto.setTaskType(task.getTaskType());
        return dto;
    }

    private LovGetListDto getLovDetailsFromTaskCategory(Long taskCategoryPoid, String taskSubCategoryDescription) {
        if (taskCategoryPoid == null) return null;
        List<TaskSubCategoryDto> subCategories = taskCategoryService.getSubCategoriesByCategoryPoid(taskCategoryPoid);
        if (subCategories == null || subCategories.isEmpty()) return null;

        TaskSubCategoryDto matchedSubCategory = subCategories.stream()
                .filter(sub -> sub.getDescription() != null
                        && sub.getDescription().equalsIgnoreCase(taskSubCategoryDescription))
                .findFirst()
                .orElse(null);

        if (matchedSubCategory == null) return null;

        LovGetListDto lovGetListDto = new LovGetListDto();
        lovGetListDto.setPoid(matchedSubCategory.getPoid());
        lovGetListDto.setLabel(matchedSubCategory.getLabel());
        lovGetListDto.setValue(matchedSubCategory.getValue());
        lovGetListDto.setCode(matchedSubCategory.getCode());
        lovGetListDto.setDescription(matchedSubCategory.getDescription());
        return lovGetListDto;
    }
}



