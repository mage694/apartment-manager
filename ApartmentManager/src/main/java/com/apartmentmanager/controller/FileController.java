package com.apartmentmanager.controller;

import com.apartmentmanager.constant.ModuleEnum;
import com.apartmentmanager.remote.FileRemoteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.apartmentmanager.remote.FileRemoteClient.FileForm;
import static com.apartmentmanager.remote.FileRemoteClient.FileInfo;

@RestController
@RequestMapping("/files")
public class FileController {
    @Autowired
    private FileRemoteClient fileRemoteClient;
    @Value("${spring.application.name}")
    private String app;

    @DeleteMapping("/{uuid}")
    public void deleteFile(@PathVariable("uuid") String uuid) {
        fileRemoteClient.deleteFile(uuid);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileInfo uploadFile(FileForm fileForm) {
        ModuleEnum module = ModuleEnum.getByName(fileForm.getModule());
        Assert.notNull(module, "Unknown module name");
        fileForm.setApplication(app);
        return fileRemoteClient.upload(fileForm);
    }


    @PostMapping(value = "/idCard/upload", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileRemoteClient.IdCardInfo uploadAndParse(FileForm fileForm) {
        fileForm.setModule(ModuleEnum.CUSTOMER.getModuleName());
        fileForm.setApplication(app);
        return fileRemoteClient.uploadAndParse(fileForm);
    }
}
