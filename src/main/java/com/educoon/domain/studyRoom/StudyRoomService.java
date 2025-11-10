package com.educoon.domain.studyRoom;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyRoomService {


    private final StudyRoomRepository studyRoomRepository;

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

}
