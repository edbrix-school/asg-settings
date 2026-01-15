package com.asg.settings.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.settings.dto.LastTaskDto;
import com.asg.settings.dto.TaskDto;
import com.asg.settings.entity.Task;
import com.asg.settings.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.*;

@RestController
@RequestMapping("/v1/task")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private LoggingService loggingService;

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @Operation(
            summary = "Create Task",
            description = "Create a new task based on the request payload."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    ### Request Parameters
                        - **userPoid:** User's Primary Key

                    ### Request Body
                        Provide task details.  
                        - **transactionPoid:** If `null`, a new task will be created.  
                        - **taskCategory:** Category of the task (e.g., ERP_TASK).  
                        - **taskSubCategory:** Sub-category of the task.  
                        - **taskDescription:** Detailed description of the task.  
                        - **taskPriority:** Priority level (e.g., NORMAL, HIGH).  
                        - **taskType:** Type of task (e.g., Minor Enhancement).  
                        - **taskUserPoid:** User allocated to the task.  
                        - Other fields as applicable for task management.
                    """,
            content = @Content(
                    schema = @Schema(implementation = Task.class),
                    examples = {
                            @ExampleObject(
                                    name = "Task Create Example",
                                    value = """
                                            {
                                              "transactionPoid": null,
                                              "taskCategory": "ERP_TASK",
                                              "taskSubCategory": "General",
                                              "taskDescription": "Button replacement",
                                              "taskPriority": "NORMAL",
                                              "taskType": "Minor Enhancement",
                                              "taskUserPoid": 12345,
                                              "taskReportedBy": 67890,
                                              "startDate": "2025-09-01",
                                              "dueDate": "2025-09-10",
                                              "progressPercent": null,
                                              "taskStatus": "PENDING",
                                              "estHours": 10,
                                              "durationHrs": 0,
                                              "actionDetails": "Replace faulty switch",
                                              "faPoid": null,
                                              "recurringDays": null,
                                              "autoTask": "N",
                                              "refDocRef": null,
                                              "holdReason": null
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/create")
    public ResponseEntity<?> createTask(
            @RequestBody @Valid Task task,
            @RequestParam(required = false, defaultValue = "0") Long userPoid
    ) {
        try {

            if (task.getTransactionPoid() != null) {
                return badRequest("transactionPoid must be null when creating");
            }

            String taskPoid = taskService.saveOrUpdateTask(task, userPoid);

            Map<String, Object> data = Map.of("taskPoid", taskPoid);
            return success("Task created successfully", data);

        }catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            return internalServerError("Failed to create task: " + ex.getMessage());
        }
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @Operation(
            summary = "Update Task",
            description = "Update an existing one based on the request payload."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    ### Request Parameters
                        - **userPoid:** User's Primary Key

                    ### Request Body
                        Provide task details.  
                        - **transactionPoid:** The existing task will be updated.  
                        - **taskCategory:** Category of the task (e.g., ERP_TASK).  
                        - **taskSubCategory:** Sub-category of the task.  
                        - **taskDescription:** Detailed description of the task.  
                        - **taskPriority:** Priority level (e.g., NORMAL, HIGH).  
                        - **taskType:** Type of task (e.g., Minor Enhancement).  
                        - **taskUserPoid:** User allocated to the task.  
                        - Other fields as applicable for task management.
                    """,
            content = @Content(
                    schema = @Schema(implementation = Task.class),
                    examples = {
                            @ExampleObject(
                                    name = "Task Update Example",
                                    value = """
                                            {
                                              "transactionPoid": 123,
                                              "taskCategory": "ERP_TASK",
                                              "taskSubCategory": "General",
                                              "taskDescription": "Button replacement",
                                              "taskPriority": "NORMAL",
                                              "taskType": "Minor Enhancement",
                                              "taskUserPoid": 12345,
                                              "taskReportedBy": 67890,
                                              "startDate": "2025-09-01",
                                              "dueDate": "2025-09-10",
                                              "progressPercent": null,
                                              "taskStatus": "PENDING",
                                              "estHours": 10,
                                              "durationHrs": 0,
                                              "actionDetails": "Replace faulty switch",
                                              "faPoid": null,
                                              "recurringDays": null,
                                              "autoTask": "N",
                                              "refDocRef": null,
                                              "holdReason": null
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/update")
    public ResponseEntity<?> updateTask(
            @RequestBody @Valid Task task,
            @RequestParam(required = false, defaultValue = "0") Long userPoid
    ) {
        try {

            if (task.getTransactionPoid() == null) {
                return badRequest("transactionPoid is required when updating");
            }
            String taskPoid = taskService.saveOrUpdateTask(task, userPoid);
            Map<String, Object> data = Map.of("taskPoid", taskPoid);
            return success("Task Updated successfully", data);

        } catch (ValidationException ex) {
            throw ex;
        }catch (Exception ex) {
            return internalServerError("Failed to update task: " + ex.getMessage());
        }
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(
            summary = "Get Last Task",
            description = """
                        Fetch the last task created for a given user and company.
                               
                        ### Request Parameters
                        - **userPoid:** User's Primary Key
                        - **companyPoid:** Company's Primary Key
                    """
    )
    @GetMapping("/getLastTask")
    public ResponseEntity<?> getLastTask(@RequestParam(required = false, defaultValue = "0") Long userPoid,
                                         @RequestParam(required = false, defaultValue = "0") Long companyPoid) {
        try {
            if (userPoid == null || companyPoid == null) {
                throw new RuntimeException("Missing or empty parameter: userPoid or companyPoid");
            }
            List<LastTaskDto> lastTask = taskService.getLastTask(userPoid, companyPoid);
            return success("Last Task Details fetched successfully", lastTask);
        } catch (Exception ex) {
            return internalServerError("Failed to fetch Last Task Details: " + ex.getMessage());
        }
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @Operation(
            summary = "Import Tasks from Excel",
            description = """
                        Upload an Excel file to import tasks into the system.  

                        ### Request Parameters
                        - **file:** Excel file (`.csv` format only) containing task data  
                        - **userPoid:** User's Primary Key 
                        - **companyPoid:** Company Primary Key
                    """
    )
    @PostMapping("/upload-excel")
    public ResponseEntity<?> importTasksFromExcel(@RequestParam("file") MultipartFile file, @RequestParam(required = false, defaultValue = "0") Long userPoid,
                                                  @RequestParam(required = false, defaultValue = "0") Long companyPoid) {
        try {
            String result = taskService.uploadTasks(file, companyPoid, userPoid);
            if (result == null || result.toUpperCase().contains("ERROR")) {
                return internalServerError(result);
            }
            return success(result, "");
        } catch (Exception e) {
            return internalServerError("Failed to process request: " + e.getMessage());
        }
    }
    @Operation(
            summary = "Get Task by TransactionPoid",
            description = """
                        Fetch a specific task using its Transaction Poid.
                               
                        ### Request Parameters
                        - **transactionPoid:** Transaction Poid reference identifier for the task
                    """
    )
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getTaskByDocRef(
            @Parameter(description = "Transaction Poid reference identifier", required = true)
            @PathVariable String transactionPoid) {
        try {
            TaskDto task = taskService.getTaskByTransactionPoid(transactionPoid);
            loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid);
            return success("Task fetched successfully", task);
        } catch (ValidationException ex) {
            return badRequest(ex.getMessage());
        } catch (Exception ex) {
            return internalServerError("Failed to fetch task: " + ex.getMessage());
        }
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(
            summary = "List Tasks with Search and Sort",
            description = "Provide search filters within date range. Valid `searchField` values: GLOBALSEARCH or (TRANSACTION_DATE,DOC_REF,TASK_DESCRIPTION,TASK_CATEGORY,TASK_SUB_CATEGORY,TASK_PRIORITY, TASK_STATUS,TASK_TYPE). Sorting default on transactiondate, asc"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    - ### Filters:
                      Use either:
                      1. A single `GLOBALSEARCH` filter, OR
                      2. Any combination of specific fields (TRANSACTION_DATE,DOC_REF,TASK_DESCRIPTION,TASK_CATEGORY,TASK_SUB_CATEGORY,TASK_PRIORITY, TASK_STATUS,TASK_TYPE).
                      3. operator field will either have "AND" or "OR", if not given will be considered as "OR"
                      4. isDeleted when 'N' or null, will search and return non deleted records, 'Y' will check and return deleted records
                    """,
            content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = FilterDto.class)),
                    examples = {
                            @ExampleObject(
                                    name = "Task Filters",
                                    value = """
                                            {
                                                "operator":"AND",
                                                "isDeleted":"N",
                                                "filters":[
                                                   { "searchField": "TASK_CATEGORY", "searchValue": "ERP_TASK" },
                                                   { "searchField": "TASK_DESCRIPTION", "searchValue": "button" },
                                                   { "searchField": "TASK_PRIORITY", "searchValue": "HIGH|NORMAL" },
                                                   { "searchField": "TASK_STATUS", "searchValue": "PENDING" },
                                                   { "searchField": "TASK_TYPE", "searchValue": "Enhancement" },
                                                   { "searchField": "DOC_REF", "searchValue": "ERP_TASK123" }
                                                ]
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping("/list")
    public ResponseEntity<?> getTasks(
            @ParameterObject Pageable pageable,
            @RequestBody(required = false) FilterRequestDto filters,

            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate){
        try {

            if((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
                return badRequest("Both startDate and endDate should be specified or both dates should be empty.");
            }

            Map<String, Object> tasks = taskService.listTasks(UserContext.getDocumentId(), filters, startDate, endDate, pageable);

            return success("Task list fetched successfully", tasks);

        } catch (Exception e) {
            return internalServerError("Unable to fetch task list: " + e.getMessage());
        }
    }

    @DeleteMapping("/{taskPoid}")
    public ResponseEntity<?> softDeleteTask(
            @PathVariable("taskPoid") Long taskPoid,
            @RequestBody(required = false) DeleteReasonDto deleteReasonDto) {
        try {
            taskService.softDeleteTask(taskPoid, deleteReasonDto);
            return success("Task soft deleted successfully", null);
        } catch (ValidationException ex) {
            return badRequest(ex.getMessage());
        } catch (Exception ex) {
            return internalServerError("Failed to soft delete task: " + ex.getMessage());
        }
    }

}