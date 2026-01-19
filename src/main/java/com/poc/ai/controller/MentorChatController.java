package com.poc.ai.controller;

import com.poc.ai.dto.ChatRequestDto;
import com.poc.ai.dto.ChatResponseDto;
import com.poc.ai.service.MentorChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentor-chat")
public class MentorChatController {

    private final MentorChatService service;

    @PostMapping
    public ResponseEntity<ChatResponseDto> sendMessage(
            @RequestHeader("username") String username,
            @RequestBody ChatRequestDto dto
    ){
        var responseDto = service.sendMessage(dto, username);

        return ResponseEntity.ok(responseDto);
    }
}
