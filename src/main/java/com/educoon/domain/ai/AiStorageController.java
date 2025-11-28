package com.educoon.domain.ai;

import com.educoon.domain.aiNote.AiNote;
import com.educoon.domain.aiNote.AiNoteDetailResponse;
import com.educoon.domain.aiNote.AiNoteSaveRequest;
import com.educoon.domain.user.User;
import com.educoon.domain.user.UserService;
import com.educoon.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ai/storage")
@RequiredArgsConstructor
public class AiStorageController {

    private final AiStorageService aiStorageService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<AiStorageResponse>> getMyStorage(
            @RequestParam(required = false) String type
    ) {
        String kakaoId = SecurityUtils.getCurrentUserKakaoId();
        User user = userService.findByKakaoId(kakaoId);

        return ResponseEntity.ok(aiStorageService.getMyStorage(user.getUserId(), type));
    }

    // 노트 저장
    @PostMapping("/notes")
    public ResponseEntity<String> saveNote(@RequestBody AiNoteSaveRequest request) {
        String kakaoId = SecurityUtils.getCurrentUserKakaoId();
        User user = userService.findByKakaoId(kakaoId);

        aiStorageService.saveNote(kakaoId, request);
        return ResponseEntity.ok("Note saved");
    }

    // 노트 상세 조회
    @GetMapping("/notes/{noteId}")
    public ResponseEntity<AiNoteDetailResponse> getNoteDetail(@PathVariable Long noteId) {
        return ResponseEntity.ok(aiStorageService.getNoteDetail(noteId));
    }


    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<AiQuizDetailResponse> getQuizDetail(@PathVariable Long quizId) {
        AiQuizDetailResponse aiQuizDetailResponse = aiStorageService.getQuizDetail(quizId);

        log.info("Controller: " + aiQuizDetailResponse.toString());

        return ResponseEntity.ok(aiQuizDetailResponse);
    }
}
