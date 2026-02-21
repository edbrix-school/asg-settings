package com.asg.settings.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "GLOBAL_KPI_MASTERS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalKpiMastersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_kpi_masters_seq")
    @SequenceGenerator(
            name = "global_kpi_masters_seq",
            sequenceName = "GLOBAL_KPI_MASTERS_SEQ",
            allocationSize = 1
    )
    @Column(name = "GLOBAL_KPI_MASTERS_POID", nullable = false)
    private Long globalKpiMastersPoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

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

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "DELETED", length = 1)
    private String deleted = "N";
}
