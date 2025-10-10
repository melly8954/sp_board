package com.melly.sp_board.filestorage.service;

import com.melly.sp_board.common.config.FileConfig;
import com.melly.sp_board.common.exception.CustomException;
import com.melly.sp_board.common.exception.ErrorType;
import com.melly.sp_board.filestorage.domain.StoredFile;
import com.melly.sp_board.filestorage.service.iface.FileService;
import com.melly.sp_board.filestorage.service.iface.FileStorageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final FileConfig fileConfig;
    private final FileStorageStrategy localStrategy;
    private final FileStorageStrategy s3Strategy;

    private boolean useLocal = true; // 클래스 레벨에서 선언

    private FileStorageStrategy getStrategy() {
        return useLocal ? localStrategy : s3Strategy;
    }

    @Override
    public List<String> saveFiles(List<MultipartFile> files, String typeKey) {
        if (files == null || files.isEmpty()) {
            throw new CustomException(ErrorType.NOT_FOUND, "첨부 파일이 없습니다.");
        }

        List<StoredFile> savedFiles = getStrategy().store(files, typeKey);

        return savedFiles.stream()
                .map(f -> getStrategy().generateFileUrl(f, typeKey))
                .toList();
    }

    @Override
    public String saveFile(MultipartFile file, String typeKey) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorType.NOT_FOUND, "첨부 파일이 없습니다.");
        }

        StoredFile saved = getStrategy().store(file, typeKey);
        return getStrategy().generateFileUrl(saved, typeKey);
    }

    @Override
    public void deleteFile(String filePath, String typeKey) {
        if (filePath == null || filePath.isBlank()) return;

        try {
            // URL에서 base URL 제거 (예: http://localhost:8080/files/board/ 제거)
            String relativePath = filePath.replaceFirst("^.*/files/", "");

            // URL 디코딩 (한글/공백 복원)
            relativePath = java.net.URLDecoder.decode(relativePath, StandardCharsets.UTF_8);

            // typeKey 제거 (중복 방지)
            if (relativePath.startsWith(typeKey + "/")) {
                relativePath = relativePath.substring(typeKey.length() + 1); // +1은 '/' 제거
            }

            // 실제 파일 경로 계산
            String baseStoragePath = fileConfig.getStoragePath();
            Path path = Paths.get(baseStoragePath, relativePath);

            // 4. 삭제
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("삭제 성공: " + path);
            } else {
                System.out.println("삭제할 파일 없음: " + path);
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 실패: " + filePath, e);
        }
    }
}
