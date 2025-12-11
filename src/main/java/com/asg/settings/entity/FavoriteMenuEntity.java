package com.asg.settings.entity;

import lombok.Data;

@Data
public class FavoriteMenuEntity {
    private String menuId;
    private String menuName;
    private Long menuLevel;
    private String menuGroup;
    private String taskflowUrl;
    private String docType;
    private String moduleId;
    private Long catSeqNo;
    private Long docSeqNo;

}