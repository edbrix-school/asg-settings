package com.asg.settings.repository;

import com.asg.settings.entity.AddressDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface AddressDetailsRepository extends JpaRepository<AddressDetails, String> {

    List<AddressDetails> findByAddressMasterPoidOrderByAddressType(Long poid);

    @Modifying
    @Transactional
    void deleteByAddressMasterPoid(Long addressMasterPoid);

    Optional<AddressDetails> findByAddressPoid(String addressPoid);

}