package com.filemanager.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class IdCardInfo implements Serializable {
    private String idNumber;
    private String name;
    private String birthDate;
    private String address;
    private String gender;
    private String imgUuid;
    private String uri;
}
