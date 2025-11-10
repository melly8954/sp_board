## Spring-Board 게시판 토이 프로젝트 (BE)

### 🗂️ Spring Boot 백엔드 프로젝트 폴더 구조

```text
+---main
|   +---java
|   |   \---com
|   |       \---melly
|   |           \---sp_board
|   |               |   SpBoardApplication.java
|   |               |   
|   |               +---auth
|   |               |   +---controller
|   |               |   |       AuthController.java
|   |               |   +---dto
|   |               |   |       LoginRequest.java
|   |               |   |       LoginResponse.java
|   |               |   |       RefreshTokenDto.java
|   |               |   |       ReIssueTokenDto.java
|   |               |   +---jwt
|   |               |   |       JwtFilter.java
|   |               |   |       JwtProvider.java
|   |               |   +---security
|   |               |   |       CustomAuthenticationProvider.java
|   |               |   |       PrincipalDetails.java
|   |               |   |       PrincipalDetailsService.java
|   |               |   \---service
|   |               |           AuthService.java
|   |               |           AuthServiceImpl.java
|   |               +---board
|   |               |   +---controller
|   |               |   |       BoardController.java
|   |               |   |       BoardTypeController.java
|   |               |   +---domain
|   |               |   |       Board.java
|   |               |   |       BoardStatus.java
|   |               |   |       BoardType.java
|   |               |   +---dto
|   |               |   |       BoardFilter.java
|   |               |   |       BoardListResponse.java
|   |               |   |       BoardResponse.java
|   |               |   |       BoardTypeResponse.java
|   |               |   |       CreateBoardRequest.java
|   |               |   |       CreateBoardResponse.java
|   |               |   |       UpdateBoardRequest.java
|   |               |   |       UpdateBoardResponse.java
|   |               |   +---repository
|   |               |   |       BoardRepository.java
|   |               |   |       BoardTypeRepository.java
|   |               |   \---service
|   |               |           BoardService.java
|   |               |           BoardServiceImpl.java
|   |               |           BoardTypeService.java
|   |               |           BoardTypeServiceImpl.java
|   |               +---comment
|   |               |   +---controller
|   |               |   |       CommentController.java
|   |               |   +---domain
|   |               |   |       Comment.java
|   |               |   |       CommentStatus.java
|   |               |   +---dto
|   |               |   |       CommentFilter.java
|   |               |   |       CommentListResponse.java
|   |               |   |       CreateCommentRequest.java
|   |               |   |       CreateCommentResponse.java
|   |               |   |       UpdateCommentRequest.java
|   |               |   |       UpdateCommentResponse.java
|   |               |   +---repository
|   |               |   |       CommentRepository.java
|   |               |   \---service
|   |               |           CommentService.java
|   |               |           CommentServiceImpl.java
|   |               +---common
|   |               |   +---config
|   |               |   |       FileConfig.java
|   |               |   |       PasswordConfig.java
|   |               |   |       RedisConfig.java
|   |               |   |       SecurityConfig.java
|   |               |   |       WebConfig.java
|   |               |   +---controller
|   |               |   |       ResponseController.java
|   |               |   +---domain
|   |               |   |       BaseEntity.java
|   |               |   +---dto
|   |               |   |       PageResponseDto.java
|   |               |   |       ResponseDto.java
|   |               |   |       SearchParamDto.java
|   |               |   +---exception
|   |               |   |       CustomException.java
|   |               |   |       ErrorType.java
|   |               |   |       GlobalExceptionHandler.java
|   |               |   +---logging
|   |               |   |       CustomP6SpyFormatter.java
|   |               |   +---trace
|   |               |   |       RequestTraceIdFilter.java
|   |               |   \---util
|   |               |           CookieUtil.java
|   |               +---filestorage
|   |               |   +---controller
|   |               |   +---domain
|   |               |   |       FileMeta.java
|   |               |   |       StoredFile.java
|   |               |   +---dto
|   |               |   |       FileDto.java
|   |               |   +---repository
|   |               |   |       FileRepository.java
|   |               |   \---service
|   |               |       |   FileServiceImpl.java
|   |               |       \---iface
|   |               |               FileService.java
|   |               |               FileStorageStrategy.java
|   |               +---like
|   |               |   +---domain
|   |               |   |       Like.java
|   |               |   +---repository
|   |               |   |       LikeRepository.java
|   |               |   \---service
|   |               |           LikeService.java
|   |               |           LikeServiceImpl.java
|   |               \---member
|   |                   +---controller
|   |                   |       MemberController.java
|   |                   +---domain
|   |                   |       Member.java
|   |                   |       MemberRole.java
|   |                   |       MemberStatus.java
|   |                   +---dto
|   |                   |       CreateMemberRequest.java
|   |                   |       CreateMemberResponse.java
|   |                   |       MemberDto.java
|   |                   +---repository
|   |                   |       MemberRepository.java
|   |                   \---service
|   |                           MemberService.java
|   |                           MemberServiceImpl.java
|   \---resources
|       |   application-local.yml
|       |   application.yml
|       |   spy.properties
|       +---static
|       \---templates
\---test
    \---java
        \---com
            \---equip
                \---sp_board
                    |   SpBoardApplicationTests.java
                    +---auth
                    |       AuthServiceImplTest.java
                    +---board
                    |       BoardServiceImplTest.java
                    |       BoardTypeServiceImplTest.java
                    +---comment
                    |       CommentServiceImplTest.java
                    +---like
                    |       LikeServiceImplTest.java
                    \---member
                            MemberServiceImplTest.java
