package com.filemanager.repository;

import com.filemanager.po.FileInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileInfoRepository extends MongoRepository<FileInfo, String> {
}
