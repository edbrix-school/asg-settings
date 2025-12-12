package com.asg.settings.service;

import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.settings.dto.request.DivisionCreateRequest;
import com.asg.settings.dto.request.DivisionUpdateRequest;
import com.asg.settings.dto.response.DivisionResponse;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

public interface DivisionService {

    DivisionResponse createDivision(DivisionCreateRequest request);

    Map<String, Object> listDivisions(String documentId, FilterRequestDto filters, Pageable pageable);

    Optional<DivisionResponse> getDivisionById(Long id);

    DivisionResponse updateDivision(Long id, DivisionUpdateRequest request);

    void softDeleteDivision(Long id, String updatedBy);

    void activateDivision(Long id, String updatedBy);

    void deactivateDivision(Long id, String updatedBy);

    boolean existsByDivisionCodeAndDeleted(String divisionCode, int deleted);
}
