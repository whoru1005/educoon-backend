package com.educoon.domain.chatMessage.service;

import com.educoon.domain.chatMessage.WebSocketMessage;
import com.educoon.domain.chatMessage.dto.ChatMessageRequest;
import com.educoon.domain.chatMessage.entity.ChatMessage;
import com.educoon.domain.chatMessage.repository.ChatMessageRepository;
import com.educoon.domain.studyRoom.entity.StudyRoom;
import com.educoon.domain.studyRoom.service.StudyRoomService;
import com.educoon.domain.user.entity.User;
import com.educoon.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private StudyRoomService studyRoomService;
    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    @Test
    @DisplayName("메시지 전송 및 저장 성공")
    void sendMessage_Success() {
        // given
        Long roomId = 1L;
        String kakaoId = "12345678";
        ChatMessageRequest request = new ChatMessageRequest("안녕하세요");
        
        User user = User.builder().userId(1L).nickname("테스트유저").build();
        StudyRoom room = StudyRoom.builder().roomId(roomId).build();
        ChatMessage chatMessage = ChatMessage.builder()
                .content("안녕하세요")
                .user(user)
                .studyRoom(room)
                .build();
        // 리플렉션 대신 빌더나 수동 설정을 통해 timestamp 등을 모킹할 수도 있지만, 
        // 여기서는 저장된 메시지를 모킹합니다.
        
        given(userService.findByKakaoId(kakaoId)).willReturn(user);
        given(studyRoomService.getStudyRoomById(roomId)).willReturn(room);
        given(chatMessageRepository.save(any())).willReturn(chatMessage);

        // when
        WebSocketMessage result = chatMessageService.sendMessage(roomId, kakaoId, request);

        // then
        assertThat(result.getContent()).isEqualTo("안녕하세요");
        assertThat(result.getNickname()).isEqualTo("테스트유저");
    }
}
