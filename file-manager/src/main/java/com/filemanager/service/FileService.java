package com.filemanager.service;

import com.filemanager.dto.FileBindingInfo;
import com.filemanager.dto.FileForm;
import com.filemanager.dto.FileInfoView;
import com.filemanager.po.FileInfo;
import com.filemanager.repository.FileInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileService {
    @Value("${file.upload.path}")
    private String path;
    @Autowired
    private FileInfoRepository repository;

    @Transactional
    public void bindFiles(List<FileBindingInfo> bindingInfos) {
        for (FileBindingInfo bindingInfo : bindingInfos) {
            Iterable<FileInfo> files = repository.findAllById(bindingInfo.getFileIds());
            files.forEach(f -> f.setExternalId(bindingInfo.getExternalId()));
            repository.saveAll(files);
        }
    }

    public List<FileInfoView> getFiles(String app, String module, String externalId) {
        BeanCopier copier = BeanCopier.create(FileInfo.class, FileInfoView.class, false);
        FileInfo example = new FileInfo();
        example.setApplication(app);
        example.setModule(module);
        example.setExternalId(externalId);
        return repository.findAll(Example.of(example)).stream().map(po -> {
            FileInfoView vo = new FileInfoView();
            copier.copy(po, vo, null);
            return vo;
        }).collect(Collectors.toList());
    }

    @Transactional
    public FileInfoView uploadFile(FileForm fileForm) {
        FileInfo po = transfer(fileForm.getApplication(), fileForm.getModule(), fileForm.getFile());
        BeanCopier copier = BeanCopier.create(FileForm.class, FileInfo.class, false);
        copier.copy(fileForm, po, null);

        repository.save(po);
        FileInfoView result = new FileInfoView();
        copier = BeanCopier.create(FileInfo.class, FileInfoView.class, false);
        copier.copy(po, result, null);
        return result;
    }

    @Transactional
    public void deleteFile(String id) {
        FileInfo po = repository.findById(id).orElseThrow(IllegalArgumentException::new);
        File file = new File(path + File.separator + po.getLocation());
        file.delete();
        repository.delete(po);
    }

    private FileInfo transfer(String application, String module, MultipartFile file) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setId(UUID.randomUUID().toString());
        try {
            fileInfo.setFileName(file.getOriginalFilename());
            String ext = StringUtils.substringAfter(file.getOriginalFilename(), ".");
            String uri = application + "/" + module + "/" + fileInfo.getId() + "." + ext;
            fileInfo.setLocation(uri);
            File dest = new File(path + "/" + uri);
            if (!dest.getParentFile().exists()) { //判断文件父目录是否存在
                dest.getParentFile().mkdirs();
            }
            file.transferTo(dest);
        } catch (Exception e) {
            log.error("Error to upload file", e);
        }
        return fileInfo;
    }
}
