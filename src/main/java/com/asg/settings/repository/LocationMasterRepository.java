package com.asg.settings.repository;

import com.asg.settings.entity.LocationMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationMasterRepository extends JpaRepository<LocationMasterEntity, Long> {

    LocationMasterEntity findByLocationPoid(Long locationPoid);
}
