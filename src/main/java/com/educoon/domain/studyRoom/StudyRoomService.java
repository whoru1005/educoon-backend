package com.educoon.domain.studyRoom;

import com.educoon.domain.roomParticipant.RoomParticipant;
import com.educoon.domain.roomParticipant.RoomParticipantRepository;
import com.educoon.domain.roomParticipant.RoomParticipantResponse;
import com.educoon.domain.studyRecord.StudyRecordRepository;
import com.educoon.domain.tag.Tag;
import com.educoon.domain.tag.TagRepository;
import com.educoon.domain.user.User;
import com.educoon.domain.user.UserDuration;
import com.educoon.domain.user.UserRepository;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyRoomService {


    private final StudyRoomRepository studyRoomRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final StudyRecordRepository studyRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoomParticipantRepository roomParticipantRepository;

    /**
     * 모든 스터디룸 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<StudyRoomSummaryResponse> getAllStudyRooms(Pageable pageable){

        Page<StudyRoom> studyRoomPage = studyRoomRepository.findAll(pageable);

        return studyRoomPage.map(StudyRoomSummaryResponse::new);
    }

    /**
     * 현재 사용자의 스터디룸 목록 조회
     */
    @Transactional(readOnly = true)
    public List<StudyRoomSummaryResponse> getMyStudyRooms(String kakaoId){

        List<StudyRoom> studyRoomPage = studyRoomRepository.findMyStudyRoomsByKakaoId(kakaoId);

        return studyRoomPage.stream()
                .map(StudyRoomSummaryResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudyRoomDetailResponse getStudyRoomDetails(Long roomId){

        StudyRoom studyRoom = studyRoomRepository.findRoomDetailsById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        return new StudyRoomDetailResponse(studyRoom);
    }

    @Transactional(readOnly = true)
    public Page<StudyRoomSummaryResponse> searchStudyRoomsBytitle(String title, Pageable pageable){

        Page<StudyRoom> entityPage =
                studyRoomRepository.findByTitleContaining(title, pageable);

        // 2. Page<Entity> -> Page<DTO> 변환 (DTO 생성자 재사용)
        return entityPage.map(StudyRoomSummaryResponse::new);
    }

    @Transactional
    public StudyRoomDetailResponse createStudyRoom(
            StudyRoomCreateRequest studyRoomCreateRequest,
            String currentUserKakaoId){

        User owner = userRepository.findByKakaoId(currentUserKakaoId)
                .orElseThrow(()->new CustomException(ErrorCode.KAKAO_USER_INFO_FAILED));

        List<Tag> tags = tagRepository.findAllById(studyRoomCreateRequest.getTagIds());
        if(tags.size() != studyRoomCreateRequest.getTagIds().size()){
            throw new CustomException(ErrorCode.TAG_NOT_FOUND);
        }

        boolean isPublic = (studyRoomCreateRequest.getPassword() == null || studyRoomCreateRequest.getPassword().isBlank());
        String encodedPassword = isPublic ? null : passwordEncoder.encode(studyRoomCreateRequest.getPassword());

        StudyRoom newStudyRoom = StudyRoom.builder()
                .owner(owner)
                .title(studyRoomCreateRequest.getTitle())
                .description(studyRoomCreateRequest.getDescription())
                .maxCapacity(studyRoomCreateRequest.getMaxCapacity())
                .isPublic(isPublic)
                .password(encodedPassword)
                .build();

        newStudyRoom.addParticipant(owner);

        for (Tag tag : tags) {
            newStudyRoom.addTag(tag);
        }

        StudyRoom savedRoom = studyRoomRepository.save(newStudyRoom);

        return new StudyRoomDetailResponse(savedRoom);
    }

    @Transactional(readOnly = true)
    public List<RoomParticipantResponse> getRoomParticipants(Long roomId){
        StudyRoom studyRoom = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        Long ownerId = studyRoom.getOwner().getUserId();

        List<RoomParticipant> participants = roomParticipantRepository.findAllWithUserByRoomId(roomId);

        return participants.stream()
                .map(rp -> new RoomParticipantResponse(
                        rp.getUser(),
                        rp.getUser().getUserId().equals(ownerId)
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public StudyRoomDetailResponse updateRoom(Long roomId, StudyRoomUpdateRequest studyRoomUpdateRequest, String userKakaoId){
        User user = userRepository.findByKakaoId(userKakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


        StudyRoom studyRoom = studyRoomRepository.findByIdWithTags(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));


        if(!studyRoom.getOwner().getUserId().equals(user.getUserId())){
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION);
        }

        String rawPassword = studyRoomUpdateRequest.getPassword();
        String encodedPassword;

        if (StringUtils.hasText(rawPassword)) {
            encodedPassword = passwordEncoder.encode(rawPassword);
        } else {
            encodedPassword = null;
        }


        studyRoom.updateDetails(
                studyRoomUpdateRequest.getTitle(),
                encodedPassword,
                studyRoomUpdateRequest.getMaxCapacity(),
                studyRoomUpdateRequest.getDescription()
        );

        List<Tag> newTags = tagRepository.findAllById(studyRoomUpdateRequest.getTagIds());
        studyRoom.updateTags(newTags);

        return new StudyRoomDetailResponse(studyRoom);
    }

    @Transactional
    public void joinRoom(Long roomId, String currentUserKakaoId, String password){

        User user = userRepository.findByKakaoId(currentUserKakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        StudyRoom studyRoom = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        if(roomParticipantRepository.existsByUserAndStudyRoom(user, studyRoom)){
            throw new CustomException(ErrorCode.ALREADY_JOINED_ROOM);
        }

        long currentMembers = roomParticipantRepository.countByStudyRoom(studyRoom);
        if(currentMembers >= studyRoom.getMaxCapacity()){
            throw new CustomException(ErrorCode.ROOM_IS_FULL);
        }

        if(!studyRoom.getIsPublic()){
            if(password == null || !passwordEncoder.matches(password, studyRoom.getPassword())){
                throw new CustomException(ErrorCode.INVALID_ROOM_PASSWORD);
            }
        }

        RoomParticipant newParticipant = RoomParticipant.builder()
                .user(user)
                .studyRoom(studyRoom)
                .build();

        roomParticipantRepository.save(newParticipant);
    }

    public void leaveRoom(Long roomId, String currentUserKakaoId){

        User user = userRepository.findByKakaoId(currentUserKakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        StudyRoom studyRoom = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        boolean isOwner = studyRoom.getOwner().getUserId().equals(user.getUserId());

        // [ 5. 이 'if' 블록을 통째로 수정합니다 ]
        if (isOwner) {
            // [방장 탈퇴 로직]
            User newOwner = findNextOwner(studyRoom, user);

            if (newOwner == null) {
                studyRoomRepository.delete(studyRoom);
                return;
            } else {

                studyRoom.setOwner(newOwner);
            }
        }

        RoomParticipant participant = roomParticipantRepository.findByUserAndStudyRoom(user, studyRoom)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_PARTICIPANT));

        roomParticipantRepository.delete(participant);
    }

    private User findNextOwner(StudyRoom room, User currentOwner) {

        // 1. "월간" 기준 설정 (최근 30일)
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(30);

        // 2. 월간 공부량 1위 조회 (방장 제외)
        List<UserDuration> topStudierList = studyRecordRepository.findTopStudierInRoom(
                room, currentOwner, start, end
        );

        if (!topStudierList.isEmpty()) {
            // 2-1. 공부 기록이 있는 유저 중 1위 반환
            return topStudierList.get(0).getUser();
        }

        // 3. (Fallback) 공부 기록이 아무도 없으면, 가장 오래된 멤버 조회
        List<User> oldestMemberList = roomParticipantRepository.findOldestMemberInRoom(
                room, currentOwner, PageRequest.of(0, 1) // LIMIT 1
        );

        if (!oldestMemberList.isEmpty()) {
            // 3-1. 가장 오래된 멤버 반환
            return oldestMemberList.get(0);
        }

        // 4. (Fallback) 방에 방장 외 아무도 없음
        return null;
    }

    public void deleteRoom(Long roomId, String currentUserKakaoId){

        User user = userRepository.findByKakaoId(currentUserKakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        StudyRoom studyRoom = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        if (!studyRoom.getOwner().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION);
        }

        studyRoomRepository.delete(studyRoom);
    }

}
