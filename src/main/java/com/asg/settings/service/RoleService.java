package com.asg.settings.service;

import com.asg.settings.dto.UserAuthRoleDto;
import com.asg.settings.dto.UserRoleDto;
import com.asg.settings.entity.RoleEntity;
import com.asg.settings.entity.UserAuthRoleEntity;
import com.asg.settings.repository.RoleRepository;
import com.asg.settings.repository.UserAuthRoleRepository;
import com.asg.settings.repository.UserRoleRepository;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class RoleService {

    @Autowired
    private RoleRepository rolesRepository;

    @Autowired
    private UserRoleRepository usersRolesRepository;

    @Autowired
    private UserAuthRoleRepository userAuthRoleRepository;

    // Method to get Roles mapped to User
    public List<UserRoleDto> getUserRoles(Long userPoid) {
        try {
            List<UserRoleDto> roles = usersRolesRepository.findAllById_UserPoid(userPoid).stream()
                    .map(userRole -> {
                        RoleEntity role = rolesRepository.findByUserRolePoid(
                                userRole.getUserRolePoid()
                        );
                        if (role != null) {
                            return new UserRoleDto(
                                    role.getUserRolePoid(),
                                    role.getUserRoleId(),
                                    role.getUserRoleName(),
                                    userRole.getExpiryDate(),
                                    role.getDeleted(),
                                    role.getActive(),
                                    ""
                            );
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList();

            return roles;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    // Derived from getUserRoles to get only roleids
    public List<String> getUserRoleNames(Long userPoid) {
        return getUserRoles(userPoid).stream()
                .map(UserRoleDto::roleName)
                .toList();
    }


    public List<UserAuthRoleDto> getUserAuthRoles(Long userPoid) {

        List<UserAuthRoleEntity> authRoles = userAuthRoleRepository.findAllById_UserPoid(userPoid);

        return authRoles.stream()
                .map(x -> {
                    RoleEntity userRole = rolesRepository.findByUserRolePoid(x.getUserRolePoid());
                    if (userRole == null) {
                        return null;
                    }

                    UserAuthRoleDto dto = new UserAuthRoleDto();
                    dto.setUserPoid(x.getId().getUserPoid());
                    dto.setDetRowId(x.getId().getDetRowId());
                    dto.setUserRolePoId(x.getUserRolePoid());
                    dto.setRoleId(userRole.getUserRoleId());
                    dto.setRoleName(userRole.getUserRoleName());
                    dto.setExpiryDate(x.getExpiryDate());
                    String deleted=userRole.getDeleted();
                    if(StringUtil.isBlank(deleted)){
                        dto.setDeleted("N");
                    }else{
                        dto.setDeleted(userRole.getDeleted());
                    }
                    dto.setActive(userRole.getActive());
                    dto.setActionType("");
                    return dto;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
