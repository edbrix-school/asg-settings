package com.asg.settings.utility;

import com.asg.settings.dto.TaskUploadDto;
import com.asg.settings.service.UserService;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class TaskExcelParser {

    private static final Logger log = LoggerFactory.getLogger(TaskExcelParser.class);
    private static final SimpleDateFormat ORACLE_FORMAT =
            new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);

    public static List<TaskUploadDto> parse(MultipartFile file, UserService userService) throws Exception {
        List<TaskUploadDto> tasks = new ArrayList<>();

        try (
                InputStream is = file.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()
        ) {

            List<String[]> rows = csvReader.readAll();

            if (rows.isEmpty()) {
                throw new IllegalArgumentException("Please enter data under column headers");
            }

            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 9 || isRowEmpty(row)) {
                    log.debug("Skipping empty or short row {}", i + 1);
                    continue;
                }
                
                log.debug("Row {} has {} columns: {}", i + 1, row.length, String.join(",", row));

                String category = row[0].trim();
                String subcategory = row[1].trim();
                String taskDescription = row[2].trim();
                String priority = row[3].trim();
                String reportedBy = validateUserIdForTask(row[4].trim(), userService, "REPORTED_BY");
                String allocatedTo = validateUserIdForTask(row[5].trim(), userService, "ALLOCATED_TO");
                String startDate = getDate(row[6].trim());
                String dueDate = getDate(row[7].trim());
                String taskType = row[8].trim();
                
                log.debug("Row {}: reportedBy='{}', allocatedTo='{}', startDate='{}', dueDate='{}', taskType='{}'", 
                    i + 1, reportedBy, allocatedTo, startDate, dueDate, taskType);

                tasks.add(new TaskUploadDto(category, subcategory, taskDescription, priority, reportedBy, allocatedTo, startDate, dueDate, taskType));
            }

            log.info("Parsed {} valid tasks", tasks.size());
        } catch (Exception e) {
            log.error("Failed to parse CSV file", e);
            throw e;
        }

        return tasks;
    }

    private static boolean isRowEmpty(String[] row) {
        for (int i = 0; i < Math.min(9, row.length); i++) {
            if (row[i] != null && !row[i].trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static String getDate(String cellValue) throws IllegalArgumentException {
        try {
            return ORACLE_FORMAT.format(ORACLE_FORMAT.parse(cellValue.trim()));
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid date format: '" + cellValue +
                            "'. Please use format DD-MMM-YYYY (e.g., 02-Sep-2025).");
        }
    }

    private static String validateUserIdForTask(String userId, UserService userService, String fieldName) throws IllegalArgumentException {
        try {
            Boolean exists = userService.isUserExistsByUserIdAndUserPoid(userId, null);
            if (exists == null || !exists) {
                throw new IllegalArgumentException("UserId '" + userId + "' in field '" + fieldName + "' does not exist.");
            }
            return userId;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to validate user '" + userId + "' in field '" + fieldName + "': " + e.getMessage()
            );
        }
    }


}
