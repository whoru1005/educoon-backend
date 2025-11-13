package com.educoon.domain.studyRecord;

import com.educoon.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studyrecords")
public class StudyRecordController {

    private final StudyRecordService studyRecordService;

    @PostMapping
    public ResponseEntity<StudyRecordResponse> createStudyRecord(@RequestBody @Valid StudyRecordCreateRequest request){
        String kakaoId = SecurityUtils.getCurrentUserKakaoId();

        StudyRecordResponse response = studyRecordService.createStudyRecord(request, kakaoId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
