package com.filemanager.controller;

import com.filemanager.dto.FileInfoView;
import com.filemanager.po.FileInfo;
import com.filemanager.service.FileService;
import com.filemanager.service.IOcrService;
import com.filemanager.dto.FileBindingInfo;
import com.filemanager.dto.FileForm;
import com.filemanager.dto.IdCardInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private IOcrService ocrService;
    @Autowired
    private FileService fileService;

    @PostMapping("/idCard/upload")
    public IdCardInfo uploadAndParse(FileForm fileForm) {
        IdCardInfo idCardInfo = ocrService.parse(fileForm.getFile());
        FileInfoView fileInfo = fileService.uploadFile(fileForm);
        idCardInfo.setUri(fileInfo.getLocation());
        idCardInfo.setImgUuid(fileInfo.getId());
        return idCardInfo;
    }

    @PostMapping("/upload")
    public FileInfoView upload(FileForm fileForm) {
        return fileService.uploadFile(fileForm);
    }

    @PutMapping("/bind")
    public void bindFiles(@RequestBody List<FileBindingInfo> bindingInfos) {
        fileService.bindFiles(bindingInfos);
    }

    @GetMapping
    public List<FileInfoView> getFiles(@RequestParam("app") String app, @RequestParam("module") String module, @RequestParam("externalId") String externalId) {
        return fileService.getFiles(app, module, externalId);
    }

    @DeleteMapping("/{id}")
    public void deleteFile(@PathVariable("id") String id) {
        fileService.deleteFile(id);
    }
}
