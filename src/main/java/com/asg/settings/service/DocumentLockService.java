package com.asg.settings.service;


import com.asg.settings.dto.request.DocAcquireLockRequestDto;
import com.asg.settings.dto.request.DocReleaseLockRequestDto;
import com.asg.settings.dto.request.DocUpdateLockRequestDto;

public interface DocumentLockService {

    String acquireLock(DocAcquireLockRequestDto request);

    String releaseLock(DocReleaseLockRequestDto request);

    String updateLock(DocUpdateLockRequestDto request);
}
