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

    // 이미지 MIME 타입 리스트
    private static final List<String> ALLOWED_IMAGE_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    // 이미지 확장자 리스트
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of(
            "jpg",
            "jpeg",
            "png",
            "gif",
            "webp"
    );

    // 허용 MIME 타입 예시
    private static final List<String> ALLOWED_FILE_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );

    // 허용 확장자 예시
    private static final List<String> ALLOWED_FILE_EXTENSIONS = List.of(
            "jpg", "jpeg", "png", "gif", "webp",
            "pdf", "doc", "docx", "txt"
    );

    @Override
    public List<String> saveFiles(List<MultipartFile> files, String typeKey) {
        // 업로드 전에 안전하게 검증
        files.forEach(this::validateBoardFile);

        List<StoredFile> savedFiles = getStrategy().store(files, typeKey);

        return savedFiles.stream()
                .map(f -> getStrategy().generateFileUrl(f, typeKey))
                .toList();
    }

    @Override
    public String saveFile(MultipartFile file, String typeKey) {
        // 회원 대표 이미지 파일 검증
        validateImageFile(file);

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

    // 게시판 첨부파일 검증
    private void validateBoardFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorType.BAD_REQUEST, "첨부 파일이 존재하지 않습니다.");
        }

        // Content-Type 체크
        String contentType = file.getContentType();
        if (!ALLOWED_FILE_CONTENT_TYPES.contains(contentType)) {
            throw new CustomException(ErrorType.BAD_REQUEST, "허용되지 않은 파일 형식입니다: " + contentType);
        }

        // 확장자 체크
        String ext = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_FILE_EXTENSIONS.contains(ext)) {
            throw new CustomException(ErrorType.BAD_REQUEST, "허용되지 않은 파일 확장자입니다: " + ext);
        }
    }

    // 이미지 파일 검증
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorType.BAD_REQUEST, "파일이 존재하지 않습니다.");
        }

        // Content-Type 체크
        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new CustomException(ErrorType.BAD_REQUEST, "허용되지 않은 이미지 형식입니다: " + contentType);
        }

        // 확장자 체크
        String ext = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(ext)) {
            throw new CustomException(ErrorType.BAD_REQUEST, "허용되지 않은 이미지 확장자입니다: " + ext);
        }
    }
    
    // 파일 확장자 추출
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1).toLowerCase();
    }
}
