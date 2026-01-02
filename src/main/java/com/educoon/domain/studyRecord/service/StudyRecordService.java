package com.educoon.domain.studyRecord.service;

import com.educoon.domain.roomParticipant.repository.RoomParticipantRepository;
import com.educoon.domain.studyRecord.repository.StudyRecordRepository;
import com.educoon.domain.studyRecord.dto.StudyRecordCreateRequest;
import com.educoon.domain.studyRecord.dto.StudyRecordResponse;
import com.educoon.domain.studyRecord.entity.StudyRecord;
import com.educoon.domain.studyRoom.entity.StudyRoom;
import com.educoon.domain.studyRoom.repository.StudyRoomRepository;
import com.educoon.domain.user.entity.User;
import com.educoon.domain.user.repository.UserRepository;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyRecordService {

    private final StudyRecordRepository studyRecordRepository;
    private final UserRepository userRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final RoomParticipantRepository roomParticipantRepository;

    public StudyRecordResponse createStudyRecord(StudyRecordCreateRequest studyRecordCreateRequest, String kakaoId){

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(studyRecordCreateRequest.getStartTime().isAfter(studyRecordCreateRequest.getEndTime())){
            throw new CustomException(ErrorCode.INVALID_TIME_RANGE);
        }

        StudyRoom studyRoom = null;

        if(studyRecordCreateRequest.getRoomId() != null){
            studyRoom = studyRoomRepository.findById(studyRecordCreateRequest.getRoomId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

            if (!roomParticipantRepository.existsByUserAndStudyRoom(user, studyRoom)) {
                throw new CustomException(ErrorCode.NOT_PARTICIPANT);
            }
        }

        long calculatedDuration = java.time.Duration.between(
                studyRecordCreateRequest.getStartTime(),
                studyRecordCreateRequest.getEndTime()
        ).getSeconds();

        StudyRecord newRecord = StudyRecord.builder()
                .user(user)
                .studyRoom(studyRoom)
                .startTime(studyRecordCreateRequest.getStartTime())
                .endTime(studyRecordCreateRequest.getEndTime())
                .duration(calculatedDuration)
                .build();

        StudyRecord savedRecord = studyRecordRepository.save(newRecord);
        return new StudyRecordResponse(savedRecord);
    }

}
