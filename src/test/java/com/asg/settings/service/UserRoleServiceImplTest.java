package com.asg.settings.service;

import com.asg.common.lib.dto.UserRoleDto;
import com.asg.common.lib.dto.UserRolesDto;
import com.asg.common.lib.exception.ResourceAlreadyExistsException;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.settings.dto.UserRoleRequestDto;
import com.asg.settings.entity.RoleEntity;
import com.asg.settings.repository.RoleRepository;
import com.asg.settings.service.impl.UserRoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserRoleServiceImpl userRoleService;

    private Pageable pageable;
    private UserRoleDto userRole1, userRole2;
    private List<UserRoleDto> userRoles;
    private RoleEntity roleEntity;
    private UserRoleRequestDto userRoleRequestDto;

    @BeforeEach
    void setUp() {
        userRole1 = UserRoleDto.builder()
                .userRolePoId(1L)
                .userRoleId("ROLE_ADMIN")
                .userRoleName("Administrator")
                .active("Y")
                .build();

        userRole2 = UserRoleDto.builder()
                .userRolePoId(2L)
                .userRoleId("ROLE_USER")
                .userRoleName("User")
                .active("Y")
                .build();

        userRoles = Arrays.asList(userRole1, userRole2);

        pageable = PageRequest.of(0, 10, Sort.by("userRoleName").ascending());

        roleEntity = new RoleEntity();
        roleEntity.setUserRolePoid(1L);
        roleEntity.setUserRoleId("ROLE_ADMIN");
        roleEntity.setUserRoleName("Administrator");
        roleEntity.setActive("Y");
        roleEntity.setCreatedDate(LocalDateTime.now());

        userRoleRequestDto = new UserRoleRequestDto();
        userRoleRequestDto.setUserRoleId("ROLE_NEW");
        userRoleRequestDto.setUserRoleName("New Role");
        userRoleRequestDto.setActive("Y");
        userRoleRequestDto.setCompanyPoid(1L);
    }

//    @Test
//    void getUserRoles_WithNoFilters_ReturnsAllRoles() {
//        Page<UserRoleDto> page = new PageImpl<>(userRoles, pageable, userRoles.size());
//        when(roleRepository.findUserRolesWithPaginationAndSorting(null, null, pageable)).thenReturn(page);
//
//        UserRoleResponse response = userRoleService.getUserRoles(null, null, pageable);
//
//        assertNotNull(response);
//        assertEquals(2, response.getTotalCount());
//        assertEquals(0, response.getCurrentPage());
//        assertEquals(10, response.getPageSize());
//        assertEquals(1, response.getTotalPages());
//        assertEquals(2, response.getRoles().size());
//    }

//    @Test
//    void getUserRoles_WithRoleIdFilter_ReturnsFilteredResults() {
//        String roleId = "ROLE_ADMIN";
//        List<UserRoleDto> filteredRoles = List.of(userRole1);
//        Page<UserRoleDto> page = new PageImpl<>(filteredRoles, pageable, filteredRoles.size());
//
//        when(roleRepository.findUserRolesWithPaginationAndSorting(roleId, null, pageable)).thenReturn(page);
//
//        UserRoleResponse response = userRoleService.getUserRoles(roleId, null, pageable);
//
//        assertNotNull(response);
//        assertEquals(1, response.getRoles().size());
//        assertEquals("ROLE_ADMIN", response.getRoles().get(0).getUserRoleId());
//        assertEquals(1, response.getTotalCount());
//    }
//
//    @Test
//    void getUserRoles_WithRoleNameFilter_ReturnsFilteredResults() {
//        String roleName = "User";
//        List<UserRoleDto> filteredRoles = List.of(userRole2);
//        Page<UserRoleDto> page = new PageImpl<>(filteredRoles, pageable, filteredRoles.size());
//
//        when(roleRepository.findUserRolesWithPaginationAndSorting(null, roleName, pageable)).thenReturn(page);
//
//        UserRoleResponse response = userRoleService.getUserRoles(null, roleName, pageable);
//
//        assertNotNull(response);
//        assertEquals(1, response.getRoles().size());
//        assertEquals("User", response.getRoles().get(0).getUserRoleName());
//        assertEquals(1, response.getTotalCount());
//    }

//    @Test
//    void getUserRoles_WithPagination_ReturnsPaginatedResults() {
//        Pageable customPageable = PageRequest.of(1, 1, Sort.by("userRoleName").ascending());
//        Page<UserRoleDto> page = new PageImpl<>(List.of(userRole2), customPageable, userRoles.size());
//
//        when(roleRepository.findUserRolesWithPaginationAndSorting(null, null, customPageable)).thenReturn(page);
//
//        UserRoleResponse response = userRoleService.getUserRoles(null, null, customPageable);
//
//        assertNotNull(response);
//        assertEquals(1, response.getRoles().size());
//        assertEquals(1, response.getCurrentPage());
//        assertEquals(1, response.getPageSize());
//        assertEquals(2, response.getTotalPages());
//        assertEquals(2, response.getTotalCount());
//    }
//
//    @Test
//    void getUserRoles_WithNoResults_ReturnsEmptyResponse() {
//        Page<UserRoleDto> emptyPage = new PageImpl<>(List.of(), pageable, 0);
//        when(roleRepository.findUserRolesWithPaginationAndSorting(any(), any(), any())).thenReturn(emptyPage);
//
//        UserRoleResponse response = userRoleService.getUserRoles("NON_EXISTENT", null, pageable);
//
//        assertNotNull(response);
//        assertTrue(response.getRoles().isEmpty());
//        assertEquals(0, response.getTotalCount());
//    }

    @Test
    void getUserRoleByRolePoid_ReturnsRoleEntity() {
        when(roleRepository.findByUserRolePoid(1L)).thenReturn(roleEntity);

        RoleEntity result = userRoleService.getUserRoleByRolePoid(1L);

        assertNotNull(result);
        assertEquals(1L, result.getUserRolePoid());
        assertEquals("ROLE_ADMIN", result.getUserRoleId());
    }

    @Test
    void addUserRoles_WithValidData_ReturnsUserRolesDto() {
        when(roleRepository.existsByUserRoleId("ROLE_NEW")).thenReturn(false);
        when(roleRepository.existsByUserRoleName("New Role")).thenReturn(false);
        when(roleRepository.save(any(RoleEntity.class))).thenReturn(roleEntity);

        UserRolesDto result = userRoleService.addUserRoles(userRoleRequestDto);

        assertNotNull(result);
        assertEquals("ROLE_ADMIN", result.getUserRoleId());
    }

    @Test
    void addUserRoles_WithExistingRoleId_ThrowsException() {
        when(roleRepository.existsByUserRoleId("ROLE_NEW")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> userRoleService.addUserRoles(userRoleRequestDto));
    }

    @Test
    void addUserRoles_WithExistingRoleName_ThrowsException() {
        when(roleRepository.existsByUserRoleId("ROLE_NEW")).thenReturn(false);
        when(roleRepository.existsByUserRoleName("New Role")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> userRoleService.addUserRoles(userRoleRequestDto));
    }

    @Test
    void updateUserRoleByUserRolePoId_WithValidData_ReturnsUpdatedDto() {
        Long userRolePoid = 1L;
        when(roleRepository.existsByUserRolePoid(userRolePoid)).thenReturn(true);
        when(roleRepository.existsByUserRoleIdAndUserRolePoidNot("ROLE_NEW", userRolePoid)).thenReturn(false);
        when(roleRepository.findByUserRolePoid(userRolePoid)).thenReturn(roleEntity);
        when(roleRepository.save(any(RoleEntity.class))).thenReturn(roleEntity);

        UserRolesDto result = userRoleService.updateUserRoleByUserRolePoId(userRolePoid, userRoleRequestDto);

        assertNotNull(result);
        assertEquals("ROLE_NEW", result.getUserRoleId());
    }

    @Test
    void updateUserRoleByUserRolePoId_WithNonExistentRole_ThrowsException() {
        Long userRolePoid = 999L;
        when(roleRepository.existsByUserRolePoid(userRolePoid)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> userRoleService.updateUserRoleByUserRolePoId(userRolePoid, userRoleRequestDto));
    }

    @Test
    void updateUserRoleByUserRolePoId_WithDuplicateRoleId_ThrowsException() {
        Long userRolePoid = 1L;
        when(roleRepository.existsByUserRolePoid(userRolePoid)).thenReturn(true);
        when(roleRepository.existsByUserRoleIdAndUserRolePoidNot("ROLE_NEW", userRolePoid)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> userRoleService.updateUserRoleByUserRolePoId(userRolePoid, userRoleRequestDto));
    }

    @Test
    void softDeleteUserRole_WithValidId_SoftDeletesRole() {
        Long userRolePoid = 1L;
        when(roleRepository.findByUserRolePoid(userRolePoid)).thenReturn(roleEntity);
        when(roleRepository.save(any(RoleEntity.class))).thenReturn(roleEntity);

        userRoleService.softDeleteUserRole(userRolePoid);

        verify(roleRepository).findByUserRolePoid(userRolePoid);
        verify(roleRepository).save(roleEntity);
        assertEquals("Y", roleEntity.getDeleted());
        assertEquals("N", roleEntity.getActive());
        assertNotNull(roleEntity.getLastModifiedDate());
    }

    @Test
    void softDeleteUserRole_WithNonExistentId_ThrowsException() {
        Long userRolePoid = 999L;
        when(roleRepository.findByUserRolePoid(userRolePoid)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> userRoleService.softDeleteUserRole(userRolePoid));

        verify(roleRepository).findByUserRolePoid(userRolePoid);
        verify(roleRepository, never()).save(any(RoleEntity.class));
    }
}
