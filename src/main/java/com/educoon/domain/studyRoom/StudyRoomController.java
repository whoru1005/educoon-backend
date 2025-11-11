package com.educoon.domain.studyRoom;

import com.educoon.domain.roomParticipant.RoomParticipantResponse;
import com.educoon.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studyrooms")
public class StudyRoomController {

    private final StudyRoomService studyRoomService;

    @GetMapping("/summaries")
    public ResponseEntity<Page<StudyRoomSummaryResponse>> getAllStudyRooms(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<StudyRoomSummaryResponse> roomPage = studyRoomService.getAllStudyRooms(pageable);

        return ResponseEntity.ok(roomPage);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<StudyRoomDetailResponse> getDetailsStudyRoom(@PathVariable Long roomId){

        StudyRoomDetailResponse studyRoomDetailResponse = studyRoomService.getStudyRoomDetails(roomId);

        return ResponseEntity.ok(studyRoomDetailResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<List<StudyRoomSummaryResponse>> getMyStudyRooms(){
        String currentKakaoId = SecurityUtils.getCurrentUserKakaoId();

        List<StudyRoomSummaryResponse> myRooms = studyRoomService.getMyStudyRooms(currentKakaoId);

        return ResponseEntity.ok(myRooms);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<StudyRoomSummaryResponse>> searchStudyRooms(
            @RequestParam("title") String title,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable){

        Page<StudyRoomSummaryResponse> searchResultPage =
            studyRoomService.searchStudyRoomsBytitle(title, pageable);

        return ResponseEntity.ok(searchResultPage);
    }

    @PostMapping
    public ResponseEntity<StudyRoomDetailResponse> createStudyRoom(
            @RequestBody StudyRoomCreateRequest studyRoomCreateRequest){

        String currentUserKakaoId = SecurityUtils.getCurrentUserKakaoId();

        StudyRoomDetailResponse createdRoom = studyRoomService.createStudyRoom(studyRoomCreateRequest, currentUserKakaoId);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }

    @GetMapping("/{roomId}/participants")
    public ResponseEntity<List<RoomParticipantResponse>> getParticipants(@PathVariable Long roomId){

        List<RoomParticipantResponse> participantResponses = studyRoomService.getRoomParticipants(roomId);

        return ResponseEntity.ok(participantResponses);
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<StudyRoomDetailResponse> updateRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody StudyRoomUpdateRequest studyRoomUpdateRequest){

        String userKakaoId = SecurityUtils.getCurrentUserKakaoId();

        StudyRoomDetailResponse updatedRoom = studyRoomService.updateRoom(roomId, studyRoomUpdateRequest,userKakaoId);

        return ResponseEntity.ok(updatedRoom);

    }
}
