package com.asg.settings.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Entity
@Table(name = "GLOBAL_STATE_MASTER")
public class State extends BaseEntity {

    @Id
    @Column(name = "STATE_POID")
    private Long statePoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "STATE_NAME")
    private String stateName;

    @Column(name = "COUNTRY_POID")
    private Long countryPoid;

    @Column(name = "ACTIVE")
    private String active;

    @Column(name = "SEQNO")
    private Integer seqNo;

    @Column(name = "DELETED")
    private String deleted;

    @Column(name = "STATE_REMARK")
    private String stateRemark;

}
