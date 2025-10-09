package com.equip.sp_board.filestorage.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class FileDto {
    private Long fileId;
    private String relatedType;
    private Long relatedId;
    private String originalName;
    private String uniqueName;
    private Integer fileOrder;
    private String filePath;
    private String fileType;
    private Long fileSize;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
