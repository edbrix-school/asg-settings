package com.asg.settings.service.impl;

import com.asg.common.lib.dto.*;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.settings.dto.*;
import com.asg.settings.dto.request.RightsUpdateRequest;
import com.asg.settings.dto.request.RolePermissionEntry;
import com.asg.settings.dto.request.RolePermissionRequest;
import com.asg.settings.dto.response.RolePermissionResponse;
import com.asg.settings.entity.RoleEntity;
import com.asg.settings.entity.UserRoleRightsDetEntity;
import com.asg.settings.entity.UserRoleRightsEntity;
import com.asg.settings.entity.key.UserRoleRightsKey;
import com.asg.settings.repository.RoleRepository;
import com.asg.settings.repository.RoleRightsCustomRepository;
import com.asg.settings.repository.UserRepository;
import com.asg.settings.repository.UserRoleRightsRepository;
import com.asg.settings.repository.projection.UserRoleProjection;
import com.asg.settings.service.RolePermissionService;
import com.asg.settings.service.UserRoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RoleRepository roleRepo;
    private final UserRoleRightsRepository rightsRepo;
    private final RoleRightsCustomRepository procRepo;
    private final UserRoleService userRoleService;
    private final UserRepository userRepository;


    @Override
    public UserRoleRightsDetDto getUserRoleRightsDetByRolePoid(Long userRolePoid) {

        RoleEntity roleEntity = userRoleService.getUserRoleByRolePoid(userRolePoid);
        if (roleEntity == null) {
            throw new ResourceNotFoundException("Role", "id", userRolePoid);
        }

        UserRoleRightsDetDto response = new UserRoleRightsDetDto();
        response.setUserRolePoid(roleEntity.getUserRolePoid());
        response.setUserRoleId(roleEntity.getUserRoleId());
        response.setUserRoleName(roleEntity.getUserRoleName());
        response.setUserRoleName2(roleEntity.getUserRoleName2());
        response.setSeqNo(roleEntity.getSeqNo());
        response.setActive(roleEntity.getActive());
        
        // Set audit fields
        response.setCreatedBy(roleEntity.getCreatedBy());
        response.setCreatedDate(roleEntity.getCreatedDate() != null ? roleEntity.getCreatedDate().atOffset(java.time.ZoneOffset.UTC) : null);
        response.setLastModifiedBy(roleEntity.getLastModifiedBy());
        response.setLastModifiedDate(roleEntity.getLastModifiedDate() != null ? roleEntity.getLastModifiedDate().atOffset(java.time.ZoneOffset.UTC) : null);

        // NEW: fetch all docs + module + existing rights for this role
        List<UserRoleRightsDetEntity> allDocsWithRights =
                rightsRepo.fetchAllDocsWithRightsForRole(userRolePoid);

        if (!allDocsWithRights.isEmpty()) {
            Map<String, List<UserRoleRightsDetEntity>> grouped =
                    allDocsWithRights.stream()
                            .collect(Collectors.groupingBy(UserRoleRightsDetEntity::getModuleShortName));

            List<ModuleDto> moduleDtos = new ArrayList<>();
            grouped.forEach((moduleShortName, docs) -> {
                ModuleDto moduleDto = new ModuleDto();
                moduleDto.setModuleShortName(moduleShortName);

                // Remove duplicates by docId and prioritize non-default rights
                Map<String, DocumenResponsetDto> uniqueDocs = new LinkedHashMap<>();
                docs.stream()
                        .map(this::toDocumentDtoWithDefaultRights)
                        .forEach(doc -> {
                            String key = doc.getDocId();
                            DocumenResponsetDto existing = uniqueDocs.get(key);
                            if (existing == null) {
                                uniqueDocs.put(key, doc);
                            } else if ("000000".equals(existing.getRights()) && !"000000".equals(doc.getRights())) {
                                // Replace default rights with actual rights
                                uniqueDocs.put(key, doc);
                            }
                            // If existing has actual rights, keep it (don't replace)
                        });

                moduleDto.setDocuments(new ArrayList<>(uniqueDocs.values()));
                moduleDtos.add(moduleDto);
            });
            response.setModules(moduleDtos);
        }

        // Fetch users in this role
        List<UserRoleProjection> usersInRole = userRepository.fetchUserRoleProjectionByRolePoid(userRolePoid);
        List<UserInRoleDto> userInRoleDtos = usersInRole.stream()
                .map(this::mapToUserInRoleDto)
                .collect(Collectors.toList());
        response.setUsers(userInRoleDtos);

        return response;
    }

    private DocumenResponsetDto toDocumentDtoWithDefaultRights(UserRoleRightsDetEntity entity) {
        DocumenResponsetDto dto = new DocumenResponsetDto();
        BeanUtils.copyProperties(entity, dto);
        // convert null rights to default (6 zeros as per your format)
        if (dto.getRights() == null || dto.getRights().isEmpty()) {
            dto.setRights("000000");
        }
        return dto;
    }

    private UserInRoleDto mapToUserInRoleDto(UserRoleProjection userProjection) {
        UserInRoleDto dto = new UserInRoleDto();
        dto.setUserPoid(userProjection.getUserPoid().longValue());
        dto.setUserName(userProjection.getUserName());
        return dto;
    }

    public List<UserRoleRightsDto> getUserRoleRights(Long userRolePoid) {
        List<UserRoleRightsEntity> userPermissionEntities = rightsRepo.findAllByIdUserRolePoid(userRolePoid);
        return userPermissionEntities.stream().map(this::getDto).toList();
    }


    private UserRoleRightsDto getDto(UserRoleRightsEntity entity) {
        UserRoleRightsDto dto = new UserRoleRightsDto();
        dto.setUserRolePoid(entity.getId().getUserRolePoid());
        dto.setDetRowId(entity.getId().getDetRowId());
        dto.setDocId(entity.getDocId());
        dto.setRights(entity.getRights());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedDate(entity.getLastModifiedDate());
        return dto;
    }

    public RolePermissionResponse addPermissions(RolePermissionRequest request) {
        List<RolePermissionError> errors = new ArrayList<>();

        if (!roleRepo.existsById(request.getRoleId())) {
            request.getPermissions().forEach(p ->
                    errors.add(new RolePermissionError(p.getDocId(), "Invalid userRoleId")));
            return new RolePermissionResponse("FAILURE", "Failed to add role permissions.", errors);
        }

        for (RolePermissionEntry entry : request.getPermissions()) {
            boolean exists = rightsRepo.existsByIdUserRolePoidAndIdDetRowIdAndDocId(
                    request.getRoleId(), entry.getDetRowId(), entry.getDocId());

            if (exists) {
                errors.add(new RolePermissionError(entry.getDocId(), "Duplicate entry for docId"));
                continue;
            }

            UserRoleRightsEntity entity = new UserRoleRightsEntity();
            entity.setId(new UserRoleRightsKey(request.getRoleId(), entry.getDetRowId()));
            entity.setDocId(entry.getDocId());
            entity.setRights(entry.getRights());
            entity.setCreatedBy(entry.getCreatedBy());
            entity.setCreatedDate(LocalDateTime.now());
            entity.setLastModifiedBy(entry.getLastModifiedBy());
            entity.setLastModifiedDate(LocalDateTime.now());

            rightsRepo.save(entity);
        }

        if (!errors.isEmpty()) {
            return new RolePermissionResponse("FAILURE", "Failed to add role permissions.", errors);
        }

        return new RolePermissionResponse("SUCCESS", "Role permissions added successfully.", Collections.emptyList());
    }

    public RolePermissionResponse updatePermissions(Long roleId, RightsUpdateRequest request) {
        List<RolePermissionError> errors = new ArrayList<>();

        if (!roleRepo.existsById(roleId)) {
            request.getRightsUpdateList().forEach(p ->
                    errors.add(new RolePermissionError(p.getDocId(), "Invalid userRoleId")));
            return new RolePermissionResponse("FAILURE", "Failed to update role permissions.", errors);
        }

        for (RightUpdateEntry entry : request.getRightsUpdateList()) {
            Optional<UserRoleRightsEntity> optional = rightsRepo.findByIdUserRolePoidAndIdDetRowIdAndDocId(
                    roleId, entry.getDetRowId(), entry.getDocId());

            if (optional.isEmpty()) {
                // Create new record if not found
                UserRoleRightsEntity newEntity = new UserRoleRightsEntity();
                newEntity.setId(new UserRoleRightsKey(roleId, entry.getDetRowId()));
                newEntity.setDocId(entry.getDocId());
                newEntity.setRights(entry.getRights());
                newEntity.setCreatedBy(entry.getLastModifiedBy());
                newEntity.setCreatedDate(LocalDateTime.now());
                newEntity.setLastModifiedBy(entry.getLastModifiedBy());
                newEntity.setLastModifiedDate(LocalDateTime.now());
                rightsRepo.save(newEntity);
            } else {
                // Update existing record
                UserRoleRightsEntity entity = optional.get();
                entity.setRights(entry.getRights());
                entity.setLastModifiedBy(entry.getLastModifiedBy());
                entity.setLastModifiedDate(LocalDateTime.now());
                rightsRepo.save(entity);
            }
        }

        return new RolePermissionResponse("SUCCESS", "Role permissions updated successfully.", Collections.emptyList());
    }

    @Override
    public String loadDefaultRights(Long loginUserPoid, Long userRolePoid) {
        return procRepo.callLoadDefaultRights(loginUserPoid, userRolePoid); // call stored procedure
    }
}