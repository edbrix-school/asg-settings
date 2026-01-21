package com.asg.settings.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.entity.CurrencyEntity;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.settings.dto.CurrencyRateDto;
import com.asg.common.lib.dto.DetailsDto;
import com.asg.settings.dto.request.CurrencyCreateRequest;
import com.asg.common.lib.entity.CurrencyRateEntity;
import com.asg.settings.repository.CurrencyCreateRepository;
import com.asg.settings.repository.CurrencyRateRepository;
import com.asg.settings.repository.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.asg.common.lib.utility.ASGHelperUtils.getCurrentUser;


@Service
public class CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyService.class);
    private final CurrencyRepository currencyRepository;
    private final CurrencyCreateRepository currencyCreateRepository;

    @Autowired
    DocumentSearchService documentService;

    @Autowired
    CurrencyRateRepository currencyRateRepository;

    @Autowired
    LoggingService loggingService;

    @Autowired
    DocumentDeleteService documentDeleteService;

    public CurrencyService(CurrencyRepository currencyRepository,
                           CurrencyCreateRepository currencyCreateRepository) {
        this.currencyRepository = currencyRepository;
        this.currencyCreateRepository = currencyCreateRepository;
    }

    public CurrencyRateDto getAllCurrencyRates(Long currencyPoid) {
        CurrencyEntity currencyEntity = currencyRepository.getByCurrencyPoid(currencyPoid);
        if (currencyEntity == null) {
            return null;
        }
        List<CurrencyRateEntity> rateHistory =
                currencyRateRepository.findAllByCurrencyCodeAndGroupPoid(currencyEntity.getCurrencyCode(), 1L);

        CurrencyRateDto currencyRateDto = new CurrencyRateDto();
        BeanUtils.copyProperties(currencyEntity, currencyRateDto);
        currencyRateDto.setRateHistory(rateHistory);
        
        // Set audit fields
        currencyRateDto.setCreatedBy(currencyEntity.getCreatedBy());
        currencyRateDto.setCreatedDate(currencyEntity.getCreatedDate());
        currencyRateDto.setLastModifiedBy(currencyEntity.getLastModifiedBy());
        currencyRateDto.setLastModifiedDate(currencyEntity.getLastModifiedDate());

        if (!currencyEntity.getRates().isEmpty()) {
            currencyRateDto.setBuyRate(currencyEntity.getRates().getFirst().getBuyRate());
            currencyRateDto.setSellRate(currencyEntity.getRates().getFirst().getSellRate());
            currencyRateDto.setRateDate(currencyEntity.getRates().getFirst().getRateDate());
            currencyRateDto.setLabel(currencyEntity.getCurrencyName());
            currencyRateDto.setValue(currencyEntity.getCurrencyPoid());
        }

        return currencyRateDto;
    }

    public CurrencyEntity createOrUpdateCurrency(CurrencyCreateRequest req, Long groupPoid, String userId) {
        // Existing record (for update case)
        CurrencyEntity oldEntity = null;
        if (req.getCurrencyPoid() != null) {
            CurrencyEntity existing = currencyRepository.findById(req.getCurrencyPoid()).orElse(null);
            if (existing != null) {
                oldEntity = new CurrencyEntity();
                BeanUtils.copyProperties(existing, oldEntity);
            }
        }
        CurrencyEntity saved = currencyCreateRepository.createOrUpdateCurrency(req, groupPoid, userId);
        String docId = UserContext.getDocumentId();
        String key = saved.getCurrencyPoid().toString();

        //       CREATE CASE
        if (oldEntity == null) {
            loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, docId, key);
        } else {
            //         UPDATE CASE
            loggingService.logChanges(oldEntity, saved, CurrencyEntity.class, docId, key, LogDetailsEnum.MODIFIED, "CURRENCY_POID");
        } return saved;
    }

    public DetailsDto getCurrencyDetailsByPoid(Long currencyPoid) {
        CurrencyEntity currencyEntity = currencyRepository.getByCurrencyPoid(currencyPoid);
        return new DetailsDto(currencyEntity.getCurrencyPoid(), currencyEntity.getCurrencyCode(), currencyEntity.getCurrencyName(), currencyEntity.getCurrencyPoid(), currencyEntity.getCurrencyName(), currencyEntity.getSeqno());
    }

    public Map<String, Object> listCurrencies(String docId, FilterRequestDto request, Pageable pageable) {

        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveFilters(request);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "CURRENCY_NAME",   // label
                "CURRENCY_POID");    // value);

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Transactional
    public void softDeleteCurrency(Long currencyPoid, DeleteReasonDto deleteReasonDto) {
        CurrencyEntity currency = currencyRepository.getByCurrencyPoid(currencyPoid);
        if (currency == null) {
            throw new ResourceNotFoundException("Currency", "currencyPoid", currencyPoid.toString());
        }
        
        documentDeleteService.deleteDocument(
                currencyPoid,
                "GLOBAL_CURRENCY_MASTER",
                "CURRENCY_POID",
                deleteReasonDto,
                null
        );

    }
}
