package com.poc.ai.service;

import com.poc.ai.dto.ChatRequestDto;
import com.poc.ai.dto.ChatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MentorChatService {

    private final ChatClient chatClient;
    @Value("${chat.client.targetLanguage}")
    private String targetLanguage;

    public ChatResponseDto sendMessage(ChatRequestDto dto, String username) {

        var response = chatClient
                .prompt()
                .system(s -> s
                        .param("name", username)
                        .param("language", targetLanguage)
                )
                .user(u -> u
                        .param("user_message", dto.message())
                        .param("language", targetLanguage)
                )
                .advisors(a -> a.param("chat_memory_conversation_id", username))
                .call()
                .content();

        return new ChatResponseDto(buildResponse(response));
    }

    private String buildResponse(String response) {
        return response != null ? response : "I'm sorry, I couldn't process that. Let's try again.";
    }
}
