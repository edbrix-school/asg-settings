package com.asg.settings.service;

import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.AlertCheckTypeEnum;
import com.asg.common.lib.enums.FrequencyTypeEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.ASGHelperUtils;
import com.asg.settings.dto.AlertAndRemainderDto;
import com.asg.settings.entity.AlertConfigEntity;
import com.asg.settings.entity.RoleEntity;
import com.asg.settings.repository.AlertConfigRepository;
import com.asg.settings.repository.RoleRepository;
import com.asg.settings.service.impl.AlertConfigServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertConfigServiceImplTest {

    @Mock
    private AlertConfigRepository alertConfigRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DocumentSearchService documentService;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private AlertConfigServiceImpl alertConfigService;

    private AlertAndRemainderDto alertAndRemainderDto;
    private AlertConfigEntity alertConfigEntity;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(alertConfigService, "loggingService", loggingService);
        
        alertAndRemainderDto = new AlertAndRemainderDto();
        alertAndRemainderDto.setAlertName("Test Alert");
        alertAndRemainderDto.setSqlQuery("SELECT * FROM test_table");
        alertAndRemainderDto.setNotifyUserRolesPoid(Arrays.asList("123", "456"));
        alertAndRemainderDto.setFrequencyType(FrequencyTypeEnum.DAY);
        alertAndRemainderDto.setAlertCheckType(AlertCheckTypeEnum.DATECHECK);

        alertConfigEntity = new AlertConfigEntity();
        alertConfigEntity.setConfigPoid(1L);
        alertConfigEntity.setAlertName("Test Alert");
        alertConfigEntity.setSqlQuery("SELECT * FROM test_table");
        alertConfigEntity.setActive("Y");
        alertConfigEntity.setCreatedBy("testUser");
        alertConfigEntity.setCreatedDate(LocalDateTime.now());
        alertConfigEntity.setFrequencyType("DAY");
        alertConfigEntity.setAlertCheckType("DateCheck");
        alertConfigEntity.setNotifyUserRolesPoid("123;456");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void testGetAllAlertConfigs_success() {
        FilterRequestDto request = new FilterRequestDto("AND", "N", Collections.emptyList());
        RawSearchResult rawResult = new RawSearchResult(
                Arrays.asList(Map.of("ALERT_NAME", "Test Alert", "CONFIG_POID", 1L)),
                Map.of("ALERT_NAME", "Alert Name", "CONFIG_POID", "Config ID"),
                1L
        );

        when(documentService.resolveOperator(request)).thenReturn("AND");
        when(documentService.resolveIsDeleted(request)).thenReturn("N");
        when(documentService.resolveFilters(request)).thenReturn(Collections.emptyList());

        when(documentService.search(anyString(), any(), anyString(), any(Pageable.class), anyString(), anyString(), anyString()))
                .thenReturn(rawResult);

        Map<String, Object> result = alertConfigService.getAllAlertConfigs("DOC123", request, pageable);

        assertNotNull(result);
        verify(documentService).search(anyString(), any(), anyString(), any(Pageable.class), anyString(), anyString(), anyString());
    }

    @Test
    void testCreateAlert_success() {
        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class);
             MockedStatic<ASGHelperUtils> helperMock = mockStatic(ASGHelperUtils.class)) {
            userContextMock.when(UserContext::getUserId).thenReturn("100");
            helperMock.when(() -> ASGHelperUtils.convertListToString(any())).thenReturn("123;456");
            helperMock.when(() -> ASGHelperUtils.convertFromStringToList(anyString())).thenReturn(Arrays.asList("123", "456"));

            when(alertConfigRepository.save(any(AlertConfigEntity.class))).thenReturn(alertConfigEntity);

            AlertAndRemainderDto result = alertConfigService.createAlert(alertAndRemainderDto);

            assertNotNull(result);
            assertEquals(1L, result.getConfigPoid());
            assertEquals("Test Alert", result.getAlertName());
            verify(alertConfigRepository).save(any(AlertConfigEntity.class));
        }
    }

    @Test
    void testGetByAlertConfigId_success() {
        try (MockedStatic<ASGHelperUtils> helperMock = mockStatic(ASGHelperUtils.class)) {
            RoleEntity roleEntity = new RoleEntity();
            roleEntity.setUserRolePoid(123L);
            roleEntity.setUserRoleName("Test Role");
            roleEntity.setActive("Y");

            helperMock.when(() -> ASGHelperUtils.convertFromStringToList("123;456")).thenReturn(Arrays.asList("123", "456"));
            when(alertConfigRepository.findByConfigPoid(1L)).thenReturn(alertConfigEntity);
            when(roleRepository.findByUserRolePoid(123L)).thenReturn(roleEntity);
            when(roleRepository.findByUserRolePoid(456L)).thenReturn(roleEntity);

            AlertAndRemainderDto result = alertConfigService.getByAlertConfigId(1L);

            assertNotNull(result);
            assertEquals("Test Alert", result.getAlertName());
            verify(alertConfigRepository).findByConfigPoid(1L);
        }
    }

    @Test
    void testGetByAlertConfigId_notFound_throwsException() {
        when(alertConfigRepository.findByConfigPoid(999L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> {
            alertConfigService.getByAlertConfigId(999L);
        });

        verify(alertConfigRepository).findByConfigPoid(999L);
    }

    @Test
    void testUpdateAlertConfig_success() {
        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class);
             MockedStatic<ASGHelperUtils> helperMock = mockStatic(ASGHelperUtils.class)) {
            Long configPoid = 1L;
            AlertAndRemainderDto request = new AlertAndRemainderDto();
            request.setAlertName("Updated Alert");
            request.setAlertCheckType(AlertCheckTypeEnum.DATECHECK);
            request.setFrequencyType(FrequencyTypeEnum.DAY);
            request.setNotifyUserRolesPoid(Arrays.asList("123"));

            userContextMock.when(UserContext::getUserId).thenReturn("100");
            helperMock.when(() -> ASGHelperUtils.convertListToString(any())).thenReturn("123");
            when(alertConfigRepository.findByConfigPoid(configPoid)).thenReturn(alertConfigEntity);
            when(alertConfigRepository.save(any(AlertConfigEntity.class))).thenReturn(alertConfigEntity);

            AlertAndRemainderDto result = alertConfigService.updateAlertConfig(configPoid, request);

            assertNotNull(result);
            verify(alertConfigRepository).findByConfigPoid(configPoid);
            verify(alertConfigRepository).save(any(AlertConfigEntity.class));
        }
    }

    @Test
    void testUpdateAlertConfig_notFound_throwsException() {
        when(alertConfigRepository.findByConfigPoid(999L)).thenReturn(null);

        AlertAndRemainderDto request = new AlertAndRemainderDto();
        request.setAlertName("Updated Alert");
        request.setAlertCheckType(AlertCheckTypeEnum.DATECHECK);
        request.setFrequencyType(FrequencyTypeEnum.DAY);

        assertThrows(ResourceNotFoundException.class, () -> {
            alertConfigService.updateAlertConfig(999L, request);
        });

        verify(alertConfigRepository).findByConfigPoid(999L);
        verify(alertConfigRepository, never()).save(any(AlertConfigEntity.class));
    }

    @Test
    void testGetInactiveAndDeletedAlerts_success() {
        List<AlertConfigEntity> entities = Arrays.asList(alertConfigEntity);
        when(alertConfigRepository.findAllByActiveAndDeleted("N", "Y")).thenReturn(entities);

        List<AlertAndRemainderDto> result = alertConfigService.getInactiveAndDeletedAlerts();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(alertConfigRepository).findAllByActiveAndDeleted("N", "Y");
    }

    @Test
    void testSoftDeleteByconfigPoid_success() {
        try (MockedStatic<ASGHelperUtils> helperMock = mockStatic(ASGHelperUtils.class)) {
            helperMock.when(ASGHelperUtils::getCurrentUser).thenReturn("testUser");
            when(alertConfigRepository.findByConfigPoid(1L)).thenReturn(alertConfigEntity);
            when(alertConfigRepository.save(any(AlertConfigEntity.class))).thenReturn(alertConfigEntity);

            boolean result = alertConfigService.softDeleteByconfigPoid(1L);

            assertTrue(result);
            verify(alertConfigRepository).findByConfigPoid(1L);
            verify(alertConfigRepository).save(any(AlertConfigEntity.class));
        }
    }

    @Test
    void testSoftDeleteByconfigPoid_notFound_throwsException() {
        when(alertConfigRepository.findByConfigPoid(999L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> {
            alertConfigService.softDeleteByconfigPoid(999L);
        });

        verify(alertConfigRepository).findByConfigPoid(999L);
        verify(alertConfigRepository, never()).save(any(AlertConfigEntity.class));
    }

    @Test
    void testCreateAlert_withNullFields() {
        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class);
             MockedStatic<ASGHelperUtils> helperMock = mockStatic(ASGHelperUtils.class)) {
            AlertAndRemainderDto dto = new AlertAndRemainderDto();
            dto.setAlertName("Test Alert");
            dto.setSqlQuery("SELECT * FROM test");

            userContextMock.when(UserContext::getUserId).thenReturn(null);
            helperMock.when(() -> ASGHelperUtils.convertListToString(null)).thenReturn(null);

            AlertConfigEntity savedEntity = new AlertConfigEntity();
            savedEntity.setConfigPoid(1L);
            savedEntity.setCreatedBy("SYSTEM");

            when(alertConfigRepository.save(any(AlertConfigEntity.class))).thenReturn(savedEntity);

            AlertAndRemainderDto result = alertConfigService.createAlert(dto);

            assertNotNull(result);
            verify(alertConfigRepository).save(any(AlertConfigEntity.class));
        }
    }

    @Test
    void testGetByAlertConfigId_withNullRolesPoid() {
        try (MockedStatic<ASGHelperUtils> helperMock = mockStatic(ASGHelperUtils.class)) {
            AlertConfigEntity entity = new AlertConfigEntity();
            entity.setConfigPoid(1L);
            entity.setAlertName("Test Alert");
            entity.setNotifyUserRolesPoid(null);
            entity.setEscalateUserRolesPoid(null);

            helperMock.when(() -> ASGHelperUtils.convertFromStringToList(null)).thenReturn(Collections.emptyList());
            when(alertConfigRepository.findByConfigPoid(1L)).thenReturn(entity);

            AlertAndRemainderDto result = alertConfigService.getByAlertConfigId(1L);

            assertNotNull(result);
            assertEquals("Test Alert", result.getAlertName());
        }
    }

    @Test
    void testGetByAlertConfigId_withEmptyRolesPoid() {
        try (MockedStatic<ASGHelperUtils> helperMock = mockStatic(ASGHelperUtils.class)) {
            AlertConfigEntity entity = new AlertConfigEntity();
            entity.setConfigPoid(1L);
            entity.setAlertName("Test Alert");
            entity.setNotifyUserRolesPoid("");
            entity.setEscalateUserRolesPoid("");

            helperMock.when(() -> ASGHelperUtils.convertFromStringToList("")).thenReturn(Collections.emptyList());
            when(alertConfigRepository.findByConfigPoid(1L)).thenReturn(entity);

            AlertAndRemainderDto result = alertConfigService.getByAlertConfigId(1L);

            assertNotNull(result);
            assertEquals("Test Alert", result.getAlertName());
        }
    }

    @Test
    void testGetByAlertConfigId_withNonExistentRoles() {
        try (MockedStatic<ASGHelperUtils> helperMock = mockStatic(ASGHelperUtils.class)) {
            AlertConfigEntity entity = new AlertConfigEntity();
            entity.setConfigPoid(1L);
            entity.setAlertName("Test Alert");
            entity.setNotifyUserRolesPoid("999;888");

            helperMock.when(() -> ASGHelperUtils.convertFromStringToList("999;888")).thenReturn(Arrays.asList("999", "888"));
            when(alertConfigRepository.findByConfigPoid(1L)).thenReturn(entity);
            when(roleRepository.findByUserRolePoid(999L)).thenReturn(null);
            when(roleRepository.findByUserRolePoid(888L)).thenReturn(null);

            AlertAndRemainderDto result = alertConfigService.getByAlertConfigId(1L);

            assertNotNull(result);
            assertTrue(result.getNotifyUserRolesPoidDet().isEmpty());
        }
    }

    @Test
    void testUpdateAlertConfig_withNullEscalateFrequency() {
        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class);
             MockedStatic<ASGHelperUtils> helperMock = mockStatic(ASGHelperUtils.class)) {
            AlertAndRemainderDto request = new AlertAndRemainderDto();
            request.setAlertName("Updated Alert");
            request.setAlertCheckType(AlertCheckTypeEnum.DATECHECK);
            request.setFrequencyType(FrequencyTypeEnum.DAY);
            request.setAlertEscalateFrequency(null);

            userContextMock.when(UserContext::getUserId).thenReturn("100");
            helperMock.when(() -> ASGHelperUtils.convertListToString(any())).thenReturn("");
            when(alertConfigRepository.findByConfigPoid(1L)).thenReturn(alertConfigEntity);
            when(alertConfigRepository.save(any(AlertConfigEntity.class))).thenReturn(alertConfigEntity);

            AlertAndRemainderDto result = alertConfigService.updateAlertConfig(1L, request);

            assertNotNull(result);
            verify(alertConfigRepository).save(any(AlertConfigEntity.class));
        }
    }

    @Test
    void testGetInactiveAndDeletedAlerts_emptyResult() {
        when(alertConfigRepository.findAllByActiveAndDeleted("N", "Y")).thenReturn(Collections.emptyList());

        List<AlertAndRemainderDto> result = alertConfigService.getInactiveAndDeletedAlerts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllAlertConfigs_withNullRequest() {
        RawSearchResult rawResult = new RawSearchResult(
                Collections.emptyList(),
                Collections.emptyMap(),
                0L
        );

        when(documentService.resolveOperator(null)).thenReturn("OR");
        when(documentService.resolveIsDeleted(null)).thenReturn("N");
        when(documentService.resolveFilters(null)).thenReturn(Collections.emptyList());

        when(documentService.search(anyString(), any(), anyString(), any(Pageable.class), anyString(), anyString(), anyString()))
                .thenReturn(rawResult);

        Map<String, Object> result = alertConfigService.getAllAlertConfigs("DOC123", null, pageable);

        assertNotNull(result);
    }

    @Test
    void testCreateAlert_withAllOptionalFields() {
        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class);
             MockedStatic<ASGHelperUtils> helperMock = mockStatic(ASGHelperUtils.class)) {
            AlertAndRemainderDto dto = new AlertAndRemainderDto();
            dto.setAlertName("Complete Alert");
            dto.setSqlQuery("SELECT * FROM test");
            dto.setExpiryDateField("EXPIRY_DATE");
            dto.setNotifyDays(5);
            dto.setActive("Y");
            dto.setSeqNo(10);
            dto.setEscalateDays(3);
            dto.setDeleted("N");
            dto.setAlertEscalateFrequency(2);
            dto.setAlertNotifyFrequency(1);
            dto.setDailyRecurrence(7);

            userContextMock.when(UserContext::getUserId).thenReturn("100");
            helperMock.when(() -> ASGHelperUtils.convertListToString(any())).thenReturn("123");

            AlertConfigEntity savedEntity = new AlertConfigEntity();
            savedEntity.setConfigPoid(1L);
            savedEntity.setAlertName("Complete Alert");

            when(alertConfigRepository.save(any(AlertConfigEntity.class))).thenReturn(savedEntity);

            AlertAndRemainderDto result = alertConfigService.createAlert(dto);

            assertNotNull(result);
            assertEquals(1L, result.getConfigPoid());
        }
    }
}
