package com.filemanager.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FileBindingInfo implements Serializable {
    private String externalId;
    private List<String> fileIds;
}
