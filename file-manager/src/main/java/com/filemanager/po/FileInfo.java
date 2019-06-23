package com.filemanager.po;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

@Data
public class FileInfo implements Serializable {
    @Id
    private String id;
    private String application;
    private String module;
    private String fileName;
    private String externalId;
    private String location;
}
