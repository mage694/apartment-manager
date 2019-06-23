package com.filemanager.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class FileForm implements Serializable {
    private String application;
    private String module;
    private MultipartFile file;
}
