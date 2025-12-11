package com.asg.settings.utility;

import com.asg.settings.dto.CurrencyRateDto;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CurrencyExcelParser {

    private static final Logger log = LoggerFactory.getLogger(CurrencyExcelParser.class);

    public static List<CurrencyRateDto> parse(MultipartFile file, List<String> allowedCodes) throws Exception {
        List<CurrencyRateDto> rates = new ArrayList<>();

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
                if (row.length < 2 || isRowEmpty(row)) {
                    log.debug("Skipping empty or short row {}", i + 1);
                    continue;
                }

                String code = row[0].trim();
                BigDecimal rate = parseDecimal(row[1], i + 1, 1);

                if (!allowedCodes.contains(code)) {
                    log.warn("Skipping unsupported currency code '{}' at row {}", code, i + 1);
                    continue;
                }

                if (rate != null) {
                    rates.add(new CurrencyRateDto(null, code, null, null, null,null, null, null,null,null,null, rate, rate, null,null, null, null, null, null));
                } else {
                    log.warn("Invalid rate for currency '{}' at row {}", code, i + 1);
                }
            }

            log.info("Parsed {} valid currency rates", rates.size());
        } catch (Exception e) {
            log.error("Failed to parse CSV file", e);
            throw e;
        }

        return rates;
    }

    private static boolean isRowEmpty(String[] row) {
        for (int i = 0; i <= 2; i++) {
            if (i < row.length && row[i] != null && !row[i].trim().isEmpty()) return false;
        }
        return true;
    }

    private static BigDecimal parseDecimal(String value, int rowIndex, int colIndex) {
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            log.warn("Invalid decimal at row={}, column={}: '{}'", rowIndex, colIndex, value);
            return null;
        }
    }
}
