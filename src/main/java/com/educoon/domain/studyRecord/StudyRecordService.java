package com.educoon.domain.studyRecord;

import com.educoon.domain.roomParticipant.RoomParticipantRepository;
import com.educoon.domain.studyRoom.StudyRoom;
import com.educoon.domain.studyRoom.StudyRoomRepository;
import com.educoon.domain.user.User;
import com.educoon.domain.user.UserRepository;
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

        StudyRoom studyRoom = null;

        if(studyRecordCreateRequest.getRoomId() != null){
            studyRoom = studyRoomRepository.findById(studyRecordCreateRequest.getRoomId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

            if (!roomParticipantRepository.existsByUserAndStudyRoom(user, studyRoom)) {
                throw new CustomException(ErrorCode.NOT_PARTICIPANT);
            }
        }

        StudyRecord newRecord = StudyRecord.builder()
                .user(user)
                .studyRoom(studyRoom)
                .startTime(studyRecordCreateRequest.getStartTime())
                .endTime(studyRecordCreateRequest.getEndTime())
                .duration(studyRecordCreateRequest.getDuration())
                .build();

        StudyRecord savedRecord = studyRecordRepository.save(newRecord);
        return new StudyRecordResponse(savedRecord);
    }


}
