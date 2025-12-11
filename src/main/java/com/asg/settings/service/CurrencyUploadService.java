package com.asg.settings.service;

import com.asg.settings.dto.CurrencyRateDto;
import com.asg.settings.dto.CurrencyUpdateRequest;
import com.asg.settings.repository.CurrencyRateTempRepository;
import com.asg.settings.repository.CurrencyRepository;
import com.asg.settings.repository.CurrencyUploadRepository;
import com.asg.settings.utility.CurrencyExcelParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class CurrencyUploadService {

    private final CurrencyRateTempRepository tempRepo;

    private final CurrencyRepository currencyRepository;

    private final CurrencyUploadRepository procRepo;

    private static final Logger log = LoggerFactory.getLogger(CurrencyUploadService.class);

    public CurrencyUploadService(CurrencyRateTempRepository tempRepo, CurrencyRepository getAllCurrencyCodes, CurrencyUploadRepository procRepo) {
        this.tempRepo = tempRepo;
        this.currencyRepository = getAllCurrencyCodes;
        this.procRepo = procRepo;
    }

    public String uploadCurrencyRates(MultipartFile file, Long groupPoid, Long companyPoid, Long userPoid) throws Exception {
        try {
            if (file == null || file.isEmpty() || file.getSize() == 0) {
                throw new IllegalArgumentException("CSV file must be provided and not be empty");
            }
            List<String> allowedCodes = currencyRepository.getAllCurrencyCodes();
            List<CurrencyRateDto> parsed = CurrencyExcelParser.parse(file, allowedCodes);
            log.info("Parsed {} currency rates from Excel.", parsed.size());
            tempRepo.clearTempTable();
            log.info("Cleared CURRENCY_RATE_UPLOAD_TEMP table.");

            int insertedCount = 0;
            for (CurrencyRateDto rate : parsed) {
                tempRepo.insertTemp(rate.getCurrencyCode(), rate.getBuyRate(), rate.getSellRate());
                insertedCount++;
            }

            log.info("Saved {} records to CURRENCY_RATE_UPLOAD_TEMP.", insertedCount);
            String status = procRepo.callCurrencyUploadProc(groupPoid, userPoid, companyPoid);

            if (status == null || status.toLowerCase().contains("error") || status.toLowerCase().contains("warning")) {
                log.warn("Stored procedure returned error: {}", status);
                return status;
            }

            log.info("Stored procedure executed successfully: {}", status);
            return status;
        } catch (Exception e) {
            log.error("Exception during currency rate upload", e);
            return "Upload failed due to error: " + e.getMessage();
        }
    }
    public String updateCurrencyRates(CurrencyUpdateRequest currencyUpdateRequest) throws Exception {
        try {

            String status = procRepo.callCurrencyUpdateProc(currencyUpdateRequest.groupPOID(), currencyUpdateRequest.currencyCode(),
                    currencyUpdateRequest.rateChangeDate(), currencyUpdateRequest.buyRate(), currencyUpdateRequest.sellRate());

            if (status == null || status.toLowerCase().contains("error")) {
                log.warn("Stored procedure returned error: {}", status);
                return status;
            }

            log.info("Stored procedure executed successfully: {}", status);
            return status;
        } catch (Exception e) {
            log.error("Exception during currency rate update", e);
            return "Update failed due to error: " + e.getMessage();
        }
    }

}