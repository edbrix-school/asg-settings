package com.asg.settings.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserRoleRightsKey implements Serializable {
    @Column(name = "USER_ROLE_POID")
    private Long userRolePoid;


    @Column(name = "DET_ROW_ID")
    private Long detRowId;
}