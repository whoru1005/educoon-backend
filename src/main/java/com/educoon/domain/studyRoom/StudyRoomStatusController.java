package com.educoon.domain.studyRoom;

import com.educoon.config.SessionRoomRegistry;
import com.educoon.domain.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudyRoomStatusController {

    private final SessionRoomRegistry sessionRoomRegistry;

    @GetMapping("/api/studyrooms/{roomId}/status")
    public ResponseEntity<List<UserStatus>> getStudyRoomCurrentStatus(@PathVariable Long roomId){
        List<UserStatus> currentStatusList = sessionRoomRegistry.getStudyRoomStatus(roomId);

        return ResponseEntity.ok(currentStatusList);
    }
}
