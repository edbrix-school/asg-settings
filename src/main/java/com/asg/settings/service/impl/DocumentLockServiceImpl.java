package com.asg.settings.service.impl;

import com.asg.settings.dto.request.DocAcquireLockRequestDto;
import com.asg.settings.dto.request.DocReleaseLockRequestDto;
import com.asg.settings.dto.request.DocUpdateLockRequestDto;
import com.asg.settings.repository.DocumentLockRepository;
import com.asg.settings.service.DocumentLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentLockServiceImpl implements DocumentLockService {

    private final DocumentLockRepository documentLockRepository;

    @Autowired
    public DocumentLockServiceImpl(DocumentLockRepository documentLockRepository) {
        this.documentLockRepository = documentLockRepository;
    }

    @Override
    public String acquireLock(DocAcquireLockRequestDto request) {
        return documentLockRepository.acquireLock(request);
    }

    @Override
    public String releaseLock(DocReleaseLockRequestDto request) {
        return documentLockRepository.releaseLock(request);
    }

    @Override
    public String updateLock(DocUpdateLockRequestDto request) {

        return documentLockRepository.updateLock(request);
    }
}
