package com.asg.settings.repository;

import com.asg.common.lib.enums.AttachmentFilterType;

import java.util.List;

public interface AttachmentCustomRepository {
    List<Object[]> fetchActiveAttachments(Long groupPoid, Long companyPoid, String docId, Long docKeyPoid);
    List<Object[]> fetchAllAttachments(Long groupPoid, Long companyPoid, String docId, Long docKeyPoid);
    List<Object[]> fetchDeletedAttachments(Long groupPoid, Long companyPoid, String docId, Long docKeyPoid);
    List<Object[]> fetchAttachmentsByFilter(Long groupPoid, Long companyPoid, String docId, Long docKeyPoid, AttachmentFilterType filterType);
}
