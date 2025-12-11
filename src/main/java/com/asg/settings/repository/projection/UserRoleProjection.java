package com.asg.settings.repository.projection;

import java.math.BigDecimal;

public interface UserRoleProjection {
    BigDecimal getUserPoid();
    String getUserId();
    String getUserName();
    String getUserEmail();
}