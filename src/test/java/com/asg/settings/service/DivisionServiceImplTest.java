package com.asg.settings.service;

import com.asg.common.lib.service.LoggingService;
import com.asg.settings.dto.request.DivisionCreateRequest;
import com.asg.settings.dto.request.DivisionUpdateRequest;
import com.asg.settings.dto.response.DivisionResponse;
import com.asg.settings.entity.DivisionMasterEntity;
import com.asg.settings.repository.DivisionRepository;
import com.asg.settings.service.impl.DivisionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DivisionServiceImplTest {

    @Mock
    private DivisionRepository divisionRepository;

    @Mock
    private DocumentService documentService;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private DivisionServiceImpl divisionService;

    private DivisionCreateRequest createRequest;
    private DivisionMasterEntity entity1;
    private DivisionUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(divisionService, "loggingService", loggingService);
        
        createRequest = new DivisionCreateRequest();
        createRequest.setDivisionCode("FIN01");
        createRequest.setDivisionName("Finance");
        createRequest.setRemarks("Handles finance operations");
        createRequest.setSeqNo(1);
        createRequest.setActive("Y");
        createRequest.setCreatedBy("Admin");

        entity1 = new DivisionMasterEntity();
        entity1.setDivisionId(1L);
        entity1.setDivisionCode("FIN01");
        entity1.setDivisionName("Finance");
        entity1.setDescription("Handles finance operations");
        entity1.setSeqNo(1);
        entity1.setActive("Y");
        entity1.setDeleted(0);
        entity1.setCreatedBy("Admin");
        entity1.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        updateRequest = new DivisionUpdateRequest();
        updateRequest.setDivisionName("Finance Updated");
        updateRequest.setRemarks("Updated description");
        updateRequest.setSeqNo(2);
        updateRequest.setActive("N");
        updateRequest.setUpdatedBy("AdminUpdate");
    }

    @Test
    void createDivision_shouldReturnDivisionResponse_whenSavedSuccessfully() {
        DivisionMasterEntity savedEntity = new DivisionMasterEntity();
        savedEntity.setDivisionId(1L);
        savedEntity.setDivisionCode("FIN01");
        savedEntity.setDivisionName("Finance");
        savedEntity.setDescription("Handles finance operations");
        savedEntity.setActive("Y");

        when(divisionRepository.save(any(DivisionMasterEntity.class))).thenReturn(savedEntity);

        DivisionResponse response = divisionService.createDivision(createRequest);

        assertNotNull(response);
        assertEquals(1L, response.getDivisionId());
        assertEquals("FIN01", response.getDivisionCode());
        assertEquals("Finance", response.getDivisionName());
        assertEquals("Handles finance operations", response.getRemarks());
        assertEquals("Y", response.getActive());
        verify(divisionRepository).save(any(DivisionMasterEntity.class));
    }

    @Test
    void createDivision_WithInvalidActiveFlag_ShouldThrowException() {
        createRequest.setActive("X");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> divisionService.createDivision(createRequest));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Active must be Y or N", exception.getReason());
        verify(divisionRepository, never()).save(any(DivisionMasterEntity.class));
    }

    @Test
    void createDivision_WithNullActive_ShouldDefaultToN() {
        createRequest.setActive(null);

        DivisionMasterEntity savedEntity = new DivisionMasterEntity();
        savedEntity.setDivisionId(1L);
        savedEntity.setActive("N");

        when(divisionRepository.save(any(DivisionMasterEntity.class))).thenReturn(savedEntity);

        DivisionResponse response = divisionService.createDivision(createRequest);

        assertNotNull(response);
        assertEquals("N", response.getActive());

        ArgumentCaptor<DivisionMasterEntity> captor = ArgumentCaptor.forClass(DivisionMasterEntity.class);
        verify(divisionRepository).save(captor.capture());
        assertEquals("N", captor.getValue().getActive());
    }

    @Test
    void createDivision_WithEmptyActiveFlag_ShouldThrowException() {
        createRequest.setActive("");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> divisionService.createDivision(createRequest));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Active must be Y or N", exception.getReason());
    }

    @Test
    void createDivision_WithLowercaseActiveFlag_ShouldThrowException() {
        createRequest.setActive("y");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> divisionService.createDivision(createRequest));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Active must be Y or N", exception.getReason());
    }

    @Test
    void createDivision_WithRepositoryException_ShouldPropagateException() {
        when(divisionRepository.save(any(DivisionMasterEntity.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> divisionService.createDivision(createRequest));

        assertEquals("Database connection failed", exception.getMessage());
    }

//    @Test
//    void getAllDivisions_shouldReturnMappedPage() {
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<DivisionMasterEntity> pageEntities = new PageImpl<>(List.of(entity1), pageable, 1);
//
//        when(divisionRepository.findDivisions(anyList(), any(Pageable.class))).thenReturn(pageEntities);
//
//        Page<DivisionResponse> result = divisionService.getAllDivisions(List.of(), pageable);
//
//        assertNotNull(result);
//        assertEquals(1, result.getContent().size());
//        DivisionResponse first = result.getContent().get(0);
//        assertEquals(entity1.getDivisionId(), first.getDivisionId());
//        assertEquals(entity1.getDivisionCode(), first.getDivisionCode());
//        assertEquals(entity1.getDivisionName(), first.getDivisionName());
//        assertEquals(entity1.getDescription(), first.getRemarks());
//        assertEquals(entity1.getActive(), first.getActive());
//        verify(divisionRepository).findDivisions(anyList(), any(Pageable.class));
//    }

//    @Test
//    void getAllDivisions_WithEmptyPage_ShouldReturnEmptyPage() {
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<DivisionMasterEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
//
//        when(divisionRepository.findDivisions(anyList(), any(Pageable.class))).thenReturn(emptyPage);
//
//        Page<DivisionResponse> result = divisionService.getAllDivisions(List.of(), pageable);
//
//        assertNotNull(result);
//        assertTrue(result.getContent().isEmpty());
//        assertEquals(0, result.getTotalElements());
//    }

//    @Test
//    void getAllDivisions_WithRepositoryException_ShouldPropagateException() {
//        Pageable pageable = PageRequest.of(0, 10);
//        when(divisionRepository.findDivisions(anyList(), any(Pageable.class)))
//                .thenThrow(new RuntimeException("Query timeout"));
//
//        RuntimeException exception = assertThrows(RuntimeException.class,
//                () -> divisionService.getAllDivisions(List.of(), pageable));
//
//        assertEquals("Query timeout", exception.getMessage());
//    }

    @Test
    void getDivisionById_shouldReturnDivision_whenExists() {
        entity1.setDeleted(0);
        when(divisionRepository.findById(1L))
                .thenReturn(Optional.of(entity1));

        Optional<DivisionResponse> responseOpt = divisionService.getDivisionById(1L);

        assertTrue(responseOpt.isPresent());
        DivisionResponse response = responseOpt.get();
        assertEquals(entity1.getDivisionId(), response.getDivisionId());
        assertEquals(entity1.getDivisionCode(), response.getDivisionCode());
        assertEquals(entity1.getDivisionName(), response.getDivisionName());
        assertEquals(entity1.getDescription(), response.getRemarks());
        assertEquals(entity1.getActive(), response.getActive());
    }

    @Test
    void getDivisionById_shouldReturnEmpty_whenNotExists() {
        when(divisionRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<DivisionResponse> responseOpt = divisionService.getDivisionById(99L);

        assertTrue(responseOpt.isEmpty());
    }

    @Test
    void getDivisionById_WithRepositoryException_ShouldPropagateException() {
        when(divisionRepository.findById(1L))
                .thenThrow(new RuntimeException("Connection lost"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> divisionService.getDivisionById(1L));

        assertEquals("Connection lost", exception.getMessage());
    }

    @Test
    void updateDivision_shouldReturnUpdatedDivision_whenSuccessful() {
        entity1.setDeleted(0);
        when(divisionRepository.findById(1L))
                .thenReturn(Optional.of(entity1));
        when(divisionRepository.save(any(DivisionMasterEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DivisionResponse response = divisionService.updateDivision(1L, updateRequest);

        assertNotNull(response);
        assertEquals(entity1.getDivisionId(), response.getDivisionId());
        assertEquals(updateRequest.getDivisionName(), response.getDivisionName());
        assertEquals(updateRequest.getRemarks(), response.getRemarks());
        assertEquals(updateRequest.getSeqNo(), response.getSeqNo());
        assertEquals(updateRequest.getActive(), response.getActive());
        verify(divisionRepository).findById(1L);
        verify(divisionRepository).save(any(DivisionMasterEntity.class));
    }

    @Test
    void updateDivision_shouldThrowException_whenDivisionNotFound() {
        when(divisionRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> divisionService.updateDivision(99L, updateRequest));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Division not found", exception.getReason());
        verify(divisionRepository, never()).save(any(DivisionMasterEntity.class));
    }

    @Test
    void updateDivision_WithInvalidActiveFlag_ShouldThrowException() {
        updateRequest.setActive("X");
        entity1.setDeleted(0);

        when(divisionRepository.findById(1L))
                .thenReturn(Optional.of(entity1));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> divisionService.updateDivision(1L, updateRequest));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Active must be Y or N", exception.getReason());
        verify(divisionRepository, never()).save(any(DivisionMasterEntity.class));
    }

    @Test
    void updateDivision_WithPartialUpdate_ShouldUpdateOnlyProvidedFields() {
        DivisionUpdateRequest partialRequest = new DivisionUpdateRequest();
        partialRequest.setDivisionName("Updated Name");
        partialRequest.setUpdatedBy("Admin");
        entity1.setDeleted(0);

        when(divisionRepository.findById(1L))
                .thenReturn(Optional.of(entity1));
        when(divisionRepository.save(any(DivisionMasterEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DivisionResponse response = divisionService.updateDivision(1L, partialRequest);

        assertNotNull(response);
        assertEquals("Updated Name", response.getDivisionName());
        assertEquals("Handles finance operations", response.getRemarks()); // unchanged
        assertEquals(1, response.getSeqNo()); // unchanged
        assertEquals("Y", response.getActive()); // unchanged
    }

    @Test
    void softDeleteDivision_shouldMarkDeletedAndUpdateAuditFields() {
        entity1.setDeleted(0);
        when(divisionRepository.findById(1L))
                .thenReturn(Optional.of(entity1));
        when(divisionRepository.save(any(DivisionMasterEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        divisionService.softDeleteDivision(1L, "Admin");

        assertEquals(1, entity1.getDeleted());
        assertEquals("Admin", entity1.getUpdatedBy());
        assertNotNull(entity1.getUpdatedAt());
        verify(divisionRepository).save(entity1);
    }

    @Test
    void softDeleteDivision_shouldThrowException_whenDivisionNotFound() {
        when(divisionRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> divisionService.softDeleteDivision(99L, "Admin"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Division not found", exception.getReason());
        verify(divisionRepository, never()).save(any());
    }

    @Test
    void activateDivision_shouldSetActiveToY() {
        entity1.setDeleted(0);
        when(divisionRepository.findById(1L))
                .thenReturn(Optional.of(entity1));
        when(divisionRepository.save(any(DivisionMasterEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        divisionService.activateDivision(1L, "Admin");

        assertEquals("Y", entity1.getActive());
        assertEquals("Admin", entity1.getUpdatedBy());
        assertNotNull(entity1.getUpdatedAt());
        verify(divisionRepository).save(entity1);
    }

    @Test
    void activateDivision_shouldThrowException_whenDivisionNotFound() {
        when(divisionRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> divisionService.activateDivision(99L, "Admin"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Division not found", exception.getReason());
        verify(divisionRepository, never()).save(any(DivisionMasterEntity.class));
    }

    @Test
    void deactivateDivision_shouldSetActiveToN() {
        entity1.setDeleted(0);
        when(divisionRepository.findById(1L))
                .thenReturn(Optional.of(entity1));
        when(divisionRepository.save(any(DivisionMasterEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        divisionService.deactivateDivision(1L, "Admin");

        assertEquals("N", entity1.getActive());
        assertEquals("Admin", entity1.getUpdatedBy());
        assertNotNull(entity1.getUpdatedAt());
        verify(divisionRepository).save(entity1);
    }

    @Test
    void deactivateDivision_shouldThrowException_whenDivisionNotFound() {
        when(divisionRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> divisionService.deactivateDivision(99L, "Admin"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Division not found", exception.getReason());
        verify(divisionRepository, never()).save(any());
    }

    @Test
    void existsByDivisionCodeAndDeleted_ShouldReturnRepositoryResult() {
        when(divisionRepository.existsByDivisionCodeAndDeleted("FIN01", 0))
                .thenReturn(true);

        boolean result = divisionService.existsByDivisionCodeAndDeleted("FIN01", 0);

        assertTrue(result);
        verify(divisionRepository).existsByDivisionCodeAndDeleted("FIN01", 0);
    }

    @Test
    void existsByDivisionCodeAndDeleted_WithNullCode_ShouldReturnFalse() {
        when(divisionRepository.existsByDivisionCodeAndDeleted(null, 0))
                .thenReturn(false);

        boolean result = divisionService.existsByDivisionCodeAndDeleted(null, 0);

        assertFalse(result);
    }

    @Test
    void existsByDivisionCodeAndDeleted_WithEmptyCode_ShouldReturnFalse() {
        when(divisionRepository.existsByDivisionCodeAndDeleted("", 0))
                .thenReturn(false);

        boolean result = divisionService.existsByDivisionCodeAndDeleted("", 0);

        assertFalse(result);
    }
}
