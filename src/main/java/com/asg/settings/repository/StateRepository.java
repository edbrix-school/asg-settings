package com.asg.settings.repository;

import com.asg.settings.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StateRepository extends JpaRepository<State, Long> {

    List<State> findByCountryPoidAndActive(Long countryPoid, String active);

    State findByCountryPoidAndStatePoid(Long countryPoid,Long statePoid);

    boolean existsByCountryPoid(Long countryPoid);

    Boolean existsByCountryPoidAndStatePoid(Long countryPoid,Long statePoid);
}
