package com.melly.sp_board.filestorage.domain;

import com.melly.sp_board.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="file_Tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileMeta extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="file_id")
    private Long fileId;

    @Column(name="related_type")
    private String relatedType;

    @Column(name="related_id")
    private Long relatedId;

    @Column(name="original_name")
    private String originalName;

    @Column(name="unique_name")
    private String uniqueName;

    @Column(name="file_order")
    private Integer fileOrder;

    @Column(name="file_type")
    private String fileType;

    @Column(name="file_path")
    private String filePath;

    @Column(name="file_size")
    private Long fileSize;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;
}
