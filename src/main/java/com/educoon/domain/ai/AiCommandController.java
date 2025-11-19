package com.educoon.domain.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiCommandController {

    private final AiCommandService aiCommandService;

    // 프론트엔드 호출: POST /api/ai/rooms/{roomId}/command
    // Body: { "command": "@EduCoon 요약해줘" }
    @PostMapping("/rooms/{roomId}/command")
    public ResponseEntity<String> handleCommand(
            @PathVariable Long roomId,
            @RequestBody Map<String, String> requestBody
    ) {
        String command = requestBody.get("command");

        // 비동기로 AI 작업 시작
        aiCommandService.executeCommand(roomId, command);

        // 클라이언트는 기다리지 않고 바로 OK 응답 받음
        return ResponseEntity.ok("Command accepted");
    }
}