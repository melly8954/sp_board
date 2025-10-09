package com.equip.sp_board.filestorage.service.iface;

import com.equip.sp_board.filestorage.domain.StoredFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageStrategy {
    List<StoredFile> store(List<MultipartFile> files, String typeKey);
    StoredFile store(MultipartFile file, String typeKey);
    String generateFileUrl(StoredFile file, String typeKey);
    Resource load(String path);
}
