package com.filemanager.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileInfoView implements Serializable {
    private String id;
    private String application;
    private String module;
    private String fileName;
    private String externalId;
    private String location;
}
