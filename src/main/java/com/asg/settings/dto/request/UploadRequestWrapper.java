package com.asg.settings.dto.request;

import com.asg.settings.dto.AttachmentUploadDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UploadRequestWrapper {
   // private List<AttachmentUploadDto> request;
   private List<AttachmentUploadDto> request = new ArrayList<>();
    // Backward compatibility methods
    public List<AttachmentUploadDto> getRequests() {
        return request;
    }
    
    /*public List<AttachmentUploadDto> getRequest() {
        return request != null ? request : new java.util.ArrayList<>();
    }*/
    public List<AttachmentUploadDto> getRequest() {
        return request;
    }
}
