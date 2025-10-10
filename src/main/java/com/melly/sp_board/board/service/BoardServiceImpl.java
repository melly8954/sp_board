package com.melly.sp_board.board.service;

import com.melly.sp_board.board.domain.Board;
import com.melly.sp_board.board.domain.BoardStatus;
import com.melly.sp_board.board.domain.BoardType;
import com.melly.sp_board.board.dto.*;
import com.melly.sp_board.board.repository.BoardRepository;
import com.melly.sp_board.board.repository.BoardTypeRepository;
import com.melly.sp_board.common.dto.PageResponseDto;
import com.melly.sp_board.common.exception.CustomException;
import com.melly.sp_board.common.exception.ErrorType;
import com.melly.sp_board.filestorage.domain.FileMeta;
import com.melly.sp_board.filestorage.dto.FileDto;
import com.melly.sp_board.filestorage.repository.FileRepository;
import com.melly.sp_board.filestorage.service.iface.FileService;
import com.melly.sp_board.member.domain.Member;
import com.melly.sp_board.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final BoardTypeRepository boardTypeRepository;
    private final FileService fileService;
    private final FileRepository fileRepository;

    @Override
    @Transactional
    public CreateBoardResponse createBoard(CreateBoardRequest dto, List<MultipartFile> files, Long memberId) {
        Member writer = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 사용자는 존재하지 않습니다."));
        BoardType boardType = boardTypeRepository.findById(dto.getBoardTypeId())
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 게시판 타입은 존재하지 않습니다."));

        Board board = Board.builder()
                .boardType(boardType)
                .writer(writer)
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(0)
                .likeCount(0)
                .status(BoardStatus.ACTIVE)
                .build();
        boardRepository.save(board);

        List<FileMeta> savedFiles = new ArrayList<>();
        // null 체크 + 빈 파일 제거
        if (files == null) {
            files = List.of();
        } else {
            files = files.stream()
                    .filter(f -> f != null && !f.isEmpty())
                    .toList();
        }

        if (!files.isEmpty()) {
            int fileOrder = 0;
            String typeKey = "board_" + boardType.getName();
            List<String> fileUrls = fileService.saveFiles(files, typeKey);

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String url = fileUrls.get(i); // fileService에서 생성한 접근 URL

                FileMeta meta = FileMeta.builder()
                        .relatedType(typeKey)
                        .relatedId(board.getBoardId())
                        .originalName(file.getOriginalFilename())
                        .uniqueName(url.substring(url.lastIndexOf("/") + 1)) // URL 에서 uniqueName 추출
                        .fileOrder(fileOrder++)
                        .fileType(file.getContentType())
                        .filePath(url) // 접근 URL
                        .fileSize(file.getSize())
                        .build();
                savedFiles.add(meta);
            }
            fileRepository.saveAll(savedFiles);
        }

        List<FileDto> fileDtoList = savedFiles.stream()
                .map(f -> FileDto.builder()
                        .fileId(f.getFileId())
                        .relatedType(f.getRelatedType())
                        .relatedId(f.getRelatedId())
                        .originalName(f.getOriginalName())
                        .uniqueName(f.getUniqueName())
                        .fileOrder(f.getFileOrder())
                        .filePath(f.getFilePath())
                        .fileType(f.getFileType())
                        .fileSize(f.getFileSize())
                        .createdAt(f.getCreatedAt())
                        .build())
                .toList();

        return CreateBoardResponse.builder()
                .boardId(board.getBoardId())
                .boardType(boardType.getName())
                .title(dto.getTitle())
                .content(dto.getContent())
                .status(BoardStatus.ACTIVE)
                .files(fileDtoList)
                .createdAt(board.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BoardListResponse> searchBoard(BoardFilter filter) {
        Pageable pageable = filter.getPageable();

        Page<Board> page = boardRepository.findBoardByFilters(pageable, filter.getBoardTypeId());

        List<BoardListResponse> content =page.getContent().stream()
                .map(b -> BoardListResponse.builder()
                        .boardId(b.getBoardId())
                        .boardType(b.getBoardType().getName())
                        .title(b.getTitle())
                        .viewCount(b.getViewCount())
                        .likeCount(b.getLikeCount())
                        .writeName(b.getWriter().getName())
                        .createdAt(b.getCreatedAt())
                        .build())
                .toList();

        return PageResponseDto.<BoardListResponse>builder()
                .content(content)
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .numberOfElements(page.getNumberOfElements())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BoardResponse getBoard(Long boardId, Long currentUserId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 게시글은 존재하지 않습니다."));

        boolean isOwner = board.getWriter().getMemberId().equals(currentUserId);

        String relatedType = "board_" + board.getBoardType().getName();

        List<FileDto> files = fileRepository.findAllByRelatedTypeAndRelatedId(relatedType, boardId)
                .stream()
                .map(file -> FileDto.builder()
                        .fileId(file.getFileId())
                        .originalName(file.getOriginalName())
                        .filePath(file.getFilePath())
                        .build())
                .toList();

        return BoardResponse.builder()
                .boardId(boardId)
                .boardType(board.getBoardType().getName())
                .title(board.getTitle())
                .content(board.getContent())
                .writerName(board.getWriter().getName())
                .isOwner(isOwner)
                .viewCount(board.getViewCount())
                .likeCount(board.getLikeCount())
                .files(files)
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public UpdateBoardResponse updateBoard(Long boardId, UpdateBoardRequest dto, List<MultipartFile> newFiles, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 게시글은 존재하지 않습니다."));
        if(!board.getWriter().getMemberId().equals(memberId)) {
            throw new CustomException(ErrorType.FORBIDDEN, "본인 게시글이 아니면 수정할 수 없습니다.");
        }

        board.updateBoard(dto);

        String relatedType = "board_" + board.getBoardType().getName();

        // 삭제할 파일 처리
        if (dto.getRemoveFileIds() != null && !dto.getRemoveFileIds().isEmpty()) {
            List<FileMeta> removeFiles = fileRepository.findAllById(dto.getRemoveFileIds());

            removeFiles.forEach(file -> {
                fileRepository.delete(file); // DB 삭제
                fileService.deleteFile(file.getFilePath(), relatedType); // 실제 저장소 파일 삭제
            });
        }

        // null 체크 + 빈 파일 제거
        if (newFiles == null) {
            newFiles = List.of();
        } else {
            newFiles = newFiles.stream()
                    .filter(f -> f != null && !f.isEmpty())
                    .toList();
        }

        // 새로 추가할 파일 처리
        if (!newFiles.isEmpty()) {
            List<String> fileUrls = fileService.saveFiles(newFiles, relatedType);
            int fileOrder = fileRepository.countByRelatedTypeAndRelatedId(relatedType, boardId); // 기존 파일 이후 순서

            List<FileMeta> savedFiles = new ArrayList<>();
            for (int i = 0; i < newFiles.size(); i++) {
                MultipartFile file = newFiles.get(i);
                String url = fileUrls.get(i);

                FileMeta meta = FileMeta.builder()
                        .relatedType(relatedType)
                        .relatedId(boardId)
                        .originalName(file.getOriginalFilename())
                        .uniqueName(url.substring(url.lastIndexOf("/") + 1))
                        .fileOrder(fileOrder++)
                        .fileType(file.getContentType())
                        .filePath(url)
                        .fileSize(file.getSize())
                        .build();

                savedFiles.add(meta);
            }
            fileRepository.saveAll(savedFiles);
        }

        List<FileDto> fileDtoList = fileRepository.findAllByRelatedTypeAndRelatedId(relatedType, boardId)
                .stream()
                .map(f -> FileDto.builder()
                        .fileId(f.getFileId())
                        .relatedType(f.getRelatedType())
                        .relatedId(f.getRelatedId())
                        .originalName(f.getOriginalName())
                        .uniqueName(f.getUniqueName())
                        .fileOrder(f.getFileOrder())
                        .filePath(f.getFilePath())
                        .fileType(f.getFileType())
                        .fileSize(f.getFileSize())
                        .createdAt(f.getCreatedAt())
                        .build())
                .toList();

        return UpdateBoardResponse.builder()
                .boardId(board.getBoardId())
                .title(board.getTitle())
                .content(board.getContent())
                .files(fileDtoList)
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void softDeleteBoard(Long boardId, Long currentUserId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 게시글은 존재하지 않습니다."));
        Member currentUser = memberRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 회원은 존재하지 않습니다."));

        board.softDeleteBoard(currentUser);
    }
}
