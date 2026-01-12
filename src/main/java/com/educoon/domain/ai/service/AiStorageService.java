package com.educoon.domain.ai.service;

import com.educoon.domain.ai.dto.AiStorageResponse;
import com.educoon.domain.note.entity.AiNote;
import com.educoon.domain.note.dto.AiNoteDetailResponse;
import com.educoon.domain.note.repository.AiNoteRepository;
import com.educoon.domain.note.dto.AiNoteSaveRequest;
import com.educoon.domain.quiz.entity.AiQuiz;
import com.educoon.domain.quiz.dto.AiQuizDetailResponse;
import com.educoon.domain.quiz.repository.AiQuizRepository;
import com.educoon.domain.user.entity.User;
import com.educoon.domain.user.repository.UserRepository;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AiStorageService {

    private final AiQuizRepository aiQuizRepository;
    private final AiNoteRepository aiNoteRepository;
    private final UserRepository userRepository;

    public Long saveNote(String kakaoId, AiNoteSaveRequest request){
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        AiNote note = AiNote.builder()
                .user(user)
                .title(request.title())
                .summaryContent(request.content())
                .originalFileRef(request.originalFileRef())
                .build();

        return aiNoteRepository.save(note).getNoteId();
    }

    @Transactional(readOnly = true)
    public List<AiStorageResponse> getMyStorage(Long userId, String type) {
        List<AiStorageResponse> list = new ArrayList<>();

        // type이 null이거나 "QUIZ"일 때 퀴즈 추가
        if (type == null || type.equalsIgnoreCase("QUIZ")) {
            list.addAll(aiQuizRepository.findAllByUserUserId(userId).stream()
                    .map(q -> AiStorageResponse.builder()
                            .id(q.getQuizId())
                            .type("QUIZ")
                            .title(q.getTitle())
                            .createdAt(q.getCreatedAt())
                            .build())
                    .toList());
        }

        // type이 null이거나 "NOTE"일 때 노트 추가
        if (type == null || type.equalsIgnoreCase("NOTE")) {
            list.addAll(aiNoteRepository.findAllByUserUserId(userId).stream()
                    .map(n -> AiStorageResponse.builder()
                            .id(n.getNoteId())
                            .type("NOTE")
                            .title(n.getTitle())
                            .createdAt(n.getCreatedAt())
                            .build())
                    .toList());
        }

        // 최신순 정렬
        list.sort(Comparator.comparing(AiStorageResponse::getCreatedAt).reversed());
        return list;
    }

    // 3. 노트 상세 조회 (PDF 다운로드 및 재사용용)
    @Transactional(readOnly = true)
    public AiNoteDetailResponse getNoteDetail(Long noteId) {
        AiNote note = aiNoteRepository.findById(noteId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTE_NOT_FOUND));

        return new AiNoteDetailResponse(note);
    }

    @Transactional(readOnly = true)
    public AiQuizDetailResponse getQuizDetail(Long quizId) {
        AiQuiz quiz = aiQuizRepository.findById(quizId)
                .orElseThrow(() -> new CustomException(ErrorCode.QUIZ_NOT_FOUND));

        log.info("QUIZ ID: " + quiz.getTitle());

        return new AiQuizDetailResponse(quiz);
    }
}
