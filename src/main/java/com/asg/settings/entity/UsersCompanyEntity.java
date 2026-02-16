package com.asg.settings.entity;

import com.asg.common.lib.entity.BaseEntity;
import com.asg.settings.entity.key.CompanyEntityKey;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "GLOBAL_USERS_AUTH_COMP_DTL")
@Data
public class UsersCompanyEntity extends BaseEntity {

    @EmbeddedId
    private CompanyEntityKey id;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;
}
