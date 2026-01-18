package com.asg.settings.controller;


import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.settings.dto.TaskCategoryDto;
import com.asg.settings.dto.TaskSubCategoryDto;
import com.asg.settings.service.TaskCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.*;


@Slf4j
@RestController
@RequestMapping("/v1/task-category")
@SecurityRequirement(name = "bearerAuth")
public class TaskCategoryController {


    private final TaskCategoryService taskCategoryService;
    private final LoggingService loggingService;

    @Autowired
    public TaskCategoryController(TaskCategoryService taskCategoryService, LoggingService loggingService) {
        this.taskCategoryService = taskCategoryService;
        this.loggingService = loggingService;
    }

    /**
     * Retrieves a task category by its unique identifier
     *
     * @param categoryPoid    The unique identifier of the task category to retrieve
     * @return ResponseEntity containing the TaskCategoryDto if found
     */
    @Operation(
            summary = "Get task category by ID",
            description = "Retrieves a specific task category with subcategories and user role assignments",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task category retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TaskCategoryDto.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Task category not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/{categoryPoid}")
    public ResponseEntity<?> getTaskCategory(
            @Parameter(description = "CATEGORY_POID of the task category to be retrieved", required = true, example = "1")
            @PathVariable Long categoryPoid) {

        TaskCategoryDto taskCategoryDto = taskCategoryService.getTaskCategory(categoryPoid);
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), categoryPoid.toString());
        return success("Task Category retrieved successfully", taskCategoryDto);
    }

    @Operation(
            summary = "Soft-delete task category",
            description = "Marks task category as inactive (active='N') without affecting subcategories",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task category deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Task category not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @DeleteMapping("/{catPoid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> softDeleteTaskCategory(
            @Parameter(description = "Task Category POID", required = true)
            @PathVariable Long catPoid,
            @RequestBody(required = false) DeleteReasonDto deleteReasonDto) {
        try {
            return taskCategoryService.softDeleteTaskCategory(catPoid, deleteReasonDto);
        } catch (Exception ex) {
            return internalServerError("Failed to delete Task Category: " + ex.getMessage());
        }
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    - ### Filters:
                      Use either:
                      1. A single `GLOBALSEARCH` filter, OR
                      2. Any combination of specific fields (CATEGORY_DESCRIPTION, CATEGORY_CODE, CREATED_BY, etc.).
                      3. operator field will either have "AND" or "OR", if not given will be considered as "OR",
                         not required for GLOBALSEARCH, for non GLOBALSEARCH need to give only 1 time
                      4. isDeleted when 'Y' or null, will search and return non deleted records, 'Y' will check and return deleted records
                      5. sort will be default on primary key ascending if specified in db field otherwise you can override it giving the field name and the direction.
                    
                    - #### Global Search:
                      Apply one search term across multiple fields.
                      No operator need to send in this case.
                      • { "searchField": "GLOBALSEARCH", "searchValue": "ERP" }
                    
                    - #### Single Field, Single Value:
                      Search one field with one value.
                      • { "searchField": "CATEGORY_DESCRIPTION", "searchValue": "ERP_INCIDENTS" },
                    
                    - #### Single Field, Multiple Values:
                      Provide multiple values for the same field, separated by `|`.
                      • { "searchField": "CATEGORY_DESCRIPTION", "searchValue": "ERP_INCIDENTS|FINANCE" }
                    
                    - #### Multiple Different Fields, Single or Multiple Value:
                      Provide one or multiple search values across multiple fields.
                      • { "searchField": "CATEGORY_DESCRIPTION", "searchValue": "ERP_INCIDENTS" },
                      • { "searchField": "CREATED_BY", "searchValue": "ADMIN|USER" }
                      • "operator" :  "AND"/"OR"
                    
                    - ### Sorting:
                      Defaults to whatever specified in list_of_records_sql field mostly primary key ascending.
                      To override, pass `sort=<field>,ASC|DESC` in query params.
                      Examples:
                      • sort=CATEGORY_DESCRIPTION,ASC
                      • sort=CATEGORY_POID,DESC
                    """,
            content = @Content(
                    schema = @Schema(implementation = FilterRequestDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Task Category Filters",
                                    value = """
                                            {
                                            "operator": "AND",
                                            "isDeleted": "N",
                                            "filters": [
                                               { "searchField": "GLOBALSEARCH", "searchValue": "ERP" },
                                               { "searchField": "CATEGORY_DESCRIPTION", "searchValue": "ERP_INCIDENTS|FINANCE" },
                                               { "searchField": "CATEGORY_CODE", "searchValue": "ERP"},
                                               { "searchField": "CREATED_BY", "searchValue": "ADMIN"}
                                            ]
                                            }
                                            """
                            )
                    }
            )
    )
    @Operation(
            summary = "Get paginated task categories",
            description = "Retrieves paginated task categories with optional filtering and subcategories",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task categories retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TaskCategoryDto.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PostMapping("/list")
    public ResponseEntity<?> getTaskCategory(
            @ParameterObject Pageable pageable,
            @RequestBody(required = false) FilterRequestDto filters) {
        try {
            Map<String, Object> taskCategories = taskCategoryService.listTaskCategories(UserContext.getDocumentId(), filters, pageable);
            return success("Task Category fetched successfully", taskCategories);
        } catch (ValidationException ex) {
            return badRequest(ex.getMessage());
        } catch (Exception ex) {
            return internalServerError("Failed to fetch task categories: " + ex.getMessage());
        }
    }

    @Operation(
            summary = "Create task category",
            description = "Creates a new task category with optional subcategories and user role assignments",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task category created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TaskCategoryDto.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    schema = @Schema(implementation = TaskCategoryDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Create Task Category Example",
                                    value = """
                                            {
                                                "categoryDescription": "ERP Incidents",
                                                "userRolePoid": ["123", "456"],
                                                "active": "Y",
                                                "seqNo": 1,
                                                "categoryCode": "ERP_INC",
                                                "subCategories": [
                                                    {
                                                        "subCategoryDescription": "Login Issues"
                                                    },
                                                    {
                                                        "subCategoryDescription": "Data Entry Problems"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping
    public ResponseEntity<?> createTaskCategory(
            @Valid @RequestBody TaskCategoryDto taskCategoryDto) {

        TaskCategoryDto createdCategory = taskCategoryService.createTaskCategory(taskCategoryDto);
        return success("Task Category created successfully", createdCategory);
    }

    @Operation(
            summary = "Update task category",
            description = "Updates task category including subcategories with create/update/delete actions",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Task category updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TaskCategoryDto.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Task category not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    schema = @Schema(implementation = TaskCategoryDto.class),
                    examples = {
                            @ExampleObject(
                                    name = "Update Task Category Example",
                                    value = """
                                            {
                                                "categoryDescription": "Updated ERP Incidents",
                                                "userRolePoid": ["123", "789"],
                                                "active": "Y",
                                                "seqNo": 2,
                                                "categoryCode": "ERP_INC_UPD",
                                                "subCategories": [
                                                    {
                                                        "detRowId": 1,
                                                        "subCategoryDescription": "Updated Login Issues",
                                                        "actionType": "isUpdated"
                                                    },
                                                    {
                                                        "subCategoryDescription": "New Performance Issues",
                                                        "actionType": "isCreated"
                                                    },
                                                    {
                                                        "detRowId": 3,
                                                        "actionType": "isDeleted"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    }
            )
    )
    @PutMapping("/{categoryPoid}")
    public ResponseEntity<?> updateTaskCategory(
            @Parameter(description = "CATEGORY_POID of the task category to be updated", required = true)
            @PathVariable Long categoryPoid,
            @Valid @RequestBody TaskCategoryDto taskCategoryDto) {

        TaskCategoryDto updatedCategory = taskCategoryService.updateTaskCategory(categoryPoid, taskCategoryDto);
        return success("Task Category updated successfully", updatedCategory);
    }

    @Operation(
            summary = "Fetch subcategories by category",
            description = "Retrieves all task subcategories belonging to the specified category POID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Subcategories fetched successfully"),
                    @ApiResponse(responseCode = "404", description = "No subcategories found for given category POID"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/subcategories/{categoryPoid}")
    public ResponseEntity<?> getSubCategories(@PathVariable Long categoryPoid) {
        List<TaskSubCategoryDto> subCategories = taskCategoryService.getSubCategoriesByCategoryPoid(categoryPoid);
        return success("Task SubCategories for category " + categoryPoid + " retrieved successfully", subCategories);
    }

    @GetMapping("/category-exists-by-description")
    public ResponseEntity<?> isCategoryExistsByDescription(
            @Parameter(
                    description = "Category description to check",
                    example = "ERP Incidents",
                    required = true
            )
            @RequestParam String categoryDescription,

            @Parameter(
                    description = "categoryPoid to check (for update scenarios)",
                    example = "123",
                    required = false
            )
            @RequestParam(required = false) Long categoryPoid) {

        if (StringUtils.isBlank(categoryDescription)) {
            return success("Category does not exist by description", false);
        }
        Boolean isExists = taskCategoryService.isCategoryExistsByDescriptionAndCategoryPoid(categoryDescription, categoryPoid);
        if (isExists) {
            return success("Category exists by description", true);
        }
        return success("Category does not exist by description", false);
    }

}