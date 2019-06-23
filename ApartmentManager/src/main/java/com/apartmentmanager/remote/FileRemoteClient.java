package com.apartmentmanager.remote;

import com.apartmentmanager.remote.hystrix.FileRemoteFallbackFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

@FeignClient(name = "FILE-MANAGER", fallbackFactory = FileRemoteFallbackFactory.class)
public interface FileRemoteClient {

    @PostMapping(value = "/files/idCard/upload", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    IdCardInfo uploadAndParse(FileForm fileForm);

    @PostMapping(value = "files/upload", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    FileInfo upload(FileForm fileForm);

    @Async
    @PutMapping(value = "/files/bind", consumes = MediaType.APPLICATION_JSON_VALUE)
    void bindFiles(@RequestBody List<FileBindingInfo> bindingInfos);

    @DeleteMapping("/files/{id}")
    void deleteFile(@PathVariable("id") String id);

    @GetMapping("/files")
    public List<FileInfo> getFiles(@RequestParam("app") String app, @RequestParam("module") String module, @RequestParam("externalId") String externalId);

    @Data
    class IdCardInfo implements Serializable {
        private String idNumber;
        private String name;
        private String birthDate;
        private String address;
        private String gender;
        private String imgUuid;
        private String uri;
    }

    @Data
    class FileInfo implements Serializable {
        private String id;
        private String application;
        private String module;
        private String fileName;
        private String location;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class FileForm implements Serializable {
        private String application;
        private String module;
        private MultipartFile file;
    }

    @Data
    @Builder
    class FileBindingInfo implements Serializable {
        private String externalId;
        private List<String> fileIds;
    }
}
