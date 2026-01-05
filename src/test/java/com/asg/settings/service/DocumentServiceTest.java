package com.asg.settings.service;

import com.asg.common.lib.dto.DropdownStringDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.entity.DocumentEntity;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.repository.TableMetaRepository;
import com.asg.settings.dto.DocumentDto;
import com.asg.settings.entity.GlobalModuleMasterEntity;
import com.asg.settings.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentApprovalDtlRepository documentApprovalDtlRepository;

    @Mock
    private DocumentAuthDtlRepository documentAuthDtlRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private TableMetaRepository tableMetaRepository;

    @Mock
    private GlobalModuleMasterRepository globalModuleMasterRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void getInactiveAndDeletedDocuments_WhenDocumentsExist_ShouldReturnList() {
        DocumentEntity doc1 = createTestDocument(1L, "Doc 1", "N", "Y");
        DocumentEntity doc2 = createTestDocument(2L, "Doc 2", "N", "Y");
        List<DocumentEntity> mockDocuments = Arrays.asList(doc1, doc2);

        when(documentRepository.findAllByActiveAndDeleted("N", "Y"))
                .thenReturn(mockDocuments);

        List<DocumentDto> result = documentService.getInactiveAndDeletedDocuments();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getDocId());
        assertEquals("Doc 1", result.get(0).getDocName());
        verify(documentRepository, times(1)).findAllByActiveAndDeleted("N", "Y");
    }

    @Test
    void getInactiveAndDeletedDocuments_WhenNoDocuments_ShouldReturnEmptyList() {

        when(documentRepository.findAllByActiveAndDeleted("N", "Y"))
                .thenReturn(Collections.emptyList());

        List<DocumentDto> result = documentService.getInactiveAndDeletedDocuments();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(documentRepository, times(1)).findAllByActiveAndDeleted("N", "Y");
    }

    @Test
    void getDocumentById_WhenDocumentExists_ShouldReturnDto() {
        DocumentEntity doc = createTestDocument(1L, "Test Doc", "Y", "N");
        when(documentRepository.findByDocId("1")).thenReturn(doc);
        when(documentApprovalDtlRepository.findAllById_DocId("1")).thenReturn(Collections.emptyList());
        when(documentAuthDtlRepository.findAllById_DocId("1")).thenReturn(Collections.emptyList());

        DocumentDto result = documentService.getDocumentById("1");

        assertNotNull(result);
        assertEquals("1", result.getDocId());
        assertEquals("Test Doc", result.getDocName());
    }

    @Test
    void getSearchableFieldsForDropdown_WhenDocumentExists_ShouldReturnFields() {
        DocumentEntity doc = createTestDocument(1L, "Test Doc", "Y", "N");
        doc.setListOfRecordsSql("SELECT * FROM test_table");

        when(documentRepository.findByDocId("1")).thenReturn(doc);
        when(tableMetaRepository.getColumnsFromSql(anyString()))
                .thenReturn(Arrays.asList("DOC_ID", "DOC_NAME"));

        List<DropdownStringDto> result = documentService.getSearchableFieldsForDropdown("1");

        assertNotNull(result);
        assertEquals(3, result.size()); // GLOBALSEARCH + 2 columns
        assertEquals("GLOBALSEARCH", result.get(0).getValue());
        assertEquals("Generic Search", result.get(0).getLabel());
        assertEquals("DOC ID", result.get(1).getLabel());
        assertEquals("DOC NAME", result.get(2).getLabel());
    }



    /*@Test
    void resolveOperator_WhenRequestIsNull_ShouldReturnOR() {
        String result = documentService.resolveOperator(null);
        assertEquals("OR", result);
    }*/

    /*@Test
    void resolveOperator_WhenOperatorProvided_ShouldReturnUpperCase() {
        FilterRequestDto request = new FilterRequestDto("and", "N", Collections.emptyList());
        String result = documentService.resolveOperator(request);
        assertEquals("AND", result);
    }*/

    /*@Test
    void resolveIsDeleted_WhenRequestIsNull_ShouldReturnN() {
        String result = documentService.resolveIsDeleted(null);
        assertEquals("N", result);
    }*/

    /*@Test
    void resolveFilters_WhenRequestIsNull_ShouldReturnEmptyList() {
        List<FilterDto> result = documentService.resolveFilters(null);

        assertTrue(result.isEmpty());
    }*/

    @Test
    void listDocuments_WhenCalled_ShouldReturnPaginatedResult() {
        DocumentEntity doc = createTestDocument(1L, "Test Doc", "Y", "N");
        doc.setListOfRecordsSql("SELECT * FROM test_table");

        when(documentRepository.findByDocId("1")).thenReturn(doc);
        when(tableMetaRepository.getColumnsFromSql(anyString()))
                .thenReturn(Arrays.asList("DOC_ID", "DOC_NAME"));
        when(tableMetaRepository.executeDynamicQuery(anyString(), anyList(), anyList()))
                .thenReturn(Collections.emptyList());
        when(tableMetaRepository.executeCountQuery(anyString(), anyList()))
                .thenReturn(0L);

        FilterRequestDto request = new FilterRequestDto("OR", "N", Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 10);

        Map<String, Object> result = documentService.listDocuments("1", request, pageable);

        assertNotNull(result);
        // The result should contain pagination structure
        assertTrue(result.size() > 0);
    }

    private DocumentEntity createTestDocument(Long id, String name, String isActive, String isDeleted) {
        DocumentEntity doc = new DocumentEntity();
        doc.setDocId(id.toString());
        doc.setDocName(name);
        doc.setActive(isActive);
        doc.setDeleted(isDeleted);
        doc.setDocPoid(java.math.BigDecimal.valueOf(id));
        doc.setMainTableName("test_table");
        doc.setListOfRecordsSql("");
        doc.setListOfDisplayColumnsAndTypes("");
        return doc;
    }

    private Map<String, Map<String, Boolean>> createMockUserRights(String docId, boolean hasDeleteRight) {
        Map<String, Map<String, Boolean>> userRights = new HashMap<>();
        Map<String, Boolean> docRights = new HashMap<>();
        docRights.put("DELETE", hasDeleteRight);
        userRights.put(docId, docRights);
        return userRights;
    }

    @Test
    void getInactiveAndDeletedDocuments_WhenRepositoryThrowsException_ShouldPropagateException() {
        when(documentRepository.findAllByActiveAndDeleted("N", "Y"))
                .thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            documentService.getInactiveAndDeletedDocuments();
        });

        assertEquals("Database error", exception.getMessage());
        verify(documentRepository, times(1)).findAllByActiveAndDeleted("N", "Y");
    }

    @Test
    void getInactiveAndDeletedDocuments_WhenNullDocuments_ShouldHandleGracefully() {
        when(documentRepository.findAllByActiveAndDeleted("N", "Y"))
                .thenReturn(null);

        assertThrows(NullPointerException.class, () -> {
            documentService.getInactiveAndDeletedDocuments();
        });
    }

    // Edge Cases
    @Test
    void getDocumentById_WhenDocumentNotFound_ShouldThrowResourceNotFoundException() {
        when(documentRepository.findByDocId("999")).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            documentService.getDocumentById("999");
        });

        assertTrue(exception.getMessage().contains("Document"));
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void getSearchableFieldsForDropdown_WhenDocumentNotFound_ShouldThrowException() {
        when(documentRepository.findByDocId("999")).thenReturn(null);

        assertThrows(NullPointerException.class, () -> {
            documentService.getSearchableFieldsForDropdown("999");
        });
    }

    @Test
    void getSearchableFieldsForDropdown_WhenEmptySQL_ShouldUseMainTable() {
        DocumentEntity doc = createTestDocument(1L, "Test Doc", "Y", "N");
        doc.setListOfRecordsSql("");

        when(documentRepository.findByDocId("1")).thenReturn(doc);
        when(tableMetaRepository.getColumnsFromTable("test_table"))
                .thenReturn(Arrays.asList("ID", "NAME"));

        List<DropdownStringDto> result = documentService.getSearchableFieldsForDropdown("1");

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(tableMetaRepository).getColumnsFromTable("test_table");
    }

    /*@Test
    void resolveOperator_WhenEmptyOperator_ShouldReturnOR() {
        FilterRequestDto request = new FilterRequestDto("", "N", Collections.emptyList());
        String result = documentService.resolveOperator(request);
        assertEquals("OR", result);
    }*/

    /*@Test
    void resolveIsDeleted_WhenEmptyIsDeleted_ShouldReturnN() {
        FilterRequestDto request = new FilterRequestDto("OR", "", Collections.emptyList());
        String result = documentService.resolveIsDeleted(request);
        assertEquals("N", result);
    }*/

    /*@Test
    void resolveFilters_WhenNullFilters_ShouldReturnNull() {
        FilterRequestDto request = new FilterRequestDto("OR", "N", null);
        List<FilterDto> result = documentService.resolveFilters(request);

        assertNull(result);
    }*/

    @Test
    void listDocuments_WhenNullRequest_ShouldUseDefaults() {
        DocumentEntity doc = createTestDocument(1L, "Test Doc", "Y", "N");
        doc.setListOfRecordsSql("SELECT * FROM test_table");

        when(documentRepository.findByDocId("1")).thenReturn(doc);
        when(tableMetaRepository.getColumnsFromSql(anyString()))
                .thenReturn(Arrays.asList("DOC_ID"));
        when(tableMetaRepository.executeDynamicQuery(anyString(), anyList(), anyList()))
                .thenReturn(Collections.emptyList());
        when(tableMetaRepository.executeCountQuery(anyString(), anyList()))
                .thenReturn(0L);

        Pageable pageable = PageRequest.of(0, 10);
        Map<String, Object> result = documentService.listDocuments("1", null, pageable);

        assertNotNull(result);
    }

    /*@Test
    void getDocumentById_WhenModuleExists_ShouldIncludeModule() {
        DocumentEntity doc = createTestDocument(1L, "Test Doc", "Y", "N");
        doc.setModuleId("MOD-001");

        GlobalModuleMasterEntity module = new GlobalModuleMasterEntity();
        module.setModuleId("MOD-001");
        module.setModuleName("Test Module");

        when(documentRepository.findByDocId("1")).thenReturn(doc);
        when(globalModuleMasterRepository.findByModuleId("MOD-001")).thenReturn(module);
        when(documentApprovalDtlRepository.findAllById_DocId("1")).thenReturn(Collections.emptyList());
        when(documentAuthDtlRepository.findAllById_DocId("1")).thenReturn(Collections.emptyList());

        DocumentDto result = documentService.getDocumentById("1");

        assertNotNull(result);
        assertNotNull(result.getModule());
        assertEquals("MOD-001", result.getModule().getModuleId());
    }*/

    @Test
    void getSearchableFieldsForDropdown_WhenNullSQL_ShouldUseMainTable() {
        DocumentEntity doc = createTestDocument(1L, "Test Doc", "Y", "N");
        doc.setListOfRecordsSql(null);

        when(documentRepository.findByDocId("1")).thenReturn(doc);
        when(tableMetaRepository.getColumnsFromTable("test_table"))
                .thenReturn(Arrays.asList("ID"));

        List<DropdownStringDto> result = documentService.getSearchableFieldsForDropdown("1");

        assertNotNull(result);
        verify(tableMetaRepository).getColumnsFromTable("test_table");
    }

    @Test
    void getSearchableFieldsForDropdown_WhenBlankSQL_ShouldUseMainTable() {
        DocumentEntity doc = createTestDocument(1L, "Test Doc", "Y", "N");
        doc.setListOfRecordsSql("   ");

        when(documentRepository.findByDocId("1")).thenReturn(doc);
        when(tableMetaRepository.getColumnsFromTable("test_table"))
                .thenReturn(Arrays.asList("ID"));

        List<DropdownStringDto> result = documentService.getSearchableFieldsForDropdown("1");

        assertNotNull(result);
        verify(tableMetaRepository).getColumnsFromTable("test_table");
    }

    /*@Test
    void resolveOperator_WhenNullOperator_ShouldReturnOR() {
        FilterRequestDto request = new FilterRequestDto(null, "N", Collections.emptyList());
        String result = documentService.resolveOperator(request);
        assertEquals("OR", result);
    }*/

    /*@Test
    void resolveIsDeleted_WhenNullIsDeleted_ShouldReturnN() {
        FilterRequestDto request = new FilterRequestDto("OR", null, Collections.emptyList());
        String result = documentService.resolveIsDeleted(request);
        assertEquals("N", result);
    }*/

    @Test
    void listDocuments_WhenTableMetaThrowsException_ShouldPropagateException() {
        DocumentEntity doc = createTestDocument(1L, "Test Doc", "Y", "N");
        doc.setListOfRecordsSql("SELECT * FROM test_table");

        when(documentRepository.findByDocId("1")).thenReturn(doc);
        when(tableMetaRepository.getColumnsFromSql(anyString()))
                .thenThrow(new RuntimeException("SQL error"));

        FilterRequestDto request = new FilterRequestDto("OR", "N", Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(RuntimeException.class, () -> {
            documentService.listDocuments("1", request, pageable);
        });
    }

    @Test
    void getDocumentById_WhenNullModuleId_ShouldNotIncludeModule() {
        DocumentEntity doc = createTestDocument(1L, "Test Doc", "Y", "N");
        doc.setModuleId(null);

        when(documentRepository.findByDocId("1")).thenReturn(doc);
        when(documentApprovalDtlRepository.findAllById_DocId("1")).thenReturn(Collections.emptyList());
        when(documentAuthDtlRepository.findAllById_DocId("1")).thenReturn(Collections.emptyList());

        DocumentDto result = documentService.getDocumentById("1");

        assertNotNull(result);
        assertNull(result.getModule());
    }

    @Test
    void getDocumentById_WhenEmptyDocId_ShouldHandleGracefully() {
        DocumentEntity doc = createTestDocument(1L, "Test Doc", "Y", "N");
        doc.setDocId("");
        when(documentRepository.findByDocId("")).thenReturn(doc);
        when(documentApprovalDtlRepository.findAllById_DocId("")).thenReturn(Collections.emptyList());
        when(documentAuthDtlRepository.findAllById_DocId("")).thenReturn(Collections.emptyList());

        DocumentDto result = documentService.getDocumentById("");

        assertNotNull(result);
        assertEquals("", result.getDocId());
    }

    @Test
    void listDocuments_WhenZeroPageSize_ShouldThrowException() {
        FilterRequestDto request = new FilterRequestDto("OR", "N", Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> {
            PageRequest.of(0, 0);
        });
    }
}
