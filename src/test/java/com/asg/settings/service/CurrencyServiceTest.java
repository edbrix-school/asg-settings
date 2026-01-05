package com.asg.settings.service;

import com.asg.common.lib.dto.DetailsDto;
import com.asg.common.lib.entity.CurrencyEntity;
import com.asg.common.lib.entity.CurrencyRateEntity;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.settings.dto.CurrencyRateDto;
import com.asg.settings.dto.request.CurrencyCreateRequest;
import com.asg.settings.repository.CurrencyCreateRepository;
import com.asg.settings.repository.CurrencyRateRepository;
import com.asg.settings.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private CurrencyCreateRepository currencyCreateRepository;

    @Mock
    private CurrencyRateRepository currencyRateRepository;

    @InjectMocks
    private CurrencyService currencyService;

    private CurrencyEntity currencyEntity;
    private CurrencyRateEntity rateEntity;

    @BeforeEach
    void setUp() {
        // Manually inject the @Autowired field
        ReflectionTestUtils.setField(currencyService, "currencyRateRepository", currencyRateRepository);

        currencyEntity = new CurrencyEntity();
        currencyEntity.setCurrencyPoid(1L);
        currencyEntity.setCurrencyCode("USD");
        currencyEntity.setCurrencyName("US Dollar");
        currencyEntity.setSeqno(1);

        rateEntity = new CurrencyRateEntity();
        rateEntity.setBuyRate(BigDecimal.valueOf(1.0));
        rateEntity.setSellRate(BigDecimal.valueOf(1.1));
        rateEntity.setRateDate(new java.sql.Date(System.currentTimeMillis()));
        rateEntity.setCurrencyCode("USD");

        currencyEntity.setRates(List.of(rateEntity));
    }

//    @Test
//    void getCurrencyList_ShouldReturnPagedResults() {
//        List<CurrencyEntity> currencies = List.of(currencyEntity);
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<CurrencyEntity> currencyPage = new PageImpl<>(currencies, pageable, 1);
//
//        when(currencyRepository.findCurrencyList(anyList(), eq(pageable))).thenReturn(currencyPage);
//
//        Page<CurrencyLightDto> result = currencyService.getCurrencyList(Collections.emptyList(), pageable);
//
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        assertEquals("USD", result.getContent().get(0).getCurrencyCode());
//        assertEquals("US Dollar", result.getContent().get(0).getCurrencyName());
//        verify(currencyRepository).findCurrencyList(anyList(), eq(pageable));
//    }

//    @Test
//    void getCurrencyList_ShouldReturnEmptyPage_WhenNoCurrencies() {
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<CurrencyEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
//
//        when(currencyRepository.findCurrencyList(anyList(), eq(pageable))).thenReturn(emptyPage);
//
//        Page<CurrencyLightDto> result = currencyService.getCurrencyList(Collections.emptyList(), pageable);
//
//        assertNotNull(result);
//        assertEquals(0, result.getTotalElements());
//        assertTrue(result.getContent().isEmpty());
//    }

    @Test
    void getAllCurrencyRates_ShouldReturnCurrencyRateDto_WhenCurrencyExists() {
        List<CurrencyRateEntity> rateHistory = List.of(rateEntity);

        when(currencyRepository.getByCurrencyPoid(1L)).thenReturn(currencyEntity);
        when(currencyRateRepository.findAllByCurrencyCodeAndGroupPoid("USD", 1L)).thenReturn(rateHistory);

        CurrencyRateDto result = currencyService.getAllCurrencyRates(1L);

        assertNotNull(result);
        assertEquals("USD", result.getCurrencyCode());
        assertEquals("US Dollar", result.getCurrencyName());
        assertEquals(BigDecimal.valueOf(1.0), result.getBuyRate());
        assertEquals(BigDecimal.valueOf(1.1), result.getSellRate());
        assertEquals(rateHistory, result.getRateHistory());
        assertEquals("US Dollar", result.getLabel());
        assertEquals(1L, result.getValue());
        verify(currencyRepository).getByCurrencyPoid(1L);
        verify(currencyRateRepository).findAllByCurrencyCodeAndGroupPoid("USD", 1L);
    }

    @Test
    void getAllCurrencyRates_ShouldReturnNull_WhenCurrencyNotExists() {
        when(currencyRepository.getByCurrencyPoid(1L)).thenReturn(null);

        CurrencyRateDto result = currencyService.getAllCurrencyRates(1L);

        assertNull(result);
        verify(currencyRepository).getByCurrencyPoid(1L);
        verify(currencyRateRepository, never()).findAllByCurrencyCodeAndGroupPoid(anyString(), anyLong());
    }

    @Test
    void getAllCurrencyRates_ShouldHandleEmptyRates() {
        currencyEntity.setRates(Collections.emptyList());
        List<CurrencyRateEntity> rateHistory = Collections.emptyList();

        when(currencyRepository.getByCurrencyPoid(1L)).thenReturn(currencyEntity);
        when(currencyRateRepository.findAllByCurrencyCodeAndGroupPoid("USD", 1L)).thenReturn(rateHistory);

        CurrencyRateDto result = currencyService.getAllCurrencyRates(1L);

        assertNotNull(result);
        assertEquals("USD", result.getCurrencyCode());
        assertEquals("US Dollar", result.getCurrencyName());
        assertNull(result.getBuyRate());
        assertNull(result.getSellRate());
        assertNull(result.getRateDate());
        assertEquals(rateHistory, result.getRateHistory());
    }

    @Test
    void createOrUpdateCurrency_ShouldCallRepository() {
        CurrencyCreateRequest request = new CurrencyCreateRequest();
        Long groupPoid = 1L;
        Long userPoid = 1L;

        when(currencyCreateRepository.createOrUpdateCurrency(request, groupPoid, userPoid)).thenReturn(currencyEntity);

        CurrencyEntity result = currencyService.createOrUpdateCurrency(request, groupPoid, userPoid);

        assertNotNull(result);
        assertEquals(currencyEntity, result);
        verify(currencyCreateRepository).createOrUpdateCurrency(request, groupPoid, userPoid);
    }

    @Test
    void getCurrencyDetailsByPoid_ShouldReturnDetailsDto() {
        when(currencyRepository.getByCurrencyPoid(1L)).thenReturn(currencyEntity);

        DetailsDto result = currencyService.getCurrencyDetailsByPoid(1L);

        assertNotNull(result);
        assertEquals(1L, result.poid());
        assertEquals("USD", result.code());
        assertEquals("US Dollar", result.label());
        assertEquals(1L, result.value());
        assertEquals("US Dollar", result.description());
        assertEquals(1, result.seqNo());
        verify(currencyRepository).getByCurrencyPoid(1L);
    }

    @Test
    void getCurrencyDetailsByPoid_ShouldHandleNullEntity() {
        when(currencyRepository.getByCurrencyPoid(1L)).thenReturn(null);

        assertThrows(NullPointerException.class, () ->
                currencyService.getCurrencyDetailsByPoid(1L));

        verify(currencyRepository).getByCurrencyPoid(1L);
    }

//    @Test
//    void getCurrencyList_ShouldHandleNullFilters() {
//        List<CurrencyEntity> currencies = List.of(currencyEntity);
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<CurrencyEntity> currencyPage = new PageImpl<>(currencies, pageable, 1);
//
//        when(currencyRepository.findCurrencyList(isNull(), eq(pageable))).thenReturn(currencyPage);
//
//        Page<CurrencyLightDto> result = currencyService.getCurrencyList(null, pageable);
//
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        verify(currencyRepository).findCurrencyList(isNull(), eq(pageable));
//    }

//    @Test
//    void getCurrencyList_ShouldHandleMultipleCurrencies() {
//        CurrencyEntity currency2 = new CurrencyEntity();
//        currency2.setCurrencyPoid(2L);
//        currency2.setCurrencyCode("EUR");
//        currency2.setCurrencyName("Euro");
//
//        List<CurrencyEntity> currencies = List.of(currencyEntity, currency2);
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<CurrencyEntity> currencyPage = new PageImpl<>(currencies, pageable, 2);
//
//        when(currencyRepository.findCurrencyList(anyList(), eq(pageable))).thenReturn(currencyPage);
//
//        Page<CurrencyLightDto> result = currencyService.getCurrencyList(Collections.emptyList(), pageable);
//
//        assertNotNull(result);
//        assertEquals(2, result.getTotalElements());
//        assertEquals("USD", result.getContent().get(0).getCurrencyCode());
//        assertEquals("EUR", result.getContent().get(1).getCurrencyCode());
//    }

    @Test
    void getAllCurrencyRates_ShouldThrowException_WhenRatesIsNull() {
        currencyEntity.setRates(null);
        List<CurrencyRateEntity> rateHistory = Collections.emptyList();

        when(currencyRepository.getByCurrencyPoid(1L)).thenReturn(currencyEntity);
        when(currencyRateRepository.findAllByCurrencyCodeAndGroupPoid("USD", 1L)).thenReturn(rateHistory);

        assertThrows(NullPointerException.class, () ->
                currencyService.getAllCurrencyRates(1L));

        verify(currencyRepository).getByCurrencyPoid(1L);
    }

    @Test
    void getAllCurrencyRates_ShouldHandleMultipleRates() {
        CurrencyRateEntity rate2 = new CurrencyRateEntity();
        rate2.setBuyRate(BigDecimal.valueOf(0.9));
        rate2.setSellRate(BigDecimal.valueOf(0.95));
        rate2.setRateDate(new java.sql.Date(System.currentTimeMillis() - 86400000));
        rate2.setCurrencyCode("USD");

        currencyEntity.setRates(List.of(rateEntity, rate2));
        List<CurrencyRateEntity> rateHistory = List.of(rateEntity, rate2);

        when(currencyRepository.getByCurrencyPoid(1L)).thenReturn(currencyEntity);
        when(currencyRateRepository.findAllByCurrencyCodeAndGroupPoid("USD", 1L)).thenReturn(rateHistory);

        CurrencyRateDto result = currencyService.getAllCurrencyRates(1L);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1.0), result.getBuyRate()); // First rate
        assertEquals(BigDecimal.valueOf(1.1), result.getSellRate()); // First rate
        assertEquals(2, result.getRateHistory().size());
    }

    @Test
    void createOrUpdateCurrency_ShouldHandleNullRequest() {
        when(currencyCreateRepository.createOrUpdateCurrency(null, 1L, 1L)).thenReturn(currencyEntity);

        CurrencyEntity result = currencyService.createOrUpdateCurrency(null, 1L, 1L);

        assertNotNull(result);
        verify(currencyCreateRepository).createOrUpdateCurrency(null, 1L, 1L);
    }

    @Test
    void createOrUpdateCurrency_ShouldHandleNullGroupPoid() {
        CurrencyCreateRequest request = new CurrencyCreateRequest();
        when(currencyCreateRepository.createOrUpdateCurrency(request, null, 1L)).thenReturn(currencyEntity);

        CurrencyEntity result = currencyService.createOrUpdateCurrency(request, null, 1L);

        assertNotNull(result);
        verify(currencyCreateRepository).createOrUpdateCurrency(request, null, 1L);
    }

    @Test
    void createOrUpdateCurrency_ShouldHandleNullUserPoid() {
        CurrencyCreateRequest request = new CurrencyCreateRequest();
        when(currencyCreateRepository.createOrUpdateCurrency(request, 1L, null)).thenReturn(currencyEntity);

        CurrencyEntity result = currencyService.createOrUpdateCurrency(request, 1L, null);

        assertNotNull(result);
        verify(currencyCreateRepository).createOrUpdateCurrency(request, 1L, null);
    }

    @Test
    void getCurrencyDetailsByPoid_ShouldHandleNullSeqno() {
        currencyEntity.setSeqno(null);
        when(currencyRepository.getByCurrencyPoid(1L)).thenReturn(currencyEntity);

        DetailsDto result = currencyService.getCurrencyDetailsByPoid(1L);

        assertNotNull(result);
        assertNull(result.seqNo());
    }

    @Test
    void getCurrencyDetailsByPoid_ShouldHandleNullCurrencyCode() {
        currencyEntity.setCurrencyCode(null);
        when(currencyRepository.getByCurrencyPoid(1L)).thenReturn(currencyEntity);

        DetailsDto result = currencyService.getCurrencyDetailsByPoid(1L);

        assertNotNull(result);
        assertNull(result.code());
    }

    @Test
    void getCurrencyDetailsByPoid_ShouldHandleNullCurrencyName() {
        currencyEntity.setCurrencyName(null);
        when(currencyRepository.getByCurrencyPoid(1L)).thenReturn(currencyEntity);

        DetailsDto result = currencyService.getCurrencyDetailsByPoid(1L);

        assertNotNull(result);
        assertNull(result.label());
        assertNull(result.description());
    }

    @Test
    void getAllCurrencyRates_ShouldHandleZeroId() {
        when(currencyRepository.getByCurrencyPoid(0L)).thenReturn(null);

        CurrencyRateDto result = currencyService.getAllCurrencyRates(0L);

        assertNull(result);
        verify(currencyRepository).getByCurrencyPoid(0L);
    }

    @Test
    void getAllCurrencyRates_ShouldHandleNegativeId() {
        when(currencyRepository.getByCurrencyPoid(-1L)).thenReturn(null);

        CurrencyRateDto result = currencyService.getAllCurrencyRates(-1L);

        assertNull(result);
        verify(currencyRepository).getByCurrencyPoid(-1L);
    }

//    @Test
//    void getCurrencyList_ShouldHandleRepositoryException() {
//        Pageable pageable = PageRequest.of(0, 10);
//        when(currencyRepository.findCurrencyList(anyList(), eq(pageable)))
//                .thenThrow(new RuntimeException("Database error"));
//
//        assertThrows(RuntimeException.class, () ->
//                currencyService.getCurrencyList(Collections.emptyList(), pageable));
//    }

    @Test
    void softDeleteCurrency_WithValidId_SoftDeletesCurrencyAndHardDeletesRates() {
        Long currencyPoid = 1L;
        List<CurrencyRateEntity> rates = List.of(rateEntity);

        when(currencyRepository.getByCurrencyPoid(currencyPoid)).thenReturn(currencyEntity);
        when(currencyRateRepository.findAllByCurrencyCode("USD")).thenReturn(rates);
        when(currencyRepository.save(any(CurrencyEntity.class))).thenReturn(currencyEntity);

        currencyService.softDeleteCurrency(currencyPoid);

        verify(currencyRepository).getByCurrencyPoid(currencyPoid);
        verify(currencyRepository).save(currencyEntity);
        verify(currencyRateRepository).findAllByCurrencyCode("USD");
        verify(currencyRateRepository).deleteAllInBatch(rates);
        assertEquals("Y", currencyEntity.getDeleted());
        assertEquals("N", currencyEntity.getActive());
        assertNotNull(currencyEntity.getLastModifiedDate());
    }

    @Test
    void softDeleteCurrency_WithNonExistentId_ThrowsException() {
        Long currencyPoid = 999L;
        when(currencyRepository.getByCurrencyPoid(currencyPoid)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> currencyService.softDeleteCurrency(currencyPoid));

        verify(currencyRepository).getByCurrencyPoid(currencyPoid);
        verify(currencyRepository, never()).save(any(CurrencyEntity.class));
        verify(currencyRateRepository, never()).findAllByCurrencyCode(anyString());
        verify(currencyRateRepository, never()).deleteAllInBatch(anyList());
    }

    @Test
    void softDeleteCurrency_WithNoRates_OnlySoftDeletesCurrency() {
        Long currencyPoid = 1L;
        List<CurrencyRateEntity> emptyRates = Collections.emptyList();

        when(currencyRepository.getByCurrencyPoid(currencyPoid)).thenReturn(currencyEntity);
        when(currencyRateRepository.findAllByCurrencyCode("USD")).thenReturn(emptyRates);
        when(currencyRepository.save(any(CurrencyEntity.class))).thenReturn(currencyEntity);

        currencyService.softDeleteCurrency(currencyPoid);

        verify(currencyRepository).getByCurrencyPoid(currencyPoid);
        verify(currencyRepository).save(currencyEntity);
        verify(currencyRateRepository).findAllByCurrencyCode("USD");
        verify(currencyRateRepository, never()).deleteAllInBatch(anyList());
        assertEquals("Y", currencyEntity.getDeleted());
        assertEquals("N", currencyEntity.getActive());
    }
}
