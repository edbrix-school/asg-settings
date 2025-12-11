package com.asg.settings.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UserEntity {
    private BigDecimal userPoid;
    private String userId;
    private String userName;
    private String userEmail;
}
