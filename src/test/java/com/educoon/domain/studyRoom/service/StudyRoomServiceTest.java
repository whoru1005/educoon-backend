package com.educoon.domain.studyRoom.service;

import com.educoon.domain.roomParticipant.repository.RoomParticipantRepository;
import com.educoon.domain.studyRoom.dto.StudyRoomCreateRequest;
import com.educoon.domain.studyRoom.dto.StudyRoomDetailResponse;
import com.educoon.domain.studyRoom.entity.StudyRoom;
import com.educoon.domain.studyRoom.repository.StudyRoomRepository;
import com.educoon.domain.tag.repository.TagRepository;
import com.educoon.domain.user.entity.User;
import com.educoon.domain.user.repository.UserRepository;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StudyRoomServiceTest {

    @Mock
    private StudyRoomRepository studyRoomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private RoomParticipantRepository roomParticipantRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudyRoomService studyRoomService;

    @Test
    @DisplayName("스터디룸 생성 성공")
    void createStudyRoom_Success() {
        // given
        String kakaoId = "12345678";
        User user = User.builder().userId(1L).kakaoId(kakaoId).build();
        StudyRoomCreateRequest request = new StudyRoomCreateRequest("테스트 방", "설명", 10, null, Collections.emptyList());
        
        given(userRepository.findByKakaoId(kakaoId)).willReturn(Optional.of(user));
        given(tagRepository.findAllById(any())).willReturn(Collections.emptyList());
        given(studyRoomRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        StudyRoomDetailResponse result = studyRoomService.createStudyRoom(request, kakaoId);

        // then
        assertThat(result.getTitle()).isEqualTo("테스트 방");
        verify(studyRoomRepository).save(any());
    }

    @Test
    @DisplayName("스터디룸 입장 성공 - 공개방")
    void joinRoom_Success_Public() {
        // given
        Long roomId = 1L;
        String kakaoId = "12345678";
        User user = User.builder().userId(1L).kakaoId(kakaoId).build();
        StudyRoom studyRoom = StudyRoom.builder().roomId(roomId).isPublic(true).maxCapacity(10).build();

        given(userRepository.findByKakaoId(kakaoId)).willReturn(Optional.of(user));
        given(studyRoomRepository.findByWithLock(roomId)).willReturn(Optional.of(studyRoom));
        given(roomParticipantRepository.existsByUserAndStudyRoom(user, studyRoom)).willReturn(false);
        given(roomParticipantRepository.countByStudyRoom(studyRoom)).willReturn(5L);

        // when
        studyRoomService.joinRoom(roomId, kakaoId, null);

        // then
        verify(roomParticipantRepository).save(any());
    }

    @Test
    @DisplayName("스터디룸 입장 실패 - 정원 초과")
    void joinRoom_Fail_RoomFull() {
        // given
        Long roomId = 1L;
        String kakaoId = "12345678";
        User user = User.builder().userId(1L).kakaoId(kakaoId).build();
        StudyRoom studyRoom = StudyRoom.builder().roomId(roomId).isPublic(true).maxCapacity(10).build();

        given(userRepository.findByKakaoId(kakaoId)).willReturn(Optional.of(user));
        given(studyRoomRepository.findByWithLock(roomId)).willReturn(Optional.of(studyRoom));
        given(roomParticipantRepository.existsByUserAndStudyRoom(user, studyRoom)).willReturn(false);
        given(roomParticipantRepository.countByStudyRoom(studyRoom)).willReturn(10L);

        // when & then
        assertThatThrownBy(() -> studyRoomService.joinRoom(roomId, kakaoId, null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROOM_IS_FULL);
    }
}
