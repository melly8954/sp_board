package com.equip.sp_board.member;

import com.melly.sp_board.common.exception.CustomException;
import com.melly.sp_board.common.exception.ErrorType;
import com.melly.sp_board.filestorage.repository.FileRepository;
import com.melly.sp_board.filestorage.service.iface.FileService;
import com.melly.sp_board.member.domain.Member;
import com.melly.sp_board.member.dto.CreateMemberRequest;
import com.melly.sp_board.member.dto.CreateMemberResponse;
import com.melly.sp_board.member.repository.MemberRepository;
import com.melly.sp_board.member.service.MemberServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberServiceImpl 단위 테스트")
public class MemberServiceImplTest {
    @Mock MemberRepository memberRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock FileRepository fileRepository;
    @Mock FileService fileService;

    @InjectMocks
    MemberServiceImpl memberServiceImpl;

    @Nested
    @DisplayName("createMember() 단위 테스트")
    class createMember {
        @Test
        @DisplayName("성공 - 회원 가입 (No_File)")
        void createMember_Success_NoFile() {
            // given
            CreateMemberRequest dto = CreateMemberRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .confirmPassword("password123")
                    .name("tester")
                    .build();

            // username 중복 없음
            when(memberRepository.existsByUsername(dto.getUsername())).thenReturn(false);
            when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");

            // when
            CreateMemberResponse response = memberServiceImpl.createMember(dto, null);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo(dto.getUsername());
            assertThat(response.getName()).isEqualTo(dto.getName());
            assertThat(response.getProfileImageUrl()).isEmpty();

            verify(memberRepository, times(1)).save(any(Member.class));
            verify(fileService, never()).saveFile(any(), anyString());
            verify(fileRepository, never()).save(any());
        }

        @Test
        @DisplayName("성공 - 회원 가입 (With_File)")
        void createMember_Success_WithFile() throws IOException {
            // given
            CreateMemberRequest dto = CreateMemberRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .confirmPassword("password123")
                    .name("tester")
                    .build();

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "profile.png",
                    "image/png",
                    "test image".getBytes()
            );

            when(memberRepository.existsByUsername(dto.getUsername())).thenReturn(false);
            when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
            when(fileService.saveFile(file, "member")).thenReturn("/files/member/profile.png");

            // when
            CreateMemberResponse response = memberServiceImpl.createMember(dto, file);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getProfileImageUrl()).isEqualTo("/files/member/profile.png");

            verify(memberRepository, times(1)).save(any(Member.class));
            verify(fileService, times(1)).saveFile(file, "member");
            verify(fileRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("예외 - 이미 존재하는 username")
        void createMember_DuplicateUsername_ThrowsException() {
            // given
            CreateMemberRequest dto = CreateMemberRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .confirmPassword("password123")
                    .name("tester")
                    .build();

            when(memberRepository.existsByUsername(dto.getUsername())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> memberServiceImpl.createMember(dto, null))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);

            verify(memberRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - 비밀번호와 비밀번호 확인 불일치")
        void createMember_PasswordMismatch_ThrowsException() {
            // given
            CreateMemberRequest dto = CreateMemberRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .confirmPassword("password456")
                    .name("tester")
                    .build();

            when(memberRepository.existsByUsername(dto.getUsername())).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> memberServiceImpl.createMember(dto, null))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);

            verify(memberRepository, never()).save(any());
        }
    }
}
