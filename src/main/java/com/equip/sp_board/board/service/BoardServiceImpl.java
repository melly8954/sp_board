package com.equip.sp_board.board.service;

import com.equip.sp_board.board.domain.Board;
import com.equip.sp_board.board.domain.BoardStatus;
import com.equip.sp_board.board.domain.BoardType;
import com.equip.sp_board.board.dto.CreateBoardRequest;
import com.equip.sp_board.board.dto.CreateBoardResponse;
import com.equip.sp_board.board.repository.BoardRepository;
import com.equip.sp_board.board.repository.BoardTypeRepository;
import com.equip.sp_board.common.exception.CustomException;
import com.equip.sp_board.common.exception.ErrorType;
import com.equip.sp_board.filestorage.domain.FileMeta;
import com.equip.sp_board.filestorage.dto.FileDto;
import com.equip.sp_board.filestorage.repository.FileRepository;
import com.equip.sp_board.filestorage.service.iface.FileService;
import com.equip.sp_board.member.domain.Member;
import com.equip.sp_board.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
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

        if(files != null && !files.isEmpty()) {
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
}
