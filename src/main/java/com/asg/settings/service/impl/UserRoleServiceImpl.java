package com.asg.settings.service.impl;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.dto.RoleDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.PrintService;
import net.sf.jasperreports.engine.JasperReport;
import javax.sql.DataSource;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.settings.dto.UserRoleRequestDto;
import com.asg.common.lib.dto.UserRolesDto;
import com.asg.settings.entity.RoleEntity;
import com.asg.settings.repository.RoleRepository;
import com.asg.settings.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.asg.common.lib.security.util.UserContext.getCurrentUser;


@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final RoleRepository roleRepository;

    @Autowired
    LoggingService loggingService;

    @Autowired
    DocumentSearchService documentService;
    
    @Autowired
    PrintService printService;
    
    @Autowired
    DataSource dataSource;

    @Autowired
    DocumentDeleteService documentDeleteService;

    public RoleEntity getUserRoleByRolePoid(Long userRolePoid) {
        return roleRepository.findByUserRolePoid(userRolePoid);
    }

    @Transactional
    @Override
    public UserRolesDto addUserRoles(UserRoleRequestDto userRoleRequestDto) {
        boolean existsByUserRoleId = roleRepository.existsByUserRoleId(userRoleRequestDto.getUserRoleId());
        if (existsByUserRoleId) {
            throw new RuntimeException("User role ID already exists");
        }
        boolean existsByUserRoleName = roleRepository.existsByUserRoleName(userRoleRequestDto.getUserRoleName());
        if (existsByUserRoleName) {
            throw new RuntimeException("User role name already exists");
        }
        RoleEntity entity = mapToEntity(userRoleRequestDto);
        RoleEntity savedEntity = roleRepository.save(entity);
        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), savedEntity.getUserRolePoid().toString());

        UserRolesDto userRolesDto = this.mapToDto(savedEntity);
        return userRolesDto;
    }

    private UserRolesDto mapToDto(RoleEntity entity) {
        UserRolesDto userRolesDto = new UserRolesDto();
        BeanUtils.copyProperties(entity, userRolesDto);
        return userRolesDto;
    }

    private RoleEntity mapToEntity(UserRoleRequestDto dto) {
        RoleEntity entity = new RoleEntity();
        entity.setGroupPoid(dto.getGroupPoid());
        entity.setUserRoleId(dto.getUserRoleId());
        entity.setUserRoleName(dto.getUserRoleName());
        entity.setUserRoleName2(dto.getUserRoleName2());
        entity.setActive(dto.getActive() != null ? dto.getActive() : "Y");
        entity.setSeqNo(dto.getSeqNo());
        entity.setCompanyPoid(dto.getCompanyPoid());
        entity.setDeleted("N");
        return entity;
    }


    @Transactional
    @Override
    public UserRolesDto updateUserRoleByUserRolePoId(Long userRolePoid, UserRoleRequestDto userRoleRequestDto) {
        userRoleRequestDto.setUserRolePoid(userRolePoid);
        boolean existsByUserRolePoid = roleRepository.existsByUserRolePoid(userRolePoid);
        if (!existsByUserRolePoid) {
            throw new ResourceNotFoundException("User Role", "userRolePoid", userRolePoid);
        }
        if (roleRepository.existsByUserRoleIdAndUserRolePoidNot(userRoleRequestDto.getUserRoleId(), userRolePoid)) {
            throw new RuntimeException("User role ID already exists");
        }
        RoleEntity roleEntity = this.getUserRoleByRolePoid(userRolePoid);
        RoleEntity oldRole = new RoleEntity();
        BeanUtils.copyProperties(roleEntity, oldRole);
        roleEntity.setGroupPoid(userRoleRequestDto.getGroupPoid());
        roleEntity.setUserRoleId(userRoleRequestDto.getUserRoleId());
        roleEntity.setUserRoleName(userRoleRequestDto.getUserRoleName());
        roleEntity.setUserRoleName2(userRoleRequestDto.getUserRoleName2());
        roleEntity.setActive(userRoleRequestDto.getActive());
        roleEntity.setSeqNo(userRoleRequestDto.getSeqNo());
        roleEntity.setCompanyPoid(userRoleRequestDto.getCompanyPoid());
        RoleEntity updatedEntity = roleRepository.save(roleEntity);

        loggingService.logChanges(oldRole, updatedEntity, RoleEntity.class, UserContext.getDocumentId(), updatedEntity.getUserRolePoid().toString(), LogDetailsEnum.MODIFIED, "USER_ROLE_POID");

        UserRolesDto responseDto = new UserRolesDto();
        BeanUtils.copyProperties(updatedEntity, responseDto);
        return responseDto;
    }

    public Map<String, Object> listRoles(String docId, FilterRequestDto request, Pageable pageable) {

        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveFilters(request);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "USER_ROLE_ID",   // label
                "USER_ROLE_POID");    // value);

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Transactional
    @Override
    public void softDeleteUserRole(Long userRolePoid, DeleteReasonDto deleteReasonDto) {
        RoleEntity role = roleRepository.findByUserRolePoid(userRolePoid);
        if (role == null) {
            throw new ResourceNotFoundException("User Role", "userRolePoid", userRolePoid);
        }
        documentDeleteService.deleteDocument(userRolePoid, "GLOBAL_USER_ROLES", "USER_ROLE_POID", deleteReasonDto, null);
    }

    @Override
    public boolean existsByRoleId(String roleId, Long excludePoid) {
        if (excludePoid == null) {
            return roleRepository.existsByUserRoleId(roleId);
        }
        return roleRepository.existsByUserRoleIdAndUserRolePoidNot(roleId, excludePoid);
    }

    @Override
    public boolean existsByRoleName(String roleName, Long excludePoid) {
        if (excludePoid == null) {
            return roleRepository.existsByUserRoleName(roleName);
        }
        return roleRepository.existsByUserRoleNameAndUserRolePoidNot(roleName, excludePoid);
    }

    @Override
    public RoleDto getUserRoleById(Long userRolePoid) {
        RoleEntity entity = roleRepository.findByUserRolePoid(userRolePoid);
        if (entity == null) {
            throw new ResourceNotFoundException("User Role", "userRolePoid", userRolePoid);
        }
        return RoleDto.builder()
                .userRolePoid(entity.getUserRolePoid())
                .groupPoid(entity.getGroupPoid())
                .userRoleId(entity.getUserRoleId())
                .userRoleName(entity.getUserRoleName())
                .userRoleName2(entity.getUserRoleName2())
                .active(entity.getActive())
                .seqNo(entity.getSeqNo())
                .companyPoid(entity.getCompanyPoid())
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    public List<RoleDto> getUserRolesByIds(List<Long> userRolePoids) {
        List<RoleEntity> entities = roleRepository.findByUserRolePoidIn(userRolePoids);
        return entities.stream()
                .map(entity -> RoleDto.builder()
                        .userRolePoid(entity.getUserRolePoid())
                        .groupPoid(entity.getGroupPoid())
                        .userRoleId(entity.getUserRoleId())
                        .userRoleName(entity.getUserRoleName())
                        .userRoleName2(entity.getUserRoleName2())
                        .active(entity.getActive())
                        .seqNo(entity.getSeqNo())
                        .companyPoid(entity.getCompanyPoid())
                        .deleted(entity.getDeleted())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public byte[] print(Long userRolePoid) throws Exception {
        Map<String, Object> params = printService.buildBaseParams(userRolePoid, "000-009");
        params.putAll(buildUserRoleParams(userRolePoid));
        params.put("SUBREPORT1", printService.load("Settings/User_Roles_Rights_subreport1.jrxml"));
        params.put("BLACK_CHECK_MARK", "jasper/Settings/BlackCheckMark.gif");
        JasperReport mainReport = printService.load("Settings/User_Roles_Rights.jrxml");
        return printService.fillReportToPdf(mainReport, params, dataSource);
    }

    private Map<String, Object> buildUserRoleParams(Long userRolePoid) {
        RoleEntity entity = roleRepository.findByUserRolePoid(userRolePoid);
        if (entity == null) {
            throw new RuntimeException("User Role not found: " + userRolePoid);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("USER_ROLE_POID", entity.getUserRolePoid());
        params.put("USER_ROLE_ID", entity.getUserRoleId());
        params.put("USER_ROLE_NAME", entity.getUserRoleName());
        params.put("USER_ROLE_NAME2", entity.getUserRoleName2());
        params.put("ACTIVE", entity.getActive());
        params.put("SEQ_NO", entity.getSeqNo());
        params.put("GROUP_POID", entity.getGroupPoid());
        params.put("COMPANY_POID", entity.getCompanyPoid());
        return params;
    }

}
