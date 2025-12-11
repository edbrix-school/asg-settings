package com.asg.settings.repository;

import com.asg.settings.entity.AddressMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressMasterRepository extends JpaRepository<AddressMaster, Long>, JpaSpecificationExecutor<AddressMaster> {

    AddressMaster findByAddressMasterPoid(Long addressMasterPoid);

    boolean existsByAddressMasterPoid(Long addressMasterPoid);

    boolean existsByAddressNameIgnoreCase(String addressName);

    boolean existsByAddressNameIgnoreCaseAndAddressMasterPoidNot(String addressName, Long excludePoid);

    boolean existsByAddressNameIgnoreCaseAndGroupPoid(String addressName, Long groupPoid);

}

