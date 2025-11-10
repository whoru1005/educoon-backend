package com.educoon.domain.studyRoom;

import com.educoon.domain.tag.Tag;
import com.educoon.domain.tag.TagRepository;
import com.educoon.domain.user.User;
import com.educoon.domain.user.UserRepository;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyRoomService {


    private final StudyRoomRepository studyRoomRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PasswordEncoder passwordEncoder;

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

}
