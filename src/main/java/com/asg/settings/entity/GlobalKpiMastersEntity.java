package com.asg.settings.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "GLOBAL_KPI_MASTERS")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalKpiMastersEntity extends BaseEntity {

    @Id
    @Column(name = "GLOBAL_KPI_MASTERS_POID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @AuditIgnore
    private Long globalKpiMastersPoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @AuditIgnore
    @Column(name = "KPI_CODE", length = 50)
    private String kpiCode;

    @Column(name = "KPI_NAME", length = 250)
    private String kpiName;

    @Column(name = "DEPARTMENTS", length = 500)
    private String departments;

    @Column(name = "KPI_UNIT", length = 200)
    private String kpiUnit;

    @Column(name = "FREQUENCY", length = 50)
    private String frequency;

    @Column(name = "LAST_EXECUTED")
    private LocalDateTime lastExecuted;

    @Column(name = "SQL_PROCEDURE", length = 500)
    private String sqlProcedure;

    @Column(name = "SQL_QUERY_LINE_KPI", length = 2000)
    private String sqlQueryLineKpi;

    @Column(name = "SQL_QUERY_COMPANY_KPI", length = 2000)
    private String sqlQueryCompanyKpi;

    @Column(name = "SQL_QUERY_EMP_KPI", length = 2000)
    private String sqlQueryEmpKpi;

    @Column(name = "SQL_QUERY_DEPT_KPI", length = 2000)
    private String sqlQueryDeptKpi;

    @Column(name = "ACTIVE", length = 1)
    private String active = "Y";

    @Column(name = "SEQNO")
    private Long seqNo;

    @Column(name = "DELETED", length = 1)
    @AuditIgnore
    private String deleted = "N";
}
