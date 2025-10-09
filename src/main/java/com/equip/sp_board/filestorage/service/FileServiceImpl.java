package com.equip.sp_board.filestorage.service;

import com.equip.sp_board.common.config.FileConfig;
import com.equip.sp_board.filestorage.domain.StoredFile;
import com.equip.sp_board.filestorage.service.iface.FileService;
import com.equip.sp_board.filestorage.service.iface.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final FileConfig fileConfig;
    private final FileStorageStrategy fileStorageStrategy;

    @Override
    public List<String> saveFiles(List<MultipartFile> files, String typeKey) {
        List<StoredFile> savedFilenames = fileStorageStrategy.store(files, typeKey);

        String baseUrl = fileConfig.getAccessUrlBase().replaceAll("/+$", ""); // 끝의 슬래시 모두 제거

        return savedFilenames.stream()
                .map(file -> String.format("%s/%s/%s", baseUrl, typeKey.replace("_","/"), file.getLocalFileName()))
                .toList();
    }

    @Override
    public String saveFile(MultipartFile file, String typeKey) {
        StoredFile saved = fileStorageStrategy.store(file, typeKey);

        String baseUrl = fileConfig.getAccessUrlBase().replaceAll("/+$", ""); // 끝의 슬래시 모두 제거

        return String.format("%s/%s/%s", baseUrl, typeKey.replace("_", "/"), saved.getLocalFileName());
    }

    @Override
    public void deleteFile(String filePath, String typeKey) {
        if (filePath == null || filePath.isBlank()) return;

        // URL 에서 파일명만 추출
        String fileName = Paths.get(URI.create(filePath).getPath()).getFileName().toString();

        // FileConfig 를 사용해 실제 저장 경로 계산
        String fullDir = fileConfig.getFullPath(typeKey);
        Path path = Paths.get(fullDir, fileName);

        // 실제 파일 삭제
        try {
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("삭제 성공: " + path);
            } else {
                System.out.println("삭제할 파일 없음: " + path);
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 실패: " + path, e);
        }
    }
}
