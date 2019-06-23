package com.filemanager.service;

import com.filemanager.dto.IdCardInfo;
import org.springframework.web.multipart.MultipartFile;

public interface IOcrService {
    IdCardInfo parse(MultipartFile file);
}
