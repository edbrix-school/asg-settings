package com.asg.settings.repository;

import com.asg.settings.entity.AddressMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressMasterRepository extends JpaRepository<AddressMaster, Long>, JpaSpecificationExecutor<AddressMaster> {

    AddressMaster findByAddressMasterPoid(Long addressMasterPoid);

    @Query("SELECT a FROM AddressMaster a WHERE a.addressMasterPoid = :addressMasterPoid AND a.active = 'Y' AND a.deleted = 'N'")
    AddressMaster findActiveByAddressMasterPoid(@Param("addressMasterPoid") Long addressMasterPoid);

    boolean existsByAddressMasterPoid(Long addressMasterPoid);

    boolean existsByAddressNameIgnoreCase(String addressName);

    boolean existsByAddressNameIgnoreCaseAndAddressMasterPoidNot(String addressName, Long excludePoid);

    boolean existsByAddressNameIgnoreCaseAndGroupPoid(String addressName, Long groupPoid);

}

