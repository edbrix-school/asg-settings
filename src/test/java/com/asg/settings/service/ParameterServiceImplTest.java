package com.asg.settings.service;

import com.asg.common.lib.enums.BulkUpdateStatus;
import com.asg.common.lib.enums.GlobalParameterTypeEnum;
import com.asg.common.lib.enums.ParameterUpdateStatus;
import com.asg.settings.dto.*;
import com.asg.settings.entity.GlobalParameterEntity;
import com.asg.settings.repository.GlobalParameterRepository;
import com.asg.settings.repository.ParameterRepository;
import com.asg.settings.service.impl.ParameterServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Parameter Service Implementation Tests")
class ParameterServiceImplTest {

    @Mock
    private ParameterRepository parameterRepository;

    @Mock
    private GlobalParameterRepository globalParameterRepository;

    @InjectMocks
    private ParameterServiceImpl parameterService;

    private UpdateParameterRequestDTO request;
    private UpdateParameterDTO parameter1;
    private UpdateParameterDTO parameter2;

    @BeforeEach
    void setUp() {
        parameter1 = new UpdateParameterDTO();
        parameter1.setParameterPoid(1L);
        parameter1.setParameterKeyId("KEY1");
        parameter1.setParameterValue("VALUE1");

        parameter2 = new UpdateParameterDTO();
        parameter2.setParameterPoid(2L);
        parameter2.setParameterKeyId("KEY2");
        parameter2.setParameterValue("VALUE2");

        request = new UpdateParameterRequestDTO();
        request.setLoginUserPoid(100L);
        request.setParameters(Arrays.asList(parameter1, parameter2));

    }

    private GlobalParameterEntity createMockEntity(Long id) {
        GlobalParameterEntity entity = new GlobalParameterEntity();
        entity.setParameterPoid(id);
        entity.setParameterName("PARAM_" + id);
        entity.setParameterValue("VALUE_" + id);
        return entity;
    }


    @Test
    @DisplayName("getSystemParameters should return system parameters with privilege check")
    void getSystemParameters_shouldReturnSystemParametersWithPrivilege() {
        Long userPoid = 1L;
        String filter = "system";
        Pageable pageable = PageRequest.of(0, 10);
        Page<GlobalParameterEntity> mockPage = new PageImpl<>(
                Arrays.asList(createMockEntity(1L), createMockEntity(2L)),
                pageable,
                2
        );
        when(globalParameterRepository.findAllByParameterType(
                eq(GlobalParameterTypeEnum.SYSTEM.name()),
                eq(filter),
                any(Pageable.class)
        )).thenReturn(mockPage);
        when(globalParameterRepository.hasSystemPrivilege(userPoid)).thenReturn(1);
        GlobalParameterResponse response = parameterService.getSystemParameters(
                userPoid, filter, pageable
        );
        assertNotNull(response);
        assertEquals(2, response.getGlobalParameters().size());
        assertTrue(response.getPrivileged());
        verify(globalParameterRepository).hasSystemPrivilege(userPoid);
    }


    @Test
    @DisplayName("getUserParameters should return user parameters with pagination")
    void getUserParameters_shouldReturnUserParameters() {
        // Given
        String filter = "test";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("parameterPoid").ascending());
        Page<GlobalParameterEntity> mockPage = new PageImpl<>(
                Arrays.asList(createMockEntity(1L), createMockEntity(2L)),
                pageable,
                2
        );

        when(globalParameterRepository.findAllByParameterType(
                eq(GlobalParameterTypeEnum.USER.name()),
                eq(filter),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        GlobalParameterResponse response = parameterService.getUserParameters(filter, pageable);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getGlobalParameters().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertNull(response.getPrivileged());

        verify(globalParameterRepository).findAllByParameterType(
                GlobalParameterTypeEnum.USER.name(),
                filter,
                pageable
        );
    }

    @Test
    @DisplayName("getSystemParameters should handle null userPoid")
    void getSystemParameters_withNullUserPoid_shouldNotCheckPrivilege() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<GlobalParameterEntity> mockPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(globalParameterRepository.findAllByParameterType(
                eq(GlobalParameterTypeEnum.SYSTEM.name()),
                any(),
                any(Pageable.class)
        )).thenReturn(mockPage);
        GlobalParameterResponse response = parameterService.getSystemParameters(
                null, "test", pageable
        );
        assertNotNull(response);
        assertNull(response.getPrivileged());
        verify(globalParameterRepository, never()).hasSystemPrivilege(anyLong());
    }

    @Test
    @DisplayName("getParameters should handle empty results")
    void getParameters_shouldHandleEmptyResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<GlobalParameterEntity> emptyPage = new PageImpl<>(
                Collections.emptyList(),
                pageable,
                0
        );
        when(globalParameterRepository.findAllByParameterType(
                anyString(), anyString(), any(Pageable.class)
        )).thenReturn(emptyPage);
        GlobalParameterResponse response = parameterService.getUserParameters("nonexistent", pageable);
        assertNotNull(response);
        assertTrue(response.getGlobalParameters().isEmpty());
        assertEquals(0, response.getTotalElements());
    }



    @Nested
    @DisplayName("Successful Operations")
    class SuccessfulOperations {

        @DisplayName("Should return SUCCESS when all parameters update successfully")
        @Test
        void updateParameters_AllSuccess_ShouldReturnSuccess() {
            when(parameterRepository.callUpdateProcedure(anyLong(), anyLong(), anyString(), anyString()))
                    .thenReturn("SUCCESS");

            BulkUpdateResponseDTO response = parameterService.updateParameters(request);

            assertNotNull(response);
            assertEquals(BulkUpdateStatus.SUCCESS.name(), response.getOverallStatus());
            assertEquals(2, response.getResults().size());

            response.getResults().forEach(result -> {
                assertEquals(ParameterUpdateStatus.SUCCESS, result.getStatus());
                assertNull(result.getErrorMessage());
            });

            verify(parameterRepository, times(2)).callUpdateProcedure(eq(100L), anyLong(), anyString(), anyString());
        }

        @DisplayName("Should return PARTIAL_SUCCESS when some parameters succeed")
        @Test
        void updateParameters_PartialSuccess_ShouldReturnPartialSuccess() {
            when(parameterRepository.callUpdateProcedure(100L, 1L, "KEY1", "VALUE1"))
                    .thenReturn("SUCCESS");
            when(parameterRepository.callUpdateProcedure(100L, 2L, "KEY2", "VALUE2"))
                    .thenReturn("FAILED");

            BulkUpdateResponseDTO response = parameterService.updateParameters(request);

            assertNotNull(response);
            assertEquals(BulkUpdateStatus.PARTIAL_SUCCESS.name(), response.getOverallStatus());
            assertEquals(2, response.getResults().size());

            ParameterUpdateResultDTO result1 = response.getResults().get(0);
            assertEquals(ParameterUpdateStatus.SUCCESS, result1.getStatus());
            assertNull(result1.getErrorMessage());

            ParameterUpdateResultDTO result2 = response.getResults().get(1);
            assertEquals(ParameterUpdateStatus.FAILED, result2.getStatus());
            assertEquals("Unknown error", result2.getErrorMessage());

            verify(parameterRepository, times(2)).callUpdateProcedure(anyLong(), anyLong(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Failure Scenarios")
    class FailureScenarios {

        @DisplayName("Should return FAILED when all parameters fail")
        @Test
        void updateParameters_AllFailed_ShouldReturnFailed() {
            when(parameterRepository.callUpdateProcedure(anyLong(), anyLong(), anyString(), anyString()))
                    .thenReturn("FAILED");

            BulkUpdateResponseDTO response = parameterService.updateParameters(request);

            assertNotNull(response);
            assertEquals(BulkUpdateStatus.FAILED.name(), response.getOverallStatus());
            assertEquals(2, response.getResults().size());

            response.getResults().forEach(result -> {
                assertEquals(ParameterUpdateStatus.FAILED, result.getStatus());
                assertEquals("Unknown error", result.getErrorMessage());
            });

            verify(parameterRepository, times(2)).callUpdateProcedure(anyLong(), anyLong(), anyString(), anyString());
        }

        @DisplayName("Should handle repository exceptions gracefully")
        @Test
        void updateParameters_RepositoryException_ShouldHandleGracefully() {
            when(parameterRepository.callUpdateProcedure(100L, 1L, "KEY1", "VALUE1"))
                    .thenThrow(new RuntimeException("Database error"));
            when(parameterRepository.callUpdateProcedure(100L, 2L, "KEY2", "VALUE2"))
                    .thenReturn("SUCCESS");

            BulkUpdateResponseDTO response = parameterService.updateParameters(request);

            assertNotNull(response);
            assertEquals(BulkUpdateStatus.PARTIAL_SUCCESS.name(), response.getOverallStatus());
            assertEquals(2, response.getResults().size());

            ParameterUpdateResultDTO result1 = response.getResults().get(0);
            assertEquals(ParameterUpdateStatus.FAILED, result1.getStatus());
            assertEquals("Database error", result1.getErrorMessage());

            ParameterUpdateResultDTO result2 = response.getResults().get(1);
            assertEquals(ParameterUpdateStatus.SUCCESS, result2.getStatus());
            assertNull(result2.getErrorMessage());

            verify(parameterRepository, times(2)).callUpdateProcedure(anyLong(), anyLong(), anyString(), anyString());
        }

        @DisplayName("Should handle null status from repository")
        @Test
        void updateParameters_NullStatus_ShouldReturnFailed() {
            when(parameterRepository.callUpdateProcedure(anyLong(), anyLong(), anyString(), anyString()))
                    .thenReturn(null);

            BulkUpdateResponseDTO response = parameterService.updateParameters(request);

            assertNotNull(response);
            assertEquals(BulkUpdateStatus.FAILED.name(), response.getOverallStatus());
            assertEquals(2, response.getResults().size());

            response.getResults().forEach(result -> {
                assertEquals(ParameterUpdateStatus.FAILED, result.getStatus());
                assertEquals("Unknown error", result.getErrorMessage());
            });

            verify(parameterRepository, times(2)).callUpdateProcedure(anyLong(), anyLong(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @DisplayName("Should handle single parameter successfully")
        @Test
        void updateParameters_SingleParameter_ShouldWork() {
            request.setParameters(Collections.singletonList(parameter1));
            when(parameterRepository.callUpdateProcedure(100L, 1L, "KEY1", "VALUE1"))
                    .thenReturn("SUCCESS");

            BulkUpdateResponseDTO response = parameterService.updateParameters(request);

            assertNotNull(response);
            assertEquals(BulkUpdateStatus.SUCCESS.name(), response.getOverallStatus());
            assertEquals(1, response.getResults().size());
            assertEquals(ParameterUpdateStatus.SUCCESS, response.getResults().get(0).getStatus());

            verify(parameterRepository, times(1)).callUpdateProcedure(100L, 1L, "KEY1", "VALUE1");
        }

        @DisplayName("Should handle invalid status strings")
        @Test
        void updateParameters_InvalidStatus_ShouldReturnFailed() {
            when(parameterRepository.callUpdateProcedure(anyLong(), anyLong(), anyString(), anyString()))
                    .thenReturn("INVALID_STATUS");

            BulkUpdateResponseDTO response = parameterService.updateParameters(request);

            assertNotNull(response);
            assertEquals(BulkUpdateStatus.FAILED.name(), response.getOverallStatus());
            assertEquals(2, response.getResults().size());

            response.getResults().forEach(result -> {
                assertEquals(ParameterUpdateStatus.FAILED, result.getStatus());
                assertEquals("Unknown error", result.getErrorMessage());
            });

            verify(parameterRepository, times(2)).callUpdateProcedure(anyLong(), anyLong(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Response Validation")
    class ResponseValidation {

        @DisplayName("Should populate all result fields correctly")
        @Test
        void updateParameters_ShouldPopulateResultFieldsCorrectly() {
            when(parameterRepository.callUpdateProcedure(100L, 1L, "KEY1", "VALUE1"))
                    .thenReturn("SUCCESS");

            request.setParameters(Collections.singletonList(parameter1));
            BulkUpdateResponseDTO response = parameterService.updateParameters(request);

            ParameterUpdateResultDTO result = response.getResults().get(0);
            assertEquals(1L, result.getParameterPoid());
            assertEquals("KEY1", result.getParameterKeyId());
            assertEquals(ParameterUpdateStatus.SUCCESS, result.getStatus());
            assertNull(result.getErrorMessage());
        }

        @DisplayName("Should verify repository method calls with correct parameters")
        @Test
        void updateParameters_ShouldCallRepositoryWithCorrectParameters() {
            when(parameterRepository.callUpdateProcedure(anyLong(), anyLong(), anyString(), anyString()))
                    .thenReturn("SUCCESS");

            parameterService.updateParameters(request);

            verify(parameterRepository).callUpdateProcedure(100L, 1L, "KEY1", "VALUE1");
            verify(parameterRepository).callUpdateProcedure(100L, 2L, "KEY2", "VALUE2");
            verifyNoMoreInteractions(parameterRepository);
        }
    }

}
