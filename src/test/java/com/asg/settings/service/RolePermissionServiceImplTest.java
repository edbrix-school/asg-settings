package com.asg.settings.service;

import com.asg.common.lib.dto.UserRoleRightsDto;
import com.asg.settings.dto.RightUpdateEntry;
import com.asg.settings.dto.request.RightsUpdateRequest;
import com.asg.settings.dto.request.RolePermissionEntry;
import com.asg.settings.dto.request.RolePermissionRequest;
import com.asg.settings.dto.response.RolePermissionResponse;
import com.asg.settings.entity.UserRoleRightsEntity;
import com.asg.settings.entity.key.UserRoleRightsKey;
import com.asg.settings.repository.RoleRepository;
import com.asg.settings.repository.UserRoleRightsRepository;
import com.asg.settings.service.impl.RolePermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.List;


@ExtendWith(MockitoExtension.class)
@DisplayName("Role Permission Service Implementation Tests")
class RolePermissionServiceImplTest {

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private UserRoleRightsRepository rightsRepo;

    @InjectMocks
    private RolePermissionServiceImpl service;

    private RolePermissionRequest validPermissionRequest;
    private RightsUpdateRequest validRightsUpdateRequest;
    private RolePermissionEntry permissionEntry;
    private RightUpdateEntry rightUpdateEntry;
    private UserRoleRightsEntity existingEntity;

    @BeforeEach
    void setUp() {
        permissionEntry = new RolePermissionEntry();
        permissionEntry.setDetRowId(1L);
        permissionEntry.setDocId("DOC001");
        permissionEntry.setRights("111111");
        permissionEntry.setCreatedBy("admin");
        permissionEntry.setLastModifiedBy("admin");

        validPermissionRequest = new RolePermissionRequest();
        validPermissionRequest.setRoleId(1L);
        validPermissionRequest.setPermissions(Arrays.asList(permissionEntry));

        rightUpdateEntry = new RightUpdateEntry();
        rightUpdateEntry.setDetRowId(1L);
        rightUpdateEntry.setDocId("DOC001");
        rightUpdateEntry.setRights("111110");
        rightUpdateEntry.setLastModifiedBy("admin");

        validRightsUpdateRequest = new RightsUpdateRequest();
        validRightsUpdateRequest.setRightsUpdateList(Arrays.asList(rightUpdateEntry));

        existingEntity = new UserRoleRightsEntity();
        existingEntity.setId(new UserRoleRightsKey(1L, 1L));
        existingEntity.setDocId("DOC001");
        existingEntity.setRights("111111");
        existingEntity.setCreatedBy("admin");
        existingEntity.setLastModifiedBy("admin");
    }

    @Nested
    @DisplayName("Add Permissions Tests")
    class AddPermissionsTests {

        @DisplayName("Should return SUCCESS when permissions are added successfully")
        @Test
        void addPermissions_ValidRequest_ShouldReturnSuccess() {
            when(roleRepo.existsById(1L)).thenReturn(true);
            when(rightsRepo.existsByIdUserRolePoidAndIdDetRowIdAndDocId(1L, 1L, "DOC001")).thenReturn(false);
            when(rightsRepo.save(any(UserRoleRightsEntity.class))).thenReturn(existingEntity);

            RolePermissionResponse response = service.addPermissions(validPermissionRequest);

            assertNotNull(response);
            assertEquals("SUCCESS", response.getStatus());
            assertEquals("Role permissions added successfully.", response.getMessage());
            assertTrue(response.getErrors().isEmpty());

            verify(roleRepo, times(1)).existsById(1L);
            verify(rightsRepo, times(1)).existsByIdUserRolePoidAndIdDetRowIdAndDocId(1L, 1L, "DOC001");
            verify(rightsRepo, times(1)).save(any(UserRoleRightsEntity.class));
        }

        @DisplayName("Should return FAILURE when role does not exist")
        @Test
        void addPermissions_InvalidRoleId_ShouldReturnFailure() {
            when(roleRepo.existsById(1L)).thenReturn(false);

            RolePermissionResponse response = service.addPermissions(validPermissionRequest);

            assertNotNull(response);
            assertEquals("FAILURE", response.getStatus());
            assertEquals("Failed to add role permissions.", response.getMessage());
            assertEquals(1, response.getErrors().size());
            assertEquals("DOC001", response.getErrors().get(0).getDocId());
            assertEquals("Invalid roleId", response.getErrors().get(0).getError());

            verify(roleRepo, times(1)).existsById(1L);
            verify(rightsRepo, never()).save(any());
        }

        @DisplayName("Should return FAILURE when permission already exists")
        @Test
        void addPermissions_DuplicateEntry_ShouldReturnFailure() {
            when(roleRepo.existsById(1L)).thenReturn(true);
            when(rightsRepo.existsByIdUserRolePoidAndIdDetRowIdAndDocId(1L, 1L, "DOC001")).thenReturn(true);

            RolePermissionResponse response = service.addPermissions(validPermissionRequest);

            assertNotNull(response);
            assertEquals("FAILURE", response.getStatus());
            assertEquals("Failed to add role permissions.", response.getMessage());
            assertEquals(1, response.getErrors().size());
            assertEquals("DOC001", response.getErrors().get(0).getDocId());
            assertEquals("Duplicate entry for docId", response.getErrors().get(0).getError());

            verify(roleRepo, times(1)).existsById(1L);
            verify(rightsRepo, times(1)).existsByIdUserRolePoidAndIdDetRowIdAndDocId(1L, 1L, "DOC001");
            verify(rightsRepo, never()).save(any());
        }

        @DisplayName("Should handle multiple permissions with mixed results")
        @Test
        void addPermissions_MultiplePermissions_ShouldHandleMixedResults() {
            RolePermissionEntry entry2 = new RolePermissionEntry();
            entry2.setDetRowId(2L);
            entry2.setDocId("DOC002");
            entry2.setRights("222222");
            entry2.setCreatedBy("admin");
            entry2.setLastModifiedBy("admin");

            validPermissionRequest.setPermissions(Arrays.asList(permissionEntry, entry2));

            when(roleRepo.existsById(1L)).thenReturn(true);
            when(rightsRepo.existsByIdUserRolePoidAndIdDetRowIdAndDocId(1L, 1L, "DOC001")).thenReturn(false);
            when(rightsRepo.existsByIdUserRolePoidAndIdDetRowIdAndDocId(1L, 2L, "DOC002")).thenReturn(true);
            when(rightsRepo.save(any(UserRoleRightsEntity.class))).thenReturn(existingEntity);

            RolePermissionResponse response = service.addPermissions(validPermissionRequest);

            assertNotNull(response);
            assertEquals("FAILURE", response.getStatus());
            assertEquals("Failed to add role permissions.", response.getMessage());
            assertEquals(1, response.getErrors().size());
            assertEquals("DOC002", response.getErrors().get(0).getDocId());
            assertEquals("Duplicate entry for docId", response.getErrors().get(0).getError());

            verify(rightsRepo, times(1)).save(any(UserRoleRightsEntity.class));
        }
    }

    @Nested
    @DisplayName("Update Permissions Tests")
    class UpdatePermissionsTests {

        @DisplayName("Should return SUCCESS when permissions are updated successfully")
        @Test
        void updatePermissions_ValidRequest_ShouldReturnSuccess() {
            when(roleRepo.existsById(1L)).thenReturn(true);
            when(rightsRepo.findByIdUserRolePoidAndIdDetRowIdAndDocId(1L, 1L, "DOC001"))
                    .thenReturn(Optional.of(existingEntity));
            when(rightsRepo.save(any(UserRoleRightsEntity.class))).thenReturn(existingEntity);

            RolePermissionResponse response = service.updatePermissions(1L, validRightsUpdateRequest);

            assertNotNull(response);
            assertEquals("SUCCESS", response.getStatus());
            assertEquals("Role permissions updated successfully.", response.getMessage());
            assertTrue(response.getErrors().isEmpty());

            verify(roleRepo, times(1)).existsById(1L);
            verify(rightsRepo, times(1)).findByIdUserRolePoidAndIdDetRowIdAndDocId(1L, 1L, "DOC001");
            verify(rightsRepo, times(1)).save(any(UserRoleRightsEntity.class));
        }

        @DisplayName("Should return FAILURE when role does not exist")
        @Test
        void updatePermissions_InvalidRoleId_ShouldReturnFailure() {
            when(roleRepo.existsById(1L)).thenReturn(false);

            RolePermissionResponse response = service.updatePermissions(1L, validRightsUpdateRequest);

            assertNotNull(response);
            assertEquals("FAILURE", response.getStatus());
            assertEquals("Failed to update role permissions.", response.getMessage());
            assertEquals(1, response.getErrors().size());
            assertEquals("DOC001", response.getErrors().get(0).getDocId());
            assertEquals("Invalid roleId", response.getErrors().get(0).getError());

            verify(roleRepo, times(1)).existsById(1L);
            verify(rightsRepo, never()).save(any());
        }

        @DisplayName("Should create new record when permission record not found")
        @Test
        void updatePermissions_RecordNotFound_ShouldCreateNewRecord() {
            when(roleRepo.existsById(1L)).thenReturn(true);
            when(rightsRepo.findByIdUserRolePoidAndIdDetRowIdAndDocId(1L, 1L, "DOC001"))
                    .thenReturn(Optional.empty());
            when(rightsRepo.save(any(UserRoleRightsEntity.class))).thenReturn(existingEntity);

            RolePermissionResponse response = service.updatePermissions(1L, validRightsUpdateRequest);

            assertNotNull(response);
            assertEquals("SUCCESS", response.getStatus());
            assertEquals("Role permissions updated successfully.", response.getMessage());
            assertTrue(response.getErrors().isEmpty());

            verify(roleRepo, times(1)).existsById(1L);
            verify(rightsRepo, times(1)).findByIdUserRolePoidAndIdDetRowIdAndDocId(1L, 1L, "DOC001");
            verify(rightsRepo, times(1)).save(any(UserRoleRightsEntity.class));
        }

        @DisplayName("Should handle multiple updates with mixed results")
        @Test
        void updatePermissions_MultipleUpdates_ShouldHandleMixedResults() {
            RightUpdateEntry entry2 = new RightUpdateEntry();
            entry2.setDetRowId(2L);
            entry2.setDocId("DOC002");
            entry2.setRights("222220");
            entry2.setLastModifiedBy("admin");

            validRightsUpdateRequest.setRightsUpdateList(Arrays.asList(rightUpdateEntry, entry2));

            when(roleRepo.existsById(1L)).thenReturn(true);
            when(rightsRepo.findByIdUserRolePoidAndIdDetRowIdAndDocId(1L, 1L, "DOC001"))
                    .thenReturn(Optional.of(existingEntity));
            when(rightsRepo.findByIdUserRolePoidAndIdDetRowIdAndDocId(1L, 2L, "DOC002"))
                    .thenReturn(Optional.empty());
            when(rightsRepo.save(any(UserRoleRightsEntity.class))).thenReturn(existingEntity);

            RolePermissionResponse response = service.updatePermissions(1L, validRightsUpdateRequest);

            assertNotNull(response);
            assertEquals("SUCCESS", response.getStatus());
            assertEquals("Role permissions updated successfully.", response.getMessage());
            assertTrue(response.getErrors().isEmpty());

            verify(rightsRepo, times(2)).save(any(UserRoleRightsEntity.class));
        }

        @DisplayName("Should update entity fields correctly")
        @Test
        void updatePermissions_ShouldUpdateEntityFields() {
            when(roleRepo.existsById(1L)).thenReturn(true);
            when(rightsRepo.findByIdUserRolePoidAndIdDetRowIdAndDocId(1L, 1L, "DOC001"))
                    .thenReturn(Optional.of(existingEntity));
            when(rightsRepo.save(any(UserRoleRightsEntity.class))).thenReturn(existingEntity);

            service.updatePermissions(1L, validRightsUpdateRequest);

            assertEquals("111110", existingEntity.getRights());
            assertEquals("admin", existingEntity.getLastModifiedBy());
            assertNotNull(existingEntity.getLastModifiedDate());

            verify(rightsRepo, times(1)).save(existingEntity);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @DisplayName("Should handle empty permissions list")
        @Test
        void addPermissions_EmptyList_ShouldReturnSuccess() {
            validPermissionRequest.setPermissions(Collections.emptyList());
            when(roleRepo.existsById(1L)).thenReturn(true);

            RolePermissionResponse response = service.addPermissions(validPermissionRequest);

            assertNotNull(response);
            assertEquals("SUCCESS", response.getStatus());
            assertTrue(response.getErrors().isEmpty());

            verify(rightsRepo, never()).save(any());
        }

        @DisplayName("Should handle empty rights update list")
        @Test
        void updatePermissions_EmptyList_ShouldReturnSuccess() {
            validRightsUpdateRequest.setRightsUpdateList(Collections.emptyList());
            when(roleRepo.existsById(1L)).thenReturn(true);

            RolePermissionResponse response = service.updatePermissions(1L, validRightsUpdateRequest);

            assertNotNull(response);
            assertEquals("SUCCESS", response.getStatus());
            assertTrue(response.getErrors().isEmpty());

            verify(rightsRepo, never()).save(any());
        }
    }


    private UserRoleRightsEntity buildUserRoleRightsEntity(Long roleId, Long detRowId,
                                                           String docId, String rights) {
        UserRoleRightsKey entityId = new UserRoleRightsKey();
        entityId.setUserRolePoid(roleId);
        entityId.setDetRowId(detRowId);

        UserRoleRightsEntity entity = new UserRoleRightsEntity();
        entity.setId(entityId);
        entity.setDocId(docId);
        entity.setRights(rights);
        entity.setCreatedBy("admin");
        entity.setCreatedDate(LocalDateTime.now());
        entity.setLastModifiedBy("admin");
        entity.setLastModifiedDate(LocalDateTime.now());
        return entity;
    }

    @Test
    void testGetUserRoleRights_ReturnsMappedDtos() {
        Long roleId = 1L;
        UserRoleRightsEntity entity = buildUserRoleRightsEntity(roleId, 100L, "DOC_123", "READ");
        when(rightsRepo.findAllByIdUserRolePoid(roleId)).thenReturn(List.of(entity));
        List<UserRoleRightsDto> result = service.getUserRoleRights(roleId);
        assertNotNull(result);
        assertEquals(1, result.size());
        UserRoleRightsDto dto = result.get(0);
        assertAll(
                () -> assertEquals(roleId, dto.getUserRolePoid()),
                () -> assertEquals(100L, dto.getDetRowId()),
                () -> assertEquals("DOC_123", dto.getDocId()),
                () -> assertEquals("READ", dto.getRights()),
                () -> assertEquals("admin", dto.getCreatedBy())
        );
    }

    @Test
    void testGetUserRoleRights_NoPermissions() {
        Long roleId = 2L;
        when(rightsRepo.findAllByIdUserRolePoid(roleId))
                .thenReturn(Collections.emptyList());
        List<UserRoleRightsDto> result = service.getUserRoleRights(roleId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}